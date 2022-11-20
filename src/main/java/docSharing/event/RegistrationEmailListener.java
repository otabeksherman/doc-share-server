package docSharing.event;

import docSharing.Entities.User;
import docSharing.Entities.VerificationToken;
import docSharing.repository.TokenDAO;
import docSharing.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.UUID;

@Component
public class RegistrationEmailListener implements ApplicationListener<OnRegistrationSuccessEvent> {
    @Autowired
    private UserService userService;
    @Autowired
    private MailSender mailSender;
    @Autowired
    private TokenDAO tokenDAO;

    public void confirmRegistration(OnRegistrationSuccessEvent event,String token) {
        User user = event.getUser();
        String to = user.getEmail();
        String subject = "Registration Confirmation";
        String url = "/user/confirmRegistration?token=" + token;
        String message = "Thank you for registering. Please click on the below link to activate your account.";
        Properties props = getMailProperties();
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        "safaa8721@gmail.com", "wxrsqtxgqefkqmdg");// Specify the Username and the PassWord
            }
        });

        String from = "safaa8721@gmail.com";
        try {
            MimeMessage emailMessage = new MimeMessage(session);
            emailMessage.setFrom(new InternetAddress(from));
            emailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            emailMessage.setSubject(subject);
            emailMessage.setText(message + "http://localhost:8081" + url);
            Transport.send(emailMessage);
            System.out.println("message sent successfully....");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }
    private Properties getMailProperties(){
        Properties pros = new Properties();
        pros.put("mail.smtp.auth", true);
        pros.put("mail.smtp.timeout", 250000000);
        pros.put("mail.smtp.port", 587);
        pros.put("mail.smtp.socketFactory.fallback", false);
        pros.put("mail.smtp.starttls.enable", true);
        pros.put("mail.smtp.host", "smtp.gmail.com");
        return pros;
    }

    public void createVerificationToken(User user, String token) {
        VerificationToken newUserToken = new VerificationToken(token, user);
        tokenDAO.save(newUserToken);
    }

    @Override
    public void onApplicationEvent(OnRegistrationSuccessEvent event) {

    }
}