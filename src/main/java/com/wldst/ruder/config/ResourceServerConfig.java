package com.wldst.ruder.config;

//@Configuration
//@EnableResourceServer
//extends ResourceServerConfigurerAdapter
public class ResourceServerConfig {
//
//
//  @Override
//  public void configure(ResourceServerSecurityConfigurer resources) throws Exception {       // 定义异常转换类生效
//    AuthenticationEntryPoint authenticationEntryPoint = new OAuth2AuthenticationEntryPoint();
//    ((OAuth2AuthenticationEntryPoint) authenticationEntryPoint).setExceptionTranslator(new Auth2ResponseExceptionTranslator());
//    resources.authenticationEntryPoint(authenticationEntryPoint);
//  }
//
//
//  @Override
//  public void configure(HttpSecurity http) throws Exception {
//    http
//        .csrf().disable()
//        .exceptionHandling()            // 定义的不存在access_token时候响应
//        .authenticationEntryPoint(new SecurityAuthenticationEntryPoint())
//        .and()
//        .authorizeRequests().antMatchers("/**/**").permitAll()
//        .anyRequest().authenticated()
//        .and()
//        .httpBasic().disable();
//  }
}