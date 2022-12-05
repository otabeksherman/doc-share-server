package docSharing.controller;

import docSharing.Entities.LoginRequest;
import docSharing.Entities.LoginResponse;
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
    private static final String EMAIL_CORRECT = "sharedoc@gmail.com";
    private static final String EMAIL_INCORRECT = "sharedoc";
    private static final String PASSWORD_CORRECT = "123456Qw";
    private static final String PASSWORD_INCORRECT = "123456";
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
    void login_throwsResponseStatusException() {
        String token = "qweASDzxc123";
        LoginRequest userDetails = new LoginRequest(EMAIL_CORRECT, PASSWORD_CORRECT);
        LoginResponse response = new LoginResponse(token, EMAIL_CORRECT);
        when(authenticationService.login(userDetails)).thenReturn(token);

        assertEquals(ResponseEntity.ok(response), authenticationController.login(userDetails));
    }
}
