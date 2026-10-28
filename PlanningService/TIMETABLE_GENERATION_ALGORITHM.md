# How Timetable Generation Works

This document describes the school-level timetable generation path currently used by the application. The main entry point is `POST /api/planning/emplois-du-temps/generer`, which calls `EmploiDuTempsServiceV2` and then `FordFulkersonScheduler`.

## The Short Version

The scheduler is a **hybrid**, not a pure greedy algorithm:

1. The service builds course requests in sequence, grouped by class and teaching unit (UE).
2. For each UE course type, it selects the first teacher assignment that is marked as qualified for that type.
3. The scheduler processes those requests one after another. Earlier requests can use slots and rooms before later requests are considered. This is the greedy, order-dependent part.
4. For each required two-hour session, it builds a graph connecting valid time slots to compatible rooms and runs Ford–Fulkerson using breadth-first search (BFS) to find augmenting paths.
5. It saves the resulting classes and labels the remaining class slots as TPE (independent study).

So, the code combines **sequential greedy choices** with a **maximum-flow matching step** for each course request. Calling the whole implementation simply “a greedy algorithm” misses that second part.

## Generation Path

The controller route `POST /api/planning/emplois-du-temps/generer` delegates to `EmploiDuTempsServiceV2.genererEmploisDuTemps(...)`.

Before scheduling, the service:

- Chooses the next Monday and targets that week.
- Fetches classes, rooms, UEs, and teachers from the other services.
- Matches each class with UEs using level, department/specialty, and school information.
- Creates separate requests for CM, TD, and TP when the UE has hours for that type and an eligible teacher is assigned.
- Fetches teacher availability for the target week.
- Builds the five-day list of two-hour time slots.

If required data is missing, or no requests or availability are found, generation stops with an error instead of saving a timetable.

## How One Course Request Is Scheduled

A request represents one class, one UE, one course type, one assigned teacher, and the number of hours required. The scheduler converts the requested hours into two-hour sessions:

`required sessions = ceil(required hours / 2)`

For each session, it filters out time slots that fail the checks below, filters rooms to those that can serve the request, and builds this flow graph:

```text
source -> valid time slot -> compatible, available room -> sink
```

Each edge has capacity 1. This means a time slot or room can be selected at most once in this matching. Ford–Fulkerson repeatedly asks BFS to find a source-to-sink path in the residual graph, then pushes flow along that path. Reverse residual edges allow the matching to be rearranged when a later path can use a better slot/room combination.

After the flow is computed, the scheduler extracts a slot/room pair and adds the course session to the results. It then builds the next matching while treating earlier results as occupied. If no valid path remains, it stops trying to place further sessions for that request.

## Placement Constraints

A candidate session must satisfy the following checks:

- **UE time window:** The time slot must overlap the UE's start/end window. Missing UE bounds default to 08:00 and 18:00.
- **Teacher availability:** A matching availability record for the teacher must overlap the slot.
- **Class conflict:** The class cannot already have another session that overlaps that time.
- **Teacher conflict:** The teacher cannot already be teaching another session that overlaps that time.
- **School:** When a school is specified, the room must belong to it.
- **Room capacity:** The room capacity must be at least the class size.
- **Room type:** CM/TD use lecture-compatible rooms; TP uses lab/computer-compatible rooms.
- **Class-specific rooms:** A room assigned to a particular class can only serve that class. An unassigned room can be considered for any matching class.
- **Room conflict:** A room cannot host overlapping sessions.

The overlap checks prevent simultaneous commitments; they do not rank a slot by preference or try to improve the whole timetable after all requests have been processed.

## Why Request Order Matters

The scheduler completes one request before moving to the next. Therefore, a request processed earlier may consume a teacher's time or a room that a later request also needs. The later request then has fewer candidates, even if a different overall arrangement could have scheduled more courses.

The service also chooses the first eligible teacher for each course type before scheduling. It does not compare all eligible teachers to find the one with the best overall availability. These decisions make the result dependent on request and teacher-assignment ordering; this is the greedy aspect of the design.

Ford–Fulkerson only optimizes the slot/room matching inside the current request's candidate graph. It does **not** solve one global optimization problem across every class, UE, teacher, and room in the school.

## Example

Suppose a class has a two-hour CM request, taught by Teacher A, and the matching teacher availability and room constraints allow Monday 08:00–10:00 and Tuesday 10:00–12:00. The flow graph can connect both slots to the compatible rooms. The scheduler chooses one matching pair for the CM session and marks that class, teacher, and room as occupied during the selected interval. Later requests must use other non-conflicting resources.

If Teacher A is unavailable for both candidates, the request gets no session. If the class has additional requested sessions, the scheduler repeats the candidate search for each one until all required sessions are placed or no candidate path remains.

## TPE and Unfilled Slots

After course placement, the service visits every generated slot for every selected class. Any slot without a scheduled course is added as `TPE`, representing student independent study. This fills the displayed timetable grid; it does not mean that an instructor, UE, or room was assigned to that slot.

## Current Time-Grid Detail

The current service generates these slots for each weekday:

| Time | Current behavior |
| --- | --- |
| 08:00–10:00 | Schedulable |
| 10:00–12:00 | Schedulable |
| 12:00–14:00 | Schedulable in the current backend code |
| 14:00–16:00 | Schedulable |
| 16:00–18:00 | Schedulable |

In other words, the current generation path does **not** reserve 12:00–14:00 as a lunch break. To introduce a break, that interval must be removed from the backend's generated candidate slots; changing only the timetable display would not prevent the scheduler from assigning a class there.

## Implementation Caveat

The scheduler's teacher-availability check currently accepts a slot when the availability interval overlaps it at all. It does not require the availability interval to cover the entire two-hour slot. A nearby scheduler test expects a one-hour availability interval not to qualify for a two-hour slot, so the implementation and that test expectation should be reconciled before relying on partial-availability behavior.

Also, if one request cannot be fully placed, the scheduler returns the sessions it did place and stops that request. The service currently fails generation only when **zero** non-TPE courses were placed overall; it does not provide a full per-request report of every course session left unscheduled.

## Source Files

- [EmploiDuTempsController.java](planning-service/src/main/java/com/isi4/planningservice/controller/EmploiDuTempsController.java): HTTP route and service delegation.
- [EmploiDuTempsServiceV2.java](planning-service/src/main/java/com/isi4/planningservice/service/EmploiDuTempsServiceV2.java): data loading, request creation, time grid, persistence, and TPE completion.
- [FordFulkersonScheduler.java](planning-service/src/main/java/com/isi4/planningservice/algorithm/FordFulkersonScheduler.java): slot/room graph, conflict filtering, BFS, and flow matching.
- [FordFulkersonSchedulerTest.java](planning-service/src/test/java/com/isi4/planningservice/algorithm/FordFulkersonSchedulerTest.java): scheduler behavior tests.