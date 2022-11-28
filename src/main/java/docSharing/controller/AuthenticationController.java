package docSharing.controller;

import docSharing.Entities.LoginRequest;
import docSharing.service.AuthenticationService;
import docSharing.utils.ParametersValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@CrossOrigin
@RequestMapping("api/v1")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    private static final Logger LOGGER = LogManager.getLogger(AuthenticationController.class);

    /**+
     * login request from rest api POST call.
     * @param request the usre details to log in.
     * @return a Response entity containing the token for the user to use.
     * @throws ResponseStatusException if an email and password were not sent,
     * if email or password are in an incorrect format,
     * and if authentication service doesn't allow the login.
     */
    @PostMapping("login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        LOGGER.info(String.format("Login request got - email:%s, password:%s", request.getEmail(), request.getPassword()));
        if (request.getEmail() == null || request.getPassword() == null) {
            LOGGER.debug("Login request failed. didn't get email and password");
            throw new ResponseStatusException(BAD_REQUEST, "Need email and password");
        }

        if (!ParametersValidator.isCorrectEmail(request.getEmail()) || !ParametersValidator.isCorrectPassword(request.getPassword())) {
            LOGGER.debug(String.format("Login request failed - email:%s OR password:%s, incorrect format", request.getEmail(), request.getPassword()));
            throw new ResponseStatusException(BAD_REQUEST, "Incorrect email or password format");
        }

        try {
            LOGGER.info(String.format("Login request sent to service - email:%s, password:%s", request.getEmail(), request.getPassword()));
            return ResponseEntity.ok(authenticationService.login(request));
        } catch (IllegalArgumentException e) {
            LOGGER.debug(String.format("Login request failed - email:%s, password:%s. %s", request.getEmail(), request.getPassword(), e.getMessage()));
            throw new ResponseStatusException(BAD_REQUEST, e.getMessage());
        }
    }
}
