package docSharing.service;

import docSharing.Entities.Activation;
import docSharing.Entities.User;
import docSharing.Entities.VerificationToken;
import docSharing.event.RegistrationEmailListener;
import docSharing.repository.TokenRepository;
import docSharing.repository.UserRepository;
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
    public UserService(UserRepository userRepository, TokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }

    /**
     * create a new user:
     *      -if user exists in user's table:
     *              -if activated -> throw SQLDataException("Email exists in users table")
     *              -if not activated -> check if token valid:
     *                      -if valid - send it to user's email
     *                      -if invalid - create new token and send it
     *      -if user does not exist -> add it to user's table and create new activation token and add it
     * @param user - registered user
     * @return the user
     * @throws SQLDataException
     */
    public User addUser(User user) throws SQLDataException {
        User userRepo = userRepository.findByEmail(user.getEmail());
        if(userRepo!=null){
            if(userRepo.getActivated())
                throw new SQLDataException(String.format("Email %s exists in users table", user.getEmail()));
            VerificationToken userToken = tokenRepository.findByUser(userRepo);
            if(userToken==null || !userToken.isActivated()){
                String token = UUID.randomUUID().toString();
                userToken = new VerificationToken(token, userRepo);
                tokenRepository.save(userToken);
            }
            emailListener.confirmRegistration(userRepo,userToken.getToken());
        }
        else {
            String token = UUID.randomUUID().toString();
            VerificationToken newUserToken = new VerificationToken(token, user);
            userRepository.save(user);
            tokenRepository.save(newUserToken);
            emailListener.confirmRegistration(user, token);
        }
        return user;
    }

    /**
     * confirm registration and activate user's account then delete the activation token from the database
     * @param activation - (userEmail,token)
     * @return success message
     * @throws RuntimeException if the activation invalid or expired
     */
    public String confirmRegistration(Activation activation){
        if (isInvalid(activation)) {
            throw new RuntimeException("redirect:access-denied.....auth.message.invalidToken");
        }
        if (isExpired(activation)) {
            throw new RuntimeException("redirect:access-denied.....auth.message.expired");
        }
        User user = userRepository.findByEmail(activation.getUserEmail());
        VerificationToken token = tokenRepository.findByToken(activation.getToken());
        tokenRepository.deleteById(token.getId());
        user.setActivated(true);
        userRepository.save(user);
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
        if(token == null) {
            return true;
        } else {
            User user = userRepository.findByEmail(activation.getUserEmail());
            if(!token.getUser().getId().equals(user.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * check if the activation token is expired
     * @param activation - (userEmail,token)
     * @return true if the token is not activated, false if it is activated
     */
    private boolean isExpired(Activation activation) {
        VerificationToken token = tokenRepository.findByToken(activation.getToken());
        return !token.isActivated();
    }

    /**
     * check if user's account is activated
     * @param activation
     * @return true if activated , false if not activated
     * @throws IllegalArgumentException If the user does not exist
     */
    public boolean isActivated(Activation activation) {
        User user = userRepository.findByEmail(activation.getUserEmail());
        if (user != null) {
            return user.getActivated();
        } else {
            throw new IllegalArgumentException();
        }
    }
    public User getUserById(Long id){
        return userRepository.findById(id).get();
    }
}
