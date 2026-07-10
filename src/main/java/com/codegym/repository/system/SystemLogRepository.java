package com.codegym.repository.system;

import com.codegym.model.system.ActionType;
import com.codegym.model.system.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {
    Page<SystemLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT s FROM SystemLog s WHERE " +
            "(:keyword IS NULL OR LOWER(s.username) " +
            "LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(s.details) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:actionType IS NULL OR s.actionType = :actionType) " +
            "AND (:startDate IS NULL OR s.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR s.createdAt <= :endDate) " +
            "ORDER BY s.createdAt DESC")
    Page<SystemLog> filterLogs(
            @Param("keyword") String keyword,
            @Param("actionType") ActionType actionType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
