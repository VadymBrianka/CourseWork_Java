package org.carrent.coursework.controller;

import io.swagger.v3.oas.annotations.Operation;
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
    public JwtAuthenticationResponse signUp(@RequestBody @Valid SignUpRequest request) {
        return authenticationService.signUp(request);
    }

    @PostMapping("/sign-in")
    public JwtAuthenticationResponse signIn(@RequestBody @Valid SignInRequest request) {
        return authenticationService.signIn(request);
    }

    @PostMapping("/refresh-token")
    public JwtAuthenticationResponse refreshToken() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        JwtService jwtService = new JwtService();
        String jwt = jwtService.generateToken(userDetails);
        return new JwtAuthenticationResponse(jwt);
    }

    @GetMapping("/current-user")
    public String getCurrentUser(Authentication authentication) {
        return "Current user: " + authentication.getName() + ", roles: " + authentication.getAuthorities();
    }
    @GetMapping("/get-admin")
    @Operation(summary = "Получити роль ADMIN (для демонстрації)")
    public void getAdmin() {
        userService.getAdmin();
    }
}