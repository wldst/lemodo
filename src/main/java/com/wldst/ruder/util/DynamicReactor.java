package com.wldst.ruder.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
/** 
 * DynamicReactor 一个动态编译模块，负责编译源文件，复制到对应包下及加载类等过程(JDK 1.7) 
 * @author 三向板砖 
 * */
public class DynamicReactor {
	JavaCompiler compiler;
	Pattern packagePattern;
	static final String regEx = "(?<=package\\s).*(?=;)";
	public DynamicReactor() 
	  {
		compiler = ToolProvider.getSystemJavaCompiler();
		packagePattern = Pattern.compile(regEx);
	}
	/** 
   * 动态编译给定源文件 
   * @param srcPath 源文件路径 
   * @return Class 
   *   <br>若成功返回对应类的Class实例 
   *   <br>若失败返回null 
   * */
	public Class<?> dynamicCompile(String srcPath) 
	  {
		Class<?> result = null;
		//获得给定路径源文件的 
		String packName = getPackage(srcPath);
		if(packName == null) 
		    {
			System.out.println("DynamicRector:Load packageName Error!");
			return null;
		}
		//调用compiler编译指定源文件 
		int res = compiler.run(null, null, null,srcPath);
		if(res != 0) 
		    {
			System.out.println("DynamicRector:Compile Java Source Error!");
			return null;
		}
		//获得包名对应的路径，若路径不存在则创建，若指定class文件存在则覆盖 
		String packageDst = changePacketToDic(packName);
		File dstDir = new File(packageDst);
		if(!dstDir.exists()) 
		    {
			dstDir.mkdirs();
		}
		Path pathFrom = Paths.get(srcPath.split("\\.java")[0] + ".class");
		Path pathTo = Paths.get(packageDst,pathFrom.getFileName().toString());
		try {
			Files.move(pathFrom, pathTo, StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException e) {
			System.out.println("DynamicRector:Move File Fail!");
			e.printStackTrace();
		}
		try {
			result = Class.forName(packName+"."+pathFrom.getFileName().toString().split("\\.class")[0]);
		}
		catch (ClassNotFoundException e) {
			System.out.println("DynamicRector:Class Not found in Final!");
		}
		return result;
	}
	//该方法将一个合法包名转化为对应路径 
	private String changePacketToDic(String packageName) 
	  {
		String[] dirs = packageName.split("\\.");
		String res = ".\\bin";
		for (int i = 0;i < dirs.length;i++) 
		    {
			res += "\\"+dirs[i];
		}
		return res;
	}
	//该方法从给定的路径源文件中获得包名 
	private String getPackage(String srcPath) 
	  {
		String result = null;
	try (BufferedReader br = new BufferedReader(new FileReader(srcPath));) {
			String data = br.readLine();
			while(data != null) 
			      {
				if(data.indexOf("package") != -1) 
				        {
					Matcher m = packagePattern.matcher(data);
					if(m.find()) 
					          {
						result = m.group();
					}
					break;
				}
				data = br.readLine();
			}
			br.close();
		}
		catch (IOException e) {
			System.out.println("DynamicRector:Error in open file "+srcPath);
		}
		return result;
	}
}