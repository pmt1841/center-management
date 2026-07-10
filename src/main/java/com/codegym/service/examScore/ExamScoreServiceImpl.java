package com.codegym.service.examScore;

import com.codegym.dto.ClassAvgScoreDTO;
import com.codegym.dto.ScoreUpdateForm;
import com.codegym.exception.ResourceNotFoundException;
import com.codegym.model.course.ExamScore;
import com.codegym.model.user.student.Student;
import com.codegym.repository.course.ExamScoreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamScoreServiceImpl implements ExamScoreService {
    private final ExamScoreRepository examScoreRepository;

    @Override
    public Map<String, Double> getAverageScorePerClassThisMonth() {
        // 1. Lấy tháng và năm hiện tại
        LocalDate currentDate = LocalDate.now();
        int currentMonth = currentDate.getMonthValue();
        int currentYear = currentDate.getYear();

        // 2. Query dữ liệu điểm thi trong tháng
        List<ClassAvgScoreDTO> results = examScoreRepository.getAverageScorePerClassByMonthAndYear(currentMonth, currentYear);

        // 3. Chuyển đổi List sang Map, làm tròn điểm và giữ nguyên thứ tự sắp xếp giảm dần
        return results.stream()
                .collect(Collectors.toMap(
                        ClassAvgScoreDTO::classCode,
                        dto -> Math.round(dto.averageScore() * 10.0) / 10.0, // Làm tròn 1 chữ số thập phân
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));
    }

    @Override
    public List<Double> getMonthlyAveragesByYear(int currentYear) {
        // 1. Mồi sẵn mảng 12 tháng với giá trị điểm là 0.0
        List<Double> monthlyData = new ArrayList<>(Collections.nCopies(12, 0.0));

        // 2. Lấy dữ liệu thực tế từ Database
        List<Object[]> results = examScoreRepository.getMonthlyAverageScoresByYear(currentYear);

        // 3. Đắp dữ liệu vào mảng
        for (Object[] result : results) {
            // JPQL MONTH() trả về Integer
            int month = (Integer) result[0];
            // JPQL AVG() trả về Double
            Double avgScore = (Double) result[1];

            // Làm tròn điểm số 1 chữ số thập phân (VD: 8.5678 -> 8.6)
            double roundedScore = Math.round(avgScore * 10.0) / 10.0;

            // Set vào đúng vị trí tháng (Tháng 1 -> Index 0)
            monthlyData.set(month - 1, roundedScore);
        }

        return monthlyData;
    }

    @Transactional
    @Override
    public void updateScores(List<ScoreUpdateForm.ScoreItem> scoreItems) {
        if (scoreItems == null || scoreItems.isEmpty()) {
            return;
        }
        List<ExamScore> scoresToUpdate = new ArrayList<>();

        for (ScoreUpdateForm.ScoreItem item : scoreItems) {

            if (item.getScoreId() != null) {

                ExamScore examScore = examScoreRepository.findById(item.getScoreId())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dữ liệu điểm với ID: " + item.getScoreId()));

                examScore.setTheoryScore(item.getTheoryScore());
                examScore.setPracticalScore(item.getPracticalScore());

                double theory = (item.getTheoryScore() != null) ? item.getTheoryScore() : 0.0;
                double practical = (item.getPracticalScore() != null) ? item.getPracticalScore() : 0.0;

                double average = Math.round(((theory + practical) / 2) * 10.0) / 10.0;
                examScore.setAverageScore(average);
                scoresToUpdate.add(examScore);
            }
        }

        examScoreRepository.saveAll(scoresToUpdate);
    }

    @Override
    public void save(ExamScore newScore) {
        examScoreRepository.save(newScore);
    }

    @Override
    public Double calculateOverallGPA(Long id) {
        // 1. Lấy toàn bộ lịch sử thi của học viên
        List<ExamScore> allScores = examScoreRepository.findAllByStudentId(id);

        // Nếu chưa thi môn nào thì trả về null (hoặc 0.0 tùy bạn)
        if (allScores == null || allScores.isEmpty()) {
            return null;
        }

        // 2. Nhóm điểm theo Module và chỉ giữ lại điểm của lần thi MỚI NHẤT
        Map<Long, ExamScore> latestScoresPerModule = allScores.stream()
                .collect(Collectors.toMap(
                        score -> score.getCourseModule().getId(), // Key: ID của học phần
                        score -> score,                     // Value: Đối tượng ExamScore
                        (existingScore, newScore) -> {      // Hàm giải quyết xung đột nếu thi nhiều lần
                            // So sánh ngày thi, lấy bài thi có ngày sau cùng (mới nhất)
                            // Nếu hệ thống của bạn không có examDate, có thể so sánh theo ID: newScore.getId() > existingScore.getId()
                            if (newScore.getExamDate() != null && existingScore.getExamDate() != null) {
                                return newScore.getExamDate().isAfter(existingScore.getExamDate()) ? newScore : existingScore;
                            }
                            return newScore;
                        }
                ));

        // 3. Tính điểm trung bình (GPA) dựa trên các cột điểm mới nhất
        double totalGPA = 0.0;
        int moduleCount = 0;

        for (ExamScore finalScore : latestScoresPerModule.values()) {
            if (finalScore.getAverageScore() != null) {
                totalGPA += finalScore.getAverageScore();
                moduleCount++;
            }
        }

        // Tránh lỗi chia cho 0
        if (moduleCount == 0) {
            return null;
        }

        // Trả về điểm trung bình
        return totalGPA / moduleCount;
    }

    @Override
    public int countPassedModules(Long studentId, double passingScore) {
        List<ExamScore> allScores = examScoreRepository.findAllByStudentId(studentId);

        // Nếu chưa thi môn nào thì số môn đã qua chắc chắn là 0
        if (allScores == null || allScores.isEmpty()) {
            return 0;
        }

        // 2. Nhóm điểm theo ID Học phần (Module) và lấy bài thi MỚI NHẤT
        Map<Long, ExamScore> latestScoresPerModule = allScores.stream()
                .collect(Collectors.toMap(
                        score -> score.getCourseModule().getId(), // Key: ID học phần
                        score -> score,                     // Value: Đối tượng điểm
                        (existingScore, newScore) -> {      // Xử lý khi có nhiều bài thi trong cùng 1 môn
                            if (newScore.getExamDate() != null && existingScore.getExamDate() != null) {
                                return newScore.getExamDate().isAfter(existingScore.getExamDate()) ? newScore : existingScore;
                            }
                            return newScore;
                        }
                ));

        // 3. Đếm số lượng bài thi thỏa mãn điều kiện qua môn
        long passedCount = latestScoresPerModule.values().stream()
                .filter(score -> score.getAverageScore() != null && score.getAverageScore() >= passingScore)
                .count();

        // Ép kiểu từ long (của Stream) về int
        return (int) passedCount;
    }

    @Override
    public List<ExamScore> findRecentScoresByStudentId(Long studentId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);

        return examScoreRepository.findByStudentIdOrderByExamDateDesc(studentId, pageable);
    }
}
