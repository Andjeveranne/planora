# Implementation of Course and Class Management System

## Overview
This document describes the implementation of the Course and Class management system that allows teachers to see which courses they teach and in which classes, as requested in the user query.

## Implemented Components

### 1. Backend Entities

#### Classe Entity (`model/Classe.java`)
- Represents a class (group of students)
- Fields: id, nomClasse, niveau, filiere, anneeAcademique, nombreEtudiants, description, active, timestamps
- Relationships: One-to-Many with Course
- Methods: addCourse(), removeCourse(), getNombreCours(), getTotalHeuresCours(), etc.

#### Course Entity (`model/Course.java`)
- Represents a course linking UE, Teacher, and Class
- Fields: id, codeCourse, typeCours, nombreHeures, semestre, description, active, timestamps
- Relationships: 
  - Many-to-One with UE
  - Many-to-One with Enseignant
  - Many-to-One with Classe
  - One-to-Many with EmploiDuTemps
- Methods: addEmploiDuTemps(), getTotalHeures(), isCompletementProgramme(), etc.

#### Updated Enseignant Entity
- Added One-to-Many relationship with Course
- Added methods: addCourse(), removeCourse(), getClassesEnseignees(), getCoursForClasse()

#### Updated EmploiDuTemps Entity
- Added Many-to-One relationship with Course

### 2. DTOs (Data Transfer Objects)

#### ClasseDTO (`dto/ClasseDTO.java`)
- Transfer object for Classe entity
- Includes calculated fields: nombreCours, totalHeuresCours, totalCreditsECTS
- Validation annotations for required fields

#### CourseDTO (`dto/CourseDTO.java`)
- Transfer object for Course entity
- Includes detailed information: ueCode, ueNom, enseignantNom, classeNom, etc.
- Calculated fields: nombreCreneaux, heuresProgrammees, completementProgramme

### 3. Repositories

#### ClasseRepository (`repository/ClasseRepository.java`)
- CRUD operations for Classe
- Custom queries: findByNiveau, findByFiliere, searchClasses, etc.
- Statistics queries: countClassesByNiveau, countClassesByFiliere

#### CourseRepository (`repository/CourseRepository.java`)
- CRUD operations for Course
- Key queries for teacher functionality:
  - `findByEnseignantIdAndActiveTrue()` - Get courses by teacher
  - `findClassesByEnseignant()` - Get classes taught by teacher
  - `findByEnseignantIdAndClasseIdAndActiveTrue()` - Get courses by teacher and class
  - `findUEsByEnseignantAndClasse()` - Get UEs taught by teacher in specific class

### 4. Services

#### ClasseService (`service/ClasseService.java`)
- Business logic for Classe management
- CRUD operations with validation
- Statistics calculation
- Search functionality

#### CourseService (`service/CourseService.java`)
- Business logic for Course management
- Key methods for teacher functionality:
  - `getCoursesByEnseignant()` - Get all courses for a teacher
  - `getClassesByEnseignant()` - Get all classes where teacher teaches
  - `getCoursesByEnseignantAndClasse()` - Get courses for teacher in specific class

### 5. Controllers

#### ClasseController (`controller/ClasseController.java`)
- REST API endpoints for Classe management
- Endpoints: GET, POST, PUT, DELETE /api/classes
- Search and filter endpoints

#### CourseController (`controller/CourseController.java`)
- REST API endpoints for Course management
- Key endpoints for teacher functionality:
  - `GET /api/courses/enseignant/{enseignantId}` - Get courses by teacher
  - `GET /api/courses/enseignant/{enseignantId}/classes` - Get classes by teacher
  - `GET /api/courses/enseignant/{enseignantId}/classe/{classeId}` - Get courses by teacher and class

### 6. Database Schema

#### Tables Created
- `classes` - Stores class information
- `courses` - Stores course information with foreign keys to UE, Enseignant, and Classe
- Added `course_id` column to `emplois_du_temps` table

#### Sample Data
- 15 classes created (L1-INFO-A, L1-INFO-B, L2-INFO-A, etc.)
- 8 courses created linking UEs, teachers, and classes

### 7. Frontend Components

#### CourseService (`services/course.service.ts`)
- Angular service for Course and Classe API calls
- Methods for all CRUD operations
- Utility methods for formatting and validation

#### TeacherCoursesComponent
- Angular component for teachers to view their courses by class
- Features:
  - Groups courses by class and semester
  - Filtering by niveau, semestre, and search term
  - Statistics display (total classes, courses, hours)
  - Professional UI with cards and grids

## Key Features Implemented

### 1. Teacher Course Viewing
- Teachers can see all classes they teach in
- Courses are grouped by class and then by semester
- Each course shows: UE information, type (CM/TD/TP), hours, programming status

### 2. Class-Course Relationships
- Each class can have multiple courses
- Each course belongs to one class, one UE, and one teacher
- Proper foreign key relationships ensure data integrity

### 3. Statistics and Analytics
- Total courses per teacher
- Total hours per teacher
- Courses per class
- Programming completion status

### 4. Search and Filtering
- Search courses by code, UE name, or description
- Filter by niveau (L1, L2, L3, M1, M2)
- Filter by semestre (S1, S2, etc.)

### 5. Professional UI
- Modern card-based layout
- Color-coded course types and levels
- Responsive design for mobile and desktop
- Loading states and error handling

## API Endpoints Summary

### Classes
- `GET /api/classes` - Get all classes
- `GET /api/classes/{id}` - Get class by ID
- `GET /api/classes/niveau/{niveau}` - Get classes by level
- `POST /api/classes` - Create new class
- `PUT /api/classes/{id}` - Update class
- `DELETE /api/classes/{id}` - Delete class

### Courses
- `GET /api/courses` - Get all courses
- `GET /api/courses/enseignant/{enseignantId}` - Get courses by teacher
- `GET /api/courses/enseignant/{enseignantId}/classes` - Get classes by teacher
- `GET /api/courses/enseignant/{enseignantId}/classe/{classeId}` - Get courses by teacher and class
- `POST /api/courses` - Create new course
- `PUT /api/courses/{id}` - Update course
- `DELETE /api/courses/{id}` - Delete course

## Usage Example

When a teacher logs in and navigates to "Mes Cours par Classe":

1. System calls `GET /api/courses/enseignant/{teacherId}/classes` to get all classes the teacher teaches
2. For each class, system calls `GET /api/courses/enseignant/{teacherId}/classe/{classId}` to get courses
3. Frontend groups courses by semester and displays them in organized cards
4. Teacher can filter by level, semester, or search for specific courses
5. Each course card shows complete information including programming status

## Benefits

1. **Clear Organization**: Courses are logically grouped by class and semester
2. **Complete Information**: Teachers see UE details, hours, and programming status
3. **Easy Navigation**: Filtering and search make it easy to find specific information
4. **Professional Interface**: Modern UI provides excellent user experience
5. **Scalable Architecture**: Clean separation of concerns allows easy extension

## Next Steps

To complete the implementation:

1. Start the UserService application
2. Test the API endpoints
3. Integrate the TeacherCoursesComponent into the main application routing
4. Add the component to the teacher dashboard navigation
5. Test the complete flow from teacher login to course viewing

The implementation provides a solid foundation for teachers to view their courses organized by class, fulfilling the user's requirement for teachers to see "which course they teach and in which class."