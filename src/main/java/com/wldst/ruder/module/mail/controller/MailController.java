package com.wldst.ruder.module.mail.controller;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.wldst.ruder.module.mail.service.MailService;
import com.wldst.ruder.module.schedule.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;

import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.domain.EmailDomain;
import com.wldst.ruder.module.mail.MailReceiver;
import com.wldst.ruder.module.mail.MailUtils;
import com.wldst.ruder.util.MapTool;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//
//@Api(tags = "邮件管理")
@RestController
@RequestMapping("${server.context}/mail")
public class MailController extends EmailDomain{
    final static Logger logger = LoggerFactory.getLogger(MailController.class.getName());
	
    @Autowired
    private MailUtils mailUtils;
    @Autowired
    private MailReceiver receiver;
	@Autowired
	private MailService mailService;
    @Autowired
    private CrudNeo4jService neo4jService;
    private final TemplateEngine templateEngine = new TemplateEngine();

	@Autowired
	private ScheduleService s2;
   
    /**
     * 发送注册验证码
     * @return 验证码
     * @throws Exception
     */
    // @ApiOperation("发送注册验证码")
    @PostMapping("/sendHtml")
    public String sendTemplateMail(){
        mailUtils.sendHtmlMail("ruiyeclub@foxmail.com","一封html测试邮件",
                "<div style=\"text-align: center;position: absolute;\" >\n"
                        +"<h3>\"一封html测试邮件\"</h3>\n"
                        + "<div>一封html测试邮件</div>\n"
                        + "</div>");
        return "OK";
    }
    
    @GetMapping("/sendCode/{mail}")
    public String sendCodeMail(@PathVariable("mail") String mail){
	 
//        mailUtils.sendHtmlMail(mail,"注册",
//                "<div style=\"text-align: center;position: absolute;\" >\n"
//                        +"<h3>\"抖音注册邮箱验证码：\"</h3>\n"
//                        + "<div>"+generateCode()+"</div>\n"
//                        + "</div>");
        Map<String, Object> nodeMapById = neo4jService.getOne("MATCH(e:emailBox{name:\"1721903353\"}) return e");
        String username = MapTool.string(nodeMapById,EMAIL_ACCOUNT);//"1721903353@qq.com";
	String password = MapTool.string(nodeMapById,EMAIL_TOEKN);//"piuevcgisuwcbgea"; // QQ邮箱的授权码
//	String subject = MapTool.string(nodeMapById,SUBJECT);
	
	try {
	    String mailSubject = "抖音注册邮箱验证码";
	    String generateCode = generateCode();
	    String mailContent = "这是一份抖音注册邮箱验证码邮件，验证码为："+generateCode+"</br>";
	    
	    MailUtils.sendMail("smtp.qq.com", 25,
		    username,  null, 
		    mail,null, 
		    mailSubject,
			mailContent,
	    	Calendar.getInstance().getTime(),
	    	username, password);
	    saveSentMail(mail, username, mailSubject, mailContent);
	    checkCode.put(mail, generateCode);
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
        
        return "OK";
    }
    
    @GetMapping("/sendActive/{mail}")
    public String sendActiveMail(@PathVariable("mail") String mail){
        Map<String, Object> nodeMapById = neo4jService.getOne("MATCH(e:emailBox{name:\"1721903353\"}) return e");
        String username = MapTool.string(nodeMapById,EMAIL_ACCOUNT);//"1721903353@qq.com";
	String password = MapTool.string(nodeMapById,EMAIL_TOEKN);//"piuevcgisuwcbgea"; // QQ邮箱的授权码
//	String subject = MapTool.string(nodeMapById,SUBJECT);
	
	try {
	    String mailSubject = "抖音注册邮箱验证码";
	    String generateCode = generateCode();
	    String mailContent = "这是一份抖音注册邮箱验证码邮件，验证码为："+generateCode+"</br>";
	    
	    MailUtils.sendMail("smtp.qq.com", 25,
		    username,  null, 
		    mail,null, 
		    mailSubject,
			mailContent,
	    	Calendar.getInstance().getTime(),
	    	username, password);
	    saveSentMail(mail, username, mailSubject, mailContent);
	    checkCode.put(mail, generateCode);
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
        
        return "OK";
    }
    
    @GetMapping("/send/{mail}")
    public String send(@PathVariable("mail") String mail){
	 
//        mailUtils.sendHtmlMail(mail,"注册",
//                "<div style=\"text-align: center;position: absolute;\" >\n"
//                        +"<h3>\"抖音注册邮箱验证码：\"</h3>\n"
//                        + "<div>"+generateCode()+"</div>\n"
//                        + "</div>");
        Map<String, Object> nodeMapById = neo4jService.getOne("MATCH(e:emailBox{name:\"1721903353\"}) return e");
        String username = MapTool.string(nodeMapById,EMAIL_ACCOUNT);
	String password = MapTool.string(nodeMapById,EMAIL_TOEKN);
//	String subject = MapTool.string(nodeMapById,SUBJECT);
	
	try {
	    String mailSubject = "抖音注册邮箱验证码";
	    String generateCode = generateCode();
	    String mailContent = "这是一份抖音注册邮箱验证码邮件，验证码为："+generateCode+"</br>";
	    
	    MailUtils.sendMail("smtp.qq.com", 25,
		    username,  null, 
		    mail,null, 
		    mailSubject,
			mailContent,
	    	Calendar.getInstance().getTime(),
	    	username, password);
	    saveSentMail(mail, username, mailSubject, mailContent);
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
        
        return "OK";
    }

    private void saveSentMail(String mail, String username, String mailSubject, String mailContent) {
	Map<String,Object> email = new HashMap<>();
	email.put(SUBJECT, mailSubject);
	email.put(EMAIL_CONTENT, mailContent);
	email.put(FROM, username);
	email.put(TO, mail);
	//status 0,草稿，1已发送，2，收取。3已读，4，删除。
	email.put(STATUS, "1");
	email.put(SEND_DATE, Calendar.getInstance().getTime().getTime());	    
	neo4jService.save(email, "emailMessage");
    }
    
    public String generateCode() {
	 String code="";
	  for (int i = 0; i < 6; i++) {
	      code += Double.valueOf(Math.floor(Math.random() * 10)).intValue();
	  }
	  return code;
	}

    // @ApiOperation("发送html模板邮件")
    @PostMapping("/sendTemplate")
    public String sendTemplate(HttpServletRequest request,HttpServletResponse response){
//	WebContext webContext = new WebContext(request, response, request.getServletContext());
//	String process = templateEngine.process("", webContext);
//	mailUtils.sendTemplateMail("ruiyeclub@foxmail.com", "基于模板的html邮件", "hello.html");
        return "OK";
    }

    // @ApiOperation("发送带附件的邮件")
    @GetMapping("sendAttachmentsMail")
    public String sendAttachmentsMail(){
        String filePath = "D:\\projects\\springboot\\template.png";
        mailUtils.sendAttachmentsMail("xxxx@xx.com", "带附件的邮件", "邮件中有附件", filePath);
        return "OK";
    }

	@GetMapping("yhs")
	public String yhsSendMail(){
		 s2.sendBgMail();
		return "OK";
	}
    
    // @ApiOperation("发送带附件的邮件")
    @GetMapping("sendMail/{emailId}")
	public String sendMail(@PathVariable("emailId") String emailBoxId) {
		Map<String, Object> nodeMapById = neo4jService.getNodeMapById(Long.valueOf(emailBoxId));
		mailService.sendMail(nodeMapById);
		return "OK";
	}




	// @ApiOperation("收取指定邮箱的未读邮件")
    @RequestMapping(value = "/receive/{emailBoxId}", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    public String receiveMail(@PathVariable("emailBoxId") String emailBoxId){
	try {
	    if(emailBoxId==null||emailBoxId.equals("null")) {
		return "OK";
	    }
	    receiver.receive(emailBoxId);
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
        return "OK";
    }
}
