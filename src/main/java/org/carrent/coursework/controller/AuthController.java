package org.carrent.coursework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.carrent.coursework.dto.JwtAuthenticationResponse;
import org.carrent.coursework.dto.SignInRequest;
import org.carrent.coursework.dto.SignUpRequest;
import org.carrent.coursework.service.AuthenticationService;
import org.carrent.coursework.service.JwtService;
import org.carrent.coursework.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;
    private final UserService userService;

    @PostMapping("/sign-up")
    @Operation(
            summary = "User Registration",
            description = "Registers a new user in the system and returns a JWT for authentication.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User successfully registered and authenticated.",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtAuthenticationResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input data.",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public JwtAuthenticationResponse signUp(@RequestBody @Valid SignUpRequest request) {
        return authenticationService.signUp(request);
    }


    @PostMapping("/sign-in")
    @Operation(
            summary = "User Login",
            description = "Authenticates a user with their credentials and returns a JWT for further use.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User successfully authenticated.",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtAuthenticationResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid credentials.",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public JwtAuthenticationResponse signIn(@RequestBody @Valid SignInRequest request) {
        return authenticationService.signIn(request);
    }


    @PostMapping("/refresh-token")
    @Operation(
            summary = "Refresh JWT",
            description = "Generates a new JWT for the currently authenticated user based on their session.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "New JWT successfully generated.",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtAuthenticationResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Authentication required.",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public JwtAuthenticationResponse refreshToken() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        JwtService jwtService = new JwtService();
        String jwt = jwtService.generateToken(userDetails);
        return new JwtAuthenticationResponse(jwt);
    }


    @GetMapping("/current-user")
    @Operation(
            summary = "Get Current User",
            description = "Returns details of the currently authenticated user, including username and roles.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User information retrieved successfully.",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Authentication required.",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public String getCurrentUser(Authentication authentication) {
        return "Current user: " + authentication.getName() + ", roles: " + authentication.getAuthorities();
    }

    @GetMapping("/get-admin")
    @Operation(
            summary = "Get ADMIN Role (Demo Purpose)",
            description = "Fetches or ensures the existence of a user with the ADMIN role for demonstration purposes.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "ADMIN role data retrieved successfully.",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Authentication required.",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public void getAdmin() {
        userService.getAdmin();
    }

}