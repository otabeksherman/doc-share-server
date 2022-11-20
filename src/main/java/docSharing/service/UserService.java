package docSharing.service;

import docSharing.Entities.User;
import docSharing.Entities.VerificationToken;
import docSharing.event.RegistrationEmailListener;
import docSharing.repository.TokenRepository;
import docSharing.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.sql.SQLDataException;
import java.util.Calendar;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    public UserService(UserRepository userRepository, TokenRepository tokenRepository) {
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
        new RegistrationEmailListener().confirmRegistration(user,token);
        return userRepository.save(user);
    }
    public String confirmRegistration(String token){
        VerificationToken verificationToken = tokenRepository.findByToken(token);
        if(verificationToken == null) {
            return "redirect:access-denied.....auth.message.invalidToken";
        }
        User user = verificationToken.getUser();
        Calendar calendar = Calendar.getInstance();
        if((verificationToken.getExpiryDate().getTime()-calendar.getTime().getTime())<=0) {
            return "redirect:access-denied.....auth.message.expired";
        }
        user.setActivated(true);
        userRepository.save(user);
        return "The account has been activated successfully";
    }

}
