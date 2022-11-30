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
        user = userRepository.findByEmail(user.getEmail());
        if(user!=null){
            if(user.getActivated())
                throw new SQLDataException(String.format("Email %s exists in users table", user.getEmail()));
            VerificationToken userToken = tokenRepository.findByUser(user);
            if(userToken==null || !userToken.isActivated()){
                String token = UUID.randomUUID().toString();
                userToken = new VerificationToken(token, user);
                tokenRepository.save(userToken);
            }
            emailListener.confirmRegistration(user,userToken.getToken());
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

    public String confirmRegistration(Activation activation){
        if (isInvalid(activation)) {
            throw new RuntimeException("redirect:access-denied.....auth.message.invalidToken");
        }
        if (isExpired(activation)) {
            throw new RuntimeException("redirect:access-denied.....auth.message.expired");
        }
        User user = userRepository.findByEmail(activation.getEmail());
        VerificationToken token = tokenRepository.findByToken(activation.getToken());
        tokenRepository.deleteById(token.getId());
        user.setActivated(true);
        userRepository.save(user);
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
        return !token.isActivated();
    }

    public boolean isActivated(Activation activation) {
        User user = userRepository.findByEmail(activation.getEmail());
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
