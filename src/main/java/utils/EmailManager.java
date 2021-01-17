package utils;

import com.sun.security.auth.module.UnixSystem;
import global.Data;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
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

    // igm default use
    private static String IGM_MAIL_SERVER;
    private static String IGM_EMAIL_FROM;
    private static String IGM_EMAIL_TO;
    private static final long IGM_EXTERNAL_ANALYSTS_GROUP_ID = 1000018;

    // free public use
    private static final String ATAV_MAIL = "atavmail@gmail.com";

    public static void init() {
        try {
            String configPath = Data.SYSTEM_CONFIG;

            if (CommonCommand.isDebug) {
                configPath = Data.SYSTEM_CONFIG_FOR_DEBUG;
            }

            InputStream input = new FileInputStream(configPath);
            Properties prop = new Properties();
            prop.load(input);

            // only IGM server provides below settings in config
            IGM_MAIL_SERVER = prop.getProperty("mail-server", "");
            IGM_EMAIL_FROM = prop.getProperty("email-from", "");
            IGM_EMAIL_TO = prop.getProperty("email-to", "");
        } catch (IOException e) {
            ErrorManager.send(e);
        }
    }

    private static void sendEmailByColumbiaMail(String subject, String body, String to) {
        try {
            Properties props = System.getProperties();
            props.put("mail.smtp.host", IGM_MAIL_SERVER);
            Session session = Session.getInstance(props, null);

            MimeMessage msg = new MimeMessage(session);
            //set message headers
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
            msg.addHeader("format", "flowed");
            msg.addHeader("Content-Transfer-Encoding", "8bit");

            msg.setFrom(new InternetAddress(IGM_EMAIL_FROM, "IGM BIOINFO"));

            msg.setReplyTo(InternetAddress.parse(IGM_EMAIL_FROM, false));

            msg.setSubject(subject, "UTF-8");

            msg.setText(body, "UTF-8");

            msg.setSentDate(new Date());

            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));

            Transport.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendEmailByGmail(String subject, String body, String to) {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void errorReport(String subject, String body) {
        if (!IGM_MAIL_SERVER.isEmpty()) {
            sendEmailByColumbiaMail(subject, body, IGM_EMAIL_TO);
        } else {
            sendEmailByGmail(subject, body, ATAV_MAIL);
        }
    }

    public static void sendEmailToUser(String subject, String body) {
        // only send email to user when --email used
        if (CommonCommand.email) {
            if (!IGM_MAIL_SERVER.isEmpty()) {
                // IGM default use, send to valid CUMC account
                UnixSystem sys = new UnixSystem();
                boolean hasExternalGroup = false;
                for (long value : sys.getGroups()) {
                    if (value == IGM_EXTERNAL_ANALYSTS_GROUP_ID) {
                        hasExternalGroup = true;
                        break;
                    }
                }

                if (!hasExternalGroup) {
                    String to = Data.userName + "@cumc.columbia.edu";
                    sendEmailByColumbiaMail(subject, body, to);
                }
            } else if (!CommonCommand.emailReceiver.isEmpty()) {
                sendEmailByGmail(subject, body, CommonCommand.emailReceiver);
            }
        }
    }
}
