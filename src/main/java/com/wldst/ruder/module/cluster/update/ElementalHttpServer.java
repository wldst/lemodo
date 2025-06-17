package com.wldst.ruder.module.cluster.update;

/* ==================================================================== Licensed
 * to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership. The ASF licenses this file to you
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 * ==================================================================== This
 * software consists of voluntary contributions made by many individuals on
 * behalf of the Apache Software Foundation. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>. */

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Locale;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

import com.wldst.ruder.util.LoggerTool;
import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpConnectionFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpServerConnection;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnectionFactory;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.protocol.UriHttpRequestHandlerMapper;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qliu6.Q6Properties;
import com.wldst.ruder.constant.UpdaterConstants;


/**
 * Basic, yet fully functional and spec compliant, HTTP/1.1 file server.
 */
public class ElementalHttpServer implements Runnable {
	private static Logger logger = LoggerFactory
			.getLogger(ElementalHttpServer.class);

	public static void main(String[] args) throws Exception {
		// Document root directory
		new Thread(new ElementalHttpServer()).start();
	}

	public void run() {
		String docRoot = Q6Properties.getCurrentDir();
		int port = Q6Properties
				.getIntUpdateProp(UpdaterConstants.HTTP_LOCAL_PORT);
		LoggerTool.debug(logger,"file system starting ");
		fileserver(docRoot, port);
		LoggerTool.debug(logger,"file system on " + docRoot);

	}

	/**
	 * 
	 * @param args
	 * @param docRoot
	 * @param port
	 * @throws KeyStoreException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 */
	public void fileserver(String docRoot, int port) {
		// Set up the HTTP protocol processor
		HttpProcessor httpproc = HttpProcessorBuilder.create()
				.add(new ResponseDate()).add(new ResponseServer("Test/1.1"))
				.add(new ResponseContent()).add(new ResponseConnControl())
				.build();

		// Set up request handlers
		UriHttpRequestHandlerMapper reqistry = new UriHttpRequestHandlerMapper();
		reqistry.register("*", new HttpFileHandler(docRoot));

		// Set up the HTTP service
		HttpService httpService = new HttpService(httpproc, reqistry);

		SSLServerSocketFactory sf = null;
		if (port == 8443) {
			// Initialize SSL context
			ClassLoader cl = ElementalHttpServer.class.getClassLoader();
			URL url = cl.getResource("my.keystore");
			if (url == null) {
				System.out.println("Keystore not found");
				System.exit(1);
			}
			KeyStore keystore;
			try {
				keystore = KeyStore.getInstance("jks");
				keystore.load(url.openStream(), "secret".toCharArray());

				KeyManagerFactory kmfactory;

				kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory
						.getDefaultAlgorithm());

				kmfactory.init(keystore, "secret".toCharArray());
				KeyManager[] keymanagers = kmfactory.getKeyManagers();
				SSLContext sslcontext = SSLContext.getInstance("TLS");
				sslcontext.init(keymanagers, null, null);
				sf = sslcontext.getServerSocketFactory();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnrecoverableKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (KeyStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (KeyManagementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Thread t;
		try {
			t = new RequestListenerThread(port, httpService, sf);
			t.setDaemon(false);
			t.start();
		} catch (IOException e) {
			if (e instanceof BindException) {
				LoggerTool.error(logger,e.getMessage());
				e.printStackTrace();
			}
			e.printStackTrace();
		}
	}

	static class HttpFileHandler implements HttpRequestHandler {

		private final String docRoot;

		public HttpFileHandler(final String docRoot) {
			super();
			this.docRoot = docRoot;
		}

		public void handle(final HttpRequest request,
				final HttpResponse response, final HttpContext context)
				throws HttpException, IOException {

			String method = request.getRequestLine().getMethod()
					.toUpperCase(Locale.ENGLISH);
			if (!method.equals("GET") && !method.equals("HEAD")
					&& !method.equals("POST")) {
				throw new MethodNotSupportedException(method
						+ " method not supported");
			}
			String target = request.getRequestLine().getUri();

			if (request instanceof HttpEntityEnclosingRequest) {
				HttpEntity entity = ((HttpEntityEnclosingRequest) request)
						.getEntity();
				byte[] entityContent = EntityUtils.toByteArray(entity);
				System.out.println("Incoming entity content (bytes): "
						+ entityContent.length);
			}

			final File file = new File(this.docRoot, URLDecoder.decode(target,
					"UTF-8"));
			if (!file.exists()) {

				response.setStatusCode(HttpStatus.SC_NOT_FOUND);
				StringEntity entity = new StringEntity("<html><body><h1>File"
						+ file.getPath() + " not found</h1></body></html>",
						ContentType.create("text/html", "UTF-8"));
				response.setEntity(entity);
				System.out.println("File " + file.getPath() + " not found");

			} else if (!file.canRead() || file.isDirectory()) {

				response.setStatusCode(HttpStatus.SC_FORBIDDEN);
				StringEntity entity = new StringEntity(
						"<html><body><h1>Access denied</h1></body></html>",
						ContentType.create("text/html", "UTF-8"));
				response.setEntity(entity);
				System.out.println("Cannot read file " + file.getPath());

			} else {
				response.setStatusCode(HttpStatus.SC_OK);
				ContentType createContentType = ContentType.create("text/html",
						(Charset) null);

				if (file.getName().endsWith(".zip")
						|| file.getName().endsWith(".tar.gz")
						|| file.getName().endsWith(".rar")) {
					createContentType = ContentType.WILDCARD;
				}

				FileEntity body = new FileEntity(file, createContentType);
				response.setEntity(body);
				System.out.println("Serving file " + file.getPath());				
			}
		}

	}

	static class RequestListenerThread extends Thread {

		private final HttpConnectionFactory<DefaultBHttpServerConnection> connFactory;
		private final ServerSocket serversocket;
		private final HttpService httpService;

		public RequestListenerThread(final int port,
				final HttpService httpService, final SSLServerSocketFactory sf)
				throws IOException {
			this.connFactory = DefaultBHttpServerConnectionFactory.INSTANCE;
			this.serversocket = sf != null ? sf.createServerSocket(port)
					: new ServerSocket(port);
			this.httpService = httpService;
		}

		@Override
		public void run() {
			System.out.println("FileServer Listening on port "
					+ this.serversocket.getLocalPort());
			while (!Thread.interrupted()) {
				try {
					// Set up HTTP connection
					Socket socket = this.serversocket.accept();
					System.out.println("Incoming connection from "
							+ socket.getInetAddress());
					HttpServerConnection conn = this.connFactory
							.createConnection(socket);

					// Start worker thread
					Thread t = new WorkerThread(this.httpService, conn);
					t.setDaemon(true);
					t.start();
				} catch (InterruptedIOException ex) {
					break;
				} catch (IOException e) {
					System.err
							.println("I/O error initialising connection thread: "
									+ e.getMessage());
					break;
				}
			}
		}
	}

	static class WorkerThread extends Thread {

		private final HttpService httpservice;
		private final HttpServerConnection conn;

		public WorkerThread(final HttpService httpservice,
				final HttpServerConnection conn) {
			super();
			this.httpservice = httpservice;
			this.conn = conn;
		}

		@Override
		public void run() {
			LoggerTool.debug(logger,"New connection thread");
			HttpContext context = new BasicHttpContext(null);
			try {
				while (!Thread.interrupted() && this.conn.isOpen()) {
					this.httpservice.handleRequest(this.conn, context);
				}
			} catch (ConnectionClosedException ex) {
				System.err.println("Client closed connection");
			} catch (IOException ex) {
				System.err.println("I/O error: " + ex.getMessage());
			} catch (HttpException ex) {
				System.err.println("Unrecoverable HTTP protocol violation: "
						+ ex.getMessage());
			} finally {
				try {
					this.conn.shutdown();
				} catch (IOException ignore) {
				}
			}
		}

	}

}