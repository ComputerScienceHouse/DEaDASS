package mail;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * A utility class for sending emails.
 * @author Max Meinhold <mxmeinhold@gmail.com>
 */
public class Mail {

    /** The DEaDASS-UI host to route approval actions to. */
    private static final String HOST = null; //TODO

    private static final String REQUEST_STRING = "%s is requesting a database named %s for <code>%s</code>.<br/><br/>"
            + "<a href=%s/approvals>Visit the approvals page.</a><br/><br/>- DEaDASS";

    private static final String APPROVAL_STRING = "Your database %s has been created.<br/>"
            + " Please <a href=%s>login to DEaDASS</a> to reset the password and obtain your access credentials.<br/><br/>- DEaDASS";
    private static final String DENIAL_STRING = "Your database %s has been denied.<br/><br/>- DEaDASS";

    private static MimeMessage msgTemplate;
    private static Transport transport;


    /**
     * Initialises mail handling
     */
    public static void init() {
        Properties props;
        Session session;

        props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", 25);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");

        session = Session.getDefaultInstance(props);

        try {
            transport = session.getTransport();
            msgTemplate = new MimeMessage(session);
            msgTemplate.setFrom(new InternetAddress("deadass@csh.rit.edu", "DEaDASS"));
        } catch (MessagingException e) {
            System.err.println("Mail init failed.");
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            System.err.println("Mail return address bad.");
            e.printStackTrace();
        }
    }


    /**
     * Sends an email
     * @param subject the subject line
     * @param body the message body
     * @param uid the user to send to
     */
    private static void sendMail(String subject, String body, String uid) {
        try {
            msgTemplate.setRecipient(Message.RecipientType.TO, new InternetAddress(uid + "@csh.rit.edu"));
            msgTemplate.setSubject(subject);
            msgTemplate.setContent(body, "text/html");
            transport.connect(defaults.Secrets.MAIL_HOST, defaults.Secrets.MAIL_USERNAME, defaults.Secrets.MAIL_PASSWORD);

            transport.sendMessage(msgTemplate, msgTemplate.getAllRecipients());

            transport.close();
        } catch (MessagingException e) {
            System.err.println("Mail sendMail failed.");
            e.printStackTrace();
        }
    }


    /**
     * Sends a database request to an administrator
     * @param uid the user requesting a database
     * @param purpose the purpose of this database
     * @param dbName the name of the database
     */
    public static void request(String uid, String purpose, String dbName) {
        String subject = String.format("Database Request - %s:%s", uid, dbName);
        String body = String.format(REQUEST_STRING, uid, dbName, purpose, HOST);
        sendMail(subject, body, defaults.Secrets.MAIL_ADMIN_UID);
    }


    /**
     * Sends a notification of database approval
     * @param uid the user to notify
     * @param dbName the name of the db that was approved
     */
    public static void approve(String uid, String dbName) {
        String body = String.format(APPROVAL_STRING, dbName, HOST);
        sendMail("Database Creation", body, uid);
    }


    /**
     * Sends a notification of database denial.
     * @param uid the user to notify
     * @param dbName the name of the db that was denied.
     */
    public static void deny(String uid, String dbName) {
        String body = String.format(DENIAL_STRING, dbName);
        sendMail("Database Denial", body, uid);
    }
}
