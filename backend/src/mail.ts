import nodemailer = require("nodemailer");
import Mail = require("nodemailer/lib/mailer");

export type MailConfigStanza = {
  host: string;
  auth: {
    user: string;
    pass: string;
  };
  // Defaults to 25. Use 465 for secure
  port: number | undefined;
  // administrator mail address for approving requests
  admin_address: string;
  // url of the frontend, for links in mail
  ui_host: string;
};

class Mailer {
  private readonly transport: Mail;
  // Frontend url
  private readonly ui_host: string;
  // Address of people who can approve requests
  private readonly admin_address: string;

  /**
   * @param config the mail config stanza
   */
  public constructor(config: MailConfigStanza) {
    this.admin_address = config.admin_address;
    this.ui_host = config.ui_host;
    this.transport = nodemailer.createTransport({
      host: config.host,
      port: config.port || 25,
      secure: config.port == 465, // False if not port 465
      auth: config.auth,
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
    await this.transport.sendMail({
      from: '"DEaDASS" <noreply@csh.rit.edu>',
      to: recipient,
      subject: subject,
      html: body,
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
