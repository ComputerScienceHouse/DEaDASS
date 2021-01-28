import { mocked } from "ts-jest/utils";
import nodemailer = require("nodemailer");
import Mail = require("nodemailer/lib/mailer");

import Mailer, { MailConfigStanza } from "./mail";

const mail_config: MailConfigStanza = {
  host: "mail.example.com",
  auth: { user: "user", pass: "pass" },
  port: undefined,
  admin_address: "admin@example.com",
  ui_host: "ui",
};

jest.mock("nodemailer");

const mockedNodemailer = mocked(nodemailer, true);
const mockSendMail = jest.fn();
const messageInfo = new Promise((resolve) => resolve({ messageId: "id" }));
mockSendMail.mockReturnValue(messageInfo);

mockedNodemailer.createTransport.mockReturnValue(<Mail>(<unknown>{
  sendMail: mockSendMail,
}));

describe("when the mailer constructed with", () => {
  const test_port = (
    port?: number,
    is_secure = false
  ): jest.EmptyFunction => (): void => {
    const test_config = mail_config;
    mail_config.port = port;
    new Mailer(test_config);
    expect(mockedNodemailer.createTransport).toHaveBeenCalledWith({
      host: mail_config.host,
      port: port || 25,
      secure: is_secure,
      auth: mail_config.auth,
    });
  };

  beforeEach(() => mockedNodemailer.createTransport.mockClear());

  describe("an unknown port it", () => {
    it(
      "uses the default port (25) and does not use secure",
      test_port(undefined, false)
    );
  });

  describe("a nonstandard port it", () => {
    it("uses port 83 and does not use secure", test_port(83, false));
  });

  describe("the secure port it", () => {
    it("uses port 465 and does use secure", test_port(465, true));
  });
});

describe("when the mailer is configured properly it", () => {
  let mailer: Mailer;
  const uid = "uid",
    db = "database",
    reason = "reason";

  beforeAll(() => {
    mailer = new Mailer(mail_config);
    mockSendMail.mockClear();
  });

  it("sends approval emails", () =>
    mailer.approve(uid, db).then(() => {
      expect(mockSendMail).toBeCalledWith({
        from: '"DEaDASS" <noreply@csh.rit.edu>',
        to: `${uid}@csh.rit.edu`,
        subject: `Database Creation - ${db}`,
        html: `Your database ${db} has been created.<br/>Please <a href=${mail_config.ui_host}>login to DEaDASS</a> to reset the password and obtain your access credentials.<br/><br/>- DEaDASS`,
      });
    }));

  it("sends denial emails", () =>
    mailer.deny(uid, db).then(() => {
      expect(mockSendMail).toBeCalledWith({
        from: '"DEaDASS" <noreply@csh.rit.edu>',
        to: `${uid}@csh.rit.edu`,
        subject: `Database Denial - ${db}`,
        html: `Your database ${db} has been denied.<br/><br/>- DEaDASS`,
      });
    }));

  it("sends request emails", () =>
    mailer.request(uid, reason, db).then(() => {
      expect(mockSendMail).toBeCalledWith({
        from: '"DEaDASS" <noreply@csh.rit.edu>',
        to: mail_config.admin_address,
        subject: `Database Request - ${uid}:${db}`,
        html: `${uid} is requesting a database named ${db} because <code>${reason}</code>,<br/><br/><a href=${mail_config.ui_host}/approvals>Visit the approvals page.</a><br/><br/>- DEaDASS`,
      });
    }));
});
