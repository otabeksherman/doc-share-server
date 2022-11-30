package docSharing.controller;

import docSharing.Entities.LoginRequest;
import docSharing.controller.AuthenticationController;
import docSharing.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class AuthenticationControllerUnitTests {
    @Autowired
    AuthenticationController authenticationController;

    @MockBean
    AuthenticationService authenticationService;

    @Test
    void login_EmailIsNull_throwsResponseStatusException() {
        LoginRequest userDetails = new LoginRequest(null, "qwer1234");

        assertThrows(ResponseStatusException.class,() ->authenticationController.login(userDetails));
    }

    @Test
    void login_PasswordIsNull_throwsResponseStatusException() {
        LoginRequest userDetails = new LoginRequest("gideon.jaffe@gmail.com", null);

        assertThrows(ResponseStatusException.class,() ->authenticationController.login(userDetails));
    }

    @Test
    void login_EmailWrongFormat_throwsResponseStatusException() {
        LoginRequest userDetails = new LoginRequest("gideon.jaffe@gmail", "qwer1234");

        assertThrows(ResponseStatusException.class,() ->authenticationController.login(userDetails));
    }

    @Test
    void login_PasswordWrongFormat_throwsResponseStatusException() {
        LoginRequest userDetails = new LoginRequest("gideon.jaffe@gmail.com", "qwertyuiop");

        assertThrows(ResponseStatusException.class,() ->authenticationController.login(userDetails));
    }

    @Test
    void login_ServiceThrowsError_throwsResponseStatusException() {
        LoginRequest userDetails = new LoginRequest("gideon.jaffe@gmail.com", "qwer1234");
        when(authenticationService.login(userDetails)).thenThrow(IllegalArgumentException.class);

        assertThrows(ResponseStatusException.class,() ->authenticationController.login(userDetails));
    }

    @Test
    void login_GoodLogin_throwsResponseStatusException() {
        LoginRequest userDetails = new LoginRequest("gideon.jaffe@gmail.com", "qwer1234");
        when(authenticationService.login(userDetails)).thenReturn("qweASDzxc123");

        assertEquals(ResponseEntity.ok("qweASDzxc123"), authenticationController.login(userDetails));
    }
}
