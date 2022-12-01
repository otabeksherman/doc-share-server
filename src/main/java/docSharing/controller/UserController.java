package docSharing.controller;

import docSharing.Entities.Activation;
import docSharing.Entities.User;
import docSharing.service.AuthenticationService;
import docSharing.service.FolderService;
import docSharing.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLDataException;

@RestController
@CrossOrigin
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    AuthenticationService authenticationService;
    private static final Logger LOGGER = LogManager.getLogger(UserController.class);


    /**
     * create a new user and add it to the system
     * @param user
     * @return the details of the user (user.toString())
     * @throws ResponseStatusException if the user already exists
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<User> createUser(@RequestBody User user){
        LOGGER.info("Post request to create user from the client");
        try {
            return new ResponseEntity<>(userService.addUser(user), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            LOGGER.info(String.format("User email: %s already exists in user's table", user.getEmail()));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists", e);
           } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Confirm registration for user's account
     * @param activation - has (activation token , user's email)
     * @return response Entity with an appropriate notice
     *          bad request - if the token or the email is null (or if any RuntimeException is thrown)
     *          unauthorized - if the account already activated
     *          NOT_FOUND - if the token not found
     *          ok - if the account activated successfully:)
     */
    @PatchMapping("confirmRegistration")
    public ResponseEntity<String> confirmRegistration(@RequestBody Activation activation) {
        if (activation.getEmail() == null || activation.getToken() == null) {
            LOGGER.debug("User email or activation token is null");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid activation parameters");
        }
        if (!authenticationService.doesExistByEmail(activation.getEmail())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("User with email \"%s\" doesn't exist", activation.getEmail()));
        }
        try {
            if (userService.isActivated(activation)) {
                LOGGER.debug(String.format("User with email: %s already activated",activation.getEmail()));
                return new ResponseEntity<>("Already activated", HttpStatus.UNAUTHORIZED);
            } else {
                LOGGER.debug(String.format("User with email: %s not activated",activation.getEmail()));
                return new ResponseEntity<>(userService.confirmRegistration(activation), HttpStatus.OK);
            }
        } catch (IllegalArgumentException e) {
            LOGGER.debug("Token not found!");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Token not found.");
        }
    }

    /**
     * logout for the account
     * @param token - user's login token
     * @return response Entity with an appropriate notice
     */
    @PatchMapping("logout")
    public ResponseEntity<String> logout(@RequestParam String token) {
        LOGGER.info(String.format("logout for user with token: %s",token));
        return new ResponseEntity<>(authenticationService.logout(token),HttpStatus.OK);
    }
}
