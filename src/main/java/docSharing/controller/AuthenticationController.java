package docSharing.controller;

import docSharing.Entities.LoginRequest;
import docSharing.Entities.User;
import docSharing.service.AuthenticationService;
import docSharing.utils.ParametersValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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
            return ResponseEntity.badRequest().body(BAD_REQUEST.toString());
        } else {
            if (ParametersValidator.isCorrectEmail(request.getEmail())
                    && ParametersValidator.isCorrectPassword(request.getPassword())) {
                return ResponseEntity.ok(authenticationService.login(request));
            } else {
                return ResponseEntity.badRequest().body(BAD_REQUEST.toString());
            }
        }
    }
}
