package org.carrent.coursework.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.carrent.coursework.dto.CarCreationDto;
import org.carrent.coursework.dto.CarDto;
import org.carrent.coursework.dto.UserCreationDto;
import org.carrent.coursework.dto.UserDto;
import org.carrent.coursework.entity.Car;
import org.carrent.coursework.entity.User;
import org.carrent.coursework.enums.CarStatus;
import org.carrent.coursework.exception.CarAlreadyExistsException;
import org.carrent.coursework.exception.CarNotFoundException;
import org.carrent.coursework.exception.UserAlreadyExistsException;
import org.carrent.coursework.exception.UserNotFoundException;
import org.carrent.coursework.enums.Role;
import org.carrent.coursework.mapper.UserMapper;
import org.carrent.coursework.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
@Transactional
@CacheConfig(cacheResolver = "multiLevelCacheResolver")
public class UserService {
    private UserRepository userRepository;
    private UserMapper userMapper;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);



    public UserDto getById(Long id) {
        logger.info("Fetching user by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", id);
                    return new UserNotFoundException("Car not found");
                });
        logger.info("User with ID: {} successfully fetched", id);
        return userMapper.toDto(user);
    }

    public Page<UserDto> getAll(Pageable pageable) {
        logger.info("Fetching all users with pagination: {}", pageable);
        return userRepository.findAll(pageable)
                .map(userMapper::toDto);
    }


    public Page<UserDto> getAllAvailable(Pageable pageable) {
        logger.info("Fetching all available users with pagination: {}", pageable);
        Page<User> users = userRepository.findAll(pageable);
        logger.info("Fetched {} users for filtering available ones", users.getTotalElements());
        return users.stream()
                .filter(user -> !user.isDeleted())
                .map(userMapper::toDto)
                .collect(Collectors.collectingAndThen(Collectors.toList(),
                        list -> new PageImpl<>(list, pageable, users.getTotalElements())));
    }




    @Transactional
    public UserDto createUser(UserCreationDto userCreationDto) {
        logger.info("Creating user with username: {}", userCreationDto.username());
        User user = userMapper.toEntity(userCreationDto);
        if (userRepository.existsByUsername(userCreationDto.username())) {
            logger.warn("User with username {} already exists", userCreationDto.username());
            throw new UserAlreadyExistsException("Car with license plate " + userCreationDto.username() + " already exists");
        }
        User savedUser = userRepository.save(user);

        logger.info("User with username: {} created successfully", userCreationDto.username());
        return userMapper.toDto(savedUser);
    }

    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        logger.info("Updating user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with ID: " + id + " not found"));
        logger.info("User found: {}", user);
        userMapper.partialUpdate(userDto, user);
        User updatedUser = userRepository.save(user);
        logger.info("User with ID: {} successfully updated", id);
        return userMapper.toDto(updatedUser);
    }


    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User with username " + username + " not found"));
    }

    public User getCurrentUser() {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        return getByUsername(username);
    }

    @Deprecated
    public void getAdmin() {
        var user = getCurrentUser();
        user.setRole(Role.ROLE_ADMIN);
        userRepository.save(user);
    }


    public UserDetailsService userDetailsService() {
        return this::getByUsername;
    }

    public Page<UserDto> getSortedUsers(String sortBy, String order, Pageable pageable) {
        logger.info("Fetching sorted users by {} in {} order", sortBy, order);
        Sort sort = order.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<User> usersPage = userRepository.findAll(sortedPageable);
        logger.info("Fetched {} users", usersPage.getTotalElements());
        return usersPage.map(userMapper::toDto);
    }


    @Transactional
    public Page<UserDto> getFilteredUsers(Long id, String role, Pageable pageable) {
        Specification<User> specification = Specification.where(null);

        if (id != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get("id"), "%" + id + "%"));
        }
        if(role != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("role")), "%" + role.toLowerCase() + "%"));
        }

        Page<User> users = userRepository.findAll(specification, pageable);
        return users.map(userMapper::toDto);
    }

    @Transactional
    public String deleteUser(Long id) {
        logger.info("Deleting user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with ID: " + id + " not found"));
        user.setDeleted(true);
        userRepository.save(user);
        logger.info("User with ID: {} marked as deleted", id);
        return "User with ID " + id + " has been deleted.";
    }

    public Page<UserDto> getFilteredUsers(String username, Pageable pageable) {
        logger.info("Fetching filtered users with parameters: username={}", username);

        Specification<User> specification = Specification.where(null);

        if (username != null && !username.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), "%" + username.toLowerCase() + "%"));
        }
        Page<User> users = userRepository.findAll(specification, pageable);
        logger.info("Fetched {} users with applied filters", users.getTotalElements());

        return users.map(userMapper::toDto);
    }
}
