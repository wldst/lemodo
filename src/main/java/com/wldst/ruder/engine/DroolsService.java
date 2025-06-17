package com.wldst.ruder.engine;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.drools.compiler.lang.DrlDumper;
import org.drools.compiler.lang.api.DescrFactory;
import org.drools.compiler.lang.api.PackageDescrBuilder;
import org.drools.compiler.lang.api.PatternDescrBuilder;
import org.drools.compiler.lang.api.RuleDescrBuilder;
import org.drools.compiler.lang.descr.PackageDescr;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.wldst.ruder.constant.RuleConstants;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.database.DbInfoService;
import com.wldst.ruder.module.fun.service.RuleService;

@Service
public class DroolsService extends RuleConstants{

    private UserAdminService adminService;
    private DbInfoService dbGather;
    private RuleService ruleService;
    private CrudNeo4jService cruderService;

	@Autowired
    public DroolsService(DbInfoService dbGather, RuleService ruleService, @Lazy CrudNeo4jService cruderService,@Lazy  UserAdminService adminService){
        this.dbGather=dbGather;
        this.ruleService=ruleService;
        this.cruderService=cruderService;
		this.adminService=adminService;
    }

    /**
     * 创建KieSession
     * 
     * @return
     */
    // public void fireRule() {
    // KieServices ks = KieServices.Factory.get();
    // KieContainer kContainer = ks.getKieClasspathContainer();
    // KieSession kSession = kContainer.newKieSession("ksession-rules");
    // Map<String,String> map=new HashMap<>();
    // map.put("message","Hello World");
    // map.put("status","0");
    // kSession.insert(map);//插入
    // kSession.fireAllRules();//执行规则
    // kSession.dispose();
    // }


	public KieSession initSession(List<Map<String, Object>> ruleList) {
		if (CollectionUtils.isEmpty(ruleList) ) {
			return null;
		}
		// LoggerTool.info(logger,"执行规则引擎 start ....");
		System.setProperty("drools.dateformat", "yyyy-MM-dd HH:mm:ss");
		KieHelper helper = new KieHelper();
		boolean adminHas = false;
		boolean dbHas = false;
		boolean ruleHas = false;
		for (Map<String, Object> rule : ruleList) {
			String content = string(rule, "content");
			// string=string.replaceAll("<br>", "\n");
			// string=string.replaceAll("<\\/p>", "\n");
			// String rc = Html2Text.getContent(string);
			// System.out.println(rc);
			if(content.contains("com.wldst.ruder.fun")) {
				//升级专用代码
				content=content.replace("com.wldst.ruder.fun.", "com.wldst.ruder.module.fun.");
				rule.put("content",content);
				cruderService.saveById(id(rule), rule);
			}

			if(content.contains("com.wldst.ruder.database")) {
				//升级专用代码
				content=content.replace("com.wldst.ruder.database", "com.wldst.ruder.module.database");
				rule.put("content",content);
				cruderService.saveById(id(rule), rule);
			}

			if (content.contains("global com.wldst.ruder.module.auth.service.UserAdminService adminService")) {
				adminHas = true;
			}
			if (content.contains("global com.wldst.ruder.module.database.DbInfoService dbGather")) {
				dbHas = true;
			}
			if (content.contains("global com.wldst.ruder.module.fun.service.RuleService ruleService")) {
				ruleHas = true;
			}
			helper.addContent(content, ResourceType.DRL);
		}
		KieSession kSession = helper.build().newKieSession();
		if (adminHas) {
			kSession.setGlobal("adminService", adminService);
		}
		if (dbHas) {
			kSession.setGlobal("dbGather", dbGather);
		}
		if (ruleHas) {
			kSession.setGlobal("ruleService", ruleService);
		}
		return kSession;
	}

	public Map<String, Object> execute(KieSession kSession, Map<String, Object> t) {
		if ( t == null){
			return t;
		}
		kSession.insert(t);
		kSession.fireAllRules();
//		kSession.dispose();
		return t;
	}
    public Map<String, Object> execute(List<Map<String, Object>> ruleList, Map<String, Object> t) {
	if (CollectionUtils.isEmpty(ruleList) || t == null) {
	    return t;
	}
	// LoggerTool.info(logger,"执行规则引擎 start ....");
	System.setProperty("drools.dateformat", "yyyy-MM-dd HH:mm:ss");
	KieHelper helper = new KieHelper();
	boolean adminHas = false;
	boolean dbHas = false;
	boolean ruleHas = false;
	for (Map<String, Object> rule : ruleList) {
	    String content = string(rule, "content");
	    // string=string.replaceAll("<br>", "\n");
	    // string=string.replaceAll("<\\/p>", "\n");
	    // String rc = Html2Text.getContent(string);
	    // System.out.println(rc);
	    if(content.contains("com.wldst.ruder.fun")) {
		//升级专用代码
		content=content.replace("com.wldst.ruder.fun.", "com.wldst.ruder.module.fun.");
		rule.put("content",content);
		cruderService.saveById(id(rule), rule);
	    }
	    
	    if(content.contains("com.wldst.ruder.database")) {
		//升级专用代码
		content=content.replace("com.wldst.ruder.database", "com.wldst.ruder.module.database");
		rule.put("content",content);
		cruderService.saveById(id(rule), rule);
	    }
	    
	    if (content.contains("global com.wldst.ruder.module.auth.service.UserAdminService adminService")) {
		adminHas = true;
	    }
	    if (content.contains("global com.wldst.ruder.module.database.DbInfoService dbGather")) {
		dbHas = true;
	    }
	    if (content.contains("global com.wldst.ruder.module.fun.service.RuleService ruleService")) {
		ruleHas = true;
	    }
	    helper.addContent(content, ResourceType.DRL);
	}
	KieSession kSession = helper.build().newKieSession();
	if (adminHas) {
	    kSession.setGlobal("adminService", adminService);
	}
	if (dbHas) {
	    kSession.setGlobal("dbGather", dbGather);
	}
	if (ruleHas) {
	    kSession.setGlobal("ruleService", ruleService);
	}	

	kSession.insert(t);
	kSession.fireAllRules();
	kSession.dispose();
	// LoggerTool.info(logger,"执行规则引擎 end ....");
	return t;
    }
    
    public String test(Map<String, Object> data) {
	// 查询规则信息
	List<Map<String, Object>> rules = listMapObject(data, "rules");

	Set<String> imports = splitValue2Set(data, IMPORTS);
	
	PackageDescrBuilder pkgDescBuilder = DescrFactory.newPackage()
		.name(string(data, PACKAGE));
	for (String imi : imports) {
	    pkgDescBuilder.newImport().target(imi).end();
	}
	Map<String, String> globals = map(data, GLOBAL);
	for (Entry<String, String> ei : globals.entrySet()) {
	    pkgDescBuilder.newGlobal().identifier(ei.getKey()).type(ei.getValue()).end();
	}

	for (Map<String, Object> rule : rules) {
	    RuleDescrBuilder ruleDescrBuilder = pkgDescBuilder.newRule().name(string(rule, NAME));
	    // attribute
	    ruleDescrBuilder.attribute("salience", string(rule, salience));

	    // lhs
	    PatternDescrBuilder patternDescrBuilder = ruleDescrBuilder.lhs().pattern(Map.class.getSimpleName());
	    List<String> conditions = splitValue2List(rule, CONDITIONS);
	    for (String constraint : conditions) {
		if (!StringUtils.hasText(constraint)) {
		    patternDescrBuilder.constraint(constraint);
		}
	    }
	    patternDescrBuilder.end();

	    // rhs
	    String action = string(rule, ACTION);
	    ruleDescrBuilder.rhs(action);
	    ruleDescrBuilder.end();
	}
	pkgDescBuilder.end();

	// dump to String;
	PackageDescr packageDescr = pkgDescBuilder.getDescr();
	DrlDumper dumper = new DrlDumper();
	String drl = dumper.dump(packageDescr);
	System.out.println(drl);
	return drl;
    }
    
    

    public UserAdminService getAdminService() {
        return adminService;
    }

    public void setAdminService(UserAdminService adminService) {
        this.adminService = adminService;
    }

//    public static void main(String args[]) {
////	DroolsService ds = new DroolsService();
//	// ds.fireRule();
//    }
}
