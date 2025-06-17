package com.wldst.ruder.intercepter;

import java.util.List;
import java.util.Map;

import com.wldst.ruder.LemodoApplication;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.wldst.ruder.config.SpringContextUtil;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.domain.AuthDomain;
import com.wldst.ruder.domain.DomainBuffer;
import com.wldst.ruder.domain.SystemDomain;
import com.wldst.ruder.util.MapTool;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class LoginInterceptor extends AuthDomain implements HandlerInterceptor {

    private static CrudNeo4jService neo4jService;

    /**
     * 在请求被处理之前调用，
     * 可以进行安全检查，跨站攻击配置，等
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 检查每个到来的请求对应的session域中是否有登录标识
        HttpSession session = request.getSession();
        Object loginName = session.getAttribute("loginName");
        if (loginName == null) {
            loginName = request.getParameter("testUser");
        }
        String requestUrl = request.getRequestURL().toString();
        String remoteAddr = request.getRemoteAddr();
        if (neo4jService == null) {
            neo4jService = (CrudNeo4jService) SpringContextUtil.getBean("crudNeo4jService");
        }
//        if(remoteAddr.equals("127.0.0.1")){
//            session.setAttribute("loginName", "Server");
//            return true;
//        }

        if(isInnerHost(remoteAddr)){
            session.setAttribute("innerHostCall", true);
        }


//        System.out.println("loginName=" + loginName);
        if (null == loginName || !(loginName instanceof String)) {
            List<Map<String, Object>> servers = getByCache("InnerServer");

            if (servers != null && !servers.isEmpty()) {
                for (Map<String, Object> si : servers) {
                    if (SystemDomain.host(si).equals(remoteAddr)) {
                        session.setAttribute("loginName", "Server");
                        return true;
                    }
                }
            }

            List<Map<String, Object>> listAllByLabel = getByCache("InnerService");
            if (listAllByLabel != null && !listAllByLabel.isEmpty()) {
                for (Map<String, Object> si : listAllByLabel) {
                    String innerServiceUrl = MapTool.string(si, "url");
                    System.out.println("innerServiceUrl=" + innerServiceUrl);
                    boolean isInnerService = requestUrl.endsWith(innerServiceUrl) || requestUrl.contains(innerServiceUrl);
                    if (innerServiceUrl != null && isInnerService) {
                        session.setAttribute("loginName", "Server");
                        return true;
                    }
                }
            }

//            String url = LemodoApplication.MODULE_NAME+"/cruder/Session/getValue/sessionId";

//            Map<String, Object> map = AuthDomain.onlineSeMap.get("liuqiang");
//            if(map!=null&&!map.isEmpty()) {
//        	return true;
//            }

            // 未登录，重定向到登录页
            response.sendRedirect( LemodoApplication.MODULE_NAME + "/login");
            return false;
        }
        return true;
    }

    private boolean isInnerHost(String remoteAddr){
        List<Map<String, Object>> servers = getByCache("InnerServer");

        if (servers != null && !servers.isEmpty()) {
            for (Map<String, Object> si : servers) {
                String host=SystemDomain.host(si);
                if (host!=null&&host.equals(remoteAddr)) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<Map<String, Object>> getByCache(String queryKey) {
        DomainBuffer.clear(queryKey);
        List<Map<String, Object>> servers = null;
        Object object = DomainBuffer.getBufferData().get(queryKey);
        if (object == null || DomainBuffer.isExpired(queryKey)) {
            servers = neo4jService.listAllByLabel(queryKey);
            DomainBuffer.put(queryKey, servers);
        } else {
            servers = (List<Map<String, Object>>) object;
        }
        return servers;
    }

    /**
     * 在请求被处理后，视图渲染之前调用
     *
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    /**
     * 在整个请求结束后调用
     *
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}