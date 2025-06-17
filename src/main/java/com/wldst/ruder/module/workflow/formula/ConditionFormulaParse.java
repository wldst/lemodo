package com.wldst.ruder.module.workflow.formula;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wldst.ruder.util.LoggerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.constant.CruderConstant;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.module.fun.Neo4jOptByUser;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.util.MapTool;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * 流程分支任务条件解析类
 * 
 * @author wldst
 */
public class ConditionFormulaParse extends MapTool {
    // 日志对象
    private static Logger logger = LoggerFactory.getLogger(ConditionFormulaParse.class);

    // 流程实例对象
    private Map<String, Object> workflow;
    
    private static Map<String, Map<String, Object>> operatorMap = null;
    
    private CrudNeo4jService neo4jService;

    

    // 数据库操作对象

    // 关联业务数据对象
    private Map<String, Object> mainDataObject;
    private static Set<String> keySet =null;
    private static Set<String> equalsKey= new HashSet<>();
    private static Set<String> andOrKey= new HashSet<>();
    private static Set<String> boolKey= new HashSet<>();
    private static Set<String> whetherKey= new HashSet<>();
    private static Set<String> addKey= new HashSet<>();
    private static Set<String> minusKey= new HashSet<>();
    private static Set<String> multiDivi= new HashSet<>();

    public ConditionFormulaParse(Map<String, Object> workflow,CrudNeo4jService dbService) {
	this.workflow = workflow;
	this.neo4jService=dbService;
	init();
    }

    /**
     * 获取关联业务数据指定字段的值
     * 
     * @param fieldName 指定字段名
     * @return 字段的值
     */
    public String getBizValue(String fieldName) {
	String retStr = null;
	try {
	    if (this.mainDataObject != null) {
		Object valObj = this.mainDataObject.get(fieldName);
		if (null != valObj) {
		    retStr = valObj.toString();
		} else {
		    LoggerTool.error(logger,"获取流程关联业务主表信息失败:" + fieldName);
		}
	    } else {
		retStr = String.valueOf(neo4jService.getPropValueByNodeId(MapTool.longValue(workflow,"bizDataId"),fieldName));
	    }

	} catch (Exception ex) {
	    LoggerTool.error(logger,"获取流程关联业务主表信息失败:", ex);
	}
	return retStr;
    }

    public String getProjectFieldValue(String fieldName) {
	String retStr = null;
	try {
	    String projectId = getBizValue("projectId");
	    this.mainDataObject = neo4jService.getPropMapBy(projectId);
	    if (this.mainDataObject != null) {
		Object valObj = this.mainDataObject.get(fieldName);
		if (null != valObj) {
		    retStr = valObj.toString();
		} else {
		    LoggerTool.error(logger,"获取流程关联业务主表信息失败:");
		}
	    }else {
		retStr =  String.valueOf(neo4jService.getPropValueByNodeId(Long.valueOf(projectId), fieldName));
	    }

	} catch (Exception ex) {
	    LoggerTool.error(logger,"获取流程关联业务主表信息失败:", ex);
	}
	return retStr;
    }
    /**
     * 获取字段对应对象的字段值数据。
     * @param fieldKey
     * @param fieldName
     * @return
     */
    public String getValueOfFieldObject(String fieldKey,String fieldName) {
	String retStr = null;
	try {
	    String projectId = getBizValue(fieldKey);
	    this.mainDataObject = neo4jService.getPropMapBy(projectId);
	    if (this.mainDataObject != null) {
		Object valObj = this.mainDataObject.get(fieldName);
		if (null != valObj) {
		    retStr = valObj.toString();
		} else {
		    LoggerTool.error(logger,"获取流程关联业务主表信息失败:");
		}
	    }else {
		retStr =  String.valueOf(neo4jService.getPropValueByNodeId(Long.valueOf(projectId), fieldName));
	    }

	} catch (Exception ex) {
	    LoggerTool.error(logger,"获取流程关联业务主表信息失败:", ex);
	}
	return retStr;
    }

    /**
     * 获取关联业务数据指定字段的值
     * 
     * @param fieldName 指定字段名
     * @return 字段的值
     */
    public String getBizValue(String primaryKey, String fieldName) {
	String retStr = null;
	try {
	    if(primaryKey==null) {
		return retStr;
	    }
	    if(Long.valueOf(primaryKey)>0) {
		this.mainDataObject = neo4jService.getPropMapBy(primaryKey);
		    if (this.mainDataObject != null) {
			Object valObj = this.mainDataObject.get(fieldName);
			if (null != valObj) {
			    retStr = valObj.toString();
			} else {
			    LoggerTool.error(logger,"获取流程关联业务信息失败:");
			}
		    }
	    }
	    

	} catch (Exception ex) {
	    LoggerTool.error(logger,"获取流程关联业务信息失败:", ex);
	}
	return retStr;
    }

    /**
     * 解析条件公式
     * 
     * @param executorFormula 条件公式
     * @return 解析结果,boolean值
     * @throws EvalError
     * @throws EvalError
     * @throws Exception
     */
    public boolean parseConidtionFormula(String executorFormula) throws EvalError {
	boolean retFlag = false;
	if (executorFormula != null && executorFormula.trim().length() > 0) {
	    Interpreter bsh = new Interpreter();
	    StringBuffer codeBuffer = new StringBuffer();
	    codeBuffer.append(executorFormula);
	    bsh.set("parse", this);
	    bsh.eval(codeBuffer.toString());
	    retFlag = ((Boolean) bsh.get("returnValue")).booleanValue();
	}
	return retFlag;
    }
    
    public boolean parseConidtion(String ci) throws EvalError {
	LoggerTool.info(logger,ci);
	boolean hasOpt = false;
	for(String opi: equalsKey) {
	    LoggerTool.info(logger,opi);
	    String[] split = ci.split(opi);
	    if(split.length>1) {
		hasOpt=true;
	    }
	}
	if(hasOpt) {
	    return computeEqualExpress(ci, equalsKey);
	}
	return false;	
    }

    private void initOperator(Collection<Map<String, Object>> values) { 
	for(Map<String,Object> x:values) {
	    String type2 = MapTool.type(x);
	    String xName = MapTool.name(x);
	    switch(type2) {
	    case "EqualUnequalExpression":equalsKey.add(xName);break;
	    case "AndOrExpression":andOrKey.add(xName);break;
	    case "BoolExpression":boolKey.add(xName);break;
	    case "ErrorExpression":equalsKey.add(xName);break;
	    case "WhetherEqualExpression":whetherKey.add(xName);break;
	    case "MinusExpression":minusKey.add(xName);break;
	    case "PlusExpression":addKey.add(xName);break;
	    case "MultiplyDivideExpression":multiDivi.add(xName);break;	    
	    }
	}
    }

    private boolean computeEqualExpress(String ci, Set<String> keySet) {
	String trim = ci.trim();
	String operator = null;
	String left = null;
	String right = null;
	for(String opi: keySet) {
	    LoggerTool.info(logger,opi);
	    String[] split = trim.split(opi);
	    if(split.length>1) {
		    String tright = split[1];
		    boolean isRightData=true;
		    if(tright.length()>1) {
			for(String opj: keySet) {
				if(tright.startsWith(opj)) {
				    if(opj.equals(opi)&&opj.equals("=")) {
					split = trim.split("==");
					right = split[1];
				    }else {
					isRightData=false;
				    }			    
				}
				if(tright.indexOf(opj)>1) {
				    isRightData=false;
				}
			    }
		    }
		    
		    if(isRightData) {
			operator=opi;
			left=split[0];
			right=split[1];
			break;
		    }
	    }
	}
	//compute Condition
	Boolean returnVal=false;
	if(operator!=null&&left!=null&&right!=null) {
	  
	    BigDecimal leftValue=parseBizValue(left);
	    BigDecimal rightValue= parseBizValue(right);
	   if(leftValue==null||rightValue==null) {
	       return false;
	   }
	    int compareTo = leftValue.compareTo(rightValue);
	    switch(operator) {
        	    case ">":  returnVal=compareTo==1;break;
        	    case "<":returnVal=compareTo==-1;break;
        	    case "=":returnVal=compareTo==0;break;
        	    case ">=":returnVal=compareTo>=0;break;
        	    case "<=":returnVal=compareTo<=0;break;
        	    case "<>":returnVal=compareTo!=0;break;	    
	    }
	    //获取右边的值
	}
	return returnVal;
    }
    
    private BigDecimal parseExcutorExpress(String left) {
	BigDecimal leftValue=null;
	if (left.indexOf(".") > 0) {
	    String[] split = left.split("\\.");
	    // 获取的值
	    String col = neo4jService.getColOf(split[1], MapTool.label(mainDataObject));
	    Object bizValue = mainDataObject.get(col);
	    for (int i = 2; i < split.length; i++) {
		String ei = split[i];
		 //遍历查询或者公式查询。根据公式，映射函数。表达式映射，定时。
		

	    }
	} else {
	    leftValue = BigDecimal.valueOf(Double.valueOf(left));
	}
	return leftValue;
    }

    private BigDecimal parseBizValue(String left) {
	BigDecimal leftValue=null;
	if(left.indexOf(".")>0) {
        	String[] split = left.split("\\.");
        	    //获取左边的值，
        	String colOf = neo4jService.getColOf(split[1],  MapTool.label(mainDataObject));
        	Object bizValue = mainDataObject.get(colOf);
        	 
        	if(bizValue instanceof Long l) {
        	    leftValue= BigDecimal.valueOf(l); 
        	} else 
        	if(bizValue instanceof Double l) {
        	    leftValue= BigDecimal.valueOf(l); 
        	} else 
        	if(bizValue instanceof Date l) {
        	    leftValue= BigDecimal.valueOf(l.getTime()); 
        	} else if(bizValue instanceof String s) {
        	    if("".equals(s)) {
        		leftValue=null;
        	    }else {
            	    leftValue= BigDecimal.valueOf(Long.valueOf(s)); 
        	    }
        	} else {
        	    leftValue= BigDecimal.valueOf(MapTool.longValue(mainDataObject, colOf));
        	}
        	
	}else {
	    leftValue= BigDecimal.valueOf(Double.valueOf(left)); 
	}
	return leftValue;
    }

    private void init() {
	if(operatorMap==null) {
	    List<Map<String, Object>> listAllByLabel = neo4jService.listAllByLabel("ExpressionOperator");
	    operatorMap= new HashMap<>(listAllByLabel.size());
	    for(Map<String, Object> mi: listAllByLabel) {
		    operatorMap.put(MapTool.name(mi), mi);
	    }
	    
	   keySet = operatorMap.keySet();
	   initOperator(operatorMap.values());
	}
	if(mainDataObject==null) {
	    this.mainDataObject = neo4jService.getLablePropBy(MapTool.string(workflow, "bizDataId"));
	}
    }

    /**
     * 根据流程角色名编号
     * 
     * @param wfRoleCode 流程角色名编号
     * @return 用户ID数组
     */
    public long[] getUsersByRoleCode(String wfRoleCode) {
	long[] retArray = null;
	try {
	    List<Map<String, Object>> chidList = neo4jService.queryCache("MATCH(n:Role)-[r]->[m:User] where n.code='"+wfRoleCode+"' return id(m) as id");
	    if (chidList != null && chidList.size() > 0) {
		retArray = new long[chidList.size()];
		int i=0;
		for (Map<String, Object> ci:chidList) {		   
		    retArray[i] = MapTool.id(ci);
		    i++;
		}
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    LoggerTool.error(logger,"获取指定流程角色所分配的用户ID失败:", ex);
	}
	return retArray;
    }
}
