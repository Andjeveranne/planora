# 📋 PLANNORA SYSTEM - Complete Testing Guide

## 🚀 Getting Started (After System Reset)

### Prerequisites
- All services are running (use `reset-system.ps1`)
- Frontend accessible at http://localhost:4200
- All databases are clean (no existing data)

---

## 🧪 Testing Sequence

### Phase 1: Authentication & User Management

#### 1.1 Create Admin Account
```
URL: http://localhost:4200
1. Click "S'inscrire" (Register)
2. Fill admin details:
   - Email: admin@plannora.com
   - Password: admin123
   - Name: Admin
   - Role: ADMIN
3. Login with admin credentials
```

#### 1.2 Test Admin Dashboard Access
```
Expected: Admin dashboard with sidebar navigation
- Overview section
- Teachers management
- UE management  
- Classes, Rooms, etc.
```

#### 1.3 Create Teachers
```
In Admin Dashboard:
1. Go to "Enseignants" section
2. Click "+ Ajouter un enseignant"
3. Create multiple teachers:

Teacher 1:
- Name: Jean Dupont
- Email: jean.dupont@plannora.com
- Password: teacher123
- Phone: +221 77 123 45 67
- Specialty: Informatique
- Department: Sciences

Teacher 2:
- Name: Marie Martin
- Email: marie.martin@plannora.com
- Password: teacher123
- Phone: +221 77 234 56 78
- Specialty: Mathématiques
- Department: Sciences

Teacher 3:
- Name: Pierre Durand
- Email: pierre.durand@plannora.com
- Password: teacher123
- Phone: +221 77 345 67 89
- Specialty: Physique
- Department: Sciences
```

---

### Phase 2: Academic Structure Setup

#### 2.1 Create UE (Teaching Units)
```
In Admin Dashboard → UE Management:
1. Create UE:

UE 1:
- Code: INF101
- Name: Programmation Java
- Credits: 3
- Semester: 1
- Description: Introduction à la programmation Java

UE 2:
- Code: MAT201
- Name: Algèbre Linéaire
- Credits: 4
- Semester: 2
- Description: Mathématiques appliquées

UE 3:
- Code: PHY301
- Name: Mécanique Quantique
- Credits: 5
- Semester: 3
- Description: Physique avancée

UE 4:
- Code: INF202
- Name: Base de Données
- Credits: 3
- Semester: 2
- Description: Conception et gestion de BD
```

#### 2.2 Create Classes
```
In Admin Dashboard → Classes:
1. Create classes:

Class 1:
- Name: L1 Informatique A
- Level: Licence 1
- Capacity: 30
- Specialization: Informatique

Class 2:
- Name: L2 Mathématiques B
- Level: Licence 2
- Capacity: 25
- Specialization: Mathématiques

Class 3:
- Name: L3 Physique C
- Level: Licence 3
- Capacity: 20
- Specialization: Physique
```

#### 2.3 Assign Courses to Teachers
```
In Admin Dashboard → Teacher Courses:
1. Assign courses:

Jean Dupont (Informatique):
- INF101 → L1 Informatique A (Cours Magistral)
- INF202 → L2 Mathématiques B (Travaux Dirigés)

Marie Martin (Mathématiques):
- MAT201 → L2 Mathématiques B (Cours Magistral)
- MAT201 → L1 Informatique A (Travaux Pratiques)

Pierre Durand (Physique):
- PHY301 → L3 Physique C (Cours Magistral)
- PHY301 → L3 Physique C (Travaux Pratiques)
```

---

### Phase 3: Teacher Availability Testing

#### 3.1 Login as Teacher
```
1. Logout from admin
2. Login as: jean.dupont@plannora.com / teacher123
3. Expected: Teacher dashboard with availability management
```

#### 3.2 Test Availability Input (Critical - Test the Fixes!)
```
In Teacher Dashboard → Disponibilités:

Test Case 1: Normal Hours
- Date: Tomorrow's date
- Day: Lundi
- Start: 08:00
- End: 12:00
- Comment: "Matinée libre"
- Click "Ajouter"

Expected Result: 
✅ Time shows exactly 08:00-12:00 (NO 1-hour shift)
✅ 4 time slots created (8h, 9h, 10h, 11h)

Test Case 2: Afternoon Hours
- Date: Tomorrow's date  
- Day: Mardi
- Start: 14:00
- End: 17:00
- Comment: "Après-midi disponible"

Test Case 3: Partial Hours
- Date: Tomorrow's date
- Day: Mercredi  
- Start: 08:30
- End: 10:30
- Comment: "Créneau court"

Test Case 4: Multiple Days
- Add availability for Jeudi 09:00-11:00
- Add availability for Vendredi 13:00-16:00

Click "Envoyer toutes les disponibilités"
Expected: Success message, all availabilities sent
```

#### 3.3 Repeat for Other Teachers
```
Login as marie.martin@plannora.com:
- Add availability: Lundi 10:00-12:00
- Add availability: Mercredi 14:00-17:00

Login as pierre.durand@plannora.com:
- Add availability: Mardi 08:00-10:00  
- Add availability: Jeudi 15:00-18:00
```

---

### Phase 4: Admin Availability Verification

#### 4.1 Check Teacher Availabilities
```
Login as admin → Enseignants section:

For each teacher:
1. Click "Disponibilités" button
2. Verify displayed times match input times exactly
3. Check that all time slots in range are marked green
4. Navigate between weeks
5. Verify statistics are correct

Critical Verification:
- Jean's 08:00-12:00 should show 4 green slots (8h, 9h, 10h, 11h)
- NO time shift (8:00 AM input = 8:00 AM display)
- All hours in range should be filled, not just first hour
```

#### 4.2 Test Availability Export
```
In availability modal:
1. Click "Exporter CSV"
2. Verify export contains correct data
```

---

### Phase 5: Timetable Generation Testing

#### 5.1 Generate Intelligent Timetable
```
In Admin Dashboard → Enseignants:

For Jean Dupont:
1. Click "Générer EDT" button
2. Verify availability preview shows all slots
3. Click "Générer l'EDT"
4. Expected: Success message

Critical Test:
- Should create multiple course entries (not just one)
- Should fill ALL available time slots
- Should use assigned courses (INF101, INF202)
```

#### 5.2 View Generated Timetable
```
1. Click "Consulter EDT" for Jean
2. Navigate through weeks
3. Verify:
   - All time slots from availability are filled
   - Course information is correct
   - Room assignments are present
   - No overlapping courses
```

#### 5.3 Repeat for All Teachers
```
Generate and verify timetables for:
- Marie Martin
- Pierre Durand

Check for conflicts and proper scheduling
```

---

### Phase 6: System Integration Testing

#### 6.1 Cross-Service Communication
```
Test data flow between services:
1. UserService ↔ PlanningService sync
2. Authentication across all services
3. Real-time updates in admin dashboard
```

#### 6.2 Error Handling
```
Test error scenarios:
1. Generate timetable with no availability
2. Generate timetable with no assigned courses
3. Invalid time ranges
4. Network interruptions
```

#### 6.3 Performance Testing
```
1. Create 10+ teachers with multiple availabilities
2. Generate timetables for all
3. Check system responsiveness
4. Verify database performance
```

---

### Phase 7: Advanced Features Testing

#### 7.1 UE Management
```
1. Create complex UE structures
2. Test UE-Course-Teacher relationships
3. Verify UE appears in generated timetables
```

#### 7.2 Class Management
```
1. Test class capacity constraints
2. Verify class assignments in timetables
3. Test class-teacher relationships
```

#### 7.3 Reporting Features
```
1. Test availability statistics
2. Verify timetable summaries
3. Check system usage reports
```

---

## ✅ Success Criteria

### Critical Fixes Verification:
- [ ] **No Time Shift**: 8:00 AM input = 8:00 AM display
- [ ] **Complete Slot Filling**: 8:00-12:00 creates 4 separate slots
- [ ] **Proper Timetable Generation**: All available slots get courses
- [ ] **Consistent Timezone**: Times consistent across all interfaces

### Core Functionality:
- [ ] User authentication works
- [ ] Admin can create teachers
- [ ] Teachers can input availability
- [ ] Admin can view all availabilities correctly
- [ ] Timetable generation uses real availability data
- [ ] Generated timetables show in correct time slots
- [ ] UE and Class management works
- [ ] Course assignments function properly

### System Integration:
- [ ] All services communicate properly
- [ ] Data persists correctly
- [ ] Real-time updates work
- [ ] Error handling is graceful

---

## 🐛 Common Issues & Solutions

### Time Display Issues:
```
Problem: Times still showing 1-hour shift
Solution: Clear browser cache, restart services
```

### Timetable Generation Issues:
```
Problem: Only first slot filled
Solution: Check TimetableGenerationService logs
```

### Service Communication Issues:
```
Problem: Services not communicating
Solution: Check Eureka dashboard, restart in order
```

---

## 📊 Test Results Template

```
Date: ___________
Tester: ___________

Phase 1 - Authentication: ✅/❌
Phase 2 - Academic Setup: ✅/❌  
Phase 3 - Teacher Availability: ✅/❌
Phase 4 - Admin Verification: ✅/❌
Phase 5 - Timetable Generation: ✅/❌
Phase 6 - System Integration: ✅/❌
Phase 7 - Advanced Features: ✅/❌

Critical Fixes:
- Time Shift Fix: ✅/❌
- Slot Filling Fix: ✅/❌
- Timezone Consistency: ✅/❌

Notes:
_________________________________
_________________________________
```

---

## 🎯 Next Steps After Testing

1. **Document any issues found**
2. **Create production deployment plan**
3. **Set up monitoring and logging**
4. **Create user training materials**
5. **Plan data backup strategies**

Good luck with your comprehensive testing! 🚀