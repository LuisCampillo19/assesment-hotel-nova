package com.luiscampillo.hotelnova.service;

import com.luiscampillo.hotelnova.dao.UserDao;
import com.luiscampillo.hotelnova.exception.AuthenticationException;
import com.luiscampillo.hotelnova.model.entity.User;
import com.luiscampillo.hotelnova.model.enums.UserRole;
import com.luiscampillo.hotelnova.util.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserDao userDao;
    @InjectMocks AuthService service;

    private User activeUser;
    private User inactiveUser;

    @BeforeEach
    void setUp() {
        String hash = PasswordEncoder.hash("admin123");
        activeUser   = new User(1, "admin", hash, UserRole.ADMIN, "Admin", "a@x.com", true,  null);
        inactiveUser = new User(2, "ghost", hash, UserRole.ADMIN, "Ghost", "g@x.com", false, null);
    }

    @Test
    void loginSucceedsWithCorrectCredentials() {
        when(userDao.findByUsername("admin")).thenReturn(Optional.of(activeUser));
        User result = service.login("admin", "admin123");
        assertEquals(activeUser, result);
    }

    @Test
    void loginFailsWithWrongPassword() {
        when(userDao.findByUsername("admin")).thenReturn(Optional.of(activeUser));
        assertThrows(AuthenticationException.class, () -> service.login("admin", "wrong"));
    }

    @Test
    void loginFailsWhenUserDoesNotExist() {
        when(userDao.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThrows(AuthenticationException.class, () -> service.login("ghost", "any"));
    }

    @Test
    void loginFailsWhenUserIsInactive() {
        when(userDao.findByUsername("ghost")).thenReturn(Optional.of(inactiveUser));
        assertThrows(AuthenticationException.class, () -> service.login("ghost", "admin123"));
    }
}
