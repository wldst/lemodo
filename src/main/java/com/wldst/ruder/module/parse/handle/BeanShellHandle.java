package com.wldst.ruder.module.parse.handle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.config.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.bs.DomainLogicOperator;
import com.wldst.ruder.module.bs.DomainOperator;
import com.wldst.ruder.module.bs.ParseExcuteSentence;
import com.wldst.ruder.module.bs.ShellOperator;
import com.wldst.ruder.util.MapTool;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * 处理注册句子的解析
 * 
 * @author wldst
 *
 */
@Component
public class BeanShellHandle extends MapTool implements SentenceHandler {
    private Logger logger = LoggerFactory.getLogger(BeanShellHandle.class);
    private List<SentenceHandler> handler = new ArrayList<>();

    private CrudNeo4jService repo;

    private UserAdminService admin;


    private ShellOperator so;

    private ParseExcuteSentence parse;

    private DomainOperator domain;

    private DomainLogicOperator logic;

	@Autowired
	public BeanShellHandle(@Lazy CrudNeo4jService repo, @Lazy UserAdminService admin, @Lazy ShellOperator so, @Lazy ParseExcuteSentence parse,
						   @Lazy DomainOperator domain, @Lazy DomainLogicOperator logic) {
	super();
	this.repo = repo;
	this.admin = admin;
	this.so = so;
	this.parse = parse;
	this.domain = domain;
	this.logic = logic;
    }

    @Override
    public Object parse(String msgx, Map<String, Object> context) {
		String msg = msgx.trim().replaceAll("：",":");
	Interpreter in = new Interpreter();
	Object returnValue = null;
	Map<String, Object> data = null;
	String subject = null;
	if(msg.contains(":")){
		String[] split=msg.split(":");
		if(split.length<=2){
			data = so.getData(split[0].trim(), "BeanShell");
		}
		subject=split[1];
	}else{
		data = so.getData(msg.trim(), "BeanShell");
	}

	if(data==null) {
	    return null;
	}
	context.put("used", true);
	try {
	    in.set("so", so);
	    // 得有一个文档说明：
	    in.set("repo", repo);
	    in.set("parse", parse);
	    in.set("msg", msg.trim());
	    in.set("domain", domain);
	    in.set("admin", admin);
	    in.set("logic", logic);
		in.set("springContext", SpringContextUtil.getBean("springContextUtil"));
	    // 根据规则引擎？选择解析逻辑
	    // 一个段落一个Context，一片文章一个Context。
	    // 校验规则。
	    // 一句话，对应一段逻辑，通过一句话，找到解析逻辑。
	    // startMsg
	    // 包含xx andxx 。
	    // 复用语句
	    in.setStrictJava(true);
	    String string = string(data, "Content");

		if(subject!=null){
			in.set("subject", subject);
			in.set("param", subject);
		}
	    in.eval(string);
	    returnValue = in.get("returnValue");
	} catch (EvalError e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return returnValue;

    }

}
