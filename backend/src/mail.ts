import nodemailer = require("nodemailer");
import Mail = require("nodemailer/lib/mailer");
import SMTPTransport = require("nodemailer/lib/smtp-transport");

class Mailer {
  private readonly transport: Mail;

  /**
   * @param mail_host  The mailserver hostname
   * @param username  The mail account login
   * @param password  The mail account password
   * @param mail_port  (optional) The port to connect to the mailserver over
   * @param ui_host  The hostname of the frontend site
   * @param admin_address  The administrator's mail address
   */
  public constructor(
    mail_host: string,
    username: string,
    password: string,
    mail_port = 25,
    private readonly ui_host: string,
    private readonly admin_address: string
  ) {
    this.transport = nodemailer.createTransport({
      host: mail_host,
      port: mail_port,
      secure: mail_port == 465, // False if not port 465
      auth: {
        user: username,
        pass: password,
      },
    });
  }

  /**
   * Send an email
   * @param recipient  The address to send mail to
   * @param subject  The message subject
   * @param body  The message body
   */
  private async send(
    recipient: string,
    subject: string,
    body: string
  ): Promise<void> {
    await this.transport
      .sendMail({
        from: '"DEaDASS" <noreply@csh.rit.edu>',
        to: recipient,
        subject: subject,
        html: body,
      })
      .then((info: SMTPTransport.SentMessageInfo) =>
        console.log(`Message sent: ${info.messageId}`)
      )
      .catch((error) => {
        console.error(error);
        throw error;
      });
  }

  /**
   * Notify the administrator of a new pending request
   * @param uid  the user who is requesting a db
   * @param purpose  the stated reason for the new db
   * @param db_name  the name of the new db
   * @returns  a promise that will resolve when the email sending succeeds
   */
  public request(uid: string, purpose: string, db_name: string): Promise<void> {
    const body: string =
      `${uid} is requesting a database named ${db_name} because ` +
      `<code>${purpose}</code>,<br/><br/><a href=${this.ui_host}/approvals>` +
      `Visit the approvals page.</a><br/><br/>- DEaDASS`;
    return this.send(
      this.admin_address,
      `Database Request - ${uid}:${db_name}`,
      body
    );
  }

  /**
   * Send an approval email
   * @param uid  the user who's request has been approved
   * @param db_name  the name of the db that has been approved
   * @returns  a promise that will resolve when the email sending succeeds
   */
  public approve(uid: string, db_name: string): Promise<void> {
    const body: string =
      `Your database ${db_name} has been created.<br/>` +
      `Please <a href=${this.ui_host}>login to DEaDASS</a> to reset the ` +
      `password and obtain your access credentials.<br/><br/>- DEaDASS`;
    return this.send(
      `${uid}@csh.rit.edu`,
      `Database Creation - ${db_name}`,
      body
    );
  }

  /**
   * Send a denial email
   * @param uid  the user who's request has been denied
   * @param db_name  the name of the db that has been denied
   * @returns  a promise that will resolve when the email sending succeeds
   */
  public deny(uid: string, db_name: string): Promise<void> {
    const body = `Your database ${db_name} has been denied.<br/><br/>- DEaDASS`;
    return this.send(
      `${uid}@csh.rit.edu`,
      `Database Denial - ${db_name}`,
      body
    );
  }
}

export default Mailer;
