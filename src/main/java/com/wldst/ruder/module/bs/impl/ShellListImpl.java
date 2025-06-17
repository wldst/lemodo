package com.wldst.ruder.module.bs.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wldst.ruder.domain.BeanShellDomain;
import com.wldst.ruder.module.bs.Shell;
import com.wldst.ruder.module.bs.ShellList;

public class ShellListImpl extends BeanShellDomain implements ShellList {
    private static Logger logger = LoggerFactory.getLogger(ShellListImpl.class);
    private Map<String, Object> data;
    private Map<String, Object> retData;

    private List<Shell> shellList = new ArrayList<>();

    /**
     * 宏命令聚集管理方法
     */
    @Override
    public void add(Shell cmd) {
	shellList.add(cmd);
    }

    /**
     * 宏命令聚集管理方法
     */
    @Override
    public void remove(Shell cmd) {
	shellList.remove(cmd);
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> newDataMap) {
	// LoggerTool.info(logger,MapTool.mapString(data));
	if (shellList.isEmpty()) {
	    return newDataMap;
	}
	retData = new HashMap<>(shellList.size());
	// Object object = this.getRetData().get(getLabel());
	// if(object!=null) {
	// paramMap.put("prop",object);
	// }
	this.data = newDataMap;

	for (Shell cmd : shellList) {
	    Map<String, Object> execute = cmd.execute(this.data);
	    retData.put(cmd.getId(), execute);
	}
	return retData;
    }

    @Override
    public boolean isEmpty() {
	return shellList.isEmpty();
    }

    @Override
    public Map<String, Object> getRetData() {
	return retData;
    }

    @Override
    public Map<String, Object> execute() {
	if (shellList.isEmpty()) {
	    return null;
	}
	retData = new HashMap<>(shellList.size());

	for (Shell cmd : shellList) {
	    Map<String, Object> execute = cmd.execute(this.data);
	    retData.put(cmd.getId(), execute);
	}
	return retData;
    }

    @Override
    public String getId() {
	// TODO Auto-generated method stub
	return null;
    }

}
