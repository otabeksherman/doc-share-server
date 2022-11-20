package docSharing.service;

import docSharing.Entities.User;
import docSharing.Entities.VerificationToken;
import docSharing.event.OnRegistrationSuccessEvent;
import docSharing.event.RegistrationEmailListener;
import docSharing.repository.TokenDAO;
import docSharing.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.sql.SQLDataException;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TokenDAO tokenRepository;
    public UserService(UserRepository userRepository, TokenDAO tokenRepository) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }


    public User addUser(User user) throws SQLDataException {
        if(userRepository.findByEmail(user.getEmail())!=null){
            throw new SQLDataException(String.format("Email %s exists in users table", user.getEmail()));
        }
        userRepository.save(user);
        String token = UUID.randomUUID().toString();
        VerificationToken newUserToken = new VerificationToken(token, user);
        tokenRepository.save(newUserToken);
        new RegistrationEmailListener().confirmRegistration(new OnRegistrationSuccessEvent(user),token);
        return userRepository.save(user);
    }

}
