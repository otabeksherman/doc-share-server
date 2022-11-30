package docSharing.controller;

import docSharing.Entities.Activation;
import docSharing.Entities.User;
import docSharing.service.AuthenticationService;
import docSharing.service.UserService;
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

    /**
     * create a new user and add it to the system
     * @param user
     * @return the details of the user (user.toString())
     * @throws ResponseStatusException if the user already exists
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<String> createUser(@RequestBody User user){
        try {
            return new ResponseEntity<>(userService.addUser(user).toString(), HttpStatus.OK);
        } catch (SQLDataException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Email already exists", e);
        }
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<User> getUserById(@RequestParam int id){
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value="/delete/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable("id") int id){
        return ResponseEntity.noContent().build();
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
        if (activation.getUserEmail() == null || activation.getToken() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid activation parameters");
        }
        try {
            if (userService.isActivated(activation)) {
                return new ResponseEntity<>("Already activated", HttpStatus.UNAUTHORIZED);
            } else {
                return new ResponseEntity<>(userService.confirmRegistration(activation), HttpStatus.OK);
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Token not found.");
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.toString());
        }
    }

    /**
     * logout for the account
     * @param token - user's login token
     * @return response Entity with an appropriate notice
     */
    @PatchMapping("logout")
    public ResponseEntity<String> logout(@RequestParam String token) {
        return new ResponseEntity<>(authenticationService.logout(token),HttpStatus.OK);
    }
}
