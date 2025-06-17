package com.wldst.ruder.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.LemodoApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.wldst.ruder.domain.SystemDomain;

/**
 * 
 * 接口调用工具类
 * 
 * @ClassName: RestTemplateUtil
 * @Description:
 * @author liuqiang
 * @date 2019-4-8 17:16:51
 * @version V1.1
 */
@Service
public class RestApi extends SystemDomain {
    private String authUrl = "http://client.lemoredo.online:9500/";
    private String serverUrl = "http://client.lemoredo.online:9500/";
    public static final String SAVE = "save";
    public static final String QUERY = "query";
    public static final String UPDATE = "update";
    public static final String DEL = "del";

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }
//    @Autowired
    private RestTemplate restTemplate=restTemplate();
  //60 * 1000
    @Value("${rest.connectTimeout:60000}")
    private int connectTimeout;
    //5 * 60 * 1000
    @Value("${rest.readTimeout:300000}")
    private int readTimeout;
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setConnectTimeout(connectTimeout);
        simpleClientHttpRequestFactory.setReadTimeout(readTimeout);
        RestTemplate restTemplate = new RestTemplate(simpleClientHttpRequestFactory);
        return restTemplate;
    }

    public <T> T postForObject(String url, Map<String, Object> body, Class<T> class1) {
	HttpHeaders headers = new HttpHeaders(); // http请求头
	headers.setContentType(MediaType.APPLICATION_JSON); // 请求头设置属性
	 
	HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<Map<String, Object>>(body, headers);
	return restTemplate.postForObject(url, requestEntity, class1);
    }
    
    public <T> T postApi(Map<String, Object> body, Class<T> class1) {
	HttpHeaders headers = new HttpHeaders(); // http请求头
	headers.setContentType(MediaType.APPLICATION_JSON); // 请求头设置属性
	HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<Map<String, Object>>(body, headers);
	return restTemplate.postForObject(serverUrl, requestEntity, class1);
    }

    public Map<String, Object> login(Map<String, Object> body) {
	return postForObject(authUrl + "oauth/token", body, Map.class);
    }

    public Map<String, Object> login(String username, String password) {
	Map<String, Object> body = new HashMap<>();
	body.put("username", username);
	body.put("password", password);
	return login(body);
    }

    public Map<String, Object> online(Map<String, Object> body) {
	return postForObject(authUrl + LemodoApplication.MODULE_NAME+"/server/clientUp", body, Map.class);
    }
    
    public Map<String, Object> onlineX(Map<String, Object> body,String server) {
	return postForObject(server + LemodoApplication.MODULE_NAME+"/server/clientUp", body, Map.class);
    }

    public Map<String, Object> share(Map<String, Object> body) {
	return postForObject(authUrl + LemodoApplication.MODULE_NAME+"/server/share", body, Map.class);
    }
    
    public Map<String, Object> shareX(Map<String, Object> body,String server) {
	return postForObject(server + LemodoApplication.MODULE_NAME+"/server/share", body, Map.class);
    }

    public Map<String, Object> sent(Map<String, Object> body, String cmd) {
	Map<String, Object> sentData = new HashMap<>();
	sentData.put(CMD, cmd);
	sentData.put(DATA, body);
	return postForObject(authUrl + LemodoApplication.MODULE_NAME+"/server/share", body, Map.class);
    }
    
    public Map<String, Object> sentX(Map<String, Object> body, String cmd,String server) {
   	Map<String, Object> sentData = new HashMap<>();
   	sentData.put(CMD, cmd);
   	sentData.put(DATA, body);
   	return postForObject(server + LemodoApplication.MODULE_NAME+"/server/share", body, Map.class);
       }
    
    public Map<String, Object> sent(Map<String, Object> body, String cmd,Map<String, Object> meta) {
	Map<String, Object> sentData = new HashMap<>();
	sentData.put(CMD, cmd);
	sentData.put(DATA, body);
	sentData.put(META_DATA, meta);
	return postForObject(authUrl + LemodoApplication.MODULE_NAME+"/server/share", sentData, Map.class);
    }
    
    public Map<String, Object> sentX(Map<String, Object> body, String cmd,Map<String, Object> meta,String server) {
	Map<String, Object> sentData = new HashMap<>();
	sentData.put(CMD, cmd);
	sentData.put(DATA, body);
	sentData.put(META_DATA, meta);
	return postForObject(server + LemodoApplication.MODULE_NAME+"/server/share", sentData, Map.class);
    }
    
    public Map<String, Object> sent(List<Map<String, Object>> body, String cmd,Map<String, Object> meta) {
	Map<String, Object> sentData = new HashMap<>();
	sentData.put(CMD, cmd);
	sentData.put(DATA, body);
	sentData.put(META_DATA, meta);
	return postForObject(authUrl + LemodoApplication.MODULE_NAME+"/server/share", sentData, Map.class);
    }
    
    public Map<String, Object> sentX(List<Map<String, Object>> body, String cmd,Map<String, Object> meta,String server) {
   	Map<String, Object> sentData = new HashMap<>();
   	sentData.put(CMD, cmd);
   	sentData.put(DATA, body);
   	sentData.put(META_DATA, meta);
   	return postForObject(server + LemodoApplication.MODULE_NAME+"/server/share", sentData, Map.class);
       }
    
    public List<Map<String, Object>> query(Map<String, Object> body, String cmd,Map<String, Object> meta) {
	Map<String, Object> sentData = new HashMap<>();
	sentData.put(CMD, cmd);
	sentData.put(DATA, body);
	sentData.put(META_DATA, meta);
	return postForObject(authUrl + LemodoApplication.MODULE_NAME+"/server/share", sentData, List.class);
    }
    
    public List<Map<String, Object>> queryX(Map<String, Object> body, String cmd,Map<String, Object> meta,String server) {
	Map<String, Object> sentData = new HashMap<>();
	sentData.put(CMD, cmd);
	sentData.put(DATA, body);
	sentData.put(META_DATA, meta);
	return postForObject(server + LemodoApplication.MODULE_NAME+"/server/share", sentData, List.class);
    }
    
    public Map<String, Object> report(Map<String, Object> body,Map<String, Object> metaData) {
	return  sent(body,CMD_REPORT,metaData);
    }
    public Map<String, Object> reportX(Map<String, Object> body,Map<String, Object> metaData,String server) {
	return  sentX(metaData,CMD_REPORT,server);
    }
    public Map<String, Object> reportData(List<Map<String, Object>> body,Map<String, Object> metaData) {
	return  sent(body,CMD_UP_DATA_LIST,metaData);
    }
    public Map<String, Object> reportDataX(List<Map<String, Object>> dataList,Map<String, Object> metaData,String server) {
	return  sentX(dataList,CMD_UP_DATA_LIST,metaData,server);
    }

}
