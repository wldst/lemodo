package com.wldst.ruder.domain;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.wldst.ruder.LemodoApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wldst.ruder.util.CommonUtil;
import com.wldst.ruder.util.FileUtils;
import com.wldst.ruder.util.ServiceException;

import jakarta.servlet.http.HttpServletResponse;
import com.wldst.ruder.LemodoApplication;

/**
 * 文件相关的常量
 * 
 * @author wldst
 *
 */
public class FileDomain extends UserSpaceDomain {
    final static Logger log = LoggerFactory.getLogger(FileDomain.class);
    
    public static final String FILE = "File";
    public static final String FILE_SHOW_URL = LemodoApplication.MODULE_NAME+"/file/show/";
    public static final String FILE_DOWN_URL = LemodoApplication.MODULE_NAME+"/file/download/";
    public static final String FILE_PATH = "path";
    public static final String FILE_STORE_NAME = "fileStoreName";
    public static final String FILE_SIZE = "FileSize";
    public static final String FILE_TYPE = "FileType";
    public static final String FILE_STORE_PATH = "file_store_path";
    public static final String UI_PUBLISH_PATH = "ui_publish_path";

    public static final String FILE_TYPE_JSON = "json";
    public static final String FILE_TYPE_ZIP = "zip";
    public static final String FILE_TYPE_RAR = "RAR";
    public static final String FILE_TYPE_XLS = ".xls";
    public static final String FILE_TYPE_XLSX = ".xlsx";

    public static final String FILE_PORT = "filePort";

    protected void writeImage(HttpServletResponse response, String fileName, String pathname)
	    throws UnsupportedEncodingException {
		File file = new File(pathname);
		if (file.exists()) { // 判断文件父目录是否存在
			response.setContentType("application/vnd.ms-excel;charset=UTF-8");
			response.setCharacterEncoding("UTF-8");
			// response.setContentType("application/force-download");

			response.setHeader("Content-Disposition",
				"attachment;fileName=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
			byte[] buffer = new byte[1024];

			try (OutputStream os = response.getOutputStream();
				FileInputStream fis = new FileInputStream(file);
				BufferedInputStream bis = new BufferedInputStream(fis);) {
			int i = bis.read(buffer);
			while (i != -1) {
				os.write(buffer);
				i = bis.read(buffer);
			}

			} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		}
    }
    
    public static void download(String filePath, String fName, HttpServletResponse response) throws ServiceException {
   	InputStream inStream = null;
   	OutputStream outStream = null;
   	try {
   	    inStream = new FileInputStream(filePath);
   	    outStream = response.getOutputStream();
   	    response.reset();
   	    response.setCharacterEncoding("UTF-8");
   	    if (fName == null) {
   		response.setContentType("application/octet-stream");
   		response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fName, "UTF-8"));
   	    }
   	    else {
   		FileUtils.fileTypeToHeader(response, fName);
   	    }
   	    

   	    String inputStreamToString = inputStreamToString(inStream);
   	    if (inputStreamToString.startsWith("data:image")) {

   		String content = inputStreamToString.split(",")[1].trim();

   		// response.setContentType("image/*");
   		// response.setHeader("Content-Type", "image/jpeg");
   		// response.setHeader("Content-Disposition",
   		// "fileName=" + URLEncoder.encode(fName, Constants.SYSTEM_CHARSET));

   		byte[] b = Base64.getDecoder().decode(content);
   		response.addHeader("Content-length", b.length + "");
   		log.info("b===" + b.length);
   		outStream.write(b, 0, b.length);
   	    } else {
   		Long exactFileSize = CommonUtil.getExactFileSize(filePath);
   		response.addHeader("Content-length", exactFileSize + "");
   		byte[] b = new byte[1024];
   		int len;
   		while ((len = inStream.read(b)) > 0) {
   		    // log.info("writ size" + len);
   		    if (len > 0) {
   			outStream.write(b, 0, len);
   		    }
   		}
   	    }

   	} catch (FileNotFoundException e) {
   	    log.error(e.getMessage(), e);
   	    throw new ServiceException("下载文件失败!");
   	} catch (IOException e) {
   	    log.error(e.getMessage(), e);
   	    throw new ServiceException("处理文件流异常!");
   	} finally {
   	    try {
   		if (inStream != null) {
   		    inStream.close();
   		    inStream = null;
   		}
   		if (outStream != null) {
   		    outStream.close();
   		    outStream = null;
   		}
   	    } catch (IOException e) {
   		// TODO Auto-generated catch block
   		log.error(e.getMessage(), e);
   		e.printStackTrace();
   	    }
   	}
       }
    public static String inputStreamToString(InputStream inputStream) {
	StringBuilder stringBuilder = new StringBuilder();
	try (BufferedReader bufferedReader = new BufferedReader(
		new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
	    String line;
	    while ((line = bufferedReader.readLine()) != null) {
		stringBuilder.append(line).append(System.lineSeparator());
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return stringBuilder.toString();
    }

    public static boolean isFileExist(String string) {
	File file2 = new File(string);
	return string != null && file2.exists() && !file2.isDirectory();
    }
    
    public static boolean delete(String string) {
	File file2 = new File(string);
	if(file2.exists()) {
	    deleteFile(file2);
	}
	
	return string != null && file2.exists() && !file2.isDirectory();
    }

    public static void deleteFile(File file2) {
	if(file2.isFile()) {
	    file2.delete();
	}else {
	    File[] listFiles = file2.listFiles();
	    if(listFiles!=null) {
		for(File fi:listFiles) {
			deleteFile(fi);
		    }
	    }
	    
	    file2.delete();
	}
    }
    
    public static String tempFile() {
	String property = "java.io.tmpdir";
	// 获取临时目录并打印。
	String tempDir = System.getProperty(property);
	return tempDir;
    }
    
    
    
    public static void unzip(final String ouputfile, final String source)
    {
	File zf = new File(ouputfile);
	if(!zf.exists()) {
	    zf.mkdirs();
	    zf.mkdir();
	}
	unzipFile(ouputfile,new File(source));
    }
    
    private static void unzipFile(final String ouputfile, final File zf)
    {
        File file = null;
        try
        {
            ZipFile zipFile = new ZipFile(zf.toString());
            Enumeration<?> entries = zipFile.entries();
            while (entries.hasMoreElements())
            {
                ZipEntry entry = (ZipEntry)entries.nextElement();

                String filename = entry.getName();

                //目录，产生后返回。
                if (entry.isDirectory())
                {
                    file = new File(ouputfile + File.separator +filename);
                    file.mkdirs();
                    continue;
                }
                else if (filename.indexOf('/') >= 0)
                {
                    String subdir = filename.substring(0, filename.lastIndexOf('/'));
                    file = new File(ouputfile + File.separator +subdir);
                    file.mkdirs();
                }
                file = new File(ouputfile + File.separator +filename);
                file.createNewFile();

                InputStream      is  = zipFile.getInputStream(entry);
                FileOutputStream fos = new FileOutputStream(file);
                int count = 0;
                byte buf[] = new byte[4096];
                while ((count = is.read(buf)) > 0)
                { 
                    fos.write(buf, 0, count);
                }
                fos.close();
                is.close();
            }
            zipFile.close();
        }
        catch (Exception e)
        {
            System.out.println(ouputfile+", "+zf+", "+file);
            e.printStackTrace();
        }
    }
    
}
