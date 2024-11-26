package org.carrent.coursework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.carrent.coursework.dto.CarDto;
import org.carrent.coursework.dto.UserCreationDto;
import org.carrent.coursework.dto.UserDto;
import org.carrent.coursework.service.UserService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(
            summary = "Get user by ID",
            description = "Fetches user details based on the provided ID.",
            security = @SecurityRequirement(name = "BearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully fetched user details",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UserDto.class))),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @GetMapping("{id}")
    @Cacheable(value = "users", key = "#id")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @Operation(
            summary = "Get all users with pagination",
            description = "Retrieves a paginated list of users with optional sorting.",
            security = @SecurityRequirement(name = "BearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully fetched list of users",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Page.class))),
            }
    )
    @GetMapping
    @Cacheable(value = "users")
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order
    ) {
        Sort sort = order.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserDto> users = userService.getAll(pageable);
        return ResponseEntity.ok(users);
    }

    @Operation(
            summary = "Get all available users",
            description = "Retrieves a paginated list of available users with optional sorting.",
            security = @SecurityRequirement(name = "BearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully fetched list of available users",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Page.class))),
            }
    )
    @GetMapping("/available")
    @Cacheable(value = "users")
    public ResponseEntity<Page<UserDto>> getAllUsersAvailable(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order
    ) {
        Sort sort = order.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserDto> users = userService.getAllAvailable(pageable);
        return ResponseEntity.ok(users);
    }

    @Operation(
            summary = "Create a new user",
            description = "Adds a new user to the system and clears cache.",
            security = @SecurityRequirement(name = "BearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "User created successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CarDto.class))),
            }
    )
    @PostMapping
    @CacheEvict(value = "users", allEntries = true)
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserCreationDto userCreationDto) {
        return new ResponseEntity<>(userService.createUser(userCreationDto), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Update user details",
            description = "Updates user details by ID and clears cache.",
            security = @SecurityRequirement(name = "BearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "User updated successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UserDto.class))),
                    @ApiResponse(responseCode = "404", description = "Car not found")
            }
    )
    @PutMapping("{id}")
    @CacheEvict(value = "users", allEntries = true)
    public ResponseEntity<UserDto> updateCar(
            @PathVariable Long id,
            @Valid @RequestBody UserDto userDto
    ) {
        return ResponseEntity.ok(userService.updateUser(id, userDto));
    }

    @Operation(
            summary = "Get sorted users",
            description = "Fetches a sorted list of users based on specified criteria.",
            security = @SecurityRequirement(name = "BearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully fetched sorted users",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "404", description = "No users found")
            }
    )
    @GetMapping("/sort")
    public ResponseEntity<?> getSortedUsers(@RequestParam String sortBy, @RequestParam String order, @PageableDefault Pageable pageable) {
        Page<UserDto> sortedUsers = userService.getSortedUsers(sortBy, order, pageable);
        if (sortedUsers.isEmpty()) {
            return new ResponseEntity<>("No users found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(sortedUsers, HttpStatus.OK);
    }

    @Operation(
            summary = "Get filtered users",
            description = "Retrieves a list of users filtered by specific parameters.",
            security = @SecurityRequirement(name = "BearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully fetched filtered users",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "404", description = "No users found")
            }
    )
    @GetMapping("/filter")
    public ResponseEntity<?> getFilteredUsers(
            @RequestParam(required = false) String username,
            @PageableDefault Pageable pageable
    ) {
        Page<UserDto> filteredUsers = userService.getFilteredUsers(username, pageable);
        if (filteredUsers.isEmpty()) {
            return new ResponseEntity<>("No users found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(filteredUsers, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = "users", allEntries = true)
    @Operation(
            summary = "Delete a user by ID",
            description = "Deletes a user from the database using the specified ID. Also clears the cache associated with the list of users.",
            security = @SecurityRequirement(name = "BearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User successfully deleted",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User with the specified ID not found",
                            content = @Content(mediaType = "application/json")
                    )
            },
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "The ID of the user to delete",
                            required = true,
                            example = "42"
                    )
            }
    )
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deleteUser(id));
    }


}

