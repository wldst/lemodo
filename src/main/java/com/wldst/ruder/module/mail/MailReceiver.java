package com.wldst.ruder.module.mail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import jakarta.mail.Address;
import jakarta.mail.BodyPart;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.MimeUtility;
import jakarta.mail.search.FlagTerm;

import org.neo4j.graphdb.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.druid.sql.visitor.functions.If;
import com.wldst.ruder.crud.service.CrudUserNeo4jService;
import com.wldst.ruder.domain.EmailDomain;
import com.wldst.ruder.util.DateUtil;
import com.wldst.ruder.util.MapTool;

@Component
public class MailReceiver extends EmailDomain{
    @Autowired
    private CrudUserNeo4jService neo4jService;
    
    public void receive(String id) throws Exception {
	if(id==null) {
	    return ;
	}
	// Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
	final String SSL_FACTORY = "jakarta.net.ssl.SSLSocketFactory";// ssl加密,jdk1.8无法使用
	Map<String, Object> nodeMapById = neo4jService.getNodeMapById(Long.valueOf(id));
	
	// 定义连接imap服务器的属性信息
	String port = string(nodeMapById,EMAIL_PORT);//"993";
	if(port==null) {
	    port="993";
	}
	String imapServer = string(nodeMapById, IMAP_SERVER);//"imap.qq.com";
	if(imapServer==null) {
	    imapServer="imap.qq.com";
	}
	String protocol ="imap";
	if(nodeMapById.containsKey(EMAIL_PROTOCOL)) {
	    String string = string(nodeMapById, EMAIL_PROTOCOL);
	    if(string!=null&&!string.trim().isEmpty()){
		protocol = string;//"imap";
	    }
	    
	}
	
	String username = string(nodeMapById,EMAIL_ACCOUNT);//"1721903353@qq.com";
	String password = string(nodeMapById,EMAIL_TOEKN);//"piuevcgisuwcbgea"; // QQ邮箱的授权码
	
	// 有些参数可能不需要
	Properties props = new Properties();
	props.setProperty("mail.imap.socketFactory.class", SSL_FACTORY);
	props.setProperty("mail.imap.socketFactory.fallback", "false");
	props.setProperty("mail.transport.protocol", protocol); // 使用的协议
	props.setProperty("mail.imap.port", port);
	props.setProperty("mail.imap.socketFactory.port", port);

	// 获取连接
	Session session = Session.getDefaultInstance(props);
	session.setDebug(false);

	// 获取Store对象
	Store store = session.getStore(protocol);
	store.connect(imapServer, username, password); // 登陆认证

	// 通过imap协议获得Store对象调用这个方法时，邮件夹名称只能指定为"INBOX"
	Folder folder = store.getFolder("INBOX");// 获得用户的邮件帐户
	folder.open(Folder.READ_WRITE); // 设置对邮件帐户的访问权限

	int n = folder.getUnreadMessageCount();// 得到未读数量

	FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), true); // false代表未读，true代表已读
	Message messages[] = folder.search(ft);
	for (Message message : messages) {
	    Map<String,Object> mailData = new HashMap<>();
	    String subject = message.getSubject();// 获得邮件主题
	    mailData.put(SUBJECT,subject);
	    Address from = (Address) message.getFrom()[0];// 获得发送者地址
	    Address[] allRecipients = message.getAllRecipients();
	    List<String> toList = new ArrayList<>();
	    for(Address ai: allRecipients) {
		toList.add(decodeText(ai.toString()));
	    }
	    mailData.put(FROM,decodeText(from.toString()));
	    mailData.put(TO,String.join(",", toList));
	    mailData.put(SEND_DATE, DateUtil.dateToStrLong(message.getSentDate()) );
	    /*            Enumeration headers = message.getAllHeaders();
	      System.out.println("----------------------allHeaders-----------------------------");
	      while (headers.hasMoreElements()) {
	             Header header = (Header)headers.nextElement();
	             System.out.println(header.getName()+" ======= "+header.getValue());
	             System.out.println("----------------------------");
	         }*/
	    List<String> attList = new ArrayList<>();
	    Object content = message.getContent();
	    if(content instanceof Multipart mi) {
		List<String> parseMultipart = parseMultipart(mi);
		    for(String ci: parseMultipart) {
			if(ci.length()<20) {
			    attList.add(ci);
			}
		    }
		    if(!parseMultipart.isEmpty()) {
			 mailData.put(EMAIL_CONTENT, String.join(",", parseMultipart));
		    }
		    if(!attList.isEmpty()) {
			mailData.put(ATTACHMENT, String.join(",", attList));
		    }
	    }
	    if(content instanceof String conStr) {
		 mailData.put(EMAIL_CONTENT, conStr);
	    }
	    
	    message.setFlag(Flags.Flag.SEEN, true); // imap读取后邮件状态会变为已读,设为未读
	    neo4jService.saveByBody(mailData, EMAIL);
	}

	folder.close(false);// 关闭邮件夹对象
	store.close(); // 关闭连接对象
    }

    protected String decodeText(String text) throws UnsupportedEncodingException {
	
	if (text == null)
	    return null;
	if(text.startsWith("=?utf-8?")&&text.indexOf("?= <")>0) {
	    return text.split("= <")[1].replace(">", "");
	}
	if (text.startsWith("=?GB") || text.startsWith("=?gb"))
	    text = MimeUtility.decodeText(text);
	else
	    text = new String(text.getBytes("ISO8859_1"));
	return text;
    }

    /**
     * 对复杂邮件的解析
     * 
     * @param multipart
     * @throws MessagingException
     * @throws IOException
     */
    public List<String> parseMultipart(Multipart multipart) throws MessagingException, IOException {
	int count = multipart.getCount();
	List<String> contentList=new ArrayList<>(count);
	System.out.println("couont =  " + count);
	for (int idx = 0; idx < count; idx++) {
	    BodyPart bodyPart = multipart.getBodyPart(idx);
	    System.out.println(bodyPart.getContentType());
	    if (bodyPart.isMimeType("text/plain")) {
		contentList.add(""+bodyPart.getContent());
		    
	    } else if (bodyPart.isMimeType("text/html")) {
		Map<String, Object> fileMap = new HashMap<>();
		 String pathname = fileMetaSave(bodyPart, fileMap);
		 InputStream is = bodyPart.getInputStream();
		    File dest = new File(pathname);
		    neo4jService.update(fileMap);
		    copy(is, new FileOutputStream(dest));
		    contentList.add(string(fileMap, ID));
	    } else if (bodyPart.isMimeType("multipart/*")) {
		Multipart mpart = (Multipart) bodyPart.getContent();
		parseMultipart(mpart);

	    } else if (bodyPart.isMimeType("application/octet-stream")) {
		String disposition = bodyPart.getDisposition();
		if (disposition.equalsIgnoreCase(BodyPart.ATTACHMENT)) {
		    InputStream is = bodyPart.getInputStream();
		    
		    Map<String, Object> fileMap = new HashMap<>();

		    String pathname = fileMetaSave(bodyPart, fileMap);
		    File dest = new File(pathname);
		    neo4jService.update(fileMap);
		    copy(is, new FileOutputStream(dest));
		    contentList.add(string(fileMap, ID));
		}
	    }
	}
	return contentList;
    }

    /**
     * 文件拷贝，在用户进行附件下载的时候，可以把附件的InputStream传给用户进行下载
     * 
     * @param is
     * @param os
     * @throws IOException
     */
    public static void copy(InputStream is, OutputStream os) throws IOException {
	byte[] bytes = new byte[1024];
	int len = 0;
	while ((len = is.read(bytes)) != -1) {
	    os.write(bytes, 0, len);
	}
	if (os != null)
	    os.close();
	if (is != null)
	    is.close();
    }

    
    public String fileMetaSave(BodyPart file, Map<String, Object> fileMap) throws MessagingException {
	String fileName = file.getFileName();
	String filePath = neo4jService.getPathBy(FILE_STORE_PATH);

	fileMap.put(NAME, fileName);
	fileMap.put(FILE_SIZE, String.valueOf(file.getSize()));
	fileMap.put(FILE_TYPE, file.getContentType());

	Node saveByBody = neo4jService.saveByBody(fileMap, FILE);
	long id2 = saveByBody.getId();
	fileMap.put(ID, id2);
	String pathname = filePath +  id2;
	fileMap.put(FILE_STORE_NAME, pathname);
	return pathname;
    }
}
