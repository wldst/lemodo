package com.wldst.ruder.module.bs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bsh.EvalError;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.util.*;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.annotation.ServiceLog;
import com.wldst.ruder.config.SpringContextUtil;
import com.wldst.ruder.domain.BeanShellDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.command.CommandList;
import com.wldst.ruder.module.command.impl.CommandListImpl;
import com.wldst.ruder.module.command.impl.RelationInCommand;
import com.wldst.ruder.module.command.impl.RelationOutCommand;
import com.wldst.ruder.module.command.impl.SaveCommand;

import bsh.Interpreter;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 脚本
 */
@Controller
@RequestMapping("${server.context}/bs/")
public class BeanShellController extends BeanShellDomain {
    private static Logger logger = LoggerFactory.getLogger(BeanShellController.class);

	private BeanShellService bss;

	private CrudNeo4jService repo;

	private CrudUtil crudUtil;
	@Autowired
    public BeanShellController(@Lazy BeanShellService bss, @Lazy CrudNeo4jService repo, CrudUtil crudUtil){
        this.bss=bss;
        this.repo=repo;
        this.crudUtil=crudUtil;
    }

    @RequestMapping(value = "/run/BeanShell/{shellId}", method = { RequestMethod.GET, RequestMethod.POST  })
	@ResponseBody
	public WrappedResult shellRun(Model model, @PathVariable("shellId") String shellId,@RequestBody Map<String, Object> vo, HttpServletRequest request)
			throws Exception {
		Object returnValue=bss.runShell(shellId, vo);
		return ResultWrapper.success(returnValue);
	}



	@RequestMapping(value = "/run/{shellId}", method = { RequestMethod.GET  })
	@ResponseBody
	public WrappedResult bshellRun(Model model, @PathVariable("shellId") String shellId, HttpServletRequest request)
			throws Exception {
		Object returnValue=bss.runBeanShell(shellId);
		return ResultWrapper.success(returnValue);
	}



	/**
     * 动态解析功能，解析操作指令。 save:{map,label, tranKey:map{key:value}, bizValue:
     * map{key:value}, childCMD:[ relin:{rLabel,sprop,sLabel,childCMD},
     * relout:{rLabel,tProp,tLabel,childCMD}, ], } relin:{rLabel,sprop,sLabel}
     * relout:{rLabel,tProp,tLabel}
     * 
     * 脚本说，已有变量：vo，label，so，domain，logic，admin vo：参数：入参 label：当前数据的标签 so：脚本函数，添加关系等
     * repo：增删改查等操作 logic：逻辑相关操作 admin：管理相关操作
     * 
     * 
     * // 获取元数据ID // Long nodeId = so.getId(META_DATA,LABEL, label); // // SaveNode
     * saveModule = so.save("module"); // SaveNode saveApp = so.save("App"); // //
     * Map<String, Object> bizValue = new HashMap<>(); //
     * bizValue.put("iconbg","#16990c"); // bizValue.put("icon","&#xe64d"); //
     * bizValue.put("url","/cd/module/{label}"); // saveApp.setBizValue(bizValue);
     * // // Map<String, String> tk = new HashMap<>(); // tk.put("label","appid");
     * // saveApp.setTransKey(tk); // // Map<String, Object> savedModule =
     * saveModule.execute(vo); // Map<String, Object> savedApp =
     * saveApp.execute(vo); // // so.addRel("appModule",savedModule, savedApp); //
     * so.addRel("moduleMeta",nodeId, savedModule);
     * 
     * @param model
     * @param poLabel
     * @param vo
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/run/{po}/{shellName}", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public WrappedResult shellByName(Model model, @PathVariable("po") String poLabel,
	    @PathVariable("shellName") String shellName, @RequestBody JSONObject vo, HttpServletRequest request)
	    throws Exception {
	Map<String, Object> viewMap = repo.getAttMapBy(LABEL, poLabel, META_DATA);
	if (viewMap == null) {
	    throw new DefineException(poLabel + "未定义！");
	}

	Map<String, Object> beanShell = repo.getAttMapBy(CODE, shellName, poLabel);
	Interpreter in = new Interpreter();
	in.set("vo", vo);
	if(null!=label(vo)){
		in.set("label", label(vo));
	}

//	String daima = shellName;
//	if (daima != null) {
//	    in.set("code", daima);
//	}
		if(null!=code(vo)){
			in.set("code", code(vo));
		}
	if(null!=name(vo)){
		in.set(NAME, name(vo));
	}
		bss.init(in);
	// in.setStrictJava(true);
	String string = string(beanShell, BS_SCRIPT);
	in.eval(string);
	// 获取元数据ID

	// Long nodeId = so.getId(META_DATA,LABEL, label);
	// 替换数据
	// repo.queryBy(attMapBy, poLabel)

	// 注册手机用户
	// List<Map<String, Object>> listAllByLabel =
	// neo4jService.listAllByLabel(poLabel);
	// for(Map<String, Object> di: listAllByLabel) {
	// String string2 = string(di,"phone");
	// if(null!=string2) {
	// adminService.registerPhoneUser(string2);
	// }
	// }
//	domain.name(vo);
//	domain.id(vo);
	
//	String queneBtn="MATCH (a) where a.name contains('权限') return distinct a";
//	List xx=repo.query(queneBtn);
//	for(Map xi: xx) {		  
//	        so.addRel("nameLikeRel",MapTool.name(xi),MapTool.id(vo), MapTool.id(xi)); 
//	}
	
	Object returnValue = in.get("returnValue");
	return ResultWrapper.success(returnValue);
    }

    

    


    /**
     * 脚本说，已有变量：vo，label，so，domain，logic，admin vo：参数：入参 label：当前数据的标签 so：脚本函数，添加关系等
     * logic：逻辑相关操作 admin：管理相关操作
     * 
     * @param model
     * @param vo
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/run", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public WrappedResult run(Model model, @RequestBody JSONObject vo, HttpServletRequest request) throws Exception {
	Object returnValue = bss.run(vo);
	return ResultWrapper.success(returnValue);
    }



    

    @RequestMapping(value = "/registerMethod/{id}", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public WrappedResult registerMethod(Model model, @PathVariable("id") String id, @RequestBody JSONObject vo,
	    HttpServletRequest request) throws Exception {

	Map<String, Object> calssNode = repo.getNodeMapById(id);
	String classFile = string(calssNode, "class");
	String[] split = classFile.split("\\.");
	String firstLow = null;
	 Class<?> forName =null;
	if (split.length > 1) {
	    firstLow = StringGet.firstLow(split[split.length - 1]);
	    forName = Class.forName(classFile);
	} else {
	    firstLow = StringGet.firstLow(classFile);
	    forName = SpringContextUtil.getType(firstLow);
	}

	Method[] declaredMethods = forName.getDeclaredMethods();
	for (Method mi : declaredMethods) {
	    int modifiers = mi.getModifiers();
	    if (modifiers != 1) {
		continue;
	    }
	    Class<?> returnType = mi.getReturnType();
	    String methodName = mi.getName();
	    Parameter[] parameters = mi.getParameters();
	    // Class<?>[] parameterTypes = mi.getParameterTypes();
	    // mi.get
	    Annotation[] annotations = mi.getAnnotations();

	    for (Annotation ai : annotations) {
		Class<? extends Annotation> annotationType = ai.annotationType();
		Field[] declaredFields = annotationType.getDeclaredFields();
		for (Field fi : declaredFields) {

		    LoggerTool.info(logger,"=======" + fi.getName() + "======" + annotationType.descriptorString());
		    // ai.annotationType().getField(fi.getName());
		}
	    }
	    Map<String, Object> method = newMap();
	    method.put("returnType", returnType.getName());
	    method.put("name", methodName);
	    method.put("code", methodName);
	    ServiceLog annotation = mi.getAnnotation(ServiceLog.class);
	    if (annotation != null) {
		String description = annotation.description();
		method.put("description", description);
	    }

	    Node methodNode = repo.save(method, "ShellMethod");
	    for (int i = 0; i < parameters.length; i++) {
		Map<String, Object> pari = newMap();
		pari.put(NAME, parameters[i].getName());
		pari.put("valueType", parameters[i].getType().getName());
		Node pi = repo.save(pari, "Parameter");
		repo.relate(CODE,methodNode.getId(), pi.getId());
	    }
	    repo.relate(CODE,id(calssNode), methodNode.getId());
	}

	return ResultWrapper.success("");
    }

    /**
     * 绑定参数
     *
     * @param parameterNames
     * @param args
     * @return
     */
    private Map<String, Object> bindParameter(String[] parameterNames, Object[] args) {
	Map<String, Object> map = new HashMap<>();
	for (int i = 0; i < parameterNames.length; i++) {
	    map.put(parameterNames[i], args[i]);
	}
	return map;
    }

    /**
     * 绑定注解类
     *
     * @param parameters
     * @param clazzs
     * @return
     */
    private Map<String, Class> bindClazz(List<String> parameters, Class[] clazzs) {
	Map<String, Class> map = new HashMap<>();
	for (int i = 0; i < parameters.size(); i++) {
	    map.put(parameters.get(i), clazzs[i]);
	}
	return map;
    }

    @RequestMapping(value = "/{po}/saveAndConnect/{endLabel}", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public WrappedResult saveAndConnect(Model model, @PathVariable("po") String poLabel,
	    @PathVariable("endLabel") String endLabel, @RequestBody JSONObject vo, HttpServletRequest request)
	    throws Exception {
	Map<String, Object> viewMap = repo.getAttMapBy(LABEL, endLabel, META_DATA);
	if (viewMap == null) {
	    throw new DefineException(endLabel + "未定义！");
	}
	Long nodeId = repo.getNodeId(CODE, "group2", endLabel);
	Long shapeNodeId = repo.getNodeId(NAME, "自定义节点", "X6Shape");

	// Long longValue = longValue(viewMap, ID);
	// neo4jService.hasPath(poLabel, longValue, endLabel, nodeId);
	CommandList mc = new CommandListImpl();

	SaveCommand save = new SaveCommand(repo, "X6DomainNode", crudUtil);
	Map<String, Object> bizValue = new HashMap<>();
	bizValue.put("shape", "custom-image");
	save.setBizValue(bizValue);
	Map<String, String> tk = new HashMap<>();
	tk.put("label", "title");
	save.setTransKey(tk);
	save.add(new RelationOutCommand(repo, "shape", shapeNodeId, "X6Shape"));
	save.add(new RelationInCommand(repo, "group", nodeId, endLabel));

	mc.add(save);
	Map<String, Object> execute = mc.execute(vo);
	return ResultWrapper.wrapResult(true, execute, null, SAVE_SUCCESS);
    }

}
