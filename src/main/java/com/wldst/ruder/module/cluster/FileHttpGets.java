package com.wldst.ruder.module.cluster;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.wldst.ruder.util.LoggerTool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.impl.DefaultBHttpClientConnection;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;

import com.alibaba.fastjson2.JSONObject;
import com.qliu6.FileOperate;
import com.qliu6.OSUtil;
import com.qliu6.Q6Properties;
import com.qliu6.SystemProperties;
import com.wldst.ruder.constant.UpdaterConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Elemental example for executing multiple GET requests sequentially.
 */
public class FileHttpGets {
	final static Logger logger = LoggerFactory.getLogger(FileHttpGets.class);

	public static void main(String[] args) throws Exception {
		String[] targets = { "/fileserver.jar", "/nohup.out", "/file/Http.zip" };
		getFileFromCenterServer(targets);
	}

	public static void getFileFromCenterServer(String[] targets)
			throws UnknownHostException, IOException, HttpException {
		HttpProcessor httpproc = HttpProcessorBuilder.create()
				.add(new RequestContent()).add(new RequestTargetHost())
				.add(new RequestConnControl())
				.add(new RequestUserAgent("Test/1.1"))
				.add(new RequestExpectContinue(true)).build();

		HttpRequestExecutor httpexecutor = new HttpRequestExecutor();

		HttpCoreContext coreContext = HttpCoreContext.create();

		HttpHost host = new HttpHost(Q6Properties.getInstance().getUpdateProp(
				UpdaterConstants.CENTER_FILE_SERVER), 8796);
		coreContext.setTargetHost(host);

		DefaultBHttpClientConnection conn = new DefaultBHttpClientConnection(
				8 * 1024);
		ConnectionReuseStrategy connStrategy = DefaultConnectionReuseStrategy.INSTANCE;

		try {
			for (int i = 0; i < targets.length; i++) {
				if (!conn.isOpen()) {
					Socket socket = new Socket(host.getHostName(),
							host.getPort());
					conn.bind(socket);
				}
				BasicHttpRequest request = new BasicHttpRequest("GET",
						targets[i]);
				System.out.println(">> Request URI: "
						+ request.getRequestLine().getUri());

				httpexecutor.preProcess(request, httpproc, coreContext);
				HttpResponse response = httpexecutor.execute(request, conn,
						coreContext);
				httpexecutor.postProcess(response, httpproc, coreContext);

				System.out.println("<< Response: " + response.getStatusLine());
				InputStream is = response.getEntity().getContent();

				String localpath = Q6Properties.getCurrentDir() + "newVersion";
				FileOperate.createFolder(localpath);
				String newPath = localpath + request.getRequestLine().getUri();
				if (newPath.length() != localpath.length() + 1) {
					newPath = OSUtil.dirHandle(newPath);
					FileOperate.copyInputStream2File(newPath, is);
				}

				System.out.println("==============");
				if (!connStrategy.keepAlive(response, coreContext)) {
					conn.close();
				} else {
					System.out.println("Connection kept alive...");
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
	}

	/**
	 * 
	 * @param json 需要包含信息：update.targets，http.local.port，myip
	 * 
	 * @throws HttpException
	 * @throws IOException
	 */
	public static void getFiles2LocalDir(JSONObject json) throws HttpException,
			IOException {
		HttpProcessor httpproc = HttpProcessorBuilder.create()
				.add(new RequestContent()).add(new RequestTargetHost())
				.add(new RequestConnControl())
				.add(new RequestUserAgent("Test/1.1"))
				.add(new RequestExpectContinue(true)).build();

		HttpRequestExecutor httpexecutor = new HttpRequestExecutor();

		HttpCoreContext coreContext = HttpCoreContext.create();

		Integer portInteger = (Integer) json.getInteger(UpdaterConstants.HTTP_LOCAL_PORT);
		String fileserver = json.getString("myip"); // OSUtil.getPingableIp(msgjson);
		HttpHost host = new HttpHost(fileserver, portInteger);
		coreContext.setTargetHost(host);
		DefaultBHttpClientConnection conn = new DefaultBHttpClientConnection(
				8 * 1024);
		ConnectionReuseStrategy connStrategy = DefaultConnectionReuseStrategy.INSTANCE;

		String target = (String) json.get(UpdaterConstants.VERSION_TARGETS);
		try {
			if (target.contains(",")) {
				LoggerTool.info(logger,"target has manyfile");
				String[] targets = target.split(",");
				for (int i = 0; i < targets.length; i++) {
					getFile2CurrentDir(httpproc, httpexecutor, coreContext,
							host, conn, connStrategy, targets[i]);
				}
			} else {
				LoggerTool.info(logger,"target is one file");
				getFile2CurrentDir(httpproc, httpexecutor, coreContext, host,
						conn, connStrategy, target);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
	}

	public static void getFiles2SpecialDir(JSONObject json, String dir)
			throws HttpException, IOException {
		HttpProcessor httpproc = HttpProcessorBuilder.create()
				.add(new RequestContent()).add(new RequestTargetHost())
				.add(new RequestConnControl())
				.add(new RequestUserAgent("Test/1.1"))
				.add(new RequestExpectContinue(true)).build();

		HttpRequestExecutor httpexecutor = new HttpRequestExecutor();

		HttpCoreContext coreContext = HttpCoreContext.create();

		Integer portInteger = (Integer) json.getInteger(UpdaterConstants.HTTP_LOCAL_PORT);
		String fileserver = json.getString("myip"); // OSUtil.getPingableIp(msgjson);
		HttpHost host = new HttpHost(fileserver, portInteger);
		coreContext.setTargetHost(host);
		DefaultBHttpClientConnection conn = new DefaultBHttpClientConnection(
				8 * 1024);
		ConnectionReuseStrategy connStrategy = DefaultConnectionReuseStrategy.INSTANCE;

		String target = (String) json.get(UpdaterConstants.VERSION_TARGETS);
		try {
			if (target.contains(",")) {
				LoggerTool.info(logger,"target has manyfile");
				String[] targets = target.split(",");
				for (int i = 0; i < targets.length; i++) {
					getFile2Dir(httpproc, httpexecutor, coreContext, host,
							conn, connStrategy, targets[i], dir);
				}
			} else {
				LoggerTool.info(logger,"target is one file");
				getFile2Dir(httpproc, httpexecutor, coreContext, host, conn,
						connStrategy, target, dir);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
	}

	private static void getFile2Dir(HttpProcessor httpproc,
			HttpRequestExecutor httpexecutor, HttpCoreContext coreContext,
			HttpHost host, DefaultBHttpClientConnection conn,
			ConnectionReuseStrategy connStrategy, String target, String dir)
			throws UnknownHostException, IOException, HttpException,
			FileNotFoundException {
		if (!conn.isOpen()) {
			Socket socket = new Socket(host.getHostName(), host.getPort());
			conn.bind(socket);
		}
		BasicHttpRequest request = new BasicHttpRequest("GET", target);
		System.out.println(">> Request URI: "
				+ request.getRequestLine().getUri());

		httpexecutor.preProcess(request, httpproc, coreContext);
		HttpResponse response = httpexecutor
				.execute(request, conn, coreContext);
		httpexecutor.postProcess(response, httpproc, coreContext);

		System.out.println("<< Response: " + response.getStatusLine());
		InputStream is = response.getEntity().getContent();

		String newResource = request.getRequestLine().getUri();

		String currentDir = Q6Properties.getCurrentDir();
		String downlaodFile = currentDir
				+ SystemProperties.dirLastSeparetor(dir) + newResource;

		FileOperate.copyInputStream2File(downlaodFile, is);

		System.out.println("==============");
		if (!connStrategy.keepAlive(response, coreContext)) {
			conn.close();
		} else {
			System.out.println("Connection kept alive...");
		}
	}

	private static void getFile2CurrentDir(HttpProcessor httpproc,
			HttpRequestExecutor httpexecutor, HttpCoreContext coreContext,
			HttpHost host, DefaultBHttpClientConnection conn,
			ConnectionReuseStrategy connStrategy, String target)
			throws UnknownHostException, IOException, HttpException,
			FileNotFoundException {
		if (!conn.isOpen()) {
			Socket socket = new Socket(host.getHostName(), host.getPort());
			conn.bind(socket);
		}
		BasicHttpRequest request = new BasicHttpRequest("GET", target);
		System.out.println(">> Request URI: "
				+ request.getRequestLine().getUri());

		httpexecutor.preProcess(request, httpproc, coreContext);
		HttpResponse response = httpexecutor
				.execute(request, conn, coreContext);
		httpexecutor.postProcess(response, httpproc, coreContext);

		System.out.println("<< Response: " + response.getStatusLine());
		InputStream is = response.getEntity().getContent();

		String newResource = request.getRequestLine().getUri();

		String currentDir = Q6Properties.getCurrentDir();
		String downlaodFile = currentDir + newResource;

		FileOperate.copyInputStream2File(downlaodFile, is);

		System.out.println("==============");
		if (!connStrategy.keepAlive(response, coreContext)) {
			conn.close();
		} else {
			System.out.println("Connection kept alive...");
		}
	}

	private static void getFile(HttpProcessor httpproc,
			HttpRequestExecutor httpexecutor, HttpCoreContext coreContext,
			HttpHost host, DefaultBHttpClientConnection conn,
			ConnectionReuseStrategy connStrategy, String target)
			throws UnknownHostException, IOException, HttpException,
			FileNotFoundException {
		if (!conn.isOpen()) {
			Socket socket = new Socket(host.getHostName(), host.getPort());
			conn.bind(socket);
		}
		BasicHttpRequest request = new BasicHttpRequest("GET", target);
		System.out.println(">> Request URI: "
				+ request.getRequestLine().getUri());

		httpexecutor.preProcess(request, httpproc, coreContext);
		HttpResponse response = httpexecutor
				.execute(request, conn, coreContext);
		httpexecutor.postProcess(response, httpproc, coreContext);

		System.out.println("<< Response: " + response.getStatusLine());
		InputStream is = response.getEntity().getContent();

		String newResource = request.getRequestLine().getUri();
		int lastdirSeparator = newResource.lastIndexOf(File.separator);
		if (lastdirSeparator > 0) {
			String resourcePath = newResource.substring(0, lastdirSeparator);
			OSUtil.dirMakeSure(resourcePath, false);
		}
		String currentDir = Q6Properties.getCurrentDir();
		String downlaodFile = currentDir + newResource;

		FileOperate.copyInputStream2File(downlaodFile, is);

		System.out.println("==============");
		if (!connStrategy.keepAlive(response, coreContext)) {
			conn.close();
		} else {
			System.out.println("Connection kept alive...");
		}
	}
}
