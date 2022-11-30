package docSharing.service;

import docSharing.Entities.LoginRequest;
import docSharing.Entities.User;
import docSharing.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AuthenticationService {

    private static final int TOKEN_LENGTH = 10;

    private static final Logger LOGGER = LogManager.getLogger(AuthenticationService.class);

    @Autowired
    private UserRepository userRepository;

    private Map<String, User> loginTokens = new HashMap<>();

    /**
     * Creates a token for the user and return it.
     * If the user is logged in the method will return the previous token.
     * @param request the email and password of the user wanting to log in.
     * @return the token for the user.
     * @throws IllegalArgumentException if the email doesn't exist, the password is incorrect, or the user is not activated.
     */
    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            throw new IllegalArgumentException(String.format("User with email:%s doesn't exist", request.getEmail()));
        }
        if (!Objects.equals(user.getPassword(), request.getPassword())) {
            throw new IllegalArgumentException(String.format("Password Incorrect for email:%s", request.getEmail()));
        }
        if(!user.getActivated()) {
            throw new IllegalArgumentException(String.format("User with email:%s Not Activated", request.getEmail()));
        }

        if (loginTokens.containsValue(user)) {
            LOGGER.info(String.format("email:%s Logged in already, sending previous token", request.getEmail()));
            return loginTokens.entrySet().stream()
                    .filter(stringUserEntry -> stringUserEntry.getValue().equals(user))
                    .map(Map.Entry::getKey).findAny().get();
        } else {
            String token = generateUniqueToken();
            loginTokens.put(token, user);
            LOGGER.info(String.format("email:%s Successful Log in, created token %s", request.getEmail(), token));
            return token;
        }
    }

    public boolean doesExistByEmail(String email) {
        return userRepository.findByEmail(email) != null;
    }

    /**
     * Creates a unique token consisting of letters(capital and lower case) and numbers.
     * @return the unique token created.
     */
    private String generateUniqueToken() {
        String alphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";
        int size = alphaNumericString.length();
        String tokenString;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < TOKEN_LENGTH; i++) {
                int index = ThreadLocalRandom.current().nextInt(size);
                sb.append(alphaNumericString.charAt(index));
            }
            tokenString = sb.toString();
        } while (loginTokens.containsKey(tokenString));
        return tokenString;
    }

    /**
     * Checks if the user is logged in.
     * @param token the string the user got to identify himself after logging in.
     * @return the user's id.
     * @throws IllegalArgumentException if the token doesn't correspond to a logged-in user.
     */
    public Long isLoggedIn(String token) {
        if (loginTokens.containsKey(token)) {
            return loginTokens.get(token).getId();
        } else {
            throw new IllegalArgumentException("Not logged in");
        }
    }

    /**
     * logout for the user (delete his token from tokens map)
     * @param token - user's login token
     * @return success message
     */
    public String logout(String token){
        loginTokens.remove(token);
        return "logout user";
    }
}
