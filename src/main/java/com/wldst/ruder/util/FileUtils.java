package com.wldst.ruder.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.servlet.http.HttpServletResponse;

import com.wldst.ruder.domain.Constants;

public class FileUtils {

    public final static Map<String, String> FILE_TYPE_MAP = new HashMap<String, String>(); // 文件上传格式

    static {
	FILE_TYPE_MAP.put("jpg", "ffd8ff"); // JPEG (jpg)
	FILE_TYPE_MAP.put("png", "89504e47"); // PNG (png)
	FILE_TYPE_MAP.put("xml", "3c3f786d6c"); // XML
	FILE_TYPE_MAP.put("bmp", "424d"); // Windows Bitmap (bmp)
	FILE_TYPE_MAP.put("zip", "504b0304140000000800"); // ZIP
	FILE_TYPE_MAP.put("rar", "52617221"); // RAR
	FILE_TYPE_MAP.put("xls", "d0cf11e0"); // MS Excel 注意：word 和 excel的文件头一样
	FILE_TYPE_MAP.put("doc", "d0cf11e0"); // MS Word
	FILE_TYPE_MAP.put("docx", "504b0304"); // MS Excel
	FILE_TYPE_MAP.put("xlsx", "504b0304"); // MS Word
	FILE_TYPE_MAP.put("pdf", "255044462d312e"); // Adobe Acrobat (pdf)
	FILE_TYPE_MAP.put("eps", "252150532d41646f6265");
	FILE_TYPE_MAP.put("ps", "252150532d41646f6265");
	FILE_TYPE_MAP.put("ceb", "466f756e646572204345420000000300"); // 国网CEB类型
    }

    /**
     * 判断文件头信息是否正确
     */
    public static boolean checkFileType(InputStream is, String fileTypeParam) {
	// 根据文件头获取文件类型
	String fileType = getFileTypeByFile(is);
	if ("xlsx".equals(fileType) || "docx".equals(fileType)) {
	    if ("xlsx".equals(fileTypeParam) || "docx".equals(fileTypeParam)) {
		return true;
	    }
	} else if ("xls".equals(fileType) || "doc".equals(fileType)) {
	    if ("xls".equals(fileTypeParam) || "doc".equals(fileTypeParam)) {
		return true;
	    }
	} else if ("jpg".equals(fileType)) {
	    if ("jpg".equals(fileTypeParam)) {
		return true;
	    }
	} else if ("zip".equals(fileType)) {
	    if ("zip".equals(fileTypeParam)) {
		return true;
	    }
	} else if ("rar".equals(fileType)) {
	    if ("rar".equals(fileTypeParam)) {
		return true;
	    }
	} else if ("pdf".equals(fileType)) {
	    if ("pdf".equals(fileTypeParam)) {
		return true;
	    }
	} else if (fileTypeParam.equals(fileType)) {
	    return true;
	}
	return false;
    }

    /**
     * 获取文件类型,包括图片,若格式不是已配置的,则返回null
     */
    public final static String getFileTypeByFile(InputStream is) {
	String result = null;
	byte[] b = new byte[50];
	try {
	    is.read(b, 0, b.length);
	    result = getFileTypeByStream(b);
	    is.close();
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return result;
    }

    /**
     * 获取文件格式
     */
    public final static String getFileTypeByStream(byte[] b) {
	String filetypeHex = String.valueOf(getFileHexString(b));
	Iterator<Entry<String, String>> entryiterator = FILE_TYPE_MAP.entrySet().iterator();
	while (entryiterator.hasNext()) {
	    Entry<String, String> entry = entryiterator.next();
	    String fileTypeHexValue = entry.getValue();
	    if (filetypeHex.startsWith(fileTypeHexValue)) {
		return entry.getKey();
	    }
	}
	return null;
    }

    /**
     * 获取文件头信息
     */
    public final static String getFileHexString(byte[] b) {
	StringBuilder stringBuilder = new StringBuilder();
	if (b == null || b.length <= 0) {
	    return null;
	}
	for (int i = 0; i < b.length; i++) {
	    int v = b[i] & 0xFF;
	    String hv = Integer.toHexString(v);
	    if (hv.length() < 2) {
		stringBuilder.append(0);
	    }
	    stringBuilder.append(hv);
	}
	return stringBuilder.toString();
    }

    /**
     * 下载文件
     * 
     * @param filePath 要下载的文件对象
     * @throws ServiceException
     */
    public static void download(String filePath, HttpServletResponse response) throws ServiceException {
	InputStream inStream = null;
	OutputStream outStream = null;
	try {
	    inStream = new FileInputStream(filePath);
	    outStream = response.getOutputStream();
	    response.reset();
	    response.setCharacterEncoding(Constants.SYSTEM_CHARSET);
	    response.setContentType("application/octet-stream");
	    response.addHeader("Content-Disposition", "attachment;filename="
		    + URLEncoder.encode(CommonUtil.getFileName(filePath), Constants.SYSTEM_CHARSET));
	    response.addHeader("Content-length", CommonUtil.getExactFileSize(filePath) + "");
	    byte[] b = new byte[1024];
	    int len;
	    while ((len = inStream.read(b)) > 0) {
		outStream.write(b, 0, len);
	    }
	} catch (FileNotFoundException e) {
	    throw new ServiceException("下载文件失败!");
	} catch (IOException e) {
	    throw new ServiceException("处理文件流异常!");
	} finally {
	    try {
		CommonUtil.closeIO(outStream, inStream);
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
    }

    /**
     * 查看文件
     * 
     * @throws ServiceException
     * @throws IOException
     */
    public static void showFile(String filePath, HttpServletResponse response) throws ServiceException, IOException {
	FileInputStream in = null;
	OutputStream out = null;
	BufferedInputStream bis = null;
	try {
	    in = new FileInputStream(new File(filePath));
	    out = response.getOutputStream();
	    String fileName = CommonUtil.getFileName(filePath);
	    fileTypeToHeader(response, fileName);

	    bis = new BufferedInputStream(in);
	    byte[] b = new byte[1024];
	    int len;
	    while ((len = bis.read(b)) > 0) {
		out.write(b, 0, len);
	    }
	    out.flush();
	} catch (FileNotFoundException e) {
	    throw new ServiceException("查看文件失败!");
	} catch (IOException e) {
	    throw new ServiceException("处理文件流异常!");
	} finally {
	    CommonUtil.closeIO(out, in);
	}
    }
    
    public static void showFile(String file,String fileName, HttpServletResponse response) throws ServiceException, IOException {
	FileInputStream in = null;
	OutputStream out = null;
	BufferedInputStream bis = null;
	try {
	    in = new FileInputStream(new File(file));
	    out = response.getOutputStream();
//	    String fileName = CommonUtil.getFileName(filePath);
	    fileTypeToHeader(response, fileName);

	    bis = new BufferedInputStream(in);
	    byte[] b = new byte[1024];
	    int len;
	    while ((len = bis.read(b)) > 0) {
		out.write(b, 0, len);
	    }
	    out.flush();
	} catch (FileNotFoundException e) {
	    throw new ServiceException("查看文件"+fileName+"("+file+")失败!");
	} catch (IOException e) {
	    throw new ServiceException("处理文件流异常!");
	} finally {
	    CommonUtil.closeIO(out, in);
	}
    }

    public static void crossNetFile(String filePath, byte[] content, HttpServletResponse response)
	    throws ServiceException, IOException {
	OutputStream out = null;
	BufferedInputStream bis = null;
	try {
	    out = response.getOutputStream();
	    String fileName = CommonUtil.getFileName(filePath);
	    fileTypeToHeader(response, fileName);
	    out.write(content);
	    out.flush();
	} catch (FileNotFoundException e) {
	    throw new ServiceException("查看PDF文件失败!");
	} catch (IOException e) {
	    throw new ServiceException("处理文件流异常!");
	} finally {
	    CommonUtil.closeIO(out);
	}
    }

    /**
     * 封装文件类型参数到HTTP的header里面去。
     * 
     * @param response
     * @param fileName
     * @throws UnsupportedEncodingException
     */
    public static void fileTypeToHeader(HttpServletResponse response, String fileName)
	    throws UnsupportedEncodingException {
	String lowerCase = fileName.toLowerCase();
	if (lowerCase.endsWith("jpg")) {
	    response.setHeader("Content-Type", "image/jpeg");
	    response.setHeader("Content-Disposition",
		    "fileName=" + URLEncoder.encode(fileName, Constants.SYSTEM_CHARSET));
	} else if (lowerCase.endsWith("png")) {
	    response.setHeader("Content-Type", "image/png");
	    response.setHeader("Content-Disposition",
		    "fileName=" + URLEncoder.encode(fileName, Constants.SYSTEM_CHARSET));
	} else if (lowerCase.endsWith("pdf")) {
	    response.setHeader("Content-Type", "application/pdf");
	    response.setHeader("Content-Disposition",
		    "fileName=" + URLEncoder.encode(fileName, Constants.SYSTEM_CHARSET));
	}  else if (lowerCase.endsWith("svg")) {
	    response.setHeader("Content-Type", "image/svg+xml");
	    response.setHeader("Content-Disposition",
		    "fileName=" + URLEncoder.encode(fileName, Constants.SYSTEM_CHARSET));
	} else {
	    response.setContentType("application/vnd.ms-excel;charset=UTF-8");
	    response.setCharacterEncoding(Constants.SYSTEM_CHARSET);
	    response.setHeader("Content-Disposition",
		    "attachment;fileName=" + URLEncoder.encode(fileName, Constants.SYSTEM_CHARSET));
	}
    }
    
    /**
     * 下载文件
     * 
     * @param filePath 要下载的文件对象
     * @throws ServiceException
     */
    public static void write(String filePath, String content) throws ServiceException {
	OutputStream outStream = null;
	try {
	    outStream = new FileOutputStream(filePath);
	    outStream.write(content.getBytes(), 0, content.length());

	} catch (FileNotFoundException e) {
	    throw new ServiceException("下载文件失败!");
	} catch (IOException e) {
	    throw new ServiceException("处理文件流异常!");
	} finally {
	    try {
		CommonUtil.closeIO(outStream);
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
    }
    
    
    private File dirSearch(String filepath,String ends,String postName) {
	String fileName;
	File dir = new File(filepath);

	for (File fi : dir.listFiles()) {
	    if (fi.isDirectory()) {
		File dirWordHtml = dirSearch(fi.getAbsolutePath(),ends,postName);
		if(dirWordHtml!=null) {
		    return dirWordHtml;
		}
	    } else {
		fileName = fi.getName();
		 if(fileName.endsWith(ends)) {
		     if(postName==null||"".equals(postName)) {
			 return fi;
		     }else {
			 if(fileName.contains(postName)||fi.getPath().contains(postName)) {
				 return fi;
			     }
		     }
		 }
	    }
	}
	return null;
    }
}