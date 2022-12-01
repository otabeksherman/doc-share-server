package docSharing.controller;

import docSharing.Entities.Activation;
import docSharing.Entities.User;
import docSharing.service.AuthenticationService;
import docSharing.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLDataException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UserControllerUnitTests {

    @Autowired
    UserController userController;

    @MockBean
    UserService userService;

    @MockBean
    AuthenticationService authenticationService;

    static User user;

    @BeforeAll
    static void setup() {
        user = new User();
        user.setEmail("gideon.jaffe@gmail.com");
        user.setPassword("qwer1234");
        user.setId(5L);
        user.setActivated(true);
    }

    @Test
    void createUser_EmailAlreadyTaken_throwsResponseStatusException() throws SQLDataException {
        when(userService.addUser(user)).thenThrow(IllegalArgumentException.class);

        assertThrows(ResponseStatusException.class, () -> userController.createUser(user));
    }

    @Test
    void createUser_GoodRequest_returnsGoodResponse() throws SQLDataException {
        when(userService.addUser(user)).thenReturn(user);

        ResponseEntity<User> response = userController.createUser(user);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody());
    }

    @Test
    void confirmRegistration_NoEmail_throwsResponseStatusException() {
        Activation activation = new Activation(null, "qweASD123zxc");

        assertThrows(ResponseStatusException.class, () -> userController.confirmRegistration(activation));
    }

    @Test
    void confirmRegistration_NoToken_throwsResponseStatusException() {
        Activation activation = new Activation("gideon.jaffe@gmail.com", null);

        assertThrows(ResponseStatusException.class, () -> userController.confirmRegistration(activation));
    }

    @Test
    void confirmRegistration_InvalidToken_throwsResponseStatusException() {
        Activation activation = new Activation("gideon.jaffe@gmail.com", "qweASD123zxc");
        when(userService.isActivated(activation)).thenThrow(IllegalArgumentException.class);

        assertThrows(ResponseStatusException.class, () -> userController.confirmRegistration(activation));
    }

    @Test
    void confirmRegistration_AlreadyActivated_throwsResponseStatusException() {
        Activation activation = new Activation("gideon.jaffe@gmail.com", "qweASD123zxc");
        when(userService.isActivated(activation)).thenReturn(true);

        ResponseEntity<String> response = userController.confirmRegistration(activation);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Already activated", response.getBody());
    }

    @Test
    void confirmRegistration_GoodRequest_returnsOkResponse() {
        Activation activation = new Activation("gideon.jaffe@gmail.com", "qweASD123zxc");
        when(userService.isActivated(activation)).thenReturn(false);
        when(userService.confirmRegistration(activation)).thenReturn("The account has been activated successfully");

        ResponseEntity<String> response = userController.confirmRegistration(activation);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("The account has been activated successfully", response.getBody());
    }

    @Test
    void logout_returnsOkResponse() {
        when(authenticationService.logout("qweASD123zxc")).thenReturn("logout");

        ResponseEntity<String> response = userController.logout("qweASD123zxc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
