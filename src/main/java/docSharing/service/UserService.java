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


    public User addUser(User user) throws SQLDataException {
        if(userRepository.findByEmail(user.getEmail())!=null){
            throw new IllegalArgumentException(String.format("Email %s already has an account", user.getEmail()));
        }
        String token = UUID.randomUUID().toString();

        User savedUser = userRepository.save(user);
        VerificationToken newUserToken = new VerificationToken(token, savedUser);
        tokenRepository.save(newUserToken);
        emailListener.confirmRegistration(savedUser,token);
        return savedUser;
    }

    public String confirmRegistration(Activation activation){
        if (isInvalid(activation)) {
            throw new RuntimeException("redirect:access-denied.....auth.message.invalidToken");
        }
        if (isExpired(activation)) {
            throw new RuntimeException("redirect:access-denied.....auth.message.expired");
        }
        User user = userRepository.findByEmail(activation.getEmail());
        VerificationToken token = tokenRepository.findByToken(activation.getToken());
        user.setActivated(true);
        token.setActivated(true);
        userRepository.save(user);
        tokenRepository.save(token);
        return "The account has been activated successfully";
    }

    private boolean isInvalid(Activation activation) {
        VerificationToken token = tokenRepository.findByToken(activation.getToken());
        if(token == null) {
            return true;
        } else {
            User user = userRepository.findByEmail(activation.getEmail());
            if (!token.getUser().getId().equals(user.getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean isExpired(Activation activation) {
        VerificationToken token = tokenRepository.findByToken(activation.getToken());
        Calendar calendar = Calendar.getInstance();
        if(token.getExpiryDate().before(calendar.getTime())) {
            return true;
        }
        return false;
    }

    public boolean isActivated(Activation activation) {
        VerificationToken token = tokenRepository
                .findByToken(activation.getToken());
        if (token != null) {
            return token.isActivated();
        } else {
            throw new IllegalArgumentException();
        }
    }
    public User getUserById(Long id){
        return userRepository.findById(id).get();
    }
}
