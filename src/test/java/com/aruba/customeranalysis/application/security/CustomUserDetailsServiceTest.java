package com.aruba.customeranalysis.application.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.aruba.customeranalysis.infrastructure.persistence.model.security.RoleEntity;
import com.aruba.customeranalysis.infrastructure.persistence.model.security.UserEntity;
import com.aruba.customeranalysis.infrastructure.persistence.security.UserRepository;

public class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        RoleEntity roleUser = new RoleEntity();
        roleUser.setName("ROLE_USER");

        userEntity = new UserEntity();
        userEntity.setUsername("alice");
        userEntity.setPassword("encoded-password");
        userEntity.setRoles(Set.of(roleUser));
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(userEntity));

        UserDetails result = customUserDetailsService.loadUserByUsername("alice");

        assertNotNull(result);
        assertEquals("alice", result.getUsername());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));

        verify(userRepository).findByUsername("alice");
    }

    @Test
    void loadUserByUsername_shouldThrow_whenUserNotFound() {

        when(userRepository.findByUsername("notfound")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("notfound"));

        verify(userRepository).findByUsername("notfound");
    }
}