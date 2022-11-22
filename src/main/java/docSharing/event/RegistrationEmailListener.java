package docSharing.event;

import docSharing.Entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;


@Configuration
@PropertySource("classpath:application.properties")
public class RegistrationEmailListener{
    @Autowired
    private Environment env;
    public void confirmRegistration(User user,String token) {
        String to = user.getEmail();
        String from = env.getProperty("email");
        String subject = "Registration Confirmation";
        String url = "/user/confirmRegistration?token=" + token;
        String message = "Thank you for registering. Please click on the below link to activate your account.";
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
            emailMessage.setText(message + " \nhttp://"+env.getProperty("host")+":"+env.getProperty("server.port") + url);
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
}