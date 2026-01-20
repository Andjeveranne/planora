# 📋 Availability Business Logic Rules

## 🎯 **Overview**

This document outlines the business logic restrictions implemented for teacher availability management in the Plannora system.

## 🔒 **Business Rules Implemented**

### **1. No Past Dates** 📅
- **Rule:** Teachers cannot select dates that have already passed
- **Frontend Validation:** Date input has `min` attribute set to today's date
- **Backend Validation:** `LocalDateTime.now()` comparison
- **Error Message:** "Les disponibilités ne peuvent pas être ajoutées pour des dates passées"

### **2. Business Hours Only** 🕐
- **Rule:** Teachers can only select hours between 8:00 AM and 5:00 PM
- **Time Range:** 08:00 - 17:00 (business hours)
- **Frontend Validation:** Time inputs have `min="08:00"` and `max="17:00"`
- **Backend Validation:** `LocalTime` comparison with business hours constants
- **Error Messages:** 
  - "L'heure de début doit être entre 8h00 et 17h00"
  - "L'heure de fin doit être entre 8h00 et 17h00"

### **3. Monday to Saturday Only** 📆
- **Rule:** Teachers can only select days between Monday and Saturday (no Sunday)
- **Frontend Validation:** Day name validation with visual warning
- **Backend Validation:** `DayOfWeek.SUNDAY` check
- **Error Message:** "Les disponibilités ne peuvent pas être ajoutées le dimanche"

### **4. Minimum Duration** ⏱️
- **Rule:** Availability slots must be at least 1 hour long
- **Frontend Validation:** Time range calculation and validation
- **Backend Validation:** Duration calculation using `java.time.Duration`
- **Error Message:** "La durée minimale d'une disponibilité est de 1 heure"

### **5. No Time Overlaps** 🚫
- **Rule:** Teachers cannot create overlapping availability slots on the same date
- **Frontend Validation:** Local array checking for conflicts
- **Backend Validation:** Database query for overlapping time ranges
- **Error Message:** "Conflit détecté avec une disponibilité existante (HH:mm - HH:mm)"

### **6. Same Day Validation** 📅
- **Rule:** Start and end times must be on the same day
- **Backend Validation:** Date comparison between start and end times
- **Error Message:** "Les heures de début et fin doivent être le même jour"

## 🎨 **Frontend Implementation**

### **Enhanced Form Validation**
```typescript
// Key validation methods added to enseignant-dashboard.component.ts

getMinDate(): string // Returns today's date for date input min attribute
isPastDate(dateString: string): boolean // Checks if date is in the past
isValidDay(jour: string): boolean // Validates Monday-Saturday only
validateTimeRange(): void // Validates business hours and duration
isWithinBusinessHours(time: string): boolean // Checks 8:00-17:00 range
isFormValid(): boolean // Comprehensive form validation
```

### **Visual Feedback**
- ✅ **Success indicators** for valid inputs
- ⚠️ **Warning messages** for invalid selections
- 🚫 **Error highlighting** for constraint violations
- 📋 **Validation summary** showing all issues

### **User Experience Enhancements**
- Real-time validation as user types/selects
- Clear error messages with emojis for better UX
- Disabled submit button until all validations pass
- Help text explaining business rules

## 🔧 **Backend Implementation**

### **Service Layer Validation**
```java
// Enhanced DisponibiliteService.java with business rules

private static final LocalTime BUSINESS_START = LocalTime.of(8, 0);
private static final LocalTime BUSINESS_END = LocalTime.of(17, 0);
private static final int MIN_DURATION_MINUTES = 60;

private void validateAvailabilityBusinessRules(LocalDateTime debut, LocalDateTime fin)
private void validateNoOverlap(String enseignantId, LocalDateTime debut, LocalDateTime fin)
```

### **Repository Enhancements**
```java
// New methods in DisponibiliteRepository.java

findByEnseignantIdAndDateHeureDebutBetween() // Date range queries
findOverlappingAvailabilities() // Efficient overlap detection
```

### **Error Handling**
- Specific `IllegalArgumentException` messages for each rule violation
- Comprehensive logging for debugging
- Transactional rollback on validation failures

## 📊 **Validation Flow**

### **Frontend Validation Flow**
1. **Date Selection** → Check if past date or Sunday
2. **Time Input** → Validate business hours (8:00-17:00)
3. **Time Range** → Ensure start < end and minimum 1 hour
4. **Conflict Check** → Compare with existing local availabilities
5. **Form Submission** → All validations must pass

### **Backend Validation Flow**
1. **Receive Request** → Extract availability data
2. **Business Rules** → Apply all 6 validation rules
3. **Database Check** → Query for overlapping availabilities
4. **Save or Reject** → Store if valid, throw exception if invalid

## 🎯 **Business Rules Constants**

| Rule | Value | Description |
|------|-------|-------------|
| **Business Start** | 08:00 | Earliest allowed start time |
| **Business End** | 17:00 | Latest allowed end time |
| **Min Duration** | 60 minutes | Minimum availability duration |
| **Allowed Days** | Mon-Sat | Valid days of the week |
| **Max Future** | No limit | Can schedule far in advance |

## 🧪 **Testing Scenarios**

### **Valid Scenarios** ✅
- Monday 09:00-12:00 (3 hours, business day, business hours)
- Friday 14:00-16:00 (2 hours, business day, business hours)
- Saturday 08:00-17:00 (9 hours, maximum business day)

### **Invalid Scenarios** ❌
- **Past Date:** Yesterday 10:00-12:00
- **Sunday:** Sunday 10:00-12:00
- **Early Hours:** Monday 07:00-09:00
- **Late Hours:** Monday 16:00-18:00
- **Short Duration:** Monday 10:00-10:30
- **Overlap:** Monday 10:00-12:00 when 11:00-13:00 exists

## 🔍 **Error Messages Reference**

| Validation | French Error Message |
|------------|---------------------|
| Past Date | "Les disponibilités ne peuvent pas être ajoutées pour des dates passées" |
| Sunday | "Les disponibilités ne peuvent pas être ajoutées le dimanche" |
| Early Start | "L'heure de début doit être entre 8h00 et 17h00" |
| Late End | "L'heure de fin doit être entre 8h00 et 17h00" |
| Short Duration | "La durée minimale d'une disponibilité est de 1 heure" |
| Time Order | "L'heure de début doit être avant l'heure de fin" |
| Overlap | "Conflit détecté avec une disponibilité existante (HH:mm - HH:mm)" |
| Different Days | "Les heures de début et fin doivent être le même jour" |

## 🚀 **Benefits**

### **For Teachers**
- Clear guidance on when they can be available
- Prevents invalid submissions and frustration
- Real-time feedback during form completion

### **For Administrators**
- Consistent availability data following business rules
- Reduced need to manually validate teacher inputs
- Better timetable generation with valid time slots

### **For System**
- Data integrity maintained at both frontend and backend
- Reduced server errors from invalid data
- Improved performance with validated inputs

## 📈 **Future Enhancements**

### **Potential Additions**
- **Maximum daily hours** limit per teacher
- **Break time requirements** between consecutive slots
- **Holiday calendar integration** to block unavailable dates
- **Recurring availability** patterns (weekly templates)
- **Department-specific** business hours
- **Semester-based** availability periods

## 🔧 **Configuration**

### **Modifying Business Rules**
To change business rules, update these constants:

**Frontend (TypeScript):**
```typescript
// In enseignant-dashboard.component.ts
private readonly BUSINESS_START = '08:00';
private readonly BUSINESS_END = '17:00';
private readonly MIN_DURATION_MINUTES = 60;
```

**Backend (Java):**
```java
// In DisponibiliteService.java
private static final LocalTime BUSINESS_START = LocalTime.of(8, 0);
private static final LocalTime BUSINESS_END = LocalTime.of(17, 0);
private static final int MIN_DURATION_MINUTES = 60;
```

The business logic restrictions ensure that teacher availability data is consistent, valid, and follows institutional policies while providing a smooth user experience with clear feedback and guidance. 🎉