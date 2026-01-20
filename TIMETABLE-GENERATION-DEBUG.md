# 🐛 Timetable Generation - Debug Guide

## 🔍 Current Issue Analysis

Based on the logs, the timetable generation is failing because:

### ✅ What's Working:
- Teacher can submit availabilities (3 availabilities sent successfully)
- Admin can view teacher availabilities (12 time slots mapped correctly)
- Time shift fix is working (8h-12h shows as 4 slots: 8h, 9h, 10h, 11h)

### ❌ What's Missing:
- **Course Assignments**: Teacher has NO assigned courses
- **UE Data**: UE statistics endpoint returns 500 error (no UEs in database)
- **Generated Timetable**: Returns empty array because no courses to schedule

## 🎯 Root Cause

The `TimetableGenerationService` requires:
1. **Disponibilités** (Teacher availabilities) ✅ Present
2. **Course Assignments** (UE + Class + Teacher) ❌ Missing

**Log Evidence:**
```
Aucun cours assigné trouvé pour l'enseignant a88a2ee8-4aa2-43bb-846e-0828f863dee7
```

## 🚀 Solution Steps

### Step 1: Setup Complete Test Data
```bash
# Run this to create all necessary data
setup-test-data.bat
```

This creates:
- 5 UEs (Teaching Units)
- 5 Classes  
- Course assignments for Pascal Bougong
- Additional teachers with courses

### Step 2: Verify Data Creation
```sql
-- Check if Pascal has course assignments
SELECT 
    u.nom_user as Teacher,
    ue.code_ue as UE,
    c.nom_classe as Class,
    co.type_cours as Type
FROM courses co
JOIN utilisateurs u ON co.enseignant_id = u.id_user
JOIN ues ue ON co.ue_id = ue.id
JOIN classes c ON co.classe_id = c.id
WHERE u.id_user = 'a88a2ee8-4aa2-43bb-846e-0828f863dee7';
```

Expected Result: 5 course assignments for Pascal

### Step 3: Test Timetable Generation
1. Login as admin
2. Go to Teachers section
3. Find Pascal Bougong
4. Click "Générer EDT"
5. Should now create actual course entries

## 🔧 Manual Troubleshooting

### Check 1: Verify Teacher ID
```javascript
// In browser console on admin dashboard
console.log('Teacher ID from logs:', 'a88a2ee8-4aa2-43bb-846e-0828f863dee7');
```

### Check 2: Verify Course Repository Query
The service uses this query:
```java
List<Course> coursesAssignes = courseRepository.findByEnseignantAndActiveTrue(enseignant);
```

### Check 3: Check Backend Logs
Look for these log messages:
```
✅ Good: "Génération d'emploi du temps intelligent pour l'enseignant..."
❌ Bad: "Aucun cours assigné trouvé pour l'enseignant..."
✅ Good: "Génération terminée: X créneaux créés..."
```

### Check 4: Verify Database Tables
```sql
-- Check if tables exist and have data
SELECT COUNT(*) FROM ues;
SELECT COUNT(*) FROM classes;  
SELECT COUNT(*) FROM courses;
SELECT COUNT(*) FROM disponibilites;
```

## 🧪 Testing Sequence

### 1. After Running setup-test-data.bat:

**Expected Frontend Behavior:**
- UE Management page loads without 500 error
- UE statistics show: 5 UEs, total credits, etc.
- Teacher courses section shows assigned courses

**Expected Timetable Generation:**
- Click "Générer EDT" for Pascal
- Should show success message
- Should create multiple course entries
- Should fill available time slots

### 2. Verify Generated Timetable:

**Check Admin View:**
- Click "Consulter EDT" for Pascal
- Should show courses in time slots:
  - Mardi 8h-12h: 4 course entries
  - Mercredi 13h-17h: 4 course entries  
  - Vendredi 13h-17h: 4 course entries

**Check Database:**
```sql
SELECT 
    date_jour,
    jour,
    heure_debut,
    heure_fin,
    ue,
    classe,
    salle
FROM emplois_du_temps 
WHERE enseignant_id = 'a88a2ee8-4aa2-43bb-846e-0828f863dee7'
ORDER BY date_jour, heure_debut;
```

## 🚨 Common Issues & Solutions

### Issue 1: Still No Course Assignments
**Symptom:** Setup script runs but teacher still has no courses
**Solution:** 
```sql
-- Manually verify teacher ID exists
SELECT * FROM utilisateurs WHERE id_user = 'a88a2ee8-4aa2-43bb-846e-0828f863dee7';

-- If not found, update the script with correct ID
```

### Issue 2: UE Statistics Still 500 Error
**Symptom:** UE endpoint still fails after setup
**Solution:** Restart UserService after running setup script

### Issue 3: Timetable Generation Still Empty
**Symptom:** Generation succeeds but creates no entries
**Check:**
```sql
-- Verify course repository query works
SELECT c.* FROM courses c
JOIN utilisateurs u ON c.enseignant_id = u.id_user
WHERE u.id_user = 'a88a2ee8-4aa2-43bb-846e-0828f863dee7' 
AND c.active = true;
```

### Issue 4: Frontend Not Updating
**Solution:** 
- Clear browser cache
- Refresh page
- Check browser console for errors

## 📊 Success Criteria

After setup, you should see:

### ✅ UE Management:
- No 500 errors
- Statistics show 5 UEs
- Can create/edit UEs

### ✅ Teacher Courses:
- Pascal has 5 assigned courses
- Courses show UE + Class combinations
- Can assign/unassign courses

### ✅ Timetable Generation:
- "Générer EDT" creates actual entries
- Generated timetable fills available slots
- Each availability period gets multiple course entries

### ✅ Admin Dashboard:
- Availability view shows all time slots
- Generated timetable displays properly
- Navigation between weeks works

## 🎯 Next Steps After Fix

1. **Test with multiple teachers**
2. **Test course conflicts detection**
3. **Test different availability patterns**
4. **Verify timezone consistency**
5. **Test export functionality**

## 📞 If Still Not Working

1. **Check UserService logs** for detailed error messages
2. **Verify database schema** matches expected structure
3. **Test API endpoints directly** using test-timetable-fixes.http
4. **Check service communication** via Eureka dashboard

The key is ensuring Pascal Bougong has assigned courses before attempting timetable generation!