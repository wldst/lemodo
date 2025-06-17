package com.wldst.ruder.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;

public class ZipFile {

    public void unzip() {
	String name = "hello.zip";
	String path = "d:\\";
	// 创建ZipInputStream对象
	try (ZipInputStream zin = new ZipInputStream(new FileInputStream(name));) { // try语句捕获可能发生的异常
	    // 实例化对象，指明要进行解压的文件
	    ZipEntry entry = zin.getNextEntry(); // 获取下一个ZipEntry
	    while (((entry = zin.getNextEntry()) != null) && !entry.isDirectory()) {
		// 如果entry不为空，并不在同一目录下
		File file = new File(path + entry.getName()); // 获取文件目录
		System.out.println(file);
		if (!file.exists()) { // 如果该文件不存在
		    file.mkdirs();// 创建文件所在文件夹
		    file.createNewFile(); // 创建文件
		}
		zin.closeEntry(); // 关闭当前entry
		System.out.println(entry.getName() + "解压成功");
	    }
	    zin.close(); // 关闭流
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }


    /**
     * zip压缩文 解压
     */
    public static void main(String[] args) throws IOException {
	// 读取压缩文件
	String name = "D:\\backup\\upload.zip";
	String string = "D:\\\\backup\\upload_zip\\";
	// unzip(name, string);
	unzip2JsonArrays(name);
    }


    private static void unzip(String name, String string) throws FileNotFoundException, IOException {
	try (ZipInputStream in = new ZipInputStream(new FileInputStream(name));) {
	    // zip文件实体类
	    ZipEntry entry;
	    // 遍历压缩文件内部 文件数量
	    while ((entry = in.getNextEntry()) != null) {
		if (!entry.isDirectory()) {
		    // 文件输出流
		    try (FileOutputStream out = new FileOutputStream(string + entry.getName());
			    BufferedOutputStream bos = new BufferedOutputStream(out);) {
			int len;
			byte[] buf = new byte[1024];
			while ((len = in.read(buf)) != -1) {
			    bos.write(buf, 0, len);
			}
		    }
		    }
		}
	}
    }

    public static List<JSONArray> unzip2JsonArrays(String fileName) {
	List<JSONArray> goodsData = new ArrayList<>();
	try (ZipInputStream in = new ZipInputStream(new FileInputStream(fileName));) {
	    // zip文件实体类
	    ZipEntry entry;
	    // 遍历压缩文件内部 文件数量
	    while ((entry = in.getNextEntry()) != null) {
		if (!entry.isDirectory()) {
		    StringBuilder stringBuffer = new StringBuilder();
		    int len;
		    byte[] buf = new byte[1024];
		    while ((len = in.read(buf)) != -1) {
			String ssString = new String(buf, 0, len);
			stringBuffer.append(ssString);
		    }
		    JSONArray parseObject = JSON.parseArray(stringBuffer.toString());
		    goodsData.add(parseObject);
		}
		}
	} catch (FileNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return goodsData;
    }


    private static void zipFile1(ZipOutputStream out, String zFile, String entry)
	    throws FileNotFoundException, IOException {
	try (FileInputStream fis1 = new FileInputStream(zFile);) {
	    out.putNextEntry(new ZipEntry(entry));
	    int len1;
	    byte[] buffer1 = new byte[1024];
	    // 读入需要下载的文件的内容，打包到zip文件
	    while ((len1 = fis1.read(buffer1)) > 0) {
		out.write(buffer1, 0, len1);
	    }
	}
    }

    public static void zipFile(ZipOutputStream out, byte[] buffer1, String entry)
	    throws FileNotFoundException, IOException {
	out.putNextEntry(new ZipEntry(entry));
	out.write(buffer1);
    }

}
