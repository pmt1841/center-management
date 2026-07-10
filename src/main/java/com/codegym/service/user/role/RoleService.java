package com.codegym.service.user.role;

import com.codegym.model.user.Role;

import java.util.List;
import java.util.Optional;

public interface RoleService {
    List<Role> findAll();

    Optional<Role> findById(Long roleId);
}
