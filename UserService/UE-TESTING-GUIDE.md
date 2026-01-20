# UE Management System - Testing Guide

## 🧪 Complete Testing Instructions

### Prerequisites
1. ✅ UserService running on port 8086
2. ✅ Database schema created with sample data
3. ✅ Frontend application running
4. ✅ Valid JWT token for authentication

### Backend API Testing

#### 1. Health Check
```bash
# Should return 403 (authentication required) - this is correct
curl -X GET http://localhost:8086/api/ues/health
```

#### 2. Get All UEs (with authentication)
```bash
# Replace YOUR_JWT_TOKEN with actual token
curl -X GET http://localhost:8086/api/ues \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

#### 3. Get UE Options
```bash
curl -X GET http://localhost:8086/api/ues/options \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 4. Search UEs
```bash
curl -X GET "http://localhost:8086/api/ues/search?q=math" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 5. Create New UE
```bash
curl -X POST http://localhost:8086/api/ues \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "codeUE": "TEST101",
    "nomUE": "Test Course",
    "description": "Test description",
    "heuresCM": 20,
    "heuresTD": 16,
    "heuresTP": 24,
    "creditsECTS": 6,
    "semestre": "S1",
    "niveau": "L1"
  }'
```

### Frontend Testing

#### 1. Access UE Management
1. Login as administrator
2. Navigate to admin dashboard
3. Click on "Unités d'Enseignement" in sidebar
4. Verify UE management interface loads

#### 2. Test UE List View
- ✅ Verify 26 sample UEs are displayed
- ✅ Check card layout is responsive
- ✅ Verify search functionality works
- ✅ Test semester and level filters
- ✅ Check sorting functionality

#### 3. Test UE Creation
1. Click "Ajouter une UE" button
2. Fill out the form with test data:
   - Code UE: TEST102
   - Nom UE: Test Frontend Course
   - Description: Created from frontend
   - Heures CM: 18
   - Heures TD: 12
   - Heures TP: 30
   - Crédits ECTS: 5
   - Semestre: S2
   - Niveau: L1
3. Click "Créer l'UE"
4. Verify success message and UE appears in list

#### 4. Test UE Editing
1. Click edit icon on any UE card
2. Modify some fields
3. Click "Sauvegarder"
4. Verify changes are reflected

#### 5. Test Enseignant Assignment
1. Click teacher icon on any UE card
2. Select an available enseignant
3. Verify assignment is successful
4. Check that enseignant appears in UE card

#### 6. Test Statistics View
1. Click "Statistiques" tab
2. Verify statistics cards display correct data:
   - Total UEs: 26 (or 27 if you created one)
   - Total Credits: Should match sum of all UE credits
   - Average Credits: Should be calculated correctly
   - Total Hours: Should match sum of all UE hours

### Database Verification

#### 1. Check UE Data
```sql
-- Connect to MySQL and run:
USE plannoradb;

-- Verify UEs were created
SELECT COUNT(*) as total_ues FROM ues WHERE active = TRUE;

-- Check UEs by level
SELECT niveau, COUNT(*) as count, SUM(credits_ects) as total_credits 
FROM ues 
WHERE active = TRUE 
GROUP BY niveau;

-- Verify sample data
SELECT code_ue, nom_ue, credits_ects, niveau, semestre 
FROM ues 
WHERE active = TRUE 
ORDER BY niveau, semestre 
LIMIT 10;
```

#### 2. Check Enseignant-UE Relationships
```sql
-- Verify enseignant assignments
SELECT COUNT(*) as total_assignments FROM enseignant_ue;

-- Check assignments with names
SELECT u.code_ue, u.nom_ue, ut.nom_user, ut.prenom_user
FROM ues u
JOIN enseignant_ue eu ON u.id = eu.ue_id
JOIN enseignants e ON eu.enseignant_id = e.id_user
JOIN utilisateurs ut ON e.id_user = ut.id_user
LIMIT 10;
```

#### 3. Check EmploiDuTemps Integration
```sql
-- Verify emplois du temps are linked to UEs
SELECT COUNT(*) as linked_emplois 
FROM emplois_du_temps 
WHERE ue_id IS NOT NULL;

-- Check sample linkages
SELECT edt.ue as old_ue_name, u.code_ue, u.nom_ue
FROM emplois_du_temps edt
LEFT JOIN ues u ON edt.ue_id = u.id
LIMIT 5;
```

### Performance Testing

#### 1. Load Testing
- Test with 100+ UEs to verify performance
- Check search response time with large datasets
- Verify pagination works correctly

#### 2. Concurrent Access
- Multiple users accessing UE management simultaneously
- Verify no data corruption occurs
- Check proper error handling

### Error Handling Testing

#### 1. Validation Errors
- Try creating UE with missing required fields
- Test with invalid data (negative credits, empty names)
- Verify proper error messages are displayed

#### 2. Network Errors
- Test with backend service down
- Verify graceful error handling
- Check loading states work correctly

#### 3. Authentication Errors
- Test with expired JWT token
- Verify proper redirect to login
- Check error messages are user-friendly

### Browser Compatibility

#### Test in Multiple Browsers
- ✅ Chrome (latest)
- ✅ Firefox (latest)
- ✅ Safari (if available)
- ✅ Edge (latest)

#### Responsive Design Testing
- ✅ Desktop (1920x1080)
- ✅ Tablet (768x1024)
- ✅ Mobile (375x667)
- ✅ Large screens (2560x1440)

### Accessibility Testing

#### Screen Reader Compatibility
- Test with screen reader software
- Verify all elements are properly labeled
- Check keyboard navigation works

#### Color Contrast
- Verify sufficient contrast ratios
- Test with color blindness simulators
- Check focus indicators are visible

### Integration Testing

#### 1. End-to-End Workflow
1. Admin logs in
2. Creates new UE
3. Assigns enseignant to UE
4. Verifies UE appears in enseignant's list
5. Creates emploi du temps entry for UE
6. Verifies complete integration

#### 2. Data Consistency
- Verify UE deletion removes assignments
- Check that enseignant deletion handles UE assignments
- Ensure data integrity across all operations

### Expected Results

#### Sample Data Verification
After running the migration, you should see:

**UEs by Level:**
- L1: 6 UEs, 35 ECTS total
- L2: 6 UEs, 38 ECTS total  
- L3: 5 UEs, 36 ECTS total
- M1: 5 UEs, 30 ECTS total
- M2: 4 UEs, 44 ECTS total

**Total Statistics:**
- 26 UEs created
- 183 total ECTS credits
- 15 enseignant-UE assignments
- 5+ emplois du temps linked to UEs

#### Frontend Functionality
- ✅ All 26 UEs display in card format
- ✅ Search works across code, name, description
- ✅ Filters work for semester and level
- ✅ CRUD operations function correctly
- ✅ Enseignant assignment works
- ✅ Statistics display accurate data
- ✅ Responsive design works on all devices

### Troubleshooting Common Issues

#### 1. UEs Not Loading
- Check UserService is running on port 8086
- Verify JWT token is valid and not expired
- Check browser console for network errors
- Verify database connection is working

#### 2. Authentication Errors
- Ensure user is logged in as administrator
- Check JWT token format and expiration
- Verify CORS settings allow frontend domain

#### 3. Database Issues
- Run migration script if tables don't exist
- Check MySQL service is running
- Verify database credentials in application.properties
- Ensure foreign key constraints are properly set

#### 4. Frontend Compilation Errors
- Verify all imports are correct
- Check TypeScript version compatibility
- Run `npm install` to ensure dependencies
- Clear browser cache and restart dev server

### Success Criteria

The UE Management System is working correctly if:

✅ **Backend API**
- All endpoints respond correctly with authentication
- CRUD operations work without errors
- Search and filtering return expected results
- Statistics calculations are accurate

✅ **Frontend Interface**
- UE management loads without errors
- All UI components render correctly
- Forms submit and validate properly
- Real-time search and filtering work

✅ **Database Integration**
- Sample data is properly created
- Relationships are correctly established
- Data integrity is maintained
- Performance is acceptable

✅ **System Integration**
- UE management integrates with admin dashboard
- Authentication works seamlessly
- Error handling is user-friendly
- Responsive design works on all devices

### Next Steps After Testing

1. **Production Deployment**
   - Configure production database settings
   - Set up proper SSL certificates
   - Configure production JWT secrets
   - Set up monitoring and logging

2. **User Training**
   - Create user documentation
   - Provide admin training sessions
   - Set up support procedures
   - Create video tutorials

3. **Monitoring Setup**
   - Configure application monitoring
   - Set up database performance monitoring
   - Implement error tracking
   - Create usage analytics

The UE Management System is now fully implemented and ready for production use! 🎉