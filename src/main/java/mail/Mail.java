package mail;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Mail {

    private static final String MAIL_HOST = "thoth.csh.rit.edu";
    private static final String MAIL_USERNAME = "deadass";
    private static final String HOST = null; //TODO

    // TODO need authentication on these routes.
    private static final String REQUEST_STRING = "%s is requesting a database for %s.\n\n"
            + "<a href=%s/approve/%s>Approve</a>\n" + "<a href=%s/deny/%s>Deny</a>\n\n-Deadass";

    private static final String APPROVAL_STRING = "Your database %s has been created. The password is <pre>%s</pre>\n\n-Deadass";
    private static final String DENIAL_STRING = "Your database %s has been denied.\n\n-Deadass";

    private MimeMessage msgTemplate;

    private Properties props;
    private Session session;
    private Transport transport;

    private void sendMail(String subject, String body, String uid) {
        try {
            msgTemplate.setRecipient(Message.RecipientType.TO, new InternetAddress(uid + "@csh.rit.edu"));
            msgTemplate.setSubject(subject);
            msgTemplate.setContent(body, "text/html");
            transport.connect(MAIL_HOST, MAIL_USERNAME, defaults.Secrets.MAIL_PASSWORD);

            transport.sendMessage(msgTemplate, msgTemplate.getAllRecipients());

            transport.close();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void request(String uid, String purpose, String database_name) {
        String subject = String.format("Database Request - %s:%s", uid, database_name);
        String body = String.format(REQUEST_STRING, uid, purpose, HOST, database_name, HOST, database_name);
        sendMail(subject, body, "rtp");
    }

    public void approve(String uid, String dbName, String password) {
        String body = String.format(APPROVAL_STRING, dbName, password);
        // TODO maybe emailing passwords is a bad idea.
        sendMail("Database created", body, uid);
    }

    public void deny(String uid, String dbName) {
        String body = String.format(DENIAL_STRING, dbName);
        sendMail("Database denial", body, uid);
    }

    public Mail() {
        props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", 25);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");

        session = Session.getDefaultInstance(props);

        try {
            transport = session.getTransport();
        } catch (NoSuchProviderException e1) {
            System.err.println("Error: Mail found no provider.");
        }

        msgTemplate = new MimeMessage(session);
        try {
            msgTemplate.setFrom(new InternetAddress("deadass@csh.rit.edu", "DEaDASS"));
        } catch (UnsupportedEncodingException | MessagingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
