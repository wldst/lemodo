package com.wldst.ruder.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * 邮箱相关的常量
 * 
 * @author wldst
 *
 */
public class EmailDomain extends FileDomain {

    public static final String EMAIL_ACCOUNT = "account";
    public static final String EMAIL_TOEKN = "authPassword";
    public static final String EMAIL_PASSWORD = "password";
    public static final String EMAIL_PORT = "port";
    public static final String EMAIL_PROTOCOL = "protocol";
    public static final String EMAIL_HOST = "imapServer";
    public static final String EMAIL = "emailMessage";
    public static final String IMAP_SERVER = "imapServer";

    public static final String FROM = "from";
    public static final String TO = "to";
    public static final String SUBJECT = "subject";
    public static final String EMAIL_CONTENT = "content";
    public static final String SEND_DATE = "sendDate";
    public static final String ATTACHMENT = "attachment";
    public static final Map<String, String> checkCode = new HashMap<>();
}
