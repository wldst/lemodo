package com.wldst.ruder.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.springframework.stereotype.Component;

@Component
public class RunCode {
    private String classPath = "D:\\backup\\";

    public static void main(String[] args) throws Exception {
	// Java 源代码
	String sourceCode = "public class Hello{\n public String sayHello(String name){"
		+ "return \"Hello,\"+name+\"!\";}}";
	// 类名及文件名
	String className = "Hello";
	// 方法名
	String methodName = "sayHello";
	RunCode rCode = new RunCode();

	String callClassMethod = rCode.callClassMethod(sourceCode, className, methodName, "test");
	System.out.println(callClassMethod);
    }

    public String callClassMethod(String sourceCodeStr, String className, String methodName, Object... args)
	    throws MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException,
	    NoSuchMethodException, InvocationTargetException {
	Class c = null;
	String returnString = "";
	URL[] urls = new URL[] { new URL("file:/" + classPath) };

	try (URLClassLoader loader = new URLClassLoader(urls);) {
	    c = loader.loadClass(className);
	} catch (Exception e) {
	    // TODO: handle exception
	    // 当前编译器
	    JavaCompiler complier = ToolProvider.getSystemJavaCompiler();
	    // Java标准文件管理器
	    StandardJavaFileManager fm = complier.getStandardFileManager(null, null, null);
	    // Java 文件对象
	    JavaFileObject jfo = new StringJavaObject(className, sourceCodeStr);
	    // 编译参数，类似于Javac <options> 中的options
	    List<String> optionsList = new ArrayList<String>();
	    // 编译文件的存放地方，注意：此处是为Eclipse 工具特设的

	    optionsList.addAll(Arrays.asList("-d", classPath));
	    // 要编译的单元
	    List<JavaFileObject> jfos = Arrays.asList(jfo);
	    // 设置编译环境
	    JavaCompiler.CompilationTask task = complier.getTask(null, fm, null, optionsList, null, jfos);
	    try (URLClassLoader loader2 = new URLClassLoader(urls);) {
		// 编译成功
		if (task.call()) {
		    // 生成对象
		    // Object obj = Class.forName(clsName).newInstance();
		    c = loader2.loadClass(className);
		    Object obj = c.newInstance();
		    // 调用sayHello方法
		    Method m = c.getMethod(methodName, String.class);
		    returnString = (String) m.invoke(obj, args);
		}
		return returnString;
	    } catch (Exception ex) {
		return ex.getMessage();
	    }
	}
	if (c != null) {
	    Object obj = c.newInstance();
	    // 调用sayHello方法
	    if(args==null||args.length<1) {
		 Method m = c.getMethod(methodName);
		    returnString = (String) m.invoke(obj);
	    }else {
		 Method m = c.getMethod(methodName, String.class);
		    returnString = (String) m.invoke(obj, args);
	    }
	   
	}

	return returnString;
    }

    public String getClassPath() {
	return classPath;
    }

    public void setClassPath(String classPath) {
	this.classPath = classPath;
    }

}

// 文本中的Java对象
class StringJavaObject extends SimpleJavaFileObject {
    // 源代码
    private String content = "";

    // 遵循Java规范的类名及文件
    public StringJavaObject(String _javaFileName, String _content) {
	super(_createStringJavaObjectUri(_javaFileName), Kind.SOURCE);
	content = _content;
    }

    // 产生一个URL资源库
    private static URI _createStringJavaObjectUri(String name) {
	// 注意此处没有设置包名
	return URI.create("String:///" + name + Kind.SOURCE.extension);
    }

    // 文本文件代码
    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
	// TODO Auto-generated method stub
	return content;
    }

    public String getContent() {
	return content;
    }

    public void setContent(String content) {
	this.content = content;
    }

}
