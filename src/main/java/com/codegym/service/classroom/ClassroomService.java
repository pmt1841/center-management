package com.codegym.service.classroom;

import com.codegym.dto.ClassroomDTO;
import com.codegym.model.classroom.ClassDiary;
import com.codegym.model.classroom.Classroom;
import com.codegym.model.user.lecturer.Lecturer;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ClassroomService {
    List<Classroom> findAllByLecturer(Lecturer lecturer);

    Optional<Classroom> findByClassCode(String classCode);

    long count();

    Page<Classroom> findAll(Pageable pageable);

    List<Classroom> findAll();

    Page<Classroom> searchClassrooms(String keyword, Pageable pageable);

    void saveClassDiary(ClassDiary classDiary);

    Optional<Classroom> findById(Long classroomId);

    List<ClassDiary> findAllDiariesByClassroom(Classroom classroom);

    void save(Classroom classroom);

    void updateClassroom(String classCode, @Valid ClassroomDTO classroomDTO);

    List<ClassDiary> findAllDiaries();

    long countDiariesByDate(LocalDate now);

    Long countByLecturer(Lecturer lecturer);

    void deleteByClassCode(String classCode);

    void restoreByClassCode(String classCode);

    Page<Classroom> searchClassroomsByLecturer(String keyword, Pageable pageable, Lecturer lecturer);

    Map<String, Double> getClassroomAverageScores(List<Classroom> classrooms);

    void removeStudent(String classCode, String studentCode);
}
