package org.carrent.coursework.mapper;

import org.carrent.coursework.dto.UserCreationDto;
import org.carrent.coursework.dto.UserDto;
import org.carrent.coursework.entity.User;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    //@Mapping "username", source = "username")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "role", source = "role")
    User toEntity(UserDto userDto);

    UserDto toDto(User user);
    UserCreationDto toCrDto(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    User partialUpdate(UserDto userDto, @MappingTarget User user);

    User toEntity(UserCreationDto userCreationDto);

}