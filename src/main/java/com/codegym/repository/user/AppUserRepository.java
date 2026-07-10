package com.codegym.repository.user;

import com.codegym.model.user.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);

    @Query("SELECT u FROM AppUser u " +
            "LEFT JOIN u.role r " +
            "WHERE (:roleId IS NULL OR r.id = :roleId) " +
            "AND (:keyword IS NULL OR :keyword = '' OR (" +
            "    LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "    OR LOWER(u.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            ")) ")
    Page<AppUser> searchUsers(@Param("keyword") String keyword,
                              @Param("roleId") Long roleId,
                              Pageable pageable);

    long countAppUserByRole_Name(String roleName);
}
