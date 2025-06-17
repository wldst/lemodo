package com.wldst.ruder.module.bs.impl;

import java.util.Map;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.bs.DomainLogicOperator;
import com.wldst.ruder.module.bs.DomainOperator;
import com.wldst.ruder.module.bs.ShellOperator;
import com.wldst.ruder.module.fun.Neo4jOptByUser;
import com.wldst.ruder.util.CrudUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.wldst.ruder.domain.BeanShellDomain;
import com.wldst.ruder.module.bs.BeanShellService;

import bsh.EvalError;
import bsh.Interpreter;
@Service
public class BeanShellServiceImpl extends BeanShellDomain implements BeanShellService {
	private static Logger logger = LoggerFactory.getLogger(BeanShellServiceImpl.class);

	private CrudNeo4jService repo;
	private UserAdminService admin;

	private Neo4jOptByUser cypherUtil;
	private CrudUtil crudUtil;
	private ShellOperator so;
	private DomainOperator domain;
	private DomainLogicOperator logic;
	@Autowired
    public BeanShellServiceImpl(@Lazy CrudNeo4jService repo, @Lazy UserAdminService admin, Neo4jOptByUser cypherUtil, CrudUtil crudUtil, @Lazy  ShellOperator so, @Lazy DomainOperator domain, @Lazy DomainLogicOperator logic){
        this.repo=repo;
        this.admin=admin;
        this.cypherUtil=cypherUtil;
        this.crudUtil=crudUtil;
        this.so=so;
        this.domain=domain;
        this.logic=logic;
    }


    @Override
	public void init(Interpreter in) throws EvalError {
		in.set("so", so);
		// 得有一个文档说明：
		in.set("repo", repo);
		in.set("domain", domain);
		in.set("admin", admin);
		in.set("logic", logic);
		in.set("crudUtil", crudUtil);
		in.set("cypherUtil", cypherUtil);

	}
	@Override
	public Object runBeanShell(String shellId) throws EvalError{
		Map<String, Object> attMapBy = null;
		try{
			attMapBy = repo.getNodeMapById(Long.valueOf(shellId));
		}catch(Exception e){
			attMapBy = repo.getDataBy(shellId,"BeanShell").get(0);
		}

		Interpreter in = new Interpreter();
		init(in);
		// in.setStrictJava(true);
		String string = string(attMapBy, BS_SCRIPT);
		in.eval(string);
		// 获取元数据ID
		Object returnValue = in.get("returnValue");
		return returnValue;
	}

	@Override
	public Object runShell(String shellId, Map<String, Object> vo) throws EvalError{
		if(vo==null){
			vo=newMap();
		}
		Map<String, Object> attMapBy = null;
		try{
			attMapBy = repo.getNodeMapById(Long.valueOf(shellId));
		}catch(Exception e){
			attMapBy = repo.getDataBy(shellId,"BeanShell").get(0);
		}

		Interpreter in = new Interpreter();
		init(in);
		if(vo!=null&&!vo.isEmpty()){
			in.set("cypherUtil", vo);
			in.set("vo", vo);
		}
		// in.setStrictJava(true);
		String string = string(attMapBy, BS_SCRIPT);
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
		Object returnValue = in.get("returnValue");
		return returnValue;
	}
    
    @Override
    public Object run(Map<String,Object> vo) throws EvalError {
	Interpreter in = new Interpreter();
	in.set("vo", vo);
	setValue(vo, in,LABEL);
	setValue(vo, in,CODE);
	init(in);
	in.set("me", in);
	// in.setStrictJava(true);
	String string = string(vo, BS_SCRIPT);
	in.eval(string);
	// 获取元数据ID
	Object returnValue = in.get("returnValue");
	return returnValue;
    }

    public void setValue(Map<String, Object> vo, Interpreter in,String key) throws EvalError {
	String labelx = string(vo, key);
	if(labelx!=null) {
	    in.set(key, labelx);
	}
    }

}
