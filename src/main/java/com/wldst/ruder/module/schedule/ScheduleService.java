package com.wldst.ruder.module.schedule;

import java.net.UnknownHostException;
import java.util.*;

import com.wldst.ruder.constant.CruderConstant;
import com.wldst.ruder.module.ai.service.MilvusService;
import com.wldst.ruder.module.ai.service.OllamaEmbeddingService;
import com.wldst.ruder.module.database.DbInfoService;
import com.wldst.ruder.module.mail.service.MailService;
import com.wldst.ruder.util.LoggerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.domain.AuthDomain;
import com.wldst.ruder.util.DateTool;
import com.wldst.ruder.util.NetHandleUtil;
import com.wldst.ruder.util.RestApi;

@Component
public class ScheduleService extends AuthDomain{

    private static Logger logger=LoggerFactory.getLogger(ScheduleService.class);
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private MailService mailService;
    @Autowired
    private DbInfoService gather;
    static int failNum=0;
    @Autowired
    private RestApi rest;
    @Value(value = "${http.port}")
    private String port;
    @Value(value = "${server.port}")
    private String serverPort;

    @Autowired
    private  MilvusService milvusService;

    // @Scheduled(cron = "0 30 * * * *")
    @Scheduled(cron = "30 0/1 * * * *")
    public void sessionClear(){
        String bySysCode=neo4jService.getBySysCode("workip");
        if(NetHandleUtil.getLocalIpAddress().equals(bySysCode)){
            // LoggerTool.info(logger,"==================发送会话清理开始===========================");
            clearOver30m(SESSION, "createTime");
            clearOver30m("OnlineUser", "startTime");
        }
    }

    @Scheduled(cron = "30 1 1/10 * * *")
    public void neo4j2Vector(){
        milvusService.transNeo4j2Vector();
    }

    //	@Scheduled(cron = "30 0/1 * * * *")
    public void sendMail(){
        String bySysCode=neo4jService.getBySysCode("workip");
        if(NetHandleUtil.getLocalIpAddress().equals(bySysCode)){
            // LoggerTool.info(logger,"==================发送会话清理开始===========================");
            List<Map<String, Object>> data=neo4jService.cypher("MATCH(n:mailMessage) where n.status !='1' OR n.status  IS NULL return n");
//			.listAttMapBy("status","","mailMessage");
            for(Map<String, Object> di : data){

                mailService.sendMail(di);
            }
            //查看数据库链接

        }
    }

    @Scheduled(cron = "13/30 * * * * *")
    public void sendBgMail(){
        if(!neo4jService.isOn("sendWordEmail")){
            return;
        }
        String bySysCode=neo4jService.getBySysCode("workip");
        if(NetHandleUtil.getLocalIpAddress().equals(bySysCode)){
            //查看数据库链接,//初始化数据库链接
            if(!neo4jService.isOn("sendWordEmail")){
                return;
            }
            //如何根据环境设置对应的数据源？
            Map<String, Object> ds=neo4jService.getAttMapBy(NAME, "bgdb", "DataSource");
            gather.initConnect(ds);
            String sql="SELECT d.CONSULT_ID,d.CONTACT_WAY,d.CONTENT,d.TITLE,d.SOLVE_CONTENT,e.NAME,e.EMPNO,e.USERID FROM DI_CONSULT d "+
                    " left join b_hr_him_employee e on d.creaetor=e.id  where CONSULT_TYPE='02' and sent_email is null and state='21'";
            //字段映射类型
            try{
                List<Map<String, Object>> datas=gather.query(sql);
                for(Map<String, Object> di : datas){
                    String cway=string(di, "CONTACT_WAY");
                    String content=string(di, "CONTENT");
                    String solveContent=string(di, "SOLVE_CONTENT");
                    String empno=string(di, "EMPNO");
                    String name=string(di, "NAME");
                    String userid=string(di, "USERID");

                    Map<String, Object> mail=new HashMap<>();
                    mail.put("subject", "【守廉】：有话说");
                    String mailContent="【守廉】有话说:\n"+
                            content+"\n"+
                            "依据内容: "+solveContent+"\n"+
                            "<BR/>联系方式："+cway+"\n"+
                            "<BR/>姓名："+name+"\n"+
                            "<BR/>工号："+empno+"<BR/> 账号："+userid;
                    mail.put("content", mailContent);
                    logger.info(mailContent);

                    //设置一下数据，
                    mail.put("from", neo4jService.getSettingBy("sendWordFrom"));
                    mail.put("to", neo4jService.getSettingBy("sendWordTo"));
                    mailService.sendMail(mail);
                    gather.excute("update DI_CONSULT set sent_email='1' where CONSULT_ID='"+string(di,"CONSULT_ID")+"'");
                }
            }catch(Exception e){
                throw new RuntimeException(e);
            }
        }
    }

    //    @Scheduled(cron = "35/5 * * * * *")
    public void heartBeatClear(){
        // LoggerTool.info(logger,"==================发送会话清理开始===========================");
//	    clearOver1m(MICRO_SERVICE, "createTime");
        clearOver30m(MICRO_SERVICE, "createTime");
    }

    /**
     * 与服务器进行心跳
     */
    @Scheduled(cron = "6 1/5 * * * *")
    public void heartBeat(){
        String bySysCode=neo4jService.getBySysCode("rootip");
        if(NetHandleUtil.getLocalIpAddress().equals(bySysCode)){
            try{
                String localHostName=NetHandleUtil.getLocalHostName();
                String localIpAddress=NetHandleUtil.getLocalIpAddress();
                Map<String, Object> body=new HashMap<>();
                body.put(HOST_NAME, localHostName);
                body.put(IP_ADDRESS, localIpAddress);
                body.put(MAC_ADDRESS, NetHandleUtil.getMacAddress());
                body.put(CLIENT_PORT, port);
                body.put(CLIENT_HTTPS_PORT, serverPort);
                body.put(CREATETIME, Calendar.getInstance().getTimeInMillis());
                rest.online(body);
            }catch(UnknownHostException e){
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void clearOver1m(String label, String field){
        List<Map<String, Object>> dataList=neo4jService.listAllByLabel(label);
        for(Map<String, Object> si : dataList){
            Long longValue=0l;
            try{
                longValue=longValue(si, field);
            }catch(Exception e){
                String dateStr=string(si, field);
                longValue=DateTool.dateStrShortToLong(dateStr);
            }
            Long sessionId=id(si);
            if(longValue==null){
                LoggerTool.error(logger, "session"+toMapString2(si)+","+field+" is null ,");
            }else{
                if(DateTool.over1m(longValue)){
                    LoggerTool.info(logger, " clearOver1m {}-{}-{}", label, field, sessionId);
                    neo4jService.delete(sessionId);
                }
            }

        }
    }

    public void clearOver30m(String label, String field){
        List<Map<String, Object>> dataList=neo4jService.listAllByLabel(label);
        for(Map<String, Object> si : dataList){
            Long longValue=0l;
            try{
                longValue=longValue(si, field);
            }catch(Exception e){
                String dateStr=string(si, field);
                longValue=DateTool.dateStrShortToLong(dateStr);
            }
            Long sessionId=id(si);
            if(longValue==null){
                LoggerTool.error(logger, "session"+toMapString2(si)+","+field+" is null ,");
            }else{
                if(DateTool.over30m(longValue)){
                    LoggerTool.info(logger, " {}-{}-{}", label, field, sessionId);
                    neo4jService.delete(sessionId);
                }
            }

        }
    }

}
