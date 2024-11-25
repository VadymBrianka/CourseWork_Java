package org.carrent.coursework.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.carrent.coursework.dto.UserCreationDto;
import org.carrent.coursework.dto.UserDto;
import org.carrent.coursework.entity.Employee;
import org.carrent.coursework.entity.User;
import org.carrent.coursework.exception.UserAlreadyExistsException;
import org.carrent.coursework.exception.UserNotFoundException;
import org.carrent.coursework.repository.EmployeeRepository;
import org.carrent.coursework.enums.Role;
import org.carrent.coursework.mapper.UserMapper;
import org.carrent.coursework.repository.EmployeeRepository;
import org.carrent.coursework.repository.UserRepository;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@AllArgsConstructor
@Service
@Transactional
@CacheConfig(cacheResolver = "multiLevelCacheResolver")
public class UserService {
    private UserRepository userRepository;
    private UserMapper userMapper;
    private EmployeeRepository employeeRepository;

    @Transactional
    @Cacheable
    public UserDto getUserById(Long id) {

        return userMapper.toDto(userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found")));
    }

    @Transactional
    public UserDto createUser(UserCreationDto userDto) {
        String email = userDto.email();
        String phoneNumber = userDto.phoneNumber();
        User user = userMapper.toEntity(userDto);
        if(userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("User with email " + email + " already exists");
        }
        if(userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new UserAlreadyExistsException("User with phone number " + phoneNumber + " already exists");
        }
        if(userRepository.existsByUsername(userDto.username())) {
            throw new UserAlreadyExistsException("User with username " + userDto.username() + " already exists");
        }
        User savedUser = userRepository.save(user);
//        if (user.getRole() == Role.ROLE_EMPLOYEE) {
//            Employee employee = new Employee();
//            employee.setPosition(userDto.position());
//            employeeRepository.save(employee);
//        }

        return userMapper.toDto(savedUser);
    }

    @Transactional
    public UserDto updateUser(UserDto userDto) {
        return userMapper.toDto(userRepository.save(userMapper.toEntity(userDto)));
    }

//    @Transactional
//    public UserDto updateUserDetails(UserDto userDto) {
//        User existingUser = getCurrentUser();
//        userMapper.partialUpdate(userDto, existingUser);
//
//        User updatedUser = userRepository.save(existingUser);
//
//        if (updatedUser.getRole() == Role.ROLE_AGENT) {
//            Agent agent = agentRepository.findByUserId(updatedUser)
//                    .orElse(new Agent());
//
//            agent.setUserId(updatedUser);
//            agent.setHireDate(userDto.hireDate());
//            agent.setPosition(userDto.position());
//
//            agentRepository.save(agent);
//        }
//
//        return userMapper.toDto(updatedUser);
//    }


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

//    @Deprecated
//    public void getEmployee() {
//        var user = getCurrentUser();
//        user.setRole(Role.ROLE_EMPLOYEE);
//        userRepository.save(user);
//    }

    public UserDetailsService userDetailsService() {
        return this::getByUsername;
    }


    @Transactional
    @CacheEvict
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    @Cacheable
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toDto);
    }

//    public Page<AgentDto> getAllAgents(int page, int size, String[] sort) {
//        return agentRepository.findAll(PageRequest.of(page, size, Sort.by(sort))).map(agentMapper::toDto);
//    }

    @Transactional
    public Page<UserDto> getSortedUsers(String sortBy, String order, Pageable pageable) {
        Sort sort = order.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<User> usersPage = userRepository.findAll(sortedPageable);
        return usersPage.map(userMapper::toDto);
    }

//    @Transactional
//    public AgentDto getAgentWithUser(Long agentId) {
//        Agent agent = agentRepository.findById(agentId)
//                .orElseThrow(() -> new AgentNotFoundException("Agent with ID " + agentId + " not found"));
//
//        return agentMapper.toDto(agent);
//    }

    @Transactional
    public Page<UserDto> getFilteredUsers(Long id, LocalDate birthDate, String phoneNumber, String firstName, String surname, String email, String role, Pageable pageable) {
        Specification<User> specification = Specification.where(null);

        if (id != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get("id"), "%" + id + "%"));
        }
        if(birthDate != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("birthDate")), "%" + birthDate + "%"));
        }
        if (phoneNumber != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get("phoneNumber"), "%" + phoneNumber + "%"));
        }
        if(firstName != null && !firstName.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%"));
        }
        if(surname != null && !surname.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("surname")), "%" + surname.toLowerCase() + "%"));
        }
        if(email != null && !email.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
        }
        if(role != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("role")), "%" + role.toLowerCase() + "%"));
        }

        Page<User> users = userRepository.findAll(specification, pageable);
        return users.map(userMapper::toDto);
    }
}
