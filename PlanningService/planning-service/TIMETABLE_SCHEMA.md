# Timetable Generation Schema and Redesign Plan

This document is the source of truth for the next timetable-generation redesign. The goal is to stop relying on fuzzy text matching and move to a canonical, ID-based scheduling model.

## 1. Core design rule

Every scheduling decision must use stable identifiers, not user-facing labels.

- School, department, speciality, level, and group must be represented by canonical IDs.
- Course/UE references must point to the real entity ID, not to a name string.
- Teacher qualifications and room compatibility must be explicit relations.
- Every generation run must be tracked as a dedicated planning session with its own metadata and result set.

## 2. Canonical data model

### 2.1 School

```text
School
- id: UUID
- nom: string
- code: string
- active: boolean
- createdAt, updatedAt
```

Purpose:
- Root academic institution.
- Every department and speciality belongs to one school.

### 2.2 Department

```text
Department
- id: UUID
- schoolId: UUID
- code: string
- libelle: string
- active: boolean
```

Purpose:
- Organizes the school structure.
- Used to restrict generation by department.

### 2.3 Speciality

```text
Speciality
- id: UUID
- departmentId: UUID
- code: string
- libelle: string
- active: boolean
```

Purpose:
- Represents a training profile within a department.
- Scheduling scope should be resolved by `specialityId`, not by label matching.

### 2.4 Level / Promotion

```text
Level
- id: UUID
- specialityId: UUID
- code: string    // ex: L1, L2, M1, M2
- libelle: string
- active: boolean
```

Purpose:
- Represents the study year or promotion.
- Used to determine compatible UE groups and scheduling rules.

### 2.5 Group / Section

```text
Group
- id: UUID
- levelId: UUID
- code: string
- libelle: string
- capacity: integer
- active: boolean
```

Purpose:
- The actual class cohort that must receive slots.
- Used for student-group constraints.

### 2.6 UE / Course

```text
UE
- id: UUID
- schoolId: UUID
- departmentId: UUID
- specialityId: UUID
- levelId: UUID
- code: string
- libelle: string
- ects: integer
- type: enum {MANDATORY, OPTIONAL, ELECTIVE}
- semester: integer
- active: boolean
```

Purpose:
- Represents a teaching unit.
- Must carry the exact academic scope used by generation.

### 2.7 Teacher

```text
Teacher
- id: UUID
- userId: UUID
- firstName: string
- lastName: string
- status: enum
- active: boolean
```

### 2.8 Teacher qualification / assignment

```text
TeacherQualification
- id: UUID
- teacherId: UUID
- ueId: UUID
- niveau: enum or integer
- isValid: boolean
```

Purpose:
- Explicitly states which teacher can teach which UE.
- This replaces fuzzy matching on names, aliases, or normalized labels.

### 2.9 Room

```text
Room
- id: UUID
- schoolId: UUID
- code: string
- nom: string
- capacity: integer
- roomType: enum {CLASSROOM, LAB, AMPHI, COMPUTER, OTHER}
- active: boolean
```

### 2.10 Room compatibility

```text
RoomCompatibility
- id: UUID
- roomId: UUID
- ueId: UUID
- allowed: boolean
```

Purpose:
- Restricts schedule assignment to rooms compatible with a course type.

### 2.11 Availability

```text
TeacherAvailability
- id: UUID
- teacherId: UUID
- dayOfWeek: enum
- startSlot: integer
- endSlot: integer
- isAvailable: boolean
```

```text
RoomAvailability
- id: UUID
- roomId: UUID
- dayOfWeek: enum
- startSlot: integer
- endSlot: integer
- isAvailable: boolean
```

Purpose:
- Makes time availability explicit and queryable.

### 2.12 Time slot

```text
TimeSlot
- id: UUID
- dayOfWeek: enum
- startTime: LocalTime
- endTime: LocalTime
- slotLabel: string
```

Purpose:
- Canonical weekly timetable grid.

### 2.13 Generation run

```text
GenerationRun
- id: UUID
- schoolId: UUID
- departmentId: UUID
- specialityId: UUID
- levelId: UUID
- generatedBy: UUID
- status: enum {PENDING, RUNNING, SUCCESS, FAILED}
- startedAt: datetime
- finishedAt: datetime
- notes: string
```

Purpose:
- One explicit planning job.
- All generated sessions belong to a generation run.

### 2.14 Timetable session

```text
TimetableSession
- id: UUID
- generationRunId: UUID
- ueId: UUID
- teacherId: UUID
- roomId: UUID
- groupId: UUID
- dayOfWeek: enum
- slotId: UUID
- startTime: LocalTime
- endTime: LocalTime
- status: enum {PLANNED, LOCKED, CANCELLED}
- createdAt: datetime
```

Purpose:
- Concrete scheduled lesson.
- Should be persisted as first-class data, not computed in memory only.

## 3. Required constraints

A valid generation must respect:

- scope compatibility: `UE` must belong to selected `school / department / speciality / level`
- teacher qualification: teacher can only be assigned to compatible UE
- room compatibility: room type and capacity fit the UE requirement
- availability: teacher and room must be free at the chosen slot
- no duplicate class-block collisions for same group or teacher
- one generation run must produce sessions for a single budgeted scope

## 4. Replacement for fuzzy matching

The old model depended on logic like:

- string normalization
- alias lists
- partial matches on department/speciality names
- “best effort” compatibility between labels

This must be removed.

The new model instead uses direct relational checks:

- `ue.specialityId == generationRun.specialityId`
- `teacherQualification.ueId == ue.id`
- `roomCompatibility.ueId == ue.id`
- `group.levelId == generationRun.levelId`

This guarantees that only valid data is scheduled.

## 5. Generation workflow

### Step 1: Intake a generation request

```json
{
  "schoolId": "...",
  "departmentId": "...",
  "specialityId": "...",
  "levelId": "...",
  "week": "2026-W43",
  "generatedBy": "admin-user-id"
}
```

### Step 2: Validate scope

- confirm selected school exists
- confirm department belongs to school
- confirm speciality belongs to department
- confirm level belongs to speciality
- ensure no scope is empty

### Step 3: Fetch canonical catalog

- list UEs by `specialityId` and `levelId`
- list groups by `levelId`
- fetch teacher qualifications for each UE
- fetch room compatibility for each UE
- fetch availability for teacher and room

### Step 4: Build scheduling candidates

For each UE, create candidate assignments:

- valid teachers for that UE
- valid rooms for that UE
- valid time slots for day/period combination
- availability checks for both teacher and room
- group-level constraints

### Step 5: Solve assignment problem

Use a deterministic algorithm, for example:

- greedy with backtracking
- constraint satisfaction search
- max-flow / bipartite assignment for a simplified version

Important: the solver must work on canonical IDs and explicit compatibility tables.

### Step 6: Persist sessions

- create one `GenerationRun`
- create all `TimetableSession` records
- flag the generation run as `SUCCESS` or `FAILED`
- keep the output immutable once successfully generated

### Step 7: Expose result

- return created sessions grouped by day + time + group
- provide summary counts: planned, conflict-free, failed assignments

## 6. DTOs

Use DTOs for API boundaries instead of raw entities.

```text
GenerationRequestDto
- schoolId
- departmentId
- specialityId
- levelId
- week
- generatedBy

GenerationSummaryDto
- generationRunId
- schoolId
- departmentId
- specialityId
- levelId
- status
- plannedSessions
- conflicts

TimetableSessionDto
- id
- ueId
- ueCode
- ueLibelle
- teacherId
- teacherName
- roomId
- roomCode
- groupId
- groupCode
- dayOfWeek
- startTime
- endTime
```

## 7. Migration plan

1. Add canonical models for School, Department, Speciality, Level, Group.
2. Add UE scope fields and ensure they are populated from canonical IDs.
3. Add teacher qualification and room compatibility tables.
4. Introduce `GenerationRun` and `TimetableSession` persistence.
5. Replace legacy `matchesGenerationScope` and alias-matching logic.
6. Rebuild the scheduling pipeline from the new schema.
7. Validate generation against a minimal known-good dataset.

## 8. Immediate implementation target

The planning service should be updated so it does the following:

- accepts scoped generation requests using IDs
- resolves all entities using canonical relationships
- assigns courses only to compatible teachers and rooms
- stores the result in `GenerationRun` + `TimetableSession`
- returns the timetable as structured DTOs

This is the direction to follow until the generator works reliably.
