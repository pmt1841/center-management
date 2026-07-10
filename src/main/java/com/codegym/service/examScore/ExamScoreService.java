package com.codegym.service.examScore;

import com.codegym.dto.ScoreUpdateForm;
import com.codegym.model.course.ExamScore;

import java.util.List;
import java.util.Map;

public interface ExamScoreService {
    Map<String, Double> getAverageScorePerClassThisMonth();

    List<Double> getMonthlyAveragesByYear(int currentYear);

    void updateScores(List<ScoreUpdateForm.ScoreItem> scoreItems);

    void save(ExamScore newScore);

    Double calculateOverallGPA(Long id);

    int countPassedModules(Long id, double v);

    List<ExamScore> findRecentScoresByStudentId(Long studentId, int limit);
}
