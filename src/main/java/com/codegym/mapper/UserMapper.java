package com.codegym.mapper;

import com.codegym.dto.user.ProfileUpdateDTO;
import com.codegym.dto.user.UserCreateDTO;
import com.codegym.dto.user.UserEditDTO;
import com.codegym.model.user.AppUser;
import com.codegym.model.user.student.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "role.description", target = "roleDescription")
    UserEditDTO toDto(AppUser user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    void mapToUser(UserEditDTO dto, @MappingTarget AppUser user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    void mapToUser(UserCreateDTO dto, @MappingTarget AppUser user);

    @Mapping(target = "classroomId", ignore = true)
    UserEditDTO toDto(Student student);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    void updateProfileFromDto(ProfileUpdateDTO dto, @MappingTarget AppUser entity);
}
