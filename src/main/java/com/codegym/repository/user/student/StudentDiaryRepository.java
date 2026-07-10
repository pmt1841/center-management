package com.codegym.repository.user.student;

import com.codegym.model.user.student.Student;
import com.codegym.model.user.student.StudentDiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StudentDiaryRepository extends JpaRepository<StudentDiary, Long> {
    List<StudentDiary> findByStudentOrderByCreatedDateDesc(Student student);
}
