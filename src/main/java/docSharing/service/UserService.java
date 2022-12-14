package docSharing.service;

import docSharing.Entities.Activation;
import docSharing.Entities.User;
import docSharing.Entities.VerificationToken;
import docSharing.controller.DocumentController;
import docSharing.Entities.*;
import docSharing.event.RegistrationEmailListener;
import docSharing.repository.TokenRepository;
import docSharing.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLDataException;
import java.util.*;

@Service
public class UserService {

    @Autowired
    private RegistrationEmailListener emailListener;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository, TokenRepository tokenRepository) {
        LOGGER.info("In UserService constructor");
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }

    /**
     * create a new user:
     *      -if user does not exist -> add it to user's table and create new activation token and add it
     *      -if user is alreadyActivated -> throw IllegalArgumentException("Email already activated")
     *      -if token is invalid -> create new token and send it
     *      -if token is valid -> resend it to the user
     * @param user - registered user
     * @return the user
     * @throws IllegalArgumentException
     */
    public User addUser(User user) {
        User userRepo = userRepository.findByEmail(user.getEmail());
        String token;
        if (userRepo == null) {
            userRepository.save(user);
            token = createToken(user);
        } else if (userRepo.getActivated()) {
            throw new IllegalArgumentException(String.format("Email %s already activated", user.getEmail()));
        } else {
            VerificationToken userToken = tokenRepository.findByUser(userRepo);
            if(userToken==null || !userToken.isActivated()){
                token = createToken(user);
            } else {
                token = userToken.getToken();
            }
        }
        emailListener.confirmRegistration(user, token);
        return user;
    }
    private String createToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken newUserToken = new VerificationToken(token, user);
        tokenRepository.save(newUserToken);
        return token;
    }
    /**
     * confirm registration and activate user's account then delete the activation token from the database
     * @param activation - (userEmail,token)
     * @return success message
     * @throws RuntimeException if the activation invalid or expired
     */
    public String confirmRegistration(Activation activation){
        if (isInvalid(activation)) {
            LOGGER.debug(String.format("Invalid token: %s",activation.getToken()));
            throw new RuntimeException("redirect:access-denied.....auth.message.invalidToken");
        }
        if (isExpired(activation)) {
            LOGGER.debug(String.format("Activation token: %s is expired",activation.getToken()));
            throw new RuntimeException("redirect:access-denied.....auth.message.expired");
        }
        User user = userRepository.findByEmail(activation.getEmail());
        VerificationToken token = tokenRepository.findByToken(activation.getToken());
        tokenRepository.deleteById(token.getId());
        user.setActivated(true);
        userRepository.save(user);
        LOGGER.info("Account activated successfully");
        return "The account has been activated successfully";
    }

    /**
     * check if the token does not exist or not belongs to the user
     * @param activation - (userEmail,token)
     * @return true if the activation is invalid
     *         false if it valid
     */
    private boolean isInvalid(Activation activation) {
        VerificationToken token = tokenRepository.findByToken(activation.getToken());
        User user = userRepository.findByEmail(activation.getEmail());
        if(token == null || !token.getUser().getId().equals(user.getId())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * check if the activation token is expired
     * @param activation - (userEmail,token)
     * @return true if the token is expired, false if not expired
     */
    private boolean isExpired(Activation activation) {
        VerificationToken token = tokenRepository.findByToken(activation.getToken());
        Calendar calendar = Calendar.getInstance();
        return token.getExpiryDate().before(calendar.getTime());
    }

    /**
     * check if user's account is activated
     * @param activation
     * @return true if activated , false if not activated
     */
    public boolean isActivated(Activation activation) {
        User user = userRepository.findByEmail(activation.getEmail());
        if (user == null) {
            throw new IllegalArgumentException(String.format("No user for mail %s", activation.getEmail()));
        }
        return user.getActivated();
    }

    public User getUserById(Long id){
        return userRepository.findById(id).get();
    }
}
