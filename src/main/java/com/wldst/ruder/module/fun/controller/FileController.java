package com.wldst.ruder.module.fun.controller;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.api.Result;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.CrudUserNeo4jService;
import com.wldst.ruder.domain.FileDomain;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.bs.ShellOperator;
import com.wldst.ruder.module.ws.web.MessageServer;
import com.wldst.ruder.util.FileUtils;
import com.wldst.ruder.util.LoggerTool;
import com.wldst.ruder.util.MapTool;
import com.wldst.ruder.util.ServiceException;
import com.wldst.ruder.util.ServerSendFile;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 首页
 * 
 * @author wldst
 *
 */
@Controller
@RequestMapping("${server.context}/file")
public class FileController extends FileDomain {
    @Autowired
    private CrudUserNeo4jService neo4jService;
    @Autowired
    private CrudNeo4jService crudService;
    @Value(value = "${file.lib}")
    private String initLib;
    @Autowired
    private UserAdminService adminService;
    @Autowired
    private ShellOperator so;

    final static Logger logger = LoggerFactory.getLogger(FileController.class);
    @RequestMapping(value = "", method = { RequestMethod.GET, RequestMethod.POST })
    public String login(Model model, HttpServletRequest request) throws Exception {
	return "layui/file";
    }
    @RequestMapping(value = "/share", method = { RequestMethod.GET, RequestMethod.POST })
    public String share(Model model, HttpServletRequest request) throws Exception {
	List<String> onlineUser = MessageServer.getOnlineUser();
	model.addAttribute("onlineUser", onlineUser);
	return "layui/fileShare";
    }
    
    @RequestMapping(value = "/doShare", method = { RequestMethod.POST })
    @ResponseBody
    public Result<String> doShare(Model model,@RequestBody JSONObject vo, HttpServletRequest request)
	    throws Exception {
//	vo
	String userrId = string(vo,"userId");
	String fileId = string(vo,"fileId");
	 String[] split = userrId.split(",");
	 for(String si: split) {
	     Map<String, Object> nodeMapById = crudService.getNodeMapById(fileId);
	     MessageServer.sendInfo("<a href='"+LemodoApplication.MODULE_NAME+"/file/show/"+fileId+"'>"+name(nodeMapById)+"</a>", si);	 
	 }
	return Result.failed("分享成功！");
    }

    /**
     * 档案，文件分享
     * 
     * @param model
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/upload", method = { RequestMethod.POST })
    @ResponseBody
    public Result fileUpload(Model model, @RequestParam("file") MultipartFile file, HttpServletRequest request)
	    throws Exception {
	if (file.isEmpty()) {
	    return Result.failed("上传失败，请选择文件");
	}
	Map<String, Object> fileMap = new HashMap<>();

	String pathname = neo4jService.multiPartFileSave(file, fileMap);
	File dest = new File(pathname);
	try {
	    if(!dest.exists()) {
		dest.mkdirs();
	    }
	    file.transferTo(dest);
	    neo4jService.update(fileMap);
	    LoggerTool.info(logger,"上传成功");
	    return Result.success(MapTool.string(fileMap, ID));
	} catch (IOException e) {
	    LoggerTool.error(logger,e.toString(), e);
	}
	return Result.failed("上传失败！");
    }
    
    @RequestMapping(value = "/upload2", method = { RequestMethod.POST })
    @ResponseBody
    public Result fileUpload(Model model, 
	    @RequestBody Map<String, Object> empDocVO,HttpServletRequest request)
	    throws Exception {
	if (empDocVO.get("file")==null) {
	    return Result.failed("上传失败，请选择文件");
	}
	 
	String file = MapTool.string(empDocVO, "file");
	String type = MapTool.string(empDocVO, "type");
	String empId = MapTool.string(empDocVO, "userId");
	LoggerTool.info(logger,"upload EmpDocAttachment String:");
	HttpSession session = request.getSession();
	// 获取当前用户登录名
	String userId = (String) session.getAttribute("UserId");
	Map<String, Object> map = new HashMap<String, Object>();

	String path = request.getSession().getServletContext().getRealPath("/imgfile");
	if (!path.endsWith("/")) {
	    path = path + File.separator;
	}

	try {
	    if (file.isEmpty()) {
		LoggerTool.error(logger,"没有文件上传");
		return Result.fail("没有文件上传");
	    } else {
		String fileStorePath = path + type + File.separator;
		String fileStoreName = fileStorePath + empId;
		File tempfile = new File(fileStorePath);
		if (!tempfile.exists()) {
		    tempfile.mkdir();
		}
		file = file.replaceAll("\r\n", "").replaceAll("\n", "");
		FileUtils.write(fileStoreName, file);
		String start = file.split(";")[0];
		Map<String, Object> fileMap = new HashMap<String, Object>();
		fileMap.put("fileStoreName", fileStoreName);
		String string = start.split("/")[1];
		if (string != null && !"".equals(string)) {
		    fileMap.put("name", empId + type + "." + string);
		    LoggerTool.info(logger,name(fileMap));
		}
		Long fileId = crudService.save(fileMap, "File").getId();
		fileMap.put(ID, fileId);
		map.put("fileUrl", "http://pmis.sgse.cn:10443/mpmis/fileDownload/" + MapTool.id(fileMap));
		return Result.success(map);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    LoggerTool.error(logger,"文件上传失败：" + e.getMessage());
	    // map.put("state", "error");
	    // map.put("details", "上传失败");
	    return Result.failed("上传失败");
	}
    }


	@RequestMapping(value = "/uploadInfo", method = {RequestMethod.POST})
	@ResponseBody
	public Result<Map<String, Object>> uploadInfo(Model model, @RequestParam("file") MultipartFile file, HttpServletRequest request)
			throws Exception{
		if(file.isEmpty()){
			return Result.failed("上传失败，请选择文件");
		}
		Map<String, Object> fileMap=new HashMap<>();

		String pathname=neo4jService.multiPartFileSave(file, fileMap);
		File dest=new File(pathname);
		try{
			if(!dest.exists()){
				dest.mkdirs();
			}
			file.transferTo(dest);
			neo4jService.update(fileMap);
			Map<String, Object> copy=copy(fileMap);
			copy.remove(FILE_STORE_NAME);
			LoggerTool.info(logger, "上传成功");
			return Result.success(copy);
		}catch(IOException e){
			LoggerTool.error(logger, e.toString(), e);
		}
		return Result.failed("上传失败！");
	}
    
    @RequestMapping(value = "/uploadImage", method = { RequestMethod.POST })
    @ResponseBody
    public Result uploadImage(Model model, @RequestParam("file") MultipartFile file, HttpServletRequest request)
	    throws Exception {
	if (file.isEmpty()) {
	    return Result.failed("上传失败，请选择文件");
	}
	Map<String, Object> fileMap = new HashMap<>();
	String fileName = file.getOriginalFilename();
	String pathname = neo4jService.multiPartFileSave(file, fileMap);
	File dest = new File(pathname);
	try {
	    if(!dest.exists()) {
		dest.mkdirs();
	    }
	    file.transferTo(dest);
	    neo4jService.update(fileMap);
	    LoggerTool.info(logger,"上传成功");
	    String string = LemodoApplication.MODULE_NAME+"/file/show/"+id(fileMap);
	    Map<String,String> data = new HashMap<>();
	    data.put("src", string);
	    data.put("title", fileName);
	    return Result.imageSucess(data);
	} catch (IOException e) {
	    LoggerTool.error(logger,e.toString(), e);
	}
	return Result.failed("上传失败！");
    }
    
    @RequestMapping(value = "/aUpload", method = { RequestMethod.POST })
    @ResponseBody
    public Result audioUpload(@RequestParam("audioFile") MultipartFile file)
	    throws Exception {
	if (file.isEmpty()) {
	    return Result.failed("上传失败，请选择文件");
	}
//	Map<String, Object> fileMap = new HashMap<>();

//	String pathname = neo4jService.multiPartFileSave(file, fileMap);
	String filePath = neo4jService.getPathBy(FILE_STORE_PATH);
	String audioFile = filePath+File.separator+"voice"+File.separator+file.getOriginalFilename();
	    
	File dest = new File(audioFile);
	try {
	    if(!dest.exists()) {
		dest.mkdirs();
	    }
	    file.transferTo(dest);
	    String voiceFileToText = so.voiceFileToText(audioFile);
	    String parseAndExcute = so.formatCmd(voiceFileToText);
	    LoggerTool.info(logger,"识别"+adminService.getCurrentPasswordId()+":"+adminService.getCurrentAccount()+"说的话成功："+parseAndExcute);
	    return Result.success(parseAndExcute,"识别结果");
	} catch (IOException e) {
	    LoggerTool.error(logger,e.toString(), e);
	}
	return Result.failed("上传失败！");
    }
    
    @RequestMapping(value = "/vRecognize", method = { RequestMethod.POST })
    @ResponseBody
    public Result voiceRecognition(@RequestParam("audioFile") MultipartFile file)
	    throws Exception {

	if (file.isEmpty()) {
	    return Result.failed("上传失败，请选择文件");
	}
//	Map<String, Object> fileMap = new HashMap<>();

//	String pathname = neo4jService.multiPartFileSave(file, fileMap);
	String filePath = neo4jService.getPathBy(FILE_STORE_PATH);
	String audioFile = filePath+File.separator+"voice"+File.separator+file.getOriginalFilename();
	    
	File dest = new File(audioFile);
	try {
	    if(!dest.exists()) {
		dest.mkdirs();
	    }
	    file.transferTo(dest);
	    String voiceFileToText = so.voiceFileToText(audioFile);
	    Map<String, Object> parseAndExcute2 = so.parseAndExcute(voiceFileToText);
	    String parseAndExcute = string(parseAndExcute2,"msg");
	    LoggerTool.info(logger,"识别"+adminService.getCurrentPasswordId()+":"+adminService.getCurrentAccount()+"说的话成功："+parseAndExcute);
	    return Result.success(parseAndExcute,"记录并执行");
	} catch (IOException e) {
	    LoggerTool.error(logger,e.toString(), e);
	}
	return Result.failed("上传失败！");
    }
    
    @RequestMapping(value = "/uploadDriver", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public Result driverFileUpload(Model model, @RequestParam("file") MultipartFile file, HttpServletRequest request)
	    throws Exception {
	if (file.isEmpty()) {
	    return Result.failed("上传失败，请选择文件");
	}
	Map<String, Object> fileMap = new HashMap<>();
	if(!initLib.endsWith(File.separator)) {
	    initLib=initLib+File.separator;
	}
	File dest = new File(initLib+file.getOriginalFilename());
	try {
	    file.transferTo(dest);
	    neo4jService.update(fileMap);
	    LoggerTool.info(logger,"上传成功");
	    return Result.success(file.getOriginalFilename(),"上传成功");
	} catch (IOException e) {
	    LoggerTool.error(logger,e.toString(), e);
	}
	return Result.failed("上传驱动失败！");
    }
    
    

    @PostMapping("/multiUpload")
    @ResponseBody
    public Result multiUpload(HttpServletRequest request) {
	List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("file");
	for (int i = 0; i < files.size(); i++) {
	    MultipartFile file = files.get(i);
	    if (file.isEmpty()) {
		return Result.failed("上传第" + (i++) + "个文件失败");
	    }

	    Map<String, Object> fileMap = new HashMap<>();

	    String pathname = neo4jService.multiPartFileSave(file, fileMap);
	    File dest = new File(pathname);
	    try {
		file.transferTo(dest);
		neo4jService.update(fileMap);
		// LoggerTool.info(logger,"上传成功");
	    } catch (IOException e) {
		// LoggerTool.error(logger,e.toString(), e);
	    }
	}

	return Result.success("上传成功");

    }

	@RequestMapping(value = "/download/{id}", method = {RequestMethod.GET, RequestMethod.POST})
	@ResponseBody
	public String fileDownload(HttpServletResponse response, Model model, @PathVariable("id") String id,
							   HttpServletRequest request) throws Exception {
		Map<String, Object> nodeMapById = neo4jService.getNodeMapById(Long.valueOf(id));
		if (nodeMapById == null || nodeMapById.isEmpty()) {
			return "";
		}

		Object object = nodeMapById.get(NODE_NAME);
		String fileName = String.valueOf(object);
		String pathCode = neo4jService.getPathBy(FILE_STORE_PATH);
		String pathname = pathCode + MapTool.string(nodeMapById, ID);
		String fileStorePath = MapTool.string(nodeMapById, FILE_STORE_NAME);
		if (isFileExist(fileStorePath)) {
			writeImage(response, fileName, fileStorePath);
		} else {
			writeImage(response, fileName, pathname);
		}

		return null;
	}

	/**
	 * Base64图片文件下载
	 *
	 * @param response
	 * @param model
	 * @param id
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/imageFile/{id}", method = {RequestMethod.GET, RequestMethod.POST})
	@ResponseBody
	public String showImage(HttpServletResponse response, Model model, @PathVariable("id") String id,
							HttpServletRequest request) throws Exception {
		if (id == null || id.isEmpty()) {
			throw new ServiceException("参数有误！id=" + id);
		}
		Map<String, Object> nodeMapById = neo4jService.getNodeMapById(Long.valueOf(id));
		if (nodeMapById == null || nodeMapById.isEmpty()) {
			throw new ServiceException("资源不存在！");
		}

		String fileName = name(nodeMapById);
		String fileStorePath = MapTool.string(nodeMapById, FILE_STORE_NAME);
		LoggerTool.info(logger,"===download==fileName=" + fileName + "=========fileStorePath===" + fileStorePath);
		if (isFileExist(fileStorePath)) {
			try {
				// FileUtils.showFile(fileStorePath, fileName, response);
				// writeImage(response, fileName, fileStorePath);
				download(fileStorePath, fileName, response);
			} catch (Exception ex) {
				LoggerTool.error(logger,ex.getMessage(), ex);
			}
		}
		return null;
	}


	@RequestMapping(value = "/show/{id}", method = {RequestMethod.GET, RequestMethod.POST})
	@ResponseBody
	public String show(HttpServletResponse response, Model model, @PathVariable("id") String id,
					   HttpServletRequest request) throws Exception {
		if (StringUtils.isEmpty(id) || "null".equals(id)) {
			throw new ServiceException("参数有误！id=" + id);
		}
		Map<String, Object> nodeMapById = neo4jService.getNodeMapById(Long.valueOf(id));
		if (nodeMapById == null || nodeMapById.isEmpty()) {
			throw new ServiceException("资源不存在！");
		}

		Object object = nodeMapById.get(NODE_NAME);
		String fileName = String.valueOf(object);
		String pathCode = neo4jService.getPathBy(FILE_STORE_PATH);
		String showFile = pathCode + fileName;

		String fileStorePath = MapTool.string(nodeMapById, FILE_STORE_NAME);
		if (isFileExist(fileStorePath)) {
			showFile = fileStorePath;
		}

		try {
			FileUtils.showFile(showFile, fileName, response);
		} catch (Exception ex) {
			showFile = pathCode + MapTool.longValue(nodeMapById, ID);
			try {
				FileUtils.showFile(showFile, fileName, response);
			} catch (Exception exx) {
				LoggerTool.error(logger,exx.getMessage(), exx);
			}
		}
		return null;
	}


	@RequestMapping(value = "/copy", method = {RequestMethod.POST})
	@ResponseBody
	public String copy(HttpServletResponse response, Model model,@RequestBody JSONObject vo,
					   HttpServletRequest request) throws Exception {
		Long id = id(vo);
		if (id==null) {
			throw new ServiceException("参数有误！id=" + id);
		}
		Map<String, Object> fileData = neo4jService.getNodeMapById(id);
		if (fileData == null || fileData.isEmpty()) {
			throw new ServiceException("资源不存在！");
		}

		Object object = fileData.get(NODE_NAME);
		String fileName = String.valueOf(object);
		String pathCode = neo4jService.getPathBy(FILE_STORE_PATH);


		String fileStorePath = MapTool.string(fileData, FILE_STORE_NAME);
		if (isFileExist(fileStorePath)) {
			String ip =request.getRemoteAddr();

			int port = integer(vo,"port");
			ServerSendFile.sendFile(fileStorePath,ip, port);
		}

		return null;
	}
}
