package docSharing.event;

import docSharing.Entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Component
public class RegistrationEmailListener{
    public void confirmRegistration(User user,String token) {
        String to = user.getEmail();
        String from = "safaa8721@gmail.com";
        String subject = "Registration Confirmation";
        String url = "/user/confirmRegistration?token=" + token;
        String message = "Thank you for registering. Please click on the below link to activate your account.";
        Properties props = getMailProperties();
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        from, "wxrsqtxgqefkqmdg");
            }
        });
        try {
            MimeMessage emailMessage = new MimeMessage(session);
            emailMessage.setFrom(new InternetAddress(from));
            emailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            emailMessage.setSubject(subject);
            emailMessage.setText(message + " \nhttp://localhost:8081" + url);
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