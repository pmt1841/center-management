package com.codegym.repository.classroom;

import com.codegym.model.classroom.ClassDiary;
import com.codegym.model.classroom.Classroom;
import com.codegym.model.user.student.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ClassDiaryRepository extends JpaRepository<ClassDiary, Long> {
    List<ClassDiary> findByClassroomOrderByCreatedDateDesc(Classroom classroom);

    long countAllByCreatedDate(LocalDate now);

    List<ClassDiary> findAllByOrderByCreatedDateDesc();
}
