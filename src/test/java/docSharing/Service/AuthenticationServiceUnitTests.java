package docSharing.Service;

import docSharing.Entities.LoginRequest;
import docSharing.Entities.User;
import docSharing.repository.UserRepository;
import docSharing.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class AuthenticationServiceUnitTests {
    @Autowired
    AuthenticationService authenticationService;

    @MockBean
    UserRepository userRepository;

    @Test
    void login_NotRegistered_throwsIllegalArgument() {
        when(userRepository.findByEmail("gideon.jaffe@gmail.com")).thenReturn(null);
        LoginRequest userDetails = new LoginRequest("gideon.jaffe@gmail.com", "qwer1234");

        assertThrows(IllegalArgumentException.class,() ->authenticationService.login(userDetails));
    }

    @Test
    void login_IncorrectPassword_throwsIllegalArgument() {
        User user = new User();
        user.setEmail("gideon.jaffe@gmail.com");
        user.setPassword("1234qwer");
        user.setId(5L);
        when(userRepository.findByEmail("gideon.jaffe@gmail.com")).thenReturn(user);
        LoginRequest userDetails = new LoginRequest("gideon.jaffe@gmail.com", "qwer1234");

        assertThrows(IllegalArgumentException.class, () -> authenticationService.login(userDetails));
    }

    @Test
    void login_UserNotActivated_throwsIllegalArgument() {
        User user = new User();
        user.setEmail("gideon.jaffe@gmail.com");
        user.setPassword("qwer1234");
        user.setId(5L);
        user.setActivated(false);
        when(userRepository.findByEmail("gideon.jaffe@gmail.com")).thenReturn(user);
        LoginRequest userDetails = new LoginRequest("gideon.jaffe@gmail.com", "qwer1234");

        assertThrows(IllegalArgumentException.class, () -> authenticationService.login(userDetails));
    }

    @Test
    void login_GoodUser_doesNotThrow() {
        User user = new User();
        user.setEmail("gideon.jaffe@gmail.com");
        user.setPassword("qwer1234");
        user.setId(5L);
        user.setActivated(true);
        when(userRepository.findByEmail("gideon.jaffe@gmail.com")).thenReturn(user);
        LoginRequest userDetails = new LoginRequest("gideon.jaffe@gmail.com", "qwer1234");

        assertDoesNotThrow(() -> authenticationService.login(userDetails));
    }

    @Test
    void login_LoggedInUser_returnSameToken() {
        User user = new User();
        user.setEmail("gideon.jaffe@gmail.com");
        user.setPassword("qwer1234");
        user.setId(5L);
        user.setActivated(true);
        when(userRepository.findByEmail("gideon.jaffe@gmail.com")).thenReturn(user);
        LoginRequest userDetails = new LoginRequest("gideon.jaffe@gmail.com", "qwer1234");
        String token = authenticationService.login(userDetails);
        assertEquals(token, authenticationService.login(userDetails));
    }

    @Test
    void isLoggedIn_UserNotLoggedIn_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> authenticationService.isLoggedIn("hello"));
    }

    @Test
    void isLoggedIn_UserLoggedIn_returnsUserId() {
        User user = new User();
        user.setEmail("gideon.jaffe@gmail.com");
        user.setPassword("qwer1234");
        user.setId(5L);
        user.setActivated(true);
        when(userRepository.findByEmail("gideon.jaffe@gmail.com")).thenReturn(user);
        LoginRequest userDetails = new LoginRequest("gideon.jaffe@gmail.com", "qwer1234");
        String token = authenticationService.login(userDetails);

        assertEquals(user.getId(), authenticationService.isLoggedIn(token));
    }

    @Test
    void logout_LoggedInUser_noLongerLoggedIn() {
        User user = new User();
        user.setEmail("gideon.jaffe@gmail.com");
        user.setPassword("qwer1234");
        user.setId(5L);
        user.setActivated(true);
        when(userRepository.findByEmail("gideon.jaffe@gmail.com")).thenReturn(user);
        LoginRequest userDetails = new LoginRequest("gideon.jaffe@gmail.com", "qwer1234");
        String token = authenticationService.login(userDetails);

        authenticationService.logout(token);

        assertThrows(IllegalArgumentException.class, () -> authenticationService.isLoggedIn(token));
    }
}
