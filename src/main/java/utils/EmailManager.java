package utils;

import com.sun.security.auth.module.UnixSystem;
import global.Data;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author nick
 */
public class EmailManager {

    private static final String ATAV_MAIL = "atavmail@gmail.com";

    private static final long EXTERNAL_ANALYSTS_GROUP_ID = 1000018;

    public static void init() {
        try {
            String configPath = Data.SYSTEM_CONFIG;

            if (CommonCommand.isDebug) {
                configPath = Data.SYSTEM_CONFIG_FOR_DEBUG;
            }

            InputStream input = new FileInputStream(configPath);
            Properties prop = new Properties();
            prop.load(input);
        } catch (IOException e) {
            ErrorManager.send(e);
        }
    }

    /**
     * Utility method to send simple HTML email
     *
     * @param subject
     * @param body
     */
    private static void sendEmail(String subject, String body, String to) {
        try {
            // only send email to user when --email used
            if (CommonCommand.email) {
                Properties prop = new Properties();
                prop.put("mail.smtp.host", "smtp.gmail.com");
                prop.put("mail.smtp.port", "465");
                prop.put("mail.smtp.auth", "true");
                prop.put("mail.smtp.socketFactory.port", "465");
                prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

                Session session = Session.getInstance(prop,
                        new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(ATAV_MAIL, "ATAVmail365");
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress("atavmail@gmail.com"));
                message.setRecipients(
                        Message.RecipientType.TO,
                        InternetAddress.parse(to)
                );
                message.setSubject(subject);
                message.setText(body);

                Transport.send(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendEmailToATAVMail(String subject, String body) {
        sendEmail(subject, body, ATAV_MAIL);
    }

    public static void sendEmailToUser(String subject, String body) {
        if (!CommonCommand.emailReceiver.isEmpty()) {
            sendEmail(subject, body, CommonCommand.emailReceiver);
        } else {
            // IGM default use, send to valid CUMC account
            UnixSystem sys = new UnixSystem();
            boolean hasExternalGroup = false;
            for (long value : sys.getGroups()) {
                if (value == EXTERNAL_ANALYSTS_GROUP_ID) {
                    hasExternalGroup = true;
                    break;
                }
            }

            if (!hasExternalGroup) {
                String to = Data.userName + "@cumc.columbia.edu";
                sendEmail(subject, body, to);
            }
        }
    }
}
