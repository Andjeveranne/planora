package com.isi4.userservice.repository;

import com.isi4.userservice.model.CourseEnrollment;
import com.isi4.userservice.model.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {

    List<CourseEnrollment> findByStudentId(String studentId);

    List<CourseEnrollment> findByStudentIdAndStatus(String studentId, EnrollmentStatus status);

    List<CourseEnrollment> findByCourseId(Long courseId);

    List<CourseEnrollment> findByCourseIdAndStatus(Long courseId, EnrollmentStatus status);

    Optional<CourseEnrollment> findByStudentIdAndCourseId(String studentId, Long courseId);

    boolean existsByStudentIdAndCourseId(String studentId, Long courseId);

    void deleteByStudentIdAndCourseId(String studentId, Long courseId);

    long countByCourseIdAndStatus(Long courseId, EnrollmentStatus status);
}

