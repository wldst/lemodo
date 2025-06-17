package com.wldst.ruder.module.fun.controller;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.wldst.ruder.util.LoggerTool;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.api.Result;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.CrudUserNeo4jService;
import com.wldst.ruder.domain.FileDomain;
import com.wldst.ruder.domain.QrCodeDomain;
import com.wldst.ruder.util.IpAddressUtil;
import com.wldst.ruder.util.MapTool;
import com.wldst.ruder.util.QRCodeUtil;

/**
 * 创建二维码
 */
@Controller
@RequestMapping("${server.context}/qrcode")
public class QrcodeController extends QrCodeDomain {
    private static Logger logger = LoggerFactory.getLogger(QrcodeController.class);
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private CrudUserNeo4jService neo4jUService;

    /**
     * 创建二维码
     * 
     * @return
     */
    @ResponseBody
    @PostMapping("/generate")
    public Result generateQrcode(HttpServletRequest request) {
	Result ajaxResult = Result.failed("创建二维码失败");
	String qrData = request.getParameter("qrData");
	String qrSuffix = request.getParameter("qrSuffix");
	String qrcode = System.currentTimeMillis() + "." + qrSuffix;
	String path = neo4jUService.getPathBy(FILE_STORE_PATH) + qrcode;
	try {
	    File file = QRCodeUtil.encode(qrData, path);
	    Map<String, Object> fileMap = neo4jUService.recordFileInfo(file);
	    ajaxResult = Result.success(MapTool.string(fileMap, ID));
	} catch (Exception e) {
	    LoggerTool.error(logger,e.getMessage(), e);
	    ajaxResult = Result.failed("创建二维码失败" + e.getMessage());
	}
	return ajaxResult;
    }

    /**
     * 接口二维码
     * 
     * @param request
     * @return
     */
    @ResponseBody
    @PostMapping("/interface/{label}")
    public Result<String> interfaceQrcode(@PathVariable("label") String label, HttpServletRequest request) {
	Result<String> ajaxResult = Result.failed("创建二维码失败");
	Map<String, Object> po = neo4jService.getAttMapBy(LABEL, label, META_DATA);

	String qrSuffix = request.getParameter("qrSuffix");
	if(qrSuffix==null) {
	    qrSuffix="png";
	}
	String qrcode = System.currentTimeMillis() + "." + qrSuffix;
	String path = neo4jUService.getPathBy(FILE_STORE_PATH) + qrcode;
	try {
	    String requestURI = request.getRequestURI();
	    String requesti = request.getRequestURL().toString();
	    String interfaceUrl = "http://" + IpAddressUtil.getIp() + ":" + request.getServerPort()
		    + LemodoApplication.MODULE_NAME+"/interface/" + label;
	    genQrCodeFile(path, interfaceUrl);
	} catch (Exception e) {
	    LoggerTool.error(logger,e.getMessage(), e);
	    ajaxResult = Result.failed("创建二维码失败" + e.getMessage());
	}
	return ajaxResult;
    }
    /**
     * 展现管理二维码
     * @param label
     * @param request
     * @return
     */
    @ResponseBody
    @PostMapping("/mManage/{label}")
    public Result<String> mManage(@PathVariable("label") String label, HttpServletRequest request) {
	Result<String> ajaxResult = Result.failed("创建二维码失败");
	Map<String, Object> po = neo4jService.getAttMapBy(LABEL, label, META_DATA);

	String qrSuffix = request.getParameter("qrSuffix");
	String qrcode = System.currentTimeMillis() + ".png";
	String path = neo4jUService.getPathBy(FILE_STORE_PATH) + qrcode;
	try {
	    String requestURI = request.getRequestURI();
	    String requesti = request.getRequestURL().toString();
	    String interfaceUrl = "https://" + IpAddressUtil.getIp() + ":" + request.getServerPort()
		    + LemodoApplication.MODULE_NAME+"/po/" + label;
	    ajaxResult =  genQrCodeFile(path, interfaceUrl);
	} catch (Exception e) {
	    LoggerTool.error(logger,e.getMessage(), e);
	    ajaxResult = Result.failed("创建二维码失败" + e.getMessage());
	}
	return ajaxResult;
    }
    
    @ResponseBody
    @PostMapping("/share")
    public Result<String> share(HttpServletRequest request,@RequestBody JSONObject vo) {
	Result<String> ajaxResult = Result.failed("创建二维码失败");
	String qrcode = System.currentTimeMillis() + ".png";
	String path = neo4jUService.getPathBy(FILE_STORE_PATH) + qrcode;
	try {
	    ajaxResult =  genQrCodeFile(path, url(vo));
	} catch (Exception e) {
	    LoggerTool.error(logger,e.getMessage(), e);
	    ajaxResult = Result.failed("创建二维码失败" + e.getMessage());
	}
	return ajaxResult;
    }
    
    @ResponseBody
    @PostMapping("/mobile")
    public Result<String> mobileCode(HttpServletRequest request) {
	Result<String> ajaxResult = Result.failed("创建二维码失败");
	
	String qrcode =  LOGIN_IMG + ".png";
	String path = neo4jUService.getPathBy(FILE_STORE_PATH) + qrcode;
	try {
	    String interfaceUrl = "https://" + IpAddressUtil.getIp() + ":" + request.getServerPort()
	    + "/static/index.html";
//	    String interfaceUrl = "http://" + IpAddressUtil.getIp() + ":" + request.getServerPort()
//		    + "/static/index.html";
	    ajaxResult= genQrCodeFile(path, interfaceUrl);
	} catch (Exception e) {
	    LoggerTool.error(logger,e.getMessage(), e);
	    ajaxResult = Result.failed("创建二维码失败" + e.getMessage());
	}
	return ajaxResult;
    }
    
    @ResponseBody
    @PostMapping("/file")
    public Result<String> file(HttpServletRequest request) {
	Result<String> ajaxResult = Result.failed("创建二维码失败");
	
	String qrcode =  LOGIN_IMG + ".png";
	String path = neo4jUService.getPathBy(FILE_STORE_PATH) + qrcode;
	try {
	    String interfaceUrl = "https://" + IpAddressUtil.getIp() + ":" + request.getServerPort()
	    + "/static/index.html";
//	    String interfaceUrl = "http://" + IpAddressUtil.getIp() + ":" + request.getServerPort()
//		    + "/static/index.html";
	    ajaxResult= genQrCodeFile(path, interfaceUrl);
	} catch (Exception e) {
	    LoggerTool.error(logger,e.getMessage(), e);
	    ajaxResult = Result.failed("创建二维码失败" + e.getMessage());
	}
	return ajaxResult;
    }

    private Result<String> genQrCodeFile(String path, String content) throws Exception {
	File file = QRCodeUtil.encode(content, path);
	Map<String, Object> fileMap = new HashMap<>();
	String pathname = neo4jUService.fileSave(file, fileMap);
	File dest = new File(pathname);
	if (!dest.exists()) {
	dest.mkdirs();
	}
	neo4jUService.update(fileMap);
	return Result.success(MapTool.string(fileMap, ID));
    }

    /**
     * 创建二维码
     * 
     * @return
     */
    @ResponseBody
    @PostMapping("/label/{label}")
    public Result metaQrcode(@PathVariable("label") String label, HttpServletRequest request) {
	Map<String, Object> po = neo4jService.getAttMapBy(LABEL, label, META_DATA);

	Result ajaxResult = Result.failed("创建二维码失败");
	String qrData = JSON.toJSONString(po);
	String qrcode = System.currentTimeMillis() + ".png";
	String path = neo4jUService.getPathBy(FILE_STORE_PATH) + qrcode;
	try {
	    ajaxResult= genQrCodeFile(path, qrData);
	} catch (Exception e) {
	    LoggerTool.error(logger,e.getMessage(), e);
	    ajaxResult = Result.failed("创建二维码失败" + e.getMessage());
	}
	return ajaxResult;
    }

    /**
     * 创建二维码
     * 
     * @return
     */
    @ResponseBody
    @PostMapping("/crud/{label}")
    public Result metaOperateQrcode(@PathVariable("label") String label, HttpServletRequest request) {
	Map<String, Object> po = neo4jService.getAttMapBy(LABEL, label, META_DATA);
	// 获取接口数据
	Result ajaxResult = Result.failed("创建二维码失败");
	String qrData = JSON.toJSONString(po);
	String qrcode = System.currentTimeMillis() + ".png";
	String path = neo4jUService.getPathBy(FILE_STORE_PATH) + File.separator + qrcode;
	try {
	    ajaxResult= genQrCodeFile(path, qrData);
	} catch (Exception e) {
	    LoggerTool.error(logger,e.getMessage(), e);
	    ajaxResult = Result.failed("创建二维码失败" + e.getMessage());
	}
	return ajaxResult;
    }

    @ResponseBody
    @PostMapping("/id/{id}")
    public Result nodeDataQrcode(@PathVariable("id") String id, HttpServletRequest request) {
	Map<String, Object> po = neo4jService.getNodeMapById(Long.valueOf(id));

	Result ajaxResult = Result.failed("创建二维码失败");
	String qrData = JSON.toJSONString(po);
	String qrcode = System.currentTimeMillis() + ".png";
	String path = neo4jUService.getPathBy(FILE_STORE_PATH) + qrcode;
	try {
	    QRCodeUtil.encode(qrData, path);
	    ajaxResult = Result.success(path);
	} catch (Exception e) {
	    LoggerTool.error(logger,e.getMessage(), e);
	    ajaxResult = Result.failed("创建二维码失败" + e.getMessage());
	}
	return ajaxResult;
    }

    /**
     * 解析二维码
     * 
     * @return
     */
    @ResponseBody
    @PostMapping("/parse")
    public Result decoderQrcode(HttpServletRequest request) {
	Result ajaxResult = Result.failed();
	String qrcode = request.getParameter("qrcode");

	String qrData;
	try {
	    qrData = QRCodeUtil.decode(neo4jUService.getPathBy(FILE_STORE_PATH) + qrcode);
	    if (qrData != null && !"".equals(qrData)) {
		return Result.success(qrData);
	    }
	} catch (Exception e) {
	    LoggerTool.error(logger,e.getMessage(), e);
	    ajaxResult.setMsg(e.getMessage());
	}
	return ajaxResult;
    }

}
