package docSharing.event;

import docSharing.Entities.User;
import docSharing.controller.UserController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Configuration
@PropertySource("classpath:application.properties")
public class RegistrationEmailListener {

    @Autowired
    private Environment env;
    private static final Logger LOGGER = LogManager.getLogger(RegistrationEmailListener.class);

    /**
     * send activation message to user's email that contains activation link with user's email and activation token
     * @param user
     * @param token - activation token
     */
    public void confirmRegistration(User user,String token) {
        String messagePartOne = "Dear customer,\n" +
                "\n" +
                "Thank you for joining the Shared Documents Application! We provide an excellent platform for " +
                "creating, editing, sharing, and viewing documents. \n" +
                "\n" +
                "To begin to enjoy our awesome application, we would like to ask you to activate your account by " +
                "clicking the following activation link:";

        String messagePartTwo = "If clicking the link does not work, please copy-and-paste or re-type it into your " +
                "browser's address bar and hit \"Enter\".\n" +
                "\n" +
                "Thank you,\n" +
                "\n" +
                "Shared Documents Team\n" +
                "\n" +
                "shared-documents.com";
        String to = user.getEmail();
        String from = env.getProperty("email");
        String subject = "Please confirm your new account";
        String params = String.format("?email=%s&token=%s", to, token);
        Properties props = getMailProperties();
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        from, env.getProperty("password"));
            }
        });
        try {
            MimeMessage emailMessage = new MimeMessage(session);
            emailMessage.setFrom(new InternetAddress(from));
            emailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            emailMessage.setSubject(subject);
            emailMessage.setText(messagePartOne + " \nhttp://"
                    + env.getProperty("client") + ":"
                    + env.getProperty("client.port") + "/"
                    + env.getProperty("page.activation")
                    + params + "\n" + messagePartTwo);
            Transport.send(emailMessage);
            LOGGER.info(String.format("Activation message sent to email: %s",to));
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }

    /**
     * set properties for mail
     * @return Properties
     */
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
}