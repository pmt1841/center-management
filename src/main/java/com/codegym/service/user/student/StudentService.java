package com.codegym.service.user.student;

import com.codegym.dto.user.StudentUpdateDTO;
import com.codegym.dto.user.UserCreateDTO;
import com.codegym.model.user.lecturer.Lecturer;
import com.codegym.model.course.ExamScore;
import com.codegym.model.user.student.Student;
import com.codegym.model.user.student.StudentDiary;
import com.codegym.model.user.student.StudentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface StudentService {
    Optional<Student> findByStudentCode(String studentCode);

    List<StudentDiary> getStudentDiaries(Student student);

    void saveStudentDiary(StudentDiary studentDiary);

    long countByLecturer(Lecturer lecturer);

    List<Student> findAll();

    Page<Student> searchStudents(String keyword, StudentStatus studentStatus, Long classroomId, Pageable pageable);

    List<ExamScore> getExamScoreByStudent(Student student);

    void update(Student student);

    long count();

    List<Integer> getMonthlyEnrollmentsByYear(int currentYear);

    long countStudentsUnderAverageScore(double averageScore);

    void updateStudentInfo(String studentCode, StudentUpdateDTO dto);

    Optional<Student> findById(Long studentId);

    Page<Student> searchStudentsByLecturer(String keyword, StudentStatus status, Pageable pageable, Lecturer lecturer);

    void deleteByStudentCode(String studentCode);

    void restoreByStudentCode(String studentCode);

    List<ExamScore> findScoreByStudentIdAndCourseId(Long studentId, Long courseId);
}
