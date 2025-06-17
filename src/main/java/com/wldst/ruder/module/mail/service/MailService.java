package com.wldst.ruder.module.mail.service;

import com.alibaba.fastjson2.JSON;
import com.wldst.ruder.crud.service.CrudUserNeo4jService;
import com.wldst.ruder.domain.EmailDomain;
import com.wldst.ruder.module.mail.MailUtils;
import com.wldst.ruder.util.MapTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Map;
@Service
public class MailService extends EmailDomain {
    private static Logger logger = LoggerFactory.getLogger(MailService.class);
    @Autowired
    private CrudUserNeo4jService neo4jService;
    public void sendMail(Map<String, Object> nodeMapById) {
        String from = MapTool.string(nodeMapById, "from").trim();
        String to = MapTool.string(nodeMapById, "to").trim();
        String password = MapTool.string(nodeMapById, EMAIL_TOEKN);//"piuevcgisuwcbgea"; // QQ邮箱的授权码
        String username = from;
        Map<String, Object> fromBox = null;
        String serverHost = "";
        int port = 25;
        logger.info("from:{},to:{}",from,to);
        try {
            fromBox = neo4jService.getNodeMapById(Long.valueOf(from));
            logger.info(JSON.toJSONString(fromBox));
            if (fromBox != null && !fromBox.isEmpty()) {
                username = string(fromBox, "account");
                serverHost = string(fromBox, "serverHost");
                port = integer(fromBox, "port");
                String authPassword = string(fromBox, "authPassword");
                if (authPassword != null) {
                    password = authPassword;
                } else {
                    password = string(fromBox, "password");
                }

                from = string(fromBox, "account");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            username = from;
        }
        Map<String, Object> tobox = null;
        try {
            tobox = neo4jService.getNodeMapById(Long.valueOf(to));
            logger.info(JSON.toJSONString(tobox));
            if (tobox != null && !tobox.isEmpty()) {
                to = string(tobox, "account");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
//            username = to;
        }


        //"1721903353@qq.com";
//		获取密码

        String content = MapTool.content(nodeMapById);//"piuevcgisuwcbgea"; // QQ邮箱的授权码
        String subject = MapTool.string(nodeMapById, SUBJECT);//"piuevcgisuwcbgea"; // QQ邮箱的授权码

        try {
            MailUtils.sendMail(serverHost, port,
                    from, null, to
                    , null,
                    subject, content,
                    Calendar.getInstance().getTime(),
                    username, password);

        } catch (Exception e) {
            // TODO Auto-generated catch block
//            e.printStackTrace();
            logger.error(e.getMessage(),e);
        }
    }

    public void sendMail2(Map<String, Object> nodeMapById) {
        String from = MapTool.string(nodeMapById, "from");
        String to = MapTool.string(nodeMapById, "to");
        String password = MapTool.string(nodeMapById, EMAIL_TOEKN);//"piuevcgisuwcbgea"; // QQ邮箱的授权码
        String username = from;
        Map<String, Object> box = null;
        String serverHost = "";
        int port = 25;
        try {
            box = neo4jService.getNodeMapById(Long.valueOf(from));
            if (box != null && !box.isEmpty()) {
                username = string(box, "account");
                serverHost = string(box, "serverHost");
                port = integer(box, "port");
                String authPassword = string(box, "authPassword");
                if (authPassword != null) {
                    password = authPassword;
                } else {
                    password = string(box, "password");
                }

                from = string(box, "account");
            }
        } catch (Exception e) {
            username = from;
        }
        Map<String, Object> tobox = null;
        try {
            tobox = neo4jService.getNodeMapById(Long.valueOf(to));
            if (tobox != null && !tobox.isEmpty()) {
                to = string(tobox, "account");
            }
        } catch (Exception e) {
            username = from;
        }


        //"1721903353@qq.com";
//		获取密码

        String content = MapTool.content(nodeMapById);//"piuevcgisuwcbgea"; // QQ邮箱的授权码
        String subject = MapTool.string(nodeMapById, SUBJECT);//"piuevcgisuwcbgea"; // QQ邮箱的授权码

        try {
            MailUtils.sendMail(serverHost, port,
                    from, null, to
                    , null,
                    subject, content,
                    Calendar.getInstance().getTime(),
                    username, password);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
