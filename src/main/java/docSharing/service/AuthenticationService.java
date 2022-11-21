package docSharing.service;

import docSharing.Entities.LoginRequest;
import docSharing.Entities.User;
import docSharing.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AuthenticationService {

    private static final int TOKEN_LENGTH = 10;

    @Autowired
    private UserRepository userRepository;

    private Map<String, User> loginTokens = new HashMap<>();

    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user != null) {
            if (loginTokens.containsValue(user)) {
                return "Already logged in";
            } else {
                String token = generateUniqueToken();
                loginTokens.put(token, user);
                return token;
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

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

    public Long isLoggedIn(String token) {
        if (loginTokens.containsKey(token)) {
            return loginTokens.get(token).getId();
        } else {
            throw new IllegalStateException("Not logged in");
        }
    }
}
