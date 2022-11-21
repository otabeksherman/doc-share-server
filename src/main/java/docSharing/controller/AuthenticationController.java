package docSharing.controller;

import docSharing.Entities.LoginRequest;
import docSharing.Entities.User;
import docSharing.service.AuthenticationService;
import docSharing.utils.ParametersValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @PostMapping("login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        if (request == null || request.getEmail() == null
                || request.getPassword() == null) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "Incorrect email or password format");
        } else {
            if (ParametersValidator.isCorrectEmail(request.getEmail())
                    && ParametersValidator.isCorrectPassword(request.getPassword())) {
               try {
                   return ResponseEntity.ok(authenticationService.login(request));
               } catch (IllegalArgumentException e) {
                   throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
               }
            } else {
                throw new ResponseStatusException(BAD_REQUEST,
                        "Incorrect email or password format");
            }
        }
    }
}
