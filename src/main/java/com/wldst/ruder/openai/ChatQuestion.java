package com.wldst.ruder.openai;


import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.config.SpringContextUtil;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.module.ws.web.ContextServer;
import com.wldst.ruder.util.MapTool;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ChatQuestion  extends WebSocketListener{

    public static String hostUrl = "https://spark-api.xf-yun.com/v1.1/chat";
    public static String APPID = "3d55aa86";//从开放平台控制台中获取
    public static String APIKEY = "fc110995f6e94a4ba7010760ab6c7a91";//从开放平台控制台中获取
    public static String APISecret = "ZTZlMTAxZGZlYTNlMzJiMDQ5Y2RiMjVm";//从开放平台控制台中获取
   
    public String question = "说一说结合技术java、图数据库Neo4j、ChatGPT如何更好的服务用户";//可以修改question 内容，来向模型提问
    private String answer = "";
    private boolean answered = false;
    private String myId =null;
    
    private static CrudNeo4jService neo4jService; 

    public static void main(String[] args) {

//        chat();


        // write your code here

    }


    public ChatQuestion(String question) {
	super();
	this.question = question;
    }


    public static ChatQuestion chat(String question,String myId) {
	 ChatQuestion chatQuestion = new ChatQuestion(question);
	try {
            //构建鉴权httpurl
            String authUrl = getAuthorizationUrl(hostUrl,APIKEY,APISecret);
            OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
            OkHttpClient client = new OkHttpClient();  
            String url = authUrl.replace("https://","wss://").replace("http://","ws://");
            Request request = new Request.Builder().url(url).build();
           
            chatQuestion.setMyId(myId);
	    WebSocket webSocket = okHttpClient.newWebSocket(request,chatQuestion);
	    // 构建请求  
//	        Request request = new Request.Builder()  
//	                .url(url) // 替换为您的API地址  
//	                .post(requestBody)  
//	                .build();  
	  
//	        // 发送请求  
//	        try (Response response = client.newCall(request).execute()) {  
//	            if (response.isSuccessful()) {  
//	                // 处理返回结果  
//	                String result = response.body().string();  
//	                System.out.println(result);  
//	                return result;
//	            } else {  
//	                // 处理错误  
//	                System.out.println("请求失败，错误码：" + response.code());  
//	            }  
//	        } catch (Exception e) {  
//	            e.printStackTrace();  
//	        } 
        } catch (Exception e) {
            e.printStackTrace();
        }
	return chatQuestion;
    }


    //鉴权url
   public static String  getAuthorizationUrl(String hostUrl , String apikey ,String apisecret) throws Exception {
        //获取host
       URL url = new URL(hostUrl);
       //获取鉴权时间 date
       SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
       System.out.println("format:\n" + format.toString() );
       format.setTimeZone(TimeZone.getTimeZone("GMT"));
       String date = format.format(new Date());
       //获取signature_origin字段
       StringBuilder builder = new StringBuilder("host: ").append(url.getHost()).append("\n").
               append("date: ").append(date).append("\n").
               append("GET ").append(url.getPath()).append(" HTTP/1.1");
       System.out.println("signature_origin:\n" + builder);
       //获得signatue
       Charset charset = Charset.forName("UTF-8");
       Mac mac = Mac.getInstance("hmacsha256");
       SecretKeySpec sp = new SecretKeySpec(apisecret.getBytes(charset),"hmacsha256");
       mac.init(sp);
       byte[] basebefore = mac.doFinal(builder.toString().getBytes(charset));
       String signature = Base64.getEncoder().encodeToString(basebefore);
       //获得 authorization_origin
       String authorization_origin = String.format("api_key=\"%s\",algorithm=\"%s\",headers=\"%s\",signature=\"%s\"",apikey,"hmac-sha256","host date request-line",signature);
       //获得authorization
       String authorization = Base64.getEncoder().encodeToString(authorization_origin.getBytes(charset));
       //获取httpurl
       HttpUrl httpUrl = HttpUrl.parse("https://" + url.getHost() + url.getPath()).newBuilder().//
               addQueryParameter("authorization", authorization).//
               addQueryParameter("date", date).//
               addQueryParameter("host", url.getHost()).//
               build();

        return httpUrl.toString();
    }

    //重写onopen
    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        if(neo4jService==null) {
	    neo4jService = (CrudNeo4jService) SpringContextUtil.getBean("crudNeo4jService"); 
	}
        new Thread(()->{
            JSONObject frame = new JSONObject();
            JSONObject header = new JSONObject();
            JSONObject chat = new JSONObject();
            JSONObject parameter = new JSONObject();
            JSONObject payload = new JSONObject();
            JSONObject message = new JSONObject();
            JSONObject text = new JSONObject();
            JSONArray ja = new JSONArray();

            //填充header
            header.put("app_id",APPID);
            header.put("uid","15828264059");
            //填充parameter
            chat.put("domain","general");
            chat.put("random_threshold",0);
            chat.put("max_tokens",2048);
            chat.put("auditing","default");
            parameter.put("chat",chat);
            //填充payload
            text.put("role","user");
            text.put("content",question);
            ja.add(text);
//            message.addProperty("text",ja.getAsString());
            message.put("text",ja);
            payload.put("message",message);
            frame.put("header",header);
            frame.put("parameter",parameter);
            frame.put("payload",payload);
            System.out.println("frame:\n" + frame.toString());
            webSocket.send(frame.toString());
        }


        ).start();
    }

    //重写onmessage

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);
        System.out.println("text:\n" + text);
         JSONObject responseData = JSON.parseObject(text);
//        System.out.println("code:\n" + responseData.getHeader().get("code"));
        
        Map<String, Object> header = MapTool.mapObject(responseData,"header");
        Map<String, Object> payload = MapTool.mapObject(responseData,"payload");
	if(0 == MapTool.integer(header, "code")){
            System.out.println("###########");
            if(2 != MapTool.integer(header, "status")){
                System.out.println("****************"); 
                
                Map<String, Object> choices = MapTool.mapObject(payload, "choices");
		List<Map<String, Object>> listMap = MapTool.listMapObject(choices, "text");
		if (listMap != null && !listMap.isEmpty()) {
		    String ai = MapTool.string(listMap.get(0), "content");

		    answer(ai);
		    answer += ai;
		    System.out.println(ai);
		}
//                System.out.println(answer);
            }else { 
               
                Map<String, Object> choices = MapTool.mapObject(payload, "choices");
                Map<String, Object> usage = MapTool.mapObject(payload, "usage");
		Map<String, Object> textx =  MapTool.mapObject(usage,"text");
                
                
		List<Map<String, Object>> listMap = MapTool.listMapObject(choices, "text");
                
//                JsonObject jo = temp.get(0);
                String string = MapTool.string(listMap.get(0),"content");
                System.out.println(string);
		answer += string;
		 answer(string);
                
                int prompt_tokens = MapTool.integer(textx,"prompt_tokens");
                
                System.out.println("prompt_tokens:"+prompt_tokens+"返回结果为：\n" + answer);
                webSocket.close(3,"客户端主动断开链接");
            }

        }else{
            System.out.println("返回结果错误：\n" + header.get("code") +  header.get("message") );
        }
    }


    public void answer(String string) {
	String myId2 = getMyId();
	    if (myId2 != null) {
		ContextServer.sendInfo(string, myId2);
	    }
    }

    //重写onFailure

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        super.onFailure(webSocket, t, response);
        System.out.println(response);
        answered=true;
    }
    @Override
    public void onClosed(WebSocket webSocket,int code, String reason) {
//	Map<String,Object> dd = MapTool.newMap();
//	neo4jService.saveByBody(dd, "ChatHistory");
	answered=true;
    }

    public String getMyId() {
        return myId;
    }


    public void setMyId(String myId) {
        this.myId = myId;
    }


    public String getAnswer() {
        return answer;
    }


    public void setAnswer(String answer) {
        this.answer = answer;
    }


    public boolean isAnswered() {
        return answered;
    }


    public void setAnswered(boolean answered) {
        this.answered = answered;
    }
    
    
}
