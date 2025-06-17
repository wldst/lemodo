package com.wldst.ruder.module.mail;

import java.util.Date;
import java.util.Properties;

import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

public class RedirectMail {
    public static void main(String[] args) throws Exception {
//	Properties prop = new Properties();
//	prop.put("mail.store.protocol", "pop3");
//	prop.put("mail.pop3.host", MAIL_SERVER_HOST);
//	prop.put("mail.pop3.starttls.enable", "true");
//	prop.put("mail.smtp.auth", "true");
//	prop.put("mail.smtp.host", MAIL_SERVER_HOST);
//	// 1、创建session
//	Session session = Session.getDefaultInstance(prop);
//	// 2、读取邮件夹
//	Store store = session.getStore("pop3");
//	store.connect(MAIL_SERVER_HOST, USER, PASSWORD);
//	Folder folder = store.getFolder("inbox");
//	folder.open(Folder.READ_ONLY);
//	// 获取邮件夹中第1封邮件信息
//	Message[] messages = folder.getMessages();
//	if (messages.length <= 0) {
//	    return;
//	}
//	Message message = messages[0];
//	// 打印邮件关键信息
//	String from = InternetAddress.toString(message.getFrom());
//	if (from != null) {
//	    System.out.println("From: " + from);
//	}
//	String replyTo = InternetAddress.toString(message.getReplyTo());
//	if (replyTo != null) {
//	    System.out.println("Reply-to: " + replyTo);
//	}
//	String to = InternetAddress.toString(message.getRecipients(Message.RecipientType.TO));
//	if (to != null) {
//	    System.out.println("To: " + to);
//	}
//	String subject = message.getSubject();
//	if (subject != null) {
//	    System.out.println("Subject: " + subject);
//	}
//	Date sent = message.getSentDate();
//	if (sent != null) {
//	    System.out.println("Sent: " + sent);
//	}
//	// 设置转发邮件信息头
//	Message forward = new MimeMessage(session);
//	forward.setFrom(new InternetAddress(MAIL_FROM));
//	forward.setRecipient(Message.RecipientType.TO, new InternetAddress(MAIL_TO));
//	forward.setSubject("Fwd: " + message.getSubject());
//	// 设置转发邮件内容
//	MimeBodyPart bodyPart = new MimeBodyPart();
//	bodyPart.setContent(message, "message/rfc822");
//	Multipart multipart = new MimeMultipart();
//	multipart.addBodyPart(bodyPart);
//	forward.setContent(multipart);
//	forward.saveChanges();
//	Transport ts = session.getTransport("smtp");
//	ts.connect(USER, PASSWORD);
//	ts.sendMessage(forward, forward.getAllRecipients());
//	folder.close(false);
//	store.close();
//	ts.close();
//	System.out.println("message forwarded successfully....");
    }

}
