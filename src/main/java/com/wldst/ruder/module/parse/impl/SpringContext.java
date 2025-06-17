package com.wldst.ruder.module.parse.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Map.Entry;

import org.neo4j.graphdb.Node;
import org.springframework.stereotype.Component;

import com.wldst.ruder.config.SpringContextUtil;
import com.wldst.ruder.domain.ParseExcuteDomain;
import com.wldst.ruder.module.parse.MsgProcess;

/**
 * create 将SpringContext中的Bean存到系统中去。
 * 供系统检索，在写BeanShell的时候可以进行引入。比如要实现某些功能，
 * 自动引入相关的类。
 * 
 * @param msg
 * @param context
 */
@Component
public class SpringContext extends ParseExcuteDomain implements MsgProcess {

    /**
     *  
     * 
     * @param msg
     * @param context
     */
    @Override
    public Object process(String msg, Map<String, Object> context) {

	if (msg.equals("BeansUpdate")) {
	    context.put(USED, true);
	    Map<String, Object> beans = SpringContextUtil.getBeans();
	    Map<String, Object> beanMapId = newMap();
	    validMetaData();
	    for (Entry<String, Object> si : beans.entrySet()) {		
		Map<String, Object> d = newMap();
		Class<? extends Object> class1 = si.getValue().getClass();
		String className = class1.getName();
		Node save = saveBean(si, d, class1);
		
		if(className.contains("CGLIB$")||className.contains("$")) {
		    continue;
		}
		beanMapId.put(className.replaceAll("\\.", ""), save.getId());
		packageProcess(class1, save);
	    }

	    for (Entry<String, Object> si : beans.entrySet()) {
		Class<? extends Object> clazz = si.getValue().getClass();
		String beanClazz = clazz.getName();
		if(beanClazz.contains("CGLIB$")||beanClazz.contains("$")) {
		    continue;
		}
		Long classId = longValue(beanMapId, beanClazz.replaceAll("\\.", ""));
		fieldProcess(beanMapId, clazz, classId);
		methodProcess(clazz, classId);
	    }

	}

	return null;
    }

    public Node saveBean(Entry<String, Object> si, Map<String, Object> d, Class<? extends Object> class1) {
	String className = class1.getName();
	d.put(CODE, si.getKey());
	d.put("clazz", className);
	d.put("name", class1.getSimpleName());

	Node save = neo4jService.save(d, "Bean");
	return save;
    }

    public void packageProcess(Class<? extends Object> class1, Node save) {
	String packageName = class1.getPackageName();
	Map<String, Object> packageAtt = neo4jService.getAttMapBy(CODE, packageName, "Package");
	Long packageId=null;
	if(packageAtt==null) {
	    Map<String, Object> dp =  newMap();
	    dp.put(CODE, packageName);
	    Node packageNode = neo4jService.save(dp, "Package");
	    packageId=packageNode.getId();
	}else {
	    packageId=id(packageAtt);
	}
	if(save!=null&&packageId!=null) {
	    Map<String, Object> map = newMap();
	    map.put(NAME, "includeClass");
	    relationService.addRel("package", packageId, save.getId(), map);
	}
    }

    public void methodProcess(Class<? extends Object> clazz, Long classId) {
	Method[] declaredMethods = clazz.getDeclaredMethods();
	for (Method mi : declaredMethods) {
//		    Annotation[] annotations = mi.getAnnotations();
		    int modifiers = mi.getModifiers();
//		    for(Annotation ai: annotations) {
//			if("org.springframework.web.bind.annotation.RequestMapping".equals(ai.annotationType().getClass().getName())) {
//			    
//			}
//		    }
	    
	    String methodName = mi.getName();
	    Long methodId= getMethodId(methodName);
	    
	    Map<String, Object> map = newMap();
	    
	    map.put(NAME, methodName);
	   
	    
	    if(classId!=null&&methodId!=null) {
		 relationService.addRel("method", classId, methodId, map);
	    }
	    parameterProcess(mi, methodId, map);
	}
    }

    public Long getMethodId(String methodName) {
	Long methodId;
	Map<String, Object> method = neo4jService.getAttMapBy(CODE, methodName, "Method");
	if(method==null) {
	    Map<String, Object> dp =  newMap();
	    dp.put(CODE, methodName);
	    Node methodNode = neo4jService.save(dp, "Method");
	    methodId=methodNode.getId();
	}else {
	methodId=id(method);
	}
	return methodId;
    }

    public void fieldProcess(Map<String, Object> beanMapId, Class<? extends Object> clazz, Long classId) {
	Field[] declaredFields = clazz.getDeclaredFields();
	for (Field fi : declaredFields) {
	    Class<? extends Class> class1 = fi.getType().getClass();
	    Long fiId = longValue(beanMapId, class1.getName().replaceAll("\\.", ""));
	    Map<String, Object> map = newMap();
	    map.put(NAME, fi.getName());
	    String type2Name = fi.getType().getName();
	    map.put("clazz", type2Name);
	    if(classId!=null&&fiId!=null) {
		 relationService.addRel("field", classId, fiId, map);
	    }
	}
    }

    public void parameterProcess(Method mi, Long methodId, Map<String, Object> map) {
	Parameter[] parameters = mi.getParameters();
	for(Parameter pi: parameters) {
	 Map<String, Object> dp =  newMap();
		dp.put("valueType", pi.getType().getName());
		dp.put("name", pi.getName());
		Node save = neo4jService.save(dp, "Parameter");
		Long paraId=save.getId();
		if(methodId!=null&&paraId!=null) {
		    Map<String, Object> mapx = newMap();
		    mapx.put(NAME, pi.getName());
			 relationService.addRel("parameter", methodId, paraId, mapx);
		}
		
	 }
    }

    public void validMetaData() {
	validPackageMeta();
	validBeanMetaData();
	validMethodMetaData();
	validParameterMeta();
    }

    public void validBeanMetaData() {
	Map<String, Object> attMapBy = neo4jService.getAttMapBy(LABEL, "Bean", META_DATA);
	if (attMapBy == null) {
	    Map<String, Object> md = newMap();
	    md.put(COLUMNS, "id,name,code,clazz");
	    md.put(HEADER, "编码,名称,代码,类");
	    md.put(NAME, "SpringBean");
	    md.put(CODE, "SpringBean");
	    md.put(LABEL, "Bean");

	    neo4jService.save(md, META_DATA);
	}
    }
    
    public void validMethodMetaData() {
	Map<String, Object> attMapBy = neo4jService.getAttMapBy(LABEL, "Bean", META_DATA);
	if (attMapBy == null) {
	    Map<String, Object> md = newMap();
	    md.put(COLUMNS, "id,name,code,clazz");
	    md.put(HEADER, "编码,名称,代码,类");
	    md.put(NAME, "SpringBean");
	    md.put(CODE, "SpringBean");
	    md.put(LABEL, "Bean");

	    neo4jService.save(md, META_DATA);
	}
    }

    public void validPackageMeta() {
	Map<String, Object> packageMap = neo4jService.getAttMapBy(LABEL, "package", META_DATA);
	if (packageMap == null) {
	    Map<String, Object> md = newMap();
	    md.put(COLUMNS, "id,name,code,parentId");
	    md.put(HEADER, "编码,名称,代码,父级包");
	    md.put(NAME, "包");
	    md.put(CODE, "package");
	    md.put(LABEL, "Package");
	    neo4jService.save(md, META_DATA);
	}
    }

    public void validParameterMeta() {
	Map<String, Object> parameterMap = neo4jService.getAttMapBy(LABEL, "Parameter", META_DATA);
	if (parameterMap == null) {
	    Map<String, Object> md = newMap();
	    md.put(COLUMNS, "id,name,valueType,description");
	    md.put(HEADER, "编码,参数名,参数类型,描述");
	    md.put(NAME, "参数");
	    md.put(CODE, "Parameter");
	    md.put(LABEL, "Parameter");
	    neo4jService.save(md, META_DATA);
	}
    }
}
