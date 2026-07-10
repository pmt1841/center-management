package com.codegym.service.user.student;

import com.codegym.dto.user.StudentUpdateDTO;
import com.codegym.dto.user.UserCreateDTO;
import com.codegym.exception.ResourceNotFoundException;
import com.codegym.mapper.UserMapper;
import com.codegym.model.classroom.Classroom;
import com.codegym.model.user.AppUser;
import com.codegym.model.user.lecturer.Lecturer;
import com.codegym.model.course.ExamScore;
import com.codegym.model.user.student.Student;
import com.codegym.model.user.student.StudentDiary;
import com.codegym.model.user.student.StudentStatus;
import com.codegym.repository.user.student.StudentDiaryRepository;
import com.codegym.repository.user.student.StudentRepository;
import com.codegym.repository.tuition.TuitionPaymentRepository;
import com.codegym.repository.course.ExamScoreRepository;
import com.codegym.service.classroom.ClassroomService;
import com.codegym.service.tuition.TuitionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {
    private final StudentRepository studentRepository;
    private final StudentDiaryRepository studentDiaryRepository;
    private final TuitionService tuitionService;
    private final ExamScoreRepository examScoreRepository;
    private final ClassroomService classroomService;
    private final UserMapper userMapper;

    @Override
    public Optional<Student> findByStudentCode(String studentCode) {
        return studentRepository.findByStudentCode(studentCode);
    }

    @Override
    public List<StudentDiary> getStudentDiaries(Student student) {
        return studentDiaryRepository.findByStudentOrderByCreatedDateDesc(student);
    }

    @Override
    public void saveStudentDiary(StudentDiary studentDiary) {
        studentDiaryRepository.save(studentDiary);
    }

    @Override
    public long countByLecturer(Lecturer lecturer) {
        return studentRepository.countByLecturer(lecturer);
    }

    @Override
    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    @Override
    public Page<Student> searchStudents(String keyword, StudentStatus studentStatus, Long classroomId, Pageable pageable) {
        String safeKeyword = (keyword == null) ? "" : keyword.trim();
        return studentRepository.searchStudents(safeKeyword, studentStatus, classroomId, pageable);
    }

    @Override
    public List<ExamScore> getExamScoreByStudent(Student student) {
        return examScoreRepository.findAllByStudent(student);
    }

    @Override
    public void update(Student student) {
        studentRepository.save(student);
    }

    @Override
    public long count() {
        return studentRepository.countByStudentStatusIn(List.of(StudentStatus.STUDYING, StudentStatus.SUSPENDED, StudentStatus.WAITING_TRANSFER));
    }

    @Override
    public List<Integer> getMonthlyEnrollmentsByYear(int year) {
        // 1. Khởi tạo danh sách 12 phần tử có giá trị mặc định là 0
        List<Integer> monthlyData = new ArrayList<>(Collections.nCopies(12, 0));

        // 2. Query dữ liệu từ DB (Chỉ trả về những tháng có học viên)
        List<Object[]> results = studentRepository.countEnrollmentsByMonthAndYear(year);

        // 3. Mapping dữ liệu vào đúng vị trí tháng
        for (Object[] result : results) {
            // JPQL MONTH() trả về kiểu Integer (1 -> 12)
            int month = (Integer) result[0];
            // JPQL COUNT() trả về kiểu Long
            long count = (Long) result[1];

            // Set số lượng vào list. Vì index của List bắt đầu từ 0, nên Tháng 1 -> Index 0
            monthlyData.set(month - 1, (int) count);
        }

        // Kết quả trả về luôn đảm bảo đủ 12 số: [50, 0, 12, 80, 0, ...]
        return monthlyData;
    }

    @Override
    @Transactional
    public long countStudentsUnderAverageScore(double averageScore) {
        return studentRepository.countStudentUnderAverageScore(averageScore);
    }

    @Override
    @Transactional
    public void updateStudentInfo(String studentCode, StudentUpdateDTO dto) {
        Student student = this.findByStudentCode(studentCode).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học viên"));

        student.setFullName(dto.getFullName());
        student.setStudentStatus(dto.getStudentStatus());

        if (dto.getStudentStatus() == StudentStatus.WAITING_TRANSFER
                || dto.getStudentStatus() == StudentStatus.DROPPED_OUT) {
            student.setClassroom(null);
        } else if (dto.getClassroomId() != null) {
            Classroom classroom = classroomService.findById(dto.getClassroomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học"));
            student.setClassroom(classroom);
        }

        studentRepository.save(student);
    }

    @Override
    public Optional<Student> findById(Long studentId) {
        return studentRepository.findById(studentId);
    }

    @Override
    public Page<Student> searchStudentsByLecturer(String keyword, StudentStatus status, Pageable pageable, Lecturer lecturer) {
        return studentRepository.searchStudentsByLecturer(keyword, status, pageable, lecturer);
    }

    @Override
    @Transactional
    public void deleteByStudentCode(String studentCode) {
        Optional<Student> student = studentRepository.findByStudentCode(studentCode);
        if (student.isPresent()) {
            Student stud = student.get();
            stud.setDeletedAt(LocalDateTime.now());
            studentRepository.save(stud);
        }
    }

    @Override
    public void restoreByStudentCode(String studentCode) {
        Optional<Student> student = studentRepository.findByStudentCode(studentCode);
        if (student.isPresent()) {
            Student stud = student.get();
            stud.setDeletedAt(null);
            studentRepository.save(stud);
        }
    }

    @Override
    public List<ExamScore> findScoreByStudentIdAndCourseId(Long studentId, Long courseId) {
        return examScoreRepository.findByStudentIdAndCourseId(studentId, courseId);
    }
}
