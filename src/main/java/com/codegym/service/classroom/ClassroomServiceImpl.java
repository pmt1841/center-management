package com.codegym.service.classroom;

import com.codegym.dto.ClassroomDTO;
import com.codegym.exception.ResourceNotFoundException;
import com.codegym.model.classroom.ClassDiary;
import com.codegym.model.classroom.Classroom;
import com.codegym.model.course.Course;
import com.codegym.model.user.lecturer.Lecturer;
import com.codegym.model.user.student.Student;
import com.codegym.model.user.student.StudentStatus;
import com.codegym.repository.classroom.ClassDiaryRepository;
import com.codegym.repository.classroom.ClassroomRepository;
import com.codegym.repository.course.ExamScoreRepository;
import com.codegym.repository.course.CourseRepository;
import com.codegym.repository.user.lecturer.LecturerRepository;
import com.codegym.repository.user.student.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClassroomServiceImpl implements ClassroomService {
    private final ClassroomRepository classroomRepository;
    private final ClassDiaryRepository classDiaryRepository;
    private final ExamScoreRepository examScoreRepository;
    private final LecturerRepository lecturerRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;

    @Override
    public List<Classroom> findAllByLecturer(Lecturer lecturer) {
        return classroomRepository.findAllByLecturer(lecturer);
    }

    @Override
    public Optional<Classroom> findByClassCode(String classCode) {
        return classroomRepository.findByClassCode(classCode);
    }

    @Override
    public long count() {
        return classroomRepository.count();
    }

    @Override
    public Page<Classroom> findAll(Pageable pageable) {
        return classroomRepository.findAll(pageable);
    }

    @Override
    public List<Classroom> findAll() {
        return classroomRepository.findAll();
    }

    @Override
    public Page<Classroom> searchClassrooms(String keyword, Pageable pageable) {
        String safeKeyword = (keyword == null) ? "" : keyword.trim();
        return classroomRepository.searchClassrooms(safeKeyword, pageable);
    }

    @Override
    public void saveClassDiary(ClassDiary classDiary) {
        classDiaryRepository.save(classDiary);
    }

    @Override
    public Optional<Classroom> findById(Long classroomId) {
        return classroomRepository.findById(classroomId);
    }

    @Override
    public List<ClassDiary> findAllDiariesByClassroom(Classroom classroom) {
        return classDiaryRepository.findByClassroomOrderByCreatedDateDesc(classroom);
    }

    @Override
    public void save(Classroom classroom) {
        classroomRepository.save(classroom);
    }

    @Override
    @Transactional
    public void updateClassroom(String classCode, ClassroomDTO classroomDTO) {
        Classroom classroom = classroomRepository.findByClassCode(classCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học: " + classCode));

        classroom.setClassName(classroomDTO.getClassName());

        if (classroomDTO.getCourseId() != null) {
            Course course = courseRepository.findById(classroomDTO.getCourseId())
                    .orElseThrow(() -> new IllegalArgumentException("Môn học không tồn tại"));
            classroom.setCourse(course);
        }

        if (classroomDTO.getLecturerId() != null) {
            Lecturer lecturer = lecturerRepository.findById(classroomDTO.getLecturerId())
                    .orElseThrow(() -> new IllegalArgumentException("Giảng viên không tồn tại"));
            classroom.setLecturer(lecturer);
        } else {
            classroom.setLecturer(null);
        }

        classroomRepository.save(classroom);
    }

    @Override
    public List<ClassDiary> findAllDiaries() {
        return classDiaryRepository.findAllByOrderByCreatedDateDesc();
    }

    @Override
    public long countDiariesByDate(LocalDate now) {
        return classDiaryRepository.countAllByCreatedDate(now);
    }

    @Override
    public Long countByLecturer(Lecturer lecturer) {
        return classroomRepository.countByLecturer(lecturer);
    }

    @Override
    @Transactional
    public void deleteByClassCode(String classCode) {
        Classroom classroom = classroomRepository.findByClassCode(classCode).get();
        if (classroom.getStudents().isEmpty()) {
            classroomRepository.delete(classroom);
        } else {
            classroom.setDeletedAt(LocalDateTime.now());
            classroomRepository.save(classroom);
        }
    }

    @Override
    @Transactional
    public void restoreByClassCode(String classCode) {
        Classroom classroom = classroomRepository.findByClassCode(classCode).get();
        classroom.setDeletedAt(null);
        classroomRepository.save(classroom);
    }

    @Override
    public Page<Classroom> searchClassroomsByLecturer(String keyword, Pageable pageable, Lecturer lecturer) {
        return classroomRepository.searchClassroomsByLecturer(keyword, lecturer, pageable);
    }

    @Override
    public Map<String, Double> getClassroomAverageScores(List<Classroom> classrooms) {
        Map<String, Double> chartData = new LinkedHashMap<>();

        for (Classroom classroom : classrooms) {
            String classCode = classroom.getClassCode();

            Double avgScore = examScoreRepository.calculateAverageScoreByClassroomId(classroom.getId());

            if (avgScore == null) {
                avgScore = 0.0;
            } else {
                avgScore = Math.round(avgScore * 10.0) / 10.0;
            }

            chartData.put(classCode, avgScore);
        }

        return chartData;
    }

    @Override
    @Transactional
    public void removeStudent(String classCode, String studentCode) {
        Classroom classroom = classroomRepository.findByClassCode(classCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học " + classCode));
        Student foundStudent = classroom.getStudents().stream()
                .filter(student -> student.getStudentCode().equals(studentCode))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học viên " + studentCode));
        foundStudent.setClassroom(null);
        foundStudent.setStudentStatus(StudentStatus.WAITING_TRANSFER);
        studentRepository.save(foundStudent);
    }

}
