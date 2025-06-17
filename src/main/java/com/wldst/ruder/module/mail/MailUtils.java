package com.wldst.ruder.module.mail;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Properties;

import com.wldst.ruder.util.LoggerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.sun.mail.util.MailSSLSocketFactory;

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.annotation.Resource;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeUtility;

@Component
public class MailUtils {
    final static Logger logger = LoggerFactory.getLogger(MailUtils.class.getName());

    /**
     * Spring官方提供的集成邮件服务的实现类，目前是Java后端发送邮件和集成邮件服务的主流工具。
     */
    @Resource
    private JavaMailSender mailSender;
    /**
     * 从配置文件中注入发件人的姓名
     */
    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 发送文本邮件
     *
     * @param to      收件人
     * @param subject 标题
     * @param content 正文
     */
    public void sendSimpleMail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        //发件人
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }

    /**
     * 发送html邮件
     */
    public void sendHtmlMail(String to, String subject, String content) {
        try {
            //注意这里使用的是MimeMessage
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            //第二个参数：格式是否为html
            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            LoggerTool.error(logger, "发送邮件时发生异常！", e);
        }
    }

    /**
     * 发送模板邮件
     * @param to
     * @param subject
     * @param template
     */
//    public void sendTemplateMail(String to, String subject, String template){
//        try {
//            // 获得模板
//            Template template1 = freeMarkerConfigurer.getConfiguration().getTemplate(template);
//            // 使用Map作为数据模型，定义属性和值
//            Map<String,Object> model = new HashMap<>();
//            model.put("myname","Ray。");
//            // 传入数据模型到模板，替代模板中的占位符，并将模板转化为html字符串
//            TemplateEngine
//            String templateHtml = FreeMarkerTemplateUtils.processTemplateIntoString(template1,model);
//            // 该方法本质上还是发送html邮件，调用之前发送html邮件的方法
//            this.sendHtmlMail(to, subject, templateHtml);
//        } catch (TemplateException e) {
//            log.error("发送邮件时发生异常！", e);
//        } catch (IOException e) {
//            log.error("发送邮件时发生异常！", e);
//        }
//    }

    /**
     * 发送带附件的邮件
     *
     * @param to
     * @param subject
     * @param content
     * @param filePath
     */
    public void sendAttachmentsMail(String to, String subject, String content, String filePath) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            //要带附件第二个参数设为true
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            FileSystemResource file = new FileSystemResource(new File(filePath));
            String fileName = filePath.substring(filePath.lastIndexOf(File.separator));
            helper.addAttachment(fileName, file);
            mailSender.send(message);
        } catch (MessagingException e) {
            LoggerTool.error(logger, "发送邮件时发生异常！", e);
        }

    }

    /**
     * 发送一封邮件的过程
     *
     * @param smtpHost     smtp服务，一般为smtp.163.com  smtp.qq.com
     * @param sendMail     发件人邮箱地址 一般为smtp对应
     * @param receiveMail  接收人邮箱地址，可以是任意的合法邮箱即可
     * @param mailSubject  创建的邮件主题
     * @param mailContent  创建邮件的内容，可以添加html标签
     * @param sentDate     设置发送时间，null为立即发送
     * @param authUserName 验证服务器是的用户名，一般和发件人邮箱保持一致
     * @param authPassword 验证服务器的密码，一般为登录邮箱的密码，也可能是邮箱独立密码
     * @throws Exception
     */
    public static void sendMail(String smtpHost, int port, String sendMail, String sendNickname, String receiveMail, String receiveNickname, String mailSubject, String mailContent, Date sentDate, String authUserName, String authPassword) throws Exception {
        Session session = getMailSession(smtpHost);
        logger.info("sendMail==="+sendMail+"receiveMail==="+receiveMail+"authUserName"+authUserName+"authPassword"+ authPassword);
        // 3. 创建一封邮件
        MimeMessage message = createMimeMessage(session, sendMail, sendNickname, receiveMail, receiveNickname, mailSubject, mailContent, sentDate);

        // 4. 根据 Session 获取邮件传输对象
        Transport transport = session.getTransport();

        // 5. 使用 邮箱账号 和 密码 连接邮件服务器, 这里认证的邮箱必须与 message 中的发件人邮箱一致, 否则报错
        //    PS_02: 连接失败的原因通常为以下几点, 仔细检查代码:
        //           (1) 邮箱没有开启 SMTP 服务;
        //           (2) 邮箱密码错误, 例如某些邮箱开启了独立密码;
        //           (3) 邮箱服务器要求必须要使用 SSL 安全连接;
        //           (4) 请求过于频繁或其他原因, 被邮件服务器拒绝服务;
        //           (5) 如果以上几点都确定无误, 到邮件服务器网站查找帮助。
        try {
            transport.connect(smtpHost, 25, authUserName, authPassword);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            session = getQqMailSession(smtpHost);
            // 3. 创建一封邮件
            message = createMimeMessage(session, sendMail, sendNickname, receiveMail, receiveNickname, mailSubject, mailContent, sentDate);

            // 4. 根据 Session 获取邮件传输对象
            transport = session.getTransport();

            transport.connect(smtpHost, 465, authUserName, authPassword);
        }


        // 6. 发送邮件, 发到所有的收件地址, message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
        transport.sendMessage(message, message.getAllRecipients());

        // 7. 关闭连接
        transport.close();
    }

    private static Session getMailSession(String smtpHost) {
        // 1. 创建参数配置, 用于连接邮件服务器的参数配置
        Properties props = new Properties();                    // 参数配置
        props.setProperty("mail.transport.protocol", "smtp");   // 使用的协议（JavaMail规范要求）
        props.setProperty("mail.smtp.host", smtpHost);   // 发件人的邮箱的 SMTP 服务器地址
        // 设置邮件服务器主机名
        props.setProperty("mail.host", smtpHost);
        props.setProperty("mail.smtp.auth", "true");            // 需要请求认证
        props.put("mail.smtp.ssl.enable", "false");
        props.put("mail.smtp.starttls.enable", "false");


        // 2. 根据配置创建会话对象, 用于和邮件服务器交互
        Session session = Session.getDefaultInstance(props);
        session.setDebug(true);
        return session;
    }

    private static Session getQqMailSession(String host) throws GeneralSecurityException {
        Properties props = new Properties();                    // 参数配置

        // 开启debug调试
        props.setProperty("mail.debug", "true");
        // 发送服务器需要身份验证
        props.setProperty("mail.smtp.auth", "true");
        // 设置邮件服务器主机名
        props.setProperty("mail.host", host);
        // 发送邮件协议名称
        props.setProperty("mail.transport.protocol", "smtp");

        MailSSLSocketFactory sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);

        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.socketFactory", sf);

        Session session = Session.getInstance(props);

        session.setDebug(true); // 设置为debug模式, 可以查看详细的发送 log,开发时候使用
        return session;
    }

    /**
     * 发送一封邮件的过程
     *
     * @param smtpHost        smtp服务，一般为smtp.163.com  smtp.qq.com
     * @param sendMail        发件人邮箱地址 一般为smtp对应
     * @param sendNickname    发件人昵称
     * @param receiveMail     接收人邮箱地址，可以是任意的合法邮箱即可
     * @param receiveNickname 接收人昵称
     * @param mailSubject     创建的邮件主题
     * @param mimeMultipart   上传一个复杂的邮件内容
     * @param sentDate        设置发送时间，null为立即发送
     * @param authUserName    验证服务器是的用户名，一般和发件人邮箱保持一致
     * @param authPassword    验证服务器的密码，一般为登录邮箱的密码，也可能是邮箱独立密码
     * @throws Exception
     */
    public static void sendMail(String smtpHost, String sendMail, String sendNickname, String receiveMail, String receiveNickname, String mailSubject, MimeMultipart mimeMultipart, Date sentDate, String authUserName, String authPassword) throws Exception {
        // 1. 创建参数配置, 用于连接邮件服务器的参数配置
        Properties props = new Properties();                    // 参数配置
        props.setProperty("mail.transport.protocol", "smtp");   // 使用的协议（JavaMail规范要求）
        props.setProperty("mail.smtp.host", smtpHost);   // 发件人的邮箱的 SMTP 服务器地址
        props.setProperty("mail.smtp.auth", "true");            // 需要请求认证


        // 2. 根据配置创建会话对象, 用于和邮件服务器交互
        Session session = Session.getDefaultInstance(props);

        session.setDebug(true); // 设置为debug模式, 可以查看详细的发送 log,开发时候使用

        // 3. 创建一封邮件
        MimeMessage message = createComplexMimeMessage(session, sendMail, sendNickname, receiveMail, receiveNickname, mailSubject, mimeMultipart, sentDate);

        // 4. 根据 Session 获取邮件传输对象
        Transport transport = session.getTransport();

        // 5. 使用 邮箱账号 和 密码 连接邮件服务器, 这里认证的邮箱必须与 message 中的发件人邮箱一致, 否则报错
        //    PS_02: 连接失败的原因通常为以下几点, 仔细检查代码:
        //           (1) 邮箱没有开启 SMTP 服务;
        //           (2) 邮箱密码错误, 例如某些邮箱开启了独立密码;
        //           (3) 邮箱服务器要求必须要使用 SSL 安全连接;
        //           (4) 请求过于频繁或其他原因, 被邮件服务器拒绝服务;
        //           (5) 如果以上几点都确定无误, 到邮件服务器网站查找帮助。
        transport.connect(smtpHost, authUserName, authPassword);

        // 6. 发送邮件, 发到所有的收件地址, message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
        transport.sendMessage(message, message.getAllRecipients());

        // 7. 关闭连接
        transport.close();
    }


    /**
     * 创建一封只包含文本的简单邮件
     *
     * @param session         和服务器交互的会话
     * @param sendMail        发件人邮箱
     * @param sendNickname    发送人昵称
     * @param receiveMail     收件人邮箱
     * @param receiveNickname 收件人昵称
     * @param mailSubject     邮件主题
     * @param receiveMail     邮件内容
     * @return MimeMessage        返回一份邮件
     * @throws Exception
     */
    public static MimeMessage createMimeMessage(Session session, String sendMail, String sendNickname, String receiveMail, String receiveNickname, String mailSubject, String mailContent, Date sentDate) throws Exception {
        // 1. 创建一封邮件
        MimeMessage message = new MimeMessage(session);

        // 2. From: 发件人
        message.setFrom(new InternetAddress(sendMail, sendNickname, "UTF-8"));

        // 3. To: 收件人（可以增加多个收件人、抄送、密送）
        message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(receiveMail, receiveNickname, "UTF-8"));

        // 4. Subject: 邮件主题
        message.setSubject(mailSubject, "UTF-8");

        // 5. Content: 邮件正文（可以使用html标签）
        message.setContent(mailContent, "text/html;charset=UTF-8");

        // 6. 设置发件时间
        message.setSentDate(sentDate != null ? sentDate : new Date());

        // 7. 保存设置
        message.saveChanges();

        return message;
    }


    /**
     * 创建一封复杂邮件（文本+图片+附件）
     */
    public static MimeMessage createComplexMimeMessage(Session session, String sendMail, String sendNickname, String receiveMail, String receiveNickname, String mailSubject, MimeMultipart mailContent, Date sentDate) throws Exception {
        // 1. 创建邮件对象
        MimeMessage message = new MimeMessage(session);

        // 2. From: 发件人
        message.setFrom(new InternetAddress(sendMail, sendNickname, "UTF-8"));

        // 3. To: 收件人（可以增加多个收件人、抄送、密送）
        message.addRecipient(RecipientType.TO, new InternetAddress(receiveMail, receiveNickname, "UTF-8"));

        // 4. Subject: 邮件主题
        message.setSubject(mailSubject, "UTF-8");

        // 5. Content: 复杂邮件内容，有单独方法创建，直接传入
        // 6. 设置整个邮件的关系（将最终的混合“节点”作为邮件的内容添加到邮件对象）
        message.setContent(mailContent);

        // 7. 设置发件时间
        message.setSentDate(sentDate != null ? sentDate : new Date());

        // 8. 保存上面的所有设置
        message.saveChanges();

        return message;
    }


    /**
     * 创建一个图片节点，返回节点唯一编号，在文本中引用
     *
     * @param imagePath     需要添加到邮件正文文本中的图片本地路径
     * @param only_image_ID 设置的图片Id，只要是正文文本中引用图片只需引用该ID即可
     * @return MimeBodyPart            返回创建的图片节点
     * @throws MessagingException
     */
    public static MimeBodyPart getMailContentImage(String imagePath, String only_image_ID) throws MessagingException {
        MimeBodyPart image = new MimeBodyPart();
        DataHandler dh = new DataHandler(new FileDataSource(imagePath)); // 读取本地文件
        image.setDataHandler(dh);                   // 将图片数据添加到“节点”
        image.setContentID(only_image_ID);     // 为“节点”设置一个唯一编号（在文本“节点”将引用该ID）
        return image;
    }

    /**
     * 创建一个文本节点，其中添加创建的图片节点对图片进行引用
     *
     * @param content 文本，可以是已经饮用过的文本，例如"这是一张图片<br/><img src='cid:only_image_ID'/>"
     * @return MimeBodyPart    返回一个节点
     * @throws MessagingException
     */
    public static MimeBodyPart getMailContentText(String content) throws MessagingException {
        MimeBodyPart text = new MimeBodyPart();
        //这里添加图片的方式是将整个图片包含到邮件内容中, 实际上也可以以 http 链接的形式添加网络图片
        //text.setContent("这是一张图片<br/><img src='cid:image_fairy_tail'/>", "text/html;charset=UTF-8");
        text.setContent(content, "text/html;charset=UTF-8");
        return text;
    }


    /**
     * 创建一个附件节点，附件上传文件
     *
     * @param attachmentPath 需要上传到附件的文件的路径
     * @return MimeBodyPart    返回一个节点
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */
    public static MimeBodyPart getMailContentAttachment(String attachmentPath) throws MessagingException, UnsupportedEncodingException {
        MimeBodyPart attachment = new MimeBodyPart();
        DataHandler dh = new DataHandler(new FileDataSource(attachmentPath));  // 读取本地文件
        attachment.setDataHandler(dh);                                             // 将附件数据添加到“节点”
        attachment.setFileName(MimeUtility.encodeText(dh.getName()));              // 设置附件的文件名（需要编码）
        return attachment;

    }


    /**
     * 创建一个混合节点，添加所有的普通节点
     *
     * @param mimeBodyPart 传入需要添加的普通节点数组
     * @return MimeMultipart 混合节点
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */
    public static MimeMultipart getMailContentMultipart(MimeBodyPart[] mimeBodyPart) throws MessagingException, UnsupportedEncodingException {
        MimeMultipart mimeMultipart = new MimeMultipart();
        for (int i = 0; i < mimeBodyPart.length; i++) {
            mimeMultipart.addBodyPart(mimeBodyPart[i]);
        }
        return mimeMultipart;

    }


//    public void test() {
//	ServletContextTemplateResolver templateResolver = 
//                new ServletContextTemplateResolver(null);
//        
//        // HTML is the default mode, but we set it anyway for better understanding of code
//        templateResolver.setTemplateMode(TemplateMode.HTML);
//        // This will convert "home" to "/WEB-INF/templates/home.html"
//        templateResolver.setPrefix("/WEB-INF/templates/");
//        templateResolver.setSuffix(".html");
//        // Template cache TTL=1h. If not set, entries would be cached until expelled
//        templateResolver.setCacheTTLMs(Long.valueOf(3600000L));
//        
//        // Cache is set to true by default. Set to false if you want templates to
//        // be automatically updated when modified.
//        templateResolver.setCacheable(true);
//        
////        this.templateEngine = new TemplateEngine();
////        this.templateEngine.setTemplateResolver(templateResolver);
//    }
}
