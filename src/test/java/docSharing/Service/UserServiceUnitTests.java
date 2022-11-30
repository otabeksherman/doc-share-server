package docSharing.Service;

import docSharing.Entities.Activation;
import docSharing.Entities.Folder;
import docSharing.Entities.User;
import docSharing.Entities.VerificationToken;
import docSharing.event.RegistrationEmailListener;
import docSharing.repository.TokenRepository;
import docSharing.repository.UserRepository;
import docSharing.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UserServiceUnitTests {
    @Autowired
    UserService userService;

    @MockBean
    UserRepository userRepository;

    @MockBean
    TokenRepository tokenRepository;
    @MockBean
    RegistrationEmailListener registrationEmailListener;

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
    void addUser_EmailAlreadyInUseAndUserActivated_throwsIllegalArgument() {
        user.setActivated(true);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);

        assertThrows(IllegalArgumentException.class, () -> userService.addUser(user));
    }

    @Test
    void addUser_EmailAlreadyInUseAndNoActivationToken_doesNotThrow() {
        user.setActivated(false);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(tokenRepository.findByUser(user)).thenReturn(null);

        assertDoesNotThrow(() -> userService.addUser(user));
    }

    @Test
    void addUser_EmailAlreadyInUseAndInactiveToken_doesNotThrow() {
        user.setActivated(false);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        VerificationToken verificationToken = new VerificationToken("qweASD123zxc", user, cal.getTime());
        when(tokenRepository.findByUser(user)).thenReturn(verificationToken);

        assertDoesNotThrow(() -> userService.addUser(user));
    }

    @Test
    void addUser_EmailAlreadyInUseAndGoodToken_doesNotThrow() {
        user.setActivated(false);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        VerificationToken verificationToken = new VerificationToken("qweASD123zxc", user);
        when(tokenRepository.findByUser(user)).thenReturn(verificationToken);

        assertDoesNotThrow(() -> userService.addUser(user));
    }

    @Test
    void addUser_EmailAvailable_throwsIllegalArgument() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(null);

        assertDoesNotThrow(() -> userService.addUser(user));
    }

    @Test
    void confirmRegistration_TokenDoesNotExist_throwsRuntime() {
        when(tokenRepository.findByToken("qweASD123zxc")).thenReturn(null);
        Activation activation = new Activation(user.getEmail(), "qweASD123zxc");

        assertThrows(RuntimeException.class, () -> userService.confirmRegistration(activation));
    }

    @Test
    void confirmRegistration_TokenDoesNotMatchEmail_throwsRuntime() {
        when(tokenRepository.findByToken("qweASD123zxc")).thenReturn(new VerificationToken("qweASD123zxc", user));
        when(userRepository.findByEmail("not.yours@gmail.com")).thenReturn(new User());
        Activation activation = new Activation("not.yours@gmail.com", "qweASD123zxc");

        assertThrows(RuntimeException.class, () -> userService.confirmRegistration(activation));
    }

    @Test
    void confirmRegistration_TokenExpired_throwsRuntime() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        VerificationToken verificationToken = new VerificationToken("qweASD123zxc", user, cal.getTime());
        when(tokenRepository.findByToken("qweASD123zxc")).thenReturn(verificationToken);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        Activation activation = new Activation(user.getEmail(), "qweASD123zxc");

        assertThrows(RuntimeException.class, () -> userService.confirmRegistration(activation));
    }

    @Test
    void confirmRegistration_GoodRequest_returnsString() {
        VerificationToken verificationToken = new VerificationToken("qweASD123zxc", user);
        when(tokenRepository.findByToken("qweASD123zxc")).thenReturn(verificationToken);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        Activation activation = new Activation(user.getEmail(), "qweASD123zxc");

        String returnValue = userService.confirmRegistration(activation);
        assertEquals("The account has been activated successfully", returnValue);
    }

    @Test
    void isActivated_EmailDoesNotMatchUser_throwsIllegalArgument() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(null);
        Activation activation = new Activation(user.getEmail(), "qweASD123zxc");

        assertThrows(IllegalArgumentException.class, () -> userService.isActivated(activation));
    }

    @Test
    void isActivated_GoodRequest_throwsIllegalArgument() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        Activation activation = new Activation(user.getEmail(), "qweASD123zxc");

        assertEquals(user.getActivated(), userService.isActivated(activation));
    }
}
