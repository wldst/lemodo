package com.wldst.ruder.module.parse.impl;

import bsh.EvalError;
import bsh.Interpreter;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.domain.ParseExcuteDomain;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.bs.DomainLogicOperator;
import com.wldst.ruder.module.bs.DomainOperator;
import com.wldst.ruder.module.bs.ParseExcuteSentence;
import com.wldst.ruder.module.bs.ShellOperator;
import com.wldst.ruder.module.parse.MsgProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 处理注册句子的解析
 * 
 * @author wldst
 *
 */
@Component
public class BeanShellProcess extends ParseExcuteDomain implements  MsgProcess  {
    private static final Logger  logger = LoggerFactory.getLogger(BeanShellProcess.class);
    @Autowired
    private CrudNeo4jService repo;
    @Autowired
    private UserAdminService admin;

    @Autowired
    private ShellOperator so;
    @Autowired
    private ParseExcuteSentence parse;
    @Autowired
    private DomainOperator domain;
    @Autowired
    private DomainLogicOperator logic;
    protected static List<String> preFix= Arrays.asList("bs", "BeanShell", "java脚本", "java逻辑");// 唤醒词
    
    @Override
    public Object process(String msgx, Map<String, Object> context) {
	
	String msg = msgx.trim().replaceAll("：", ":");
	Interpreter in = new Interpreter();
	StringBuilder  returnValue = new StringBuilder();
	// 并行概率执行
	for(String si:preFix) {
	    String chatPrefix = si+":";
		if (!msg.startsWith(chatPrefix)) {
		    continue;
		}
		context.put(USED, true);
		String cmdContent=msg.replaceFirst(chatPrefix, "");
		String shellName = cmdContent.substring(0, cmdContent.indexOf(":"));
		String prams = cmdContent.substring(cmdContent.indexOf(":")+1);
		List<Map<String, Object>> data = so.queryData(shellName, "BeanShell");
		Map<String, Object> userSelect = userSelect(context, data);
		if(data==null||data.isEmpty()) {
		    continue;
		}
		try {
			String jarPath =string(userSelect,"jarPath");

		    in.set("so", so);
		    // 得有一个文档说明：
		    in.set("repo", repo);
		    in.set("parse", parse);
		    in.set("msg", msg);
		    in.set("subject",  prams.trim());
		   
		    in.set("domain", domain);
		    in.set("admin", admin);
		    in.set("logic", logic);
		    // 根据规则引擎？选择解析逻辑
		    // 一个段落一个Context，一片文章一个Context。
		    // 校验规则。
		    // 一句话，对应一段逻辑，通过一句话，找到解析逻辑。
//		    char[] charArray = "一句话对应一段逻辑通过一句话找到解析逻辑".toCharArray();
		    // startMsg
		    //查看定义，JSON ID,NAME,CODE
//		    StringBuilder sb  = new StringBuilder();
//		    for(char ci: charArray) {
//			String[] hanyuPinyinStringArray = PinyinHelper.toHanyuPinyinStringArray(ci);
//			if(hanyuPinyinStringArray!=null) {
//			    sb.append(hanyuPinyinStringArray[0]);
//			}
//		    }
//		    returnValue=sb.toString();
		    // 包含xx andxx 。
		    // 复用语句
//		    in.setStrictJava(true);

//			import com.wldst.ruder.util.ShellRun;
//			String[] params=subject.split(",");
//			if(params.length<2){
//				returnValue="请输入正确的格式";
//			}else{
//				ShellRun.startupStopShell(params[0], params[1]);
//			}
//			returnValue="成功";

		   
			String string = string(userSelect, "Content");

			// 假设你有一个名为library.jar的类库，在路径 /home/user/libs/ 下


			if(jarPath!=null){
				File file = new File(jarPath);
				if(file.isDirectory()||file.exists()){
					String[] split=file.getAbsolutePath().split("\\\\");
					String dds = String.join("/", split);
					in.eval("addClassPath(\""+dds+"\");\n"+string);
				}else {
					returnValue.append(jarPath+"文件不存在");
				}
			}else{
				in.eval(string);
			}

			Object rv = in.get("returnValue");
			if(rv==null) {
				 continue;
			}
			returnValue.append(rv);
		} catch (EvalError e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
			break;
		}
		if(!returnValue.isEmpty()) {
		    break;
		}
		break;
	}
	return returnValue.toString();

    }

}
