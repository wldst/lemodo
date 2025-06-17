package com.wldst.ruder.test;

import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;

import com.wldst.ruder.module.mail.MailUtils;

public class MailTest {

	public static void main(String[] args) throws Exception {

		String sendMail = "1721903353@qq.com"; // 发件人
		String receiveMail = "442441824@qq.com"; // 收件人
		String authUserName = "1721903353@qq.com"; // 验证账户
		String authPassword = "piuevcgisuwcbgea"; // 验证密码liuqiang$2025
		// 发送简单邮件
		senfSimpleMail(sendMail, receiveMail, authUserName, authPassword);

		// 发送复杂 邮件
//		sendComplexmail(sendMail, receiveMail, authUserName, authPassword);

	}

	private static void sendComplexmail(String sendMail, String receiveMail, String authUserName, String authPassword)
			throws Exception {
		// 创建复杂邮件的正文
		// 1.创建图片，正文中引用
		String imagePath = "C:\\Users\\base\\Desktop\\测试图片.jpg";
		String only_image_ID = "add_image_id";
		MimeBodyPart image = MailUtils.getMailContentImage(imagePath, only_image_ID);
		// 2.创建普通文本，添加引用图片id
		String content = "这是验证邮件，您的图片为<img src='cid:add_image_id'/>";
		MimeBodyPart text = MailUtils.getMailContentText(content);
		// 3.创建一个附件
		String attachmentPath = "C:\\Users\\base\\Desktop\\mail.rar";
		MimeBodyPart attachment = MailUtils.getMailContentAttachment(attachmentPath);
		// 4.创建一个混合节点，添加以上普通节点
		MimeBodyPart[] mimeBodyPart = { image, text, attachment };
		MimeMultipart mimeMultipart = MailUtils.getMailContentMultipart(mimeBodyPart);

		MailUtils.sendMail("smtp.163.com", sendMail, null, receiveMail, null, "用户账户激活", mimeMultipart, null,
				authUserName, authPassword);
	}

	private static void senfSimpleMail(String sendMail, String receiveMail, String authUserName, String authPassword)
			throws Exception {
		MailUtils.sendMail("smtp.qq.com",25, sendMail, null, receiveMail, null, "用户账户激活",
				"这是一份激活邮件,如本人注册请点击链接进行激活：</br><a href=\"http://localhost:8080/user/activate \">点击激活</a>", null,
				authUserName, authPassword);
	}
}
