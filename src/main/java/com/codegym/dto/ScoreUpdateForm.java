package com.codegym.dto;

import lombok.Data;

import java.util.List;

@Data
public class ScoreUpdateForm {
    private List<ScoreItem> scoreItems;

    @Data
    public static class ScoreItem {
        private Long scoreId;
        private Double theoryScore;
        private Double practicalScore;
    }
}
