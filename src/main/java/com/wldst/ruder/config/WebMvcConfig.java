package com.wldst.ruder.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.CrudUserNeo4jService;
import com.wldst.ruder.intercepter.LoginInterceptor;

@Configuration
//@EnableWebMvc
public class WebMvcConfig  implements WebMvcConfigurer {
    @Value("server.ui.publish.path")
    private String uiPublishPath;
    @Value("server.ui.key")
    private String uiPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
	registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
	registry.addResourceHandler("/page/**").addResourceLocations("classpath:/page/");
	registry.addResourceHandler("/"+uiPath+"/**")
	.addResourceLocations("classpath:"+uiPublishPath);
	registry.addResourceHandler("/staticRes/**").addResourceLocations("classpath:/staticRes/");
	registry.addResourceHandler("/**").addResourceLocations("classpath:/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/welcome").setViewName("welcome");
        registry.addViewController("/readme").setViewName("readme");
        registry.addViewController("/login").setViewName("layui/login");
        registry.addViewController("/logout/success").setViewName("logout_success");
        //
        registry.addRedirectViewController(LemodoApplication.MODULE_NAME, LemodoApplication.MODULE_NAME+"/login");
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册拦截器
        LoginInterceptor loginInterceptor = new LoginInterceptor();
        InterceptorRegistration loginRegistry = registry.addInterceptor(loginInterceptor);
        // 拦截路径
        loginRegistry.addPathPatterns("/**");
        // 排除路径
//        loginRegistry.excludePathPatterns("/");
        loginRegistry.excludePathPatterns(LemodoApplication.MODULE_NAME+"/");
        loginRegistry.excludePathPatterns(LemodoApplication.MODULE_NAME+"/login");
        loginRegistry.excludePathPatterns(LemodoApplication.MODULE_NAME+"/register");
        loginRegistry.excludePathPatterns(LemodoApplication.MODULE_NAME+"/loginout");
        loginRegistry.excludePathPatterns("/index.html");
        loginRegistry.excludePathPatterns(LemodoApplication.MODULE_NAME+"/adminctrl/login");
        loginRegistry.excludePathPatterns(LemodoApplication.MODULE_NAME+"/cruder/Session/getValue/userName");
        loginRegistry.excludePathPatterns(LemodoApplication.MODULE_NAME+"/server/clientUp");
        loginRegistry.excludePathPatterns(LemodoApplication.MODULE_NAME+"/server/say");
        loginRegistry.excludePathPatterns(LemodoApplication.MODULE_NAME+"/server/share");
        loginRegistry.excludePathPatterns("/lemodo/**");
        loginRegistry.excludePathPatterns("/lemodo/**/**");
        
        loginRegistry.excludePathPatterns(LemodoApplication.MODULE_NAME+"/cypher/data");
        loginRegistry.excludePathPatterns(LemodoApplication.MODULE_NAME+"/adminctrl/checkUser");
        loginRegistry.excludePathPatterns(LemodoApplication.MODULE_NAME+"/adminctrl/register");
        loginRegistry.excludePathPatterns(LemodoApplication.MODULE_NAME+"/adminctrl/loginout");
        loginRegistry.excludePathPatterns(LemodoApplication.MODULE_NAME+"/adminctrl/getToken");
        loginRegistry.excludePathPatterns(LemodoApplication.MODULE_NAME+"/cruder/dataSet/save");
        loginRegistry.excludePathPatterns(LemodoApplication.MODULE_NAME+"/table/parseJoin");
        
        // 排除资源请求
        loginRegistry.excludePathPatterns("/css/login/*.css");
        loginRegistry.excludePathPatterns("/static/**");
        loginRegistry.excludePathPatterns("/page/**");
        loginRegistry.excludePathPatterns("/staticRes/**");
        loginRegistry.excludePathPatterns("/i18n/**");
	    loginRegistry.excludePathPatterns("/js/login/**");
        loginRegistry.excludePathPatterns("/image/login/*.png");
        loginRegistry.excludePathPatterns(LemodoApplication.MODULE_NAME+"/collect/**");
    }
    

}
