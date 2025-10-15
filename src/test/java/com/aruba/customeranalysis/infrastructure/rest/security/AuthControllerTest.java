package com.aruba.customeranalysis.infrastructure.rest.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.aruba.customeranalysis.application.security.CustomUserDetailsService;
import com.aruba.customeranalysis.config.security.JwtUtil;

public class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    private AuthController.AuthRequest authRequest;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        authRequest = new AuthController.AuthRequest("alice", "password");
        userDetails = User.withUsername("alice")
                .password("password")
                .roles("USER")
                .build();
    }

    @Test
    void login_shouldAuthenticateAndReturnToken() {

        when(userDetailsService.loadUserByUsername("alice")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("mock-jwt-token");

        ResponseEntity<?> response = authController.login(authRequest);

        assertEquals(200, response.getStatusCode().value());
        AuthController.AuthResponse body = (AuthController.AuthResponse) response.getBody();
        assertNotNull(body);
        assertEquals("mock-jwt-token", body.token());

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("alice", "password"));
        verify(userDetailsService).loadUserByUsername("alice");
        verify(jwtUtil).generateToken(userDetails);
        
    }

    @Test
    void login_shouldThrowException_whenAuthenticationFails() {

        doThrow(new RuntimeException("Bad credentials"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(RuntimeException.class, () -> authController.login(authRequest));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtUtil, never()).generateToken(any());
    }
}