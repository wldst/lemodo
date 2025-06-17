package com.wldst.ruder.module.bs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import com.wldst.ruder.crud.service.RelationService;
import com.wldst.ruder.util.LoggerTool;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.annotation.ServiceLog;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.CrudUserNeo4jService;
import com.wldst.ruder.crud.service.ObjectService;
import com.wldst.ruder.domain.VoiceOperateDomain;
import com.wldst.ruder.domain.WebSocketDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.parse.handle.BeanShellHandle;
import com.wldst.ruder.module.ws.web.ContextServer;
import com.wldst.ruder.util.CrudUtil;
import com.wldst.ruder.util.DateTool;
import com.wldst.ruder.util.VtPool;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * 解析聊天数据，并根据数据更新或者查询数据
 */
@Component
public class ParseExcuteSentence extends VoiceOperateDomain {
    final static Logger logger = LoggerFactory.getLogger(ParseExcuteSentence.class);
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private CrudUserNeo4jService neo4jUService;
    @Autowired
    private RelationService relationService;
    @Autowired
    private ObjectService objectService;
    @Autowired
    private UserAdminService adminService;
    @Autowired
    private CrudUtil crudUtil;
    @Autowired
    private BeanShellHandle bsHandle;
    @Autowired
    private Function fun;

    private Map<String, Map<String, Object>> context = new HashMap<>();
    List<String> stackQuit = Arrays.asList("退出", "返回", "返回上一级");// 唤醒词
    List<String> operateStack = Arrays.asList("操作", "使用", "进入", "得到");// 唤醒词

    List<String> getUseWords = Arrays.asList("操作", "使用", "进入", "打开", "获取", "得到", "获取最新的", "关于", "拿到", "找到");
    // 修改前缀
    List<String> newUpdate = Arrays.asList("把", "将", "被", "修改", "更新", "update");

    List<String> updates = Arrays.asList("保存", "更新", "update", "save");
    List<String> help = Arrays.asList("help", "帮助文档", "帮助", "帮帮忙", "说明");
    List<String> auth = Arrays.asList("给", "将");
    List<String> authAdd = Arrays.asList("添加", "增加", "授予", "授权");
    // 新的关系
    List<String> newRelation = Arrays.asList("创建关系", "新增关系", "添加关系", "添加联系", "新建关系", "有关系", "更新关系", "保存关系");
    // 新建节点
    List<String> newNode = Arrays.asList("创建", "新增", "新建", "添加", "有", "new");

    List<String> manageNode = Arrays.asList("管理", "manage", "操作", "处理", "列表");
    List<String> openWord = Arrays.asList("打开", "文档", "查看", "详情");
    List<String> me = Arrays.asList("我", "me", "wo", "俺");

    // 所属
    List<String> ownWords = Arrays.asList("的", "地", "得", "所属", "隶属的");
    // 动词读取关系
    List<String> actionWords = Arrays.asList("做", "干", "读", "听", "说", "学", "想", "写", "完成", "接龙");
    // 谓词
    List<String> kEqualv = Arrays.asList("是", "有", "等于", "叫", "为", "=");
    // 获取关系，修改关系属性
    List<String> relProp = Arrays.asList("关系属性");
    List<String> relName = Arrays.asList("属于", "签字", "朋友", "孩子", "父亲", "上级", "下级", "后序", "前序");

    List<String> need = Arrays.asList("需要", "去", "need", "要");
    List<String> count = Arrays.asList("有多少", "how much", "多少");
    List<String> countWord = Arrays.asList("个", "条", "次", "头", "只", "伙", "辆", "种", "丛", "场", "扎", "顿", "对");
    List<String> done = Arrays.asList("完成", "签名", "填写", "填报", "报名");
    // 是某某关系
    List<String> isRel = Arrays.asList("是", "在", "一起", "一同", "俩", "两个");
    List<String> andRel = Arrays.asList("和", "跟", "与", "、", " AND ", " and ", " && ");
    List<String> between = Arrays.asList("之间的");
    // 删除信息
    List<String> removes = Arrays.asList("删除", "去除", "注销", "清理", "清除", "处理掉", "delete", "remove");
    List<String> deleteWords = Arrays.asList("禁止", "删除", "注销", "清除", "去掉", "去除", "delete", "remove", "del");
    List<String> deleteRels = Arrays.asList("禁止关系", "删除关系", "注销关系", "清除关系", "去掉关系", "去除关系", "deleteRel", "removeRel",
            "delRel");

    private static String zuoKuohao = "(";
    private static String cnLeftKuoHao = "（";
    private static String rightKuohao = ")";
    private static String cnRightKuoHao = "）";
    private static String USED = "used";

    @ServiceLog(description = "根据Label，key，value获取Id")
    public Long getId(String label, String key, String value) {
        return neo4jService.getNodeId(key, value, label);
    }

    @ServiceLog(description = "根据Label，key，value获取节点")
    public Map<String, Object> getNode(String label, String key, String value) {
        return neo4jService.getAttMapBy(key, value, label);
    }

    /**
     * 自定义句式，句式识别
     *
     * @param voiceInfo
     */
    @ServiceLog(description = "获取参数中的Text，并调用通用的解析执行方法")
    public void parseText(Map<String, Object> voiceInfo) {
        String commandText = string(voiceInfo, "text");
        parseAndExcute(commandText);
    }

    /**
     * 格式化命令，清理口语助词
     *
     * @param commandText
     * @return
     */
    @ServiceLog(description = "格式化命令，清理口语助词")
    public String formatCmd(String commandText) {
        JSONArray parseArray = JSON.parseArray(commandText);
        StringBuilder cmds = new StringBuilder();
        for (Object oi : parseArray.toArray()) {
            JSONObject joi = (JSONObject) oi;
            String string = joi.getString("onebest");
            cmds.append(string);
        }
        try {
            String string = clearVoiceWord(cmds.toString());
            return string;
        } catch (Exception e) {
            e.printStackTrace();
            return "会话异常";
        }
    }

    /**
     * 替换字段所有匹配
     *
     * @param label 标签
     * @param field 字段
     * @param from  源
     * @param to    目标
     */
    @ServiceLog(description = "查找Label为label所有node，遍历node.field包含from的数据，并替换内容中from的字符串为to")
    public void replaceAll(String label, String field, String from, String to) {
        String cypher = "Match(n:" + label + ") where n." + field + " CONTAINS '" + from + "' return n";
        List<Map<String, Object>> query = neo4jService.cypher(cypher);
        for (Map<String, Object> mi : query) {
            String fieldContent = string(mi, field);
            String newValue = fieldContent.replaceAll(from, to);
            if (!newValue.equals(fieldContent)) {
                Map<String, Object> copyWithKeys = copyWithKeys(mi, field);
                copyWithKeys.put(field, newValue);
                neo4jService.update(copyWithKeys, id(mi));
            }
        }
    }

    /**
     * 替换单个字段的内容
     *
     * @param label 标签
     * @param field 字段
     * @param from  源
     * @param to    目标
     */
    @ServiceLog(description = "查找Label为label所有节点，遍历node.field包含from的数据，并替换内容中from的字符串为to")
    public void replace(String label, String field, String from, String to) {
        String cypher = "Match(n:" + label + ") return n";
        List<Map<String, Object>> query = neo4jService.cypher(cypher);
        if (query == null) {
            return;
        }
        for (Map<String, Object> mi : query) {
            String fieldContent = string(mi, field);
            // from=from.replaceAll("\\.","\\\\.");
            String newValue = fieldContent.replace(from, to);
            if (!newValue.equals(fieldContent)) {
                Map<String, Object> copyWithKeys = copyWithKeys(mi, field);
                copyWithKeys.put(field, newValue);
                neo4jService.update(copyWithKeys, id(mi));
            }
        }
    }

    @ServiceLog(description = "精确查找Node的label为label参数,且field=from的节点，迭代这些节点，并替换字段内容from为to")
    public void replaceValue(String label, String field, String from, String to) {
        if (from.equals(to)) {
            return;
        }
        String cypher = "Match(n:" + label + "{\"" + field + "\":\"" + from + "\"}) return n";
        List<Map<String, Object>> query = neo4jService.cypher(cypher);
        if (query == null) {
            return;
        }
        for (Map<String, Object> mi : query) {
            if (!from.equals(to)) {
                Map<String, Object> copyWithKeys = copyWithKeys(mi, field);
                copyWithKeys.put(field, to);
                neo4jService.update(copyWithKeys, id(mi));
            }
        }
    }

    /**
     * 解析命令自然语言，中文 第一步：翻译语句，转换为节点关系的操作。返回前端，点击确认。 翻译的结果存入命令执行记录中。记录操作人信息。谁发送的需求。
     * 第二步，根据确认信息，直接执行相关语句，并返回结果。查询直接查询，删除，更新需要进行确认。
     *
     * @param commandText
     * @return
     */
    @ServiceLog(description = "解析命令自然语言，中文 第一步：翻译语句，转换为节点关系的操作。返回前端，点击确认。 翻译的结果存入命令执行记录中。记录操作人信息。谁发送的需求。\n"
            + " 第二步，根据确认信息，直接执行相关语句，并返回结果。查询直接查询，删除，更新需要进行确认。")
    public Map<String, Object> parseTalkMsg(String commandText) {
        Map<String, Object> parseAndexcute = new HashMap<>();
        try {
            String msg = clearVoiceWord(commandText);
            // huoq唤醒词：给{用户或者角色}{添加},什么的什么权限，
            // 将{用户或者角色}的什么的什么权限,删除。
            Map<String, Object> myContext = getMyContext(adminService.getCurrentPasswordId() + "");
            Object returnValue = bsHandle.parse(msg, myContext);
            // 可以执行BeanShell
            if (returnValue != null) {
                recordExcute(commandText, returnValue);
                parseAndexcute.put("status", "200");
                parseAndexcute.put("data", returnValue);
                return parseAndexcute;
            }

            List<Map<String, Object>> parseAuthTalkAndexcute = parseAuthTalkAndexcute(commandText,
                    adminService.getCurrentPasswordId() + "");
            parseAndexcute.put("status", "200");
            parseAndexcute.put("data", parseAuthTalkAndexcute);
            if (!parseAuthTalkAndexcute.isEmpty()) {
                recordExcute(commandText, parseAuthTalkAndexcute);
            }
            return parseAndexcute;
        } catch (Exception e) {
            e.printStackTrace();
            parseAndexcute.put("msg", "会话异常");
            parseAndexcute.put("status", "error");
            parseAndexcute.put("error", e.getMessage());
            return parseAndexcute;
        }
    }

    @ServiceLog(description = "记录执ChatSys行结果")
    public void recordExcute(String commandText, Object parseAuthTalkAndexcute) {
        Map<String, Object> ret = new HashMap<>();
        ret.put("sentence", commandText);
        ret.put(STATUS, "200");
        ret.put("result", parseAuthTalkAndexcute);
        ret.put("userName", adminService.getCurrentName());
        neo4jUService.saveByBody(ret, "ChatSentence");
    }

    /**
     * @param commandText
     * @return
     */
    @ServiceLog(description = "解析并执行命令")
    public Map<String, Object> parseAndExcute(String commandText) {
        Map<String, Object> parseAndexcute = new HashMap<>();
        if (commandText.contains("{") && commandText.contains(":") && commandText.contains("}")) {
            JSONArray parseArray = JSON.parseArray(commandText);
            StringBuilder cmds = new StringBuilder();
            for (Object oi : parseArray.toArray()) {
                JSONObject joi = (JSONObject) oi;
                String string = joi.getString("onebest");
                cmds.append(string);
            }
            try {
                Long currentPasswordId = adminService.getCurrentPasswordId();
                parseAndexcute(cmds.toString(), currentPasswordId + "");
            } catch (Exception e) {
                e.printStackTrace();
                parseAndexcute.put("msg", "会话异常");
                parseAndexcute.put("status", "error");
                parseAndexcute.put("error", e.getMessage());
                return parseAndexcute;
            }
        } else {
            try {
                parseAndexcute = parseAndexcute(commandText, adminService.getCurrentPasswordId() + "");
                return parseAndexcute;
            } catch (Exception e) {
                parseAndexcute.put("msg", "会话异常");
                parseAndexcute.put("status", "error");
                parseAndexcute.put("error", e.getMessage());
                return parseAndexcute;
            }
        }
        return null;
    }

    /**
     * 解析权限命令
     *
     * @param message
     * @param sessionId
     * @return
     */
    @ServiceLog(description = "解析并执行权限相关的相关命令")
    public List<Map<String, Object>> parseAuthTalkAndexcute(String message, String sessionId) {
        List<Map<String, Object>> handleResult = new ArrayList<>();
        setMayConextProp(message, handleResult, sessionId);
        Map<String, Object> userContext = getMyContext(sessionId);
        resetConversation(userContext);
        String currentUserName = adminService.getCurrentUserName();
        userContext.put("userName", currentUserName);
        userContext.put("JsessionId", adminService.getJSessionId(currentUserName));
        // 替换掉声音助词
        String msg = clearVoiceWord(message);
        // 代词替换

        // huoq唤醒词：给{用户或者角色}{添加},什么的什么权限，
        String replaceCount = replaceCount(msg, userContext);
        if (replaceCount != null && !"".equals(replaceCount) && !msg.equals(replaceCount)) {
            Map<String, Object> data = newMap();
            data.put("msg", replaceCount);
            handleResult.add(data);
            return handleResult;
        }

        if (userContext.get(USED).equals(true)) {
            return handleResult;
        }
        // 将{用户或者角色}的什么的什么权限,删除。
//	handleStartWithGei(msg, userContext);
        if (userContext.get(USED).equals(true)) {
            return handleResult;
        }
        handleStartWithJiang(msg, userContext);
        if (userContext.get(USED).equals(true)) {
            return handleResult;
        }
        // 默认的唤醒词
        handleResult = handleStartOpen(msg, userContext);
        if (userContext.get(USED).equals(true) || handleResult != null && !handleResult.isEmpty()) {
            return handleResult;
        }
        handleResult = handleStartManage(msg, userContext);
        if (userContext.get(USED).equals(true) || handleResult != null && !handleResult.isEmpty()) {
            return handleResult;
        } else {
            handleResult = new ArrayList<>();
        }
        // 处理增删改查元数据，实例数据，处理增删改查关系数据
        handleStartCreateRel(msg, userContext);
        handleStartDelRel(msg, userContext);
        handleDelete(msg, userContext);
        handleUpdate(msg, userContext);
        if (userContext.get(USED).equals(true)) {
            handleResult.add(result("\n" + msg + "已执行\n"));
            return handleResult;
        }
        // 查询，添加权限校验。
        String someOf = querySomeOfA(msg, userContext);
        if (userContext.get(USED).equals(true) || someOf != null && !someOf.isBlank()) {
            handleResult.add(result(someOf));
            return handleResult;
        }

        if (msg.endsWith("\\?") || msg.endsWith("？")) {
            String handleAandB = queryAandB(msg, userContext);
            if (userContext.get(USED).equals(true) || handleAandB != null && !handleAandB.isBlank()) {
                handleResult.add(result(handleAandB));
                return handleResult;
            }
        } else {
            if (msg.contains(" ")) {
                String handleAandB = queryAandB(msg, userContext);
                if (userContext.get(USED).equals(true) || handleAandB != null && !handleAandB.isBlank()) {
                    handleResult.add(result(handleAandB));
                    return handleResult;
                }
            }
        }

        handleResult.add(result(msg));
        return handleResult;
    }

    public void resetConversation(Map<String, Object> userContext) {
        userContext.put(USED, false);
        userContext.remove("currentName");
        userContext.remove("which");
    }

    /**
     * 各种帮助信息打印
     *
     * @param msg
     * @param context
     * @return
     */
    public List<Map<String, Object>> handleStartHelp(String msg, Map<String, Object> context) {
        List<Map<String, Object>> data = new ArrayList<>();
        for (String prefix : help) {
            if (msg.startsWith(prefix)) {// 根据角色权限，账号权限，来确定打开范围
                context.put(USED, true);
                msg = msg.replaceFirst(prefix, "");
                if (containLabelInfo(msg)) {
                    Map<String, Object> dataOfKuohao = getDataOfKuohao(msg);
                    String meta = string(dataOfKuohao, "meta");
                    String[] split = strArray(dataOfKuohao, "split");
                    List<Map<String, Object>> metaDataByName = getMetaDataByName(meta);
                    for (Map<String, Object> mi : metaDataByName) {
                        List<Map<String, Object>> dataBy = neo4jUService.getDataBy(label(mi), split[0]);
                        if (dataBy != null) {
                            for (Map<String, Object> di : dataBy) {
                                di.put("name", name(di) + "（" + name(mi) + "）");
                                processOpenData(label(mi), data, di);
                            }
                        }
                    }
                } else {
                    handleOpen(msg, "resource", data, context);
                    if (data.isEmpty()) {
                        handleOpen(msg, "App", data, context);

                    }
                    if (data.isEmpty()) {
                        Map<String, Object> mi = getOrSelectMetaData(msg, context);
                        List<Map<String, Object>> dataBy = neo4jUService.getDataBy(label(mi), msg);
                        if (dataBy != null) {
                            for (Map<String, Object> di : dataBy) {
                                di.put("name", name(di) + "（" + name(mi) + "）");
                                processOpenData(label(mi), data, di);
                            }
                        }
                    }
                }
            }
        }
        return data;
    }

    /**
     * 打开xx, 打开xx（元数据信息）
     *
     * @param msg
     * @param context
     * @return
     */
    public List<Map<String, Object>> handleStartOpen(String msg, Map<String, Object> context) {
        List<Map<String, Object>> data = new ArrayList<>();

        for (String prefix : openWord) {
            if (msg.startsWith(prefix)) {// 根据角色权限，账号权限，来确定打开范围
                context.put(USED, true);
                msg = msg.replaceFirst(prefix, "");
                if (containLabelInfo(msg)) {
                    Map<String, Object> dataOfKuohao = getDataOfKuohao(msg);
                    String meta = string(dataOfKuohao, "meta");
                    String[] split = strArray(dataOfKuohao, "split");
                    List<Map<String, Object>> metaDataByName = getMetaDataByName(meta);
                    for (Map<String, Object> mi : metaDataByName) {
                        List<Map<String, Object>> dataBy = neo4jUService.getDataBy(label(mi), split[0]);
                        if (dataBy != null) {
                            for (Map<String, Object> di : dataBy) {
                                di.put("name", name(di) + "（" + name(mi) + "）");
                                processOpenData(label(mi), data, di);
                            }
                        }
                    }
                } else {
                    handleOpen(msg, "resource", data, context);
                    if (data.isEmpty()) {
                        handleOpen(msg, "App", data, context);
                    }
                    if (data.isEmpty()) {
                        Map<String, Object> mi = getOrSelectMetaData(msg, context);
                        List<Map<String, Object>> dataBy = neo4jUService.getDataBy(label(mi), msg);
                        if (dataBy != null) {
                            for (Map<String, Object> di : dataBy) {
                                di.put("name", name(di) + "（" + name(mi) + "）");
                                processOpenData(label(mi), data, di);
                            }
                        }
                    }
                }
            }
        }
        return data;
    }

    public Map<String, Object> getDataOfKuohao(String msgx) {
        String meta = null;
        String[] split = msgx.split("\\(");
        if (split.length < 2) {
            split = msgx.split(cnLeftKuoHao);
        }
        String msg = split[1];

        if (msg.endsWith(rightKuohao)) {
            meta = msg.replace(rightKuohao, "");
        } else if (msg.endsWith(cnRightKuoHao)) {
            meta = msg.replace(cnRightKuoHao, "");
        }
        Map<String, Object> data = newMap();
        data.put("split", String.join(",", split));
        data.put("meta", meta);

        return data;
    }

    public Map<String, Object> getOrSelectMetaData(String msg, Map<String, Object> context) {
        List<Map<String, Object>> metaDataByName = getMetaDataByName(msg);
        context.put("currentName", msg);
        Map<String, Object> mi = userSelect(context, metaDataByName);
        return mi;
    }

    public List<Map<String, Object>> handleStartManage(String msg, Map<String, Object> context) {
        List<Map<String, Object>> data = new ArrayList<>();
        String prefix = "管理";
        if (msg.startsWith(prefix)) {// 根据角色权限，账号权限，来确定打开范围
            handleManage(msg, META_DATA, data, prefix, context);
        }
        return data;
    }

    @ServiceLog(description = "已执行为开的命令，执行：包含Content的BeanShell脚本。先定义BeanShell")
    public List<Map<String, Object>> handleStartExcute(String msg, Map<String, Object> context) {
        List<Map<String, Object>> data = new ArrayList<>();
        String prefix = "执行";
        if (msg.startsWith(prefix)) {// 执行脚本，根据名称或者code来查找脚本
            handleExecute(msg, data, prefix, context);
        }
        return data;
    }

    /**
     * 查询节点之间的关系
     *
     * @param msg
     * @param context
     * @return
     */
    @ServiceLog(description = "查询两点之间的关系")
    public String queryAandB(String msg, Map<String, Object> context) {
        StringBuilder sb = new StringBuilder();
        msg = msg.trim().replaceAll("\\?", "").replaceAll("？", "");
        List<String> query = new ArrayList<>();
        query.add("有什么关系");
        query.add("有什么");
        query.add("是什么关系");
        query.add("是否可达");
        for (String si : query) {
            if (msg.endsWith(si)) {// 根据角色权限，账号权限，来确定打开范围
                context.put(USED, true);
                String string = msg.split(si)[0];
                String handleABPath = handleABPath(string, context);
                if (handleABPath != null) {
                    sb.append(handleABPath);
                }
            }
        }
        if (sb.length() < 1 && msg.contains(" ")) {
            String[] split = msg.split(" ");
            if (split.length == 2) {
                context.put(USED, true);
                Long idStart = getIdOfData(split[0], context);

                Long idEnd = getIdOfData(split[1], context);
                if (idStart != null && idEnd != null) {
                    String showPathInfo = adminService.showPathInfo(idStart, idEnd);
                    if (showPathInfo != null) {
                        sb.append(showPathInfo);
                    } else {
                        relationEndData(sb, split, idStart);
                    }
                } else {
                    relationEndData(sb, split, idStart);
                }
            }
            // if(split.length>2) {
            //
            // }
        }
        return sb.toString();
    }

    public void relationEndData(StringBuilder sb, String[] split, Long idStart) {
        List<Map<String, Object>> dd = neo4jUService.getDataBy("RelationDefine", split[1]);
        for (Map<String, Object> relMap : dd) {
            if (relMap != null) {
                Callable<String> callabel = () -> {
                    StringBuilder sbx = new StringBuilder();

                    String relCode = string(relMap, "reLabel");
                    String startLabel = string(relMap, "startLabel");
                    String endLabel = string(relMap, "endLabel");
                    String endsQuery = "MATCH (s:" + startLabel + ")-[r:" + relCode + "]->(e:" + endLabel + ") where id(s)="
                            + idStart + " return e";
                    List<Map<String, Object>> ends = neo4jService.cypher(endsQuery);
                    for (Map<String, Object> ei : ends) {
                        if (sbx.length() > 1) {
                            sbx.append("、");
                        }
                        sbx.append(neo4jUService.seeNode(ei));
                    }
                    return sbx.toString();
                };
                String vt = VtPool.vt(callabel);
                sb.append(vt);
//		    String relCode = string(relMap, "reLabel");
//		    String startLabel = string(relMap, "startLabel");
//		    String endLabel = string(relMap, "endLabel");
//		    String endsQuery = "MATCH (s:" + startLabel + ")-[r:" + relCode + "]->(e:" + endLabel + ") where id(s)="
//			    + idStart + " return e";
//		    List<Map<String, Object>> ends = neo4jService.cypher(endsQuery);
//		    for (Map<String, Object> ei : ends) {
//			if (sb.length() > 1) {
//			    sb.append("、");
//			}
//			sb.append(neo4jUService.seeNode(ei));
//		    }
            }
        }


    }

    public List<Map<String, Object>> relOf(Long idStart, Long idEnd) {
        return neo4jUService
                .cypher("MATCH (a)-[r]-(b) where id(a)=" + idStart + " and id(b)=" + idEnd + " return distinct r");
    }

    /**
     * 查询谁的属性是什么，关系有哪些
     *
     * @param msg
     * @return
     */
    @ServiceLog(description = "查询谁的属性是什么，关系有哪些，并返回答案，在Context中有是否执行")
    public String querySomeOfA(String msg, Map<String, Object> conversationContext) {
        StringBuilder sb = new StringBuilder();
        msg = msg.trim().replaceAll("\\?", "").replaceAll("？", "");
        // 并行概率执行
        List<String> query = getRelationQuery();

        String startQuery = null;
        String usedOkQuery = null;
        for (String si : query) {
            if (!bool(conversationContext, USED) && sb.length() < 1 && msg.contains(si)) {
                if (usedOkQuery != null && usedOkQuery.startsWith(si)) {
                    break;
                }
                conversationContext.put(USED, true);
                String[] queryPart = msg.split(si);
                startQuery = queryPart[0];
                if (queryPart.length > 1) {
                    conversationContext.put("answer", startQuery + si.substring(0, 1) + queryPart[1]);
                } else {
                    conversationContext.put("answer", startQuery + si.substring(0, 1));
                }

                List<Map<String, Object>> datas = null;
                // xx的xx是，xx的ss有
                for (String owni : ownWords) {
                    if (startQuery.contains(owni)) {
                        conversationContext.put("hasOwni", true);
                        LoggerTool.info(logger,msg + "包含所属关键字:" + owni);
                        String[] owns = startQuery.split(owni);
                        String startName = owns[0];
                        String por = owns[1];// 获取元数据信息
                        Map<String, String> metaNameCol = getMetaData();
                        for (Map<String, Object> oMdi : getMetaDataByName(startName)) {
                            String metaColi = metaNameCol.get(por);
                            if (metaColi != null) {
                                String string = string(oMdi, metaColi);
                                return string;
                            }
                        }

                        datas = queryPropRelOf(conversationContext, sb, startQuery, usedOkQuery, si, owni);
                    }
                }
                if (!bool(conversationContext, "hasOwni") && queryPart.length > 1) {
                    String startName = queryPart[0];
                    String endName = null;
                    if (queryPart[1].length() > 0) {
                        // 党员有xx的哪些xx：党员有待办的那些权限
                        for (String owni : ownWords) {
                            if (queryPart[1].contains(owni)) {
                                LoggerTool.info(logger,"rightHasOwni:" + queryPart[1]);
                                conversationContext.put("rightHasOwni", true);
                                String[] owns = queryPart[1].split(owni);
                                endName = owns[0];
                                String por = owns[1];// 获取元数据信息
                                if (endName != null) {
                                    conversationContext.put("endName", endName);
                                }
                                if (por != null) {
                                    por = replaceQueryWord(por);
                                    // context.put("rName", por);
                                }
                                datas = queryPropOrRelOf(sb, usedOkQuery, si, startName, endName, por,
                                        conversationContext);
                            }
                        }

                        if (!bool(conversationContext, "rightHasOwni")) {
                            LoggerTool.info(logger,"right not HasOwni:" + queryPart[1]);
                            // xx有哪些ss
                            if (queryPart[1].startsWith("哪些")) {
                                endName = replaceQueryWord(queryPart[1]);
                            }
                            conversationContext.put("endName", queryPart[1]);
                            if (usedOkQuery != null && si.startsWith(usedOkQuery)) {
                                continue;
                            }
                            endName = string(conversationContext, "endName");
                            endName = endName.trim().replaceAll("\\?", "").replaceAll("？", "");
                            if (endName.length() > 1) {
                                Map<String, Object> endMeta = getData(endName, META_DATA, conversationContext);
                                if (endMeta != null) {
                                    // 直接关系数据，一层关系
                                    String zhiJieRelData = getZhiJieRelation(msg, conversationContext, startQuery,
                                            startName, endMeta);
                                    // 获取直接关系
                                    datas = neo4jUService.cypher(zhiJieRelData);
                                    if (datas != null && datas.size() > 0) {
                                        seeEndNode(sb, datas);
                                    } else {
                                        if (datas == null) {
                                            datas = new ArrayList<>();
                                        }
                                        queryRelPathEnds(sb, startQuery, datas, endName, endMeta, conversationContext);
                                    }
                                } else {
                                    LoggerTool.info(logger,"endMeta is null to see relation");
                                    seeRelation(sb, endName, startName, conversationContext);
                                }
                            } else {
                                seeRelation(sb, endName, startName, conversationContext);
                            }
                        }
                    } else {
                        seeRelation(sb, endName, startName, conversationContext);
                        Map<String, Object> selectedOne = selectedData(conversationContext, startName);
                        datas = neo4jUService.cypher(
                                "MATCH (n)-[r*1..3]->(m) WHERE id(n)=" + id(selectedOne) + "  RETURN distinct m ");
                        seeEndNode(sb, datas);
                    }
                }

                if (datas != null && !datas.isEmpty()) {
                    usedOkQuery = si;
                }
            }
        }
        if (conversationContext.get(USED).equals(true)) {
            if (sb.toString().length() < 1) {
                return string(conversationContext, "answer") + "，答：系统中无此信息\n";
            }

            String answerQ = "\n" + string(conversationContext, "answer") + ":<BR>";
            if (bool(conversationContext, "hasOwni")) {
                answerQ = "\n" + string(conversationContext, "answer") + ":\n";
            }
            return answerQ + sb.toString();
        }

        return null;
    }

    public String getZhiJieRelation(String msg, Map<String, Object> conversationContext, String startQuery,
                                    String startName, Map<String, Object> endMeta) {
        String zhiJieRelData = null;
        if (containLabelInfo(startName)) {
            Map<String, Object> onlyContext = new HashMap<>();
            startName = onlyName(onlyContext, startName);
            String dataLabel = string(onlyContext, "dataLabel");
            // Map<String, Object> mapObject = mapObject(onlyContext, "dataMd");
            String preId = "MATCH (n:" + dataLabel + ")-[r:HAS_PERMISSION]-(m:" + label(endMeta) + ")  where n.name='"
                    + startQuery + "'   return distinct id(n) AS id";
            List<Map<String, Object>> preNode = neo4jUService.cypher(preId);

            if (preNode != null && !preNode.isEmpty()) {
                if (msg.endsWith("有哪些角色")) {
                    if (preNode.size() > 1) {
                        zhiJieRelData = userSelectQueryPermissionEnd(conversationContext, startName, endMeta,
                                dataLabel);
                    } else {
                        zhiJieRelData = "MATCH (n:" + dataLabel + ")-[r:HAS_PERMISSION]-(m:" + label(endMeta)
                                + ")  where n.name='" + startQuery + "'   return distinct m ";
                    }
                } else {
                    if (preNode.size() > 1) {
                        zhiJieRelData = userSelectQueryEnd(conversationContext, startName, endMeta, dataLabel);
                    } else {
                        zhiJieRelData = "MATCH (n)-[r]-(m:" + label(endMeta) + ")  where n.name='" + startQuery
                                + "'   return distinct m ";
                    }
                }
            }
        } else {
            // 预先判断满足查询条件的数据是否存在
            String preLabel = "MATCH (n)-[r]-(m:" + label(endMeta) + ")  where n.name='" + startQuery
                    + "' with  n unwind labels(n) as x  return distinct x";
            List<Map<String, Object>> preLabels = neo4jUService.cypher(preLabel);

            if (preLabels != null && !preLabels.isEmpty()) {
                if (preLabels.size() > 1) {// 多种节点，需要用户选择数据
                    zhiJieRelData = userSelectAuthEnd(msg, conversationContext, startName, endMeta, preLabels);
                } else {
                    zhiJieRelData = oneLableUserSelectedStart(conversationContext, startQuery, startName, endMeta,
                            preLabels);
                }
            } else {
                zhiJieRelData = "MATCH (n)-[r]-(m:" + label(endMeta) + ")  where n.name='" + startQuery
                        + "'   return distinct m ";
            }

        }
        return zhiJieRelData;
    }

    /**
     * 有确定的Label。返回查询结果
     *
     * @param conversationContext
     * @param startQuery
     * @param startName
     * @param endMeta
     * @param preLabels
     * @return
     */
    public String oneLableUserSelectedStart(Map<String, Object> conversationContext, String startQuery,
                                            String startName, Map<String, Object> endMeta, List<Map<String, Object>> preLabels) {
        String zhiJieRelData = null;
        String oneLabel = string(preLabels.get(0), "x");
        String preId = "MATCH (n:" + oneLabel + ")-[r]-(m:" + label(endMeta) + ")  where n.name='" + startQuery
                + "'   return distinct id(n) AS id";
        List<Map<String, Object>> preNode = neo4jUService.cypher(preId);
        if (preNode != null && !preNode.isEmpty()) {
            // 有多条数据，用户选择，否则直接返回
            if (preNode.size() > 1) {
                Long startId = getStartIdBy(conversationContext, startName, oneLabel);
                zhiJieRelData = "MATCH (n)-[r]-(m:" + label(endMeta) + ")  where id(n)=" + startId
                        + "  return distinct m ";
            } else {
                zhiJieRelData = "MATCH (n:" + oneLabel + ")-[r]-(m:" + label(endMeta) + ")  where n.name='" + startQuery
                        + "'   return distinct m ";
            }
        }
        return zhiJieRelData;
    }

    public Long getStartIdBy(Map<String, Object> conversationContext, String startName, String oneLabel) {
        Map<String, Object> selectedOne = getData(startName, oneLabel, conversationContext);
        Long startId = id(selectedOne);
        return startId;
    }

    public String userSelectAuthEnd(String msg, Map<String, Object> conversationContext, String startName,
                                    Map<String, Object> endMeta, List<Map<String, Object>> preLabels) {
        String zhiJieRelData = null;

        Long startId = getStartIdBy(conversationContext, startName, preLabels);
        if (msg.endsWith("有哪些角色")) {
            zhiJieRelData = "MATCH (n)-[r:HAS_PERMISSION]-(m:" + label(endMeta) + ")  where id(n)=" + startId
                    + " with n unwind labels(n) as x  where x in() return distinct m ";
        } else {
            zhiJieRelData = "MATCH (n)-[r]-(m:" + label(endMeta) + ")  where id(n)=" + startId + "  return distinct m ";
        }
        return zhiJieRelData;
    }

    public Long getStartIdBy(Map<String, Object> conversationContext, String startName,
                             List<Map<String, Object>> preLabels) {
        Map<String, Object> selectedOne = selectedData(conversationContext, startName, preLabels);
        Long startId = id(selectedOne);
        return startId;
    }

    public String userSelectQueryEnd(Map<String, Object> conversationContext, String startName,
                                     Map<String, Object> endMeta, String dataLabel) {
        Map<String, Object> selectedOne = null;
        String zhiJieRelData = null;
        if (dataLabel != null) {
            selectedOne = getData(startName, dataLabel, conversationContext);
            Long startId = id(selectedOne);
            zhiJieRelData = "MATCH (n:" + dataLabel + ")-[r]-(m:" + label(endMeta) + ")  where id(n)=" + startId
                    + "  return distinct m ";
        }
        return zhiJieRelData;
    }

    public String userSelectQueryPermissionEnd(Map<String, Object> conversationContext, String startName,
                                               Map<String, Object> endMeta, String dataLabel) {
        String zhiJieRelData;
        Map<String, Object> selectedOne;
        if (dataLabel != null) {
            selectedOne = getData(startName, dataLabel, conversationContext);
            Long startId = id(selectedOne);
            zhiJieRelData = "MATCH (n:" + dataLabel + ")-[r:HAS_PERMISSION]-(m:" + label(endMeta) + ")  where id(n)="
                    + startId + "  return distinct m ";
        } else {
            selectedOne = getData(startName, conversationContext);
            Long startId = id(selectedOne);
            zhiJieRelData = "MATCH (n)-[r:HAS_PERMISSION]-(m:" + label(endMeta) + ")  where id(n)=" + startId
                    + "  return distinct m ";
        }
        return zhiJieRelData;
    }

    /**
     * @param context
     * @param startName
     * @return
     */
    @ServiceLog(description = "StartName包含（元数据信息），返回去除元数据的名称，并在Context中，返回 context.put(\"dataLabel\", label(di));\n"
            + "		    context.put(\"dataMd\", di);信息")
    public String onlyName(Map<String, Object> context, String startName) {
        String name = getMetaInfo(startName, zuoKuohao, context);
        if (startName.equals(name)) {
            name = getMetaInfo(startName, cnLeftKuoHao, context);
        }
        return name;
    }

    /**
     * 获取某些对象的某些资源
     *
     * @param sb
     * @param startQuery
     * @param datas
     * @param endName
     * @param endMeta
     */
    public void getSomethingOfSb(StringBuilder sb, String startQuery, List<Map<String, Object>> datas, String endName,
                                 Map<String, Object> endMeta, Map<String, Object> context) {
        Boolean hasPermission = endName.contains("权限");
        String queryData = "MATCH (m:" + label(endMeta) + ") return distinct m";
        List<Map<String, Object>> ends = neo4jUService.cypher(queryData);
        Map<String, Object> data2 = getData(startQuery, context);
        Long startId = id(data2);
        if (hasPermission) {
            for (Map<String, Object> ei : ends) {
                List<Map<String, Object>> authOfRi = new ArrayList<>();
                String endCode = code(ei);

                queryData = "MATCH (n)-[r:HAS_PERMISSION]-(m:" + META_DATA + ")  where id(n)=" + startId;
                boolean endCodeNotNull = endCode != null && !"null".equals(endCode);
                if (endCodeNotNull) {
                    queryData = queryData + " and r.code contains '" + endCode + "'";
                }

                queryData = queryData + " return distinct m ";

                List<Map<String, Object>> auths = neo4jUService.cypher(queryData);
                if (auths != null && !auths.isEmpty()) {
                    authOfRi.addAll(auths);
                }
                queryData = "MATCH (n)-[r:HAS_PERMISSION]->(ro:Role)-[r1:HAS_PERMISSION]->(m:" + META_DATA
                        + ")  where id(n)=" + startId;
                if (endCodeNotNull) {
                    queryData = queryData + " and r1.code contains \"" + endCode + "\"  ";
                }
                queryData = queryData + " return distinct m ";
                List<Map<String, Object>> auth2s = neo4jUService.cypher(queryData);
                if (auth2s != null && !auth2s.isEmpty()) {
                    authOfRi.addAll(auth2s);
                }
                if (authOfRi.size() > 0) {
                    for (Map<String, Object> ai : authOfRi) {
                        ai.put("HAS_PERMISSION", name(ei));
                        datas.add(ai);
                    }
                }
            }
            if (datas.size() > 0) {
                seeEndNodeAuth(sb, datas);
            }
        } else {
            for (Map<String, Object> ri : ends) {
                List<Map<String, Object>> itsPropRel = new ArrayList<>();
                String endCode = code(ri);
                queryData = "MATCH (n)-[r]-(m:" + META_DATA + ")  where id(n)=" + startId;
                boolean endCodeNotNull = endCode != null && !"null".equals(endCode);
                if (endCodeNotNull) {
                    queryData = queryData + " and r.code contains '" + endCode + "'";
                }
                queryData = queryData + " return distinct m ";

                List<Map<String, Object>> auths = neo4jUService.cypher(queryData);
                if (auths != null && !auths.isEmpty()) {
                    itsPropRel.addAll(auths);
                }

                queryData = "MATCH (n)-[r]->(ro:Role)-[r1]->(m:" + META_DATA + ")  where id(n)=" + startId;

                if (endCodeNotNull) {
                    queryData = queryData + " and r1.code contains \"" + endCode + "\"  ";
                }
                queryData = queryData + " return distinct m ";

                List<Map<String, Object>> auth2s = neo4jUService.cypher(queryData);
                if (auth2s != null && !auth2s.isEmpty()) {
                    itsPropRel.addAll(auth2s);
                }
                if (itsPropRel.size() > 0) {
                    for (Map<String, Object> ai : itsPropRel) {
                        ai.put("HAS_", name(ri));
                        datas.add(ai);
                    }
                }
            }
            if (datas.size() > 0) {
                seeEndNodePropRel(sb, datas);
            }
        }
    }

    /**
     * 查询间接关系，多关系
     *
     * @param sb
     * @param startQuery
     * @param datas
     * @param endName
     * @param endMeta
     */
    public void queryRelPathEnds(StringBuilder sb, String startQuery, List<Map<String, Object>> datas, String endName,
                                 Map<String, Object> endMeta, Map<String, Object> context) {
        String queryData;
        Boolean hasPermission = endName.contains("权限");
        if (hasPermission) {// 权限类型的关系数据，某一大类的关系，多个类型的关系遍历查询。
            LoggerTool.info(logger,"hasPermission:" + endName);
            queryEndsOfAllRelationType(sb, startQuery, datas, endMeta, context);
        } else {// 不是权限类型的关系，终点明确，起点明确。

            // START n=node(*),m=node(*)
            // MATCH p=n-[r*1..]-m
            // WITH count(p) AS totalPaths,n,m
            // WHERE totalPaths>1
            // RETURN n,m,totalPaths
            // LIMIT 2

            // 间接关系、属性
            Map<String, Object> data2 = getData(startQuery, context);
            if (data2 == null) {
                return;
            }
            LoggerTool.info(logger,"getData:" + mapString(data2));
            queryData = "MATCH (n)-[r*2..3]-(m:" + label(endMeta) + ")  where id(n)=" + id(data2);
            if (label(endMeta).equals(META_DATA)) {
                queryData = queryData + " and m.label<>'" + META_DATA + "'";
            }

            queryData = queryData + " return distinct m ";

            List<Map<String, Object>> auths = neo4jUService.cypher(queryData);
            if (auths.size() > 0) {
                for (Map<String, Object> ai : auths) {
                    ai.put("HAS_", name(ai));
                    datas.add(ai);
                }
            }
            if (datas != null && datas.size() > 0) {
                seeEndNode(sb, datas);
            }

        }
    }

    /**
     * @param sb
     * @param startQuery
     * @param datas
     * @param endMeta    类型：关联终点的关系类型的定义元数据
     */
    public void queryEndsOfAllRelationType(StringBuilder sb, String startQuery, List<Map<String, Object>> datas,
                                           Map<String, Object> endMeta, Map<String, Object> context) {
        String queryData = "MATCH (m:" + label(endMeta) + ") return distinct m";

        Map<String, Object> data2 = getData(startQuery, context);
        if (data2 == null) {
            return;
        }
        List<Map<String, Object>> ends = neo4jUService.cypher(queryData);
        for (Map<String, Object> ei : ends) {
            List<Map<String, Object>> authOfRi = new ArrayList<>();
            String endCode = code(ei);
            boolean endCodeNotNull = endCode != null && !"null".equals(endCode);
            queryData = "MATCH (n)-[r:HAS_PERMISSION]-(m:" + META_DATA + ")  where id(n)=" + id(data2);
            if (endCodeNotNull) {
                queryData = queryData + " OR r.code contains '" + endCode + "'";
            }

            queryData = queryData + " return distinct m ";

            List<Map<String, Object>> auths = neo4jUService.cypher(queryData);
            if (auths != null && !auths.isEmpty()) {
                authOfRi.addAll(auths);
            }

            queryData = "MATCH (n)-[r:HAS_PERMISSION]->(ro:Role)-[r1:HAS_PERMISSION]->(m:" + META_DATA
                    + ")  where  id(n)=" + id(data2);

            if (endCodeNotNull) {
                queryData = queryData + " AND r1.code contains '" + endCode + "'";
            }

            queryData = queryData + " return distinct m ";
            List<Map<String, Object>> auth2s = neo4jUService.cypher(queryData);
            if (auth2s != null && !auth2s.isEmpty()) {
                authOfRi.addAll(auth2s);
            }
            if (authOfRi.size() > 0) {
                for (Map<String, Object> ai : authOfRi) {
                    ai.put("HAS_PERMISSION", name(ei));
                    datas.add(ai);
                }
            }
        }
        if (datas.size() > 0) {
            seeEndNodeAuth(sb, datas);
        }
    }

    /**
     * xx的xx是，xx的ss有
     *
     * @param context
     * @param sb
     * @param startQuery
     * @param usedOkQuery
     * @param si
     * @param owni
     * @return
     */
    public List<Map<String, Object>> queryPropRelOf(Map<String, Object> context, StringBuilder sb, String startQuery,
                                                    String usedOkQuery, String si, String owni) {
        List<Map<String, Object>> datas = new ArrayList<>();
        String[] owns = startQuery.split(owni);
        String startName = owns[0];
        String por = owns[1];// 获取元数据信息

        if (containLabelInfo(startName)) {
            LoggerTool.info(logger,startName + "包含括号");
            Map<String, Object> onlyContext = new HashMap<>();
            startName = onlyName(onlyContext, startName);
            String dataLabel = string(onlyContext, "dataLabel");
            Map<String, Object> mapObject = mapObject(onlyContext, "dataMd");
            String coli = null;
            Map<String, String> nameColumn = nameColumn(mapObject);
            if (nameColumn.get(por) != null) {
                coli = nameColumn.get(por);
            }
            if (coli != null) {// 获取自定义字段，关联字段数据信息
                seeProperty(sb, startName, coli, mapObject, context);
            } else {// 查询关系
                if (usedOkQuery != null && si.startsWith(usedOkQuery)) {
                    return datas;
                }
                if (datas.isEmpty()) {
                    datas = neo4jUService.cypher(
                            "MATCH (n:" + dataLabel + ")-[r]->(m) WHERE r.name = '" + por + "'  return distinct m ");
                    seeEndNode(sb, datas);
                }
                if (datas == null || datas.isEmpty()) {
                    Map<String, Object> endMeta = getData(por, META_DATA, context);
                    datas = neo4jUService.cypher("MATCH (n:" + dataLabel + ")-[r]->(m:" + label(endMeta)
                            + ") WHERE r.name = '" + por + "'  RETURN m ");
                    seeEndNode(sb, datas);
                }
            }
        } else {
            for (Map<String, Object> oMdi : getMetaDataByName(startName)) {
                String coli = null;
                Map<String, String> nameColumn = nameColumn(oMdi);
                if (nameColumn.get(por) != null) {
                    coli = nameColumn.get(por);
                }
                if (coli != null) {// 获取自定义字段，关联字段数据信息
                    seeProperty(sb, startName, coli, oMdi, context);
                } else {// 查询关系
                    if (usedOkQuery != null && si.startsWith(usedOkQuery)) {
                        continue;
                    }
                    Map<String, Object> selectedOne = selectedData(context, startName);
                    if (selectedOne == null){
                        continue;
                    }
                    if (datas == null || datas.isEmpty()) {
                        datas = neo4jUService.cypher("MATCH (n)-[r]->(m) WHERE id(n)=" + id(selectedOne) + " and r.name = '"
                                + por + "'  return distinct m ");
                        seeEndNode(sb, datas);
                    }
                    if (datas == null || datas.isEmpty()) {
                        Map<String, Object> endMeta = getData(por, META_DATA, context);
                        datas = neo4jUService.cypher("MATCH (n)-[r]->(m:" + label(endMeta) + ") where id(n)="
                                + id(selectedOne) + "  RETURN m ");
                        seeEndNode(sb, datas);
                    }
                }
            }
        }

        return datas;
    }

    /**
     * //党员(元数据)有xx的哪些xx：xx有待办的那些权限
     *
     * @param sb
     * @param usedOkQuery
     * @param si
     * @param startName
     * @param endName
     * @param por
     * @return
     */
    public List<Map<String, Object>> queryPropOrRelOf(StringBuilder sb, String usedOkQuery, String si, String startName,
                                                      String endName, String por, Map<String, Object> context) {
        List<Map<String, Object>> datas = new ArrayList<>();
        List<Map<String, Object>> endMetas = getMetaDataByName(endName);
        String dataLabel = null;
        if (containLabelInfo(startName)) {// 带元数据信息的开始节点
            LoggerTool.info(logger,"startName containLabelInfo:" + startName);
            Map<String, Object> onlyContext = new HashMap<>();
            startName = onlyName(onlyContext, startName);
            dataLabel = string(onlyContext, "dataLabel");
        }
        Map<String, Object> selectedOne = selectedData(context, startName);
        if(selectedOne==null){
            return datas;
        }
        LoggerTool.info(logger,"startName:{} metaInfo:{} ,selectedOne:{}", startName, dataLabel, mapString(selectedOne));
        for (Map<String, Object> oMdi : endMetas) {
            if (datas.size() > 0) {
                return datas;
            }
            Map<String, String> nameColumn = nameColumn(oMdi);
            String coli = null;
            if (nameColumn.get(por) != null) {
                coli = nameColumn.get(por);
            }
            if (coli != null) {// 获取自定义字段，关联字段数据信息
                seeProperty(sb, startName, coli, oMdi, context);
            } else {// 查询关系
                if (usedOkQuery != null && si.startsWith(usedOkQuery)) {
                    continue;
                }

                Long startId = id(selectedOne);
                if (datas.isEmpty()) {
                    String relQuery = "MATCH (n)-[r]->(m:" + label(oMdi) + ") WHERE id(n)=" + startId
                            + "  AND r.name = '" + por + "' or r.code='" + por + "'  return r ";
                    if (dataLabel != null) {
                        relQuery = "MATCH (n:" + dataLabel + ")-[r]->(m:" + label(oMdi) + ") WHERE id(n)=" + startId
                                + " AND r.name = '" + por + "' or r.code='" + por + "'  return r ";
                    }
                    collectData(sb, datas, relQuery);
                }
                if (datas.isEmpty()) {

                    String relQuery = "MATCH (n)-[r]->(m:MetaData) WHERE id(n)=" + startId + " and( r.name = '" + por
                            + "' OR r.code='" + por + "' and m.label='" + label(oMdi) + "'  RETURN  r ";
                    if (dataLabel != null) {
                        relQuery = "MATCH (n:" + dataLabel + ")-[r]->(m:MetaData) WHERE id(n)=" + startId
                                + " and( r.name = '" + por + "' OR r.code='" + por + "' and m.label='" + label(oMdi)
                                + "'  RETURN  r ";
                    }
                    collectData(sb, datas, relQuery);
                }

                if (datas.isEmpty()) {
                    List<Map<String, Object>> metaDataByName2 = getMetaDataByName(por);
                    for (Map<String, Object> ri : metaDataByName2) {
                        List<Map<String, Object>> listAllByLabel = neo4jUService.listAllByLabel(label(ri));
                        for (Map<String, Object> rdatai : listAllByLabel) {
                            String relQuery = "MATCH (n)-[r]->(m:MetaData) WHERE id(n)=" + startId + " and  r.code='"
                                    + code(rdatai) + "' and m.label='" + label(oMdi) + "'  RETURN  r ";
                            if (dataLabel != null) {
                                relQuery = "MATCH (n:" + dataLabel + ")-[r]->(m:MetaData) WHERE id(n)=" + startId
                                        + " and  r.code='" + code(rdatai) + "' and m.label='" + label(oMdi)
                                        + "'  RETURN  r ";
                            }
                            List<Map<String, Object>> datasi = neo4jUService.cypher(relQuery);
                            if (datasi != null && !datasi.isEmpty()) {
                                datas.addAll(datasi);
                                if (sb.length() > 0) {
                                    sb.append("、");
                                }
                                sb.append(name(rdatai));
                            }
                        }
                    }
                }
            }
        }
        return datas;
    }

    public void collectData(StringBuilder sb, List<Map<String, Object>> datas, String relQuery) {
        List<Map<String, Object>> datasi = neo4jUService.cypher(relQuery);
        if (datasi != null && !datasi.isEmpty()) {
            datas.addAll(datasi);
            seeEndNode(sb, datasi);
        }
    }

    public String replaceQueryWord(String por) {
        if (por.startsWith("哪些")) {

            por = por.replaceFirst("哪些", "");
        }
        return por;
    }

    /**
     * 根据名称获取元数据信息
     *
     * @param metaName
     * @return
     */
    @ServiceLog(description = "根据参数获取元数据信息")
    public List<Map<String, Object>> getMetaDataByName(String metaName) {
        List<Map<String, Object>> ownerMetas;

        String getMetaInfo = " MATCH (m:MetaData) where  m.name='" + metaName + "'  return distinct m";
        ownerMetas = neo4jUService.cypher(getMetaInfo);
        if (ownerMetas != null && !ownerMetas.isEmpty()) {
            return ownerMetas;
        }
        getMetaInfo = "MATCH (n) WHERE n.name = '" + metaName + "' unwind labels(n) AS x return x";
        ownerMetas = neo4jUService.cypher(getMetaInfo);
        List<Object> obs = new ArrayList<>();
        for (Map<String, Object> omi : ownerMetas) {
            String labeli = string(omi, "x");
            obs.add(labeli);
        }

        if (!obs.isEmpty()) {
            getMetaInfo = " MATCH (m:MetaData) where  m.label in (" + joinStr(obs) + " ) return distinct m";
            ownerMetas = neo4jUService.cypher(getMetaInfo);
        }

        return ownerMetas;
    }

    /**
     * 获取元数据的定义信息，并返回字段表头信息
     *
     * @return
     */
    @ServiceLog(description = "获取元数据的定义信息，并返回字段表头信息")
    public Map<String, String> getMetaData() {
        String getMetaInfo = " MATCH (m:MetaData) where  m.label='" + META_DATA + "'  return distinct m";
        List<Map<String, Object>> ownerMetas = neo4jService.cypher(getMetaInfo);
        if (ownerMetas != null && !ownerMetas.isEmpty()) {
            return nameColumn(ownerMetas.get(0));
        }
        return null;
    }

    public void seeRelation(StringBuilder sb, String si, String startName, Map<String, Object> userContext) {

        if (containLabelInfo(startName)) {// 包含Label信息
            LoggerTool.info(logger,"startName contains MetaInfo :{}", startName);
            Map<String, Object> onlyContext = new HashMap<>();
            startName = onlyName(onlyContext, startName);
            String dataLabel = string(onlyContext, "dataLabel");
            Map<String, Object> oneData = getData(startName, dataLabel, userContext);
            if (oneData != null && !oneData.isEmpty()) {
                String relation = "MATCH(n:" + dataLabel + ")-[r]-(m) where id(n)=" + id(oneData)
                        + " return type(r) AS rCode,r.name AS rName,m";
                List<Map<String, Object>> itsRelation = neo4jUService.cypher(relation);
                for (Map<String, Object> ri : itsRelation) {
                    String rName = string(ri, "rName");
                    if (rName != null && rName.contains(si)) {
                        if (sb.length() > 0) {
                            sb.append("、");
                        }
                        sb.append(name(ri));
                    }
                }
            }
        } else {
            Map<String, Object> oneData = selectedData(userContext, startName);
            if (oneData != null && !oneData.isEmpty()) {
                LoggerTool.info(logger,"startName:{} is {}", startName, mapString(oneData));

                String relation = "MATCH(n)-[r]-(m) where id(n)=" + id(oneData)
                        + " return type(r) AS rCode,r.name AS rName,m";
                List<Map<String, Object>> itsRelation = neo4jUService.cypher(relation);
                for (Map<String, Object> ri : itsRelation) {
                    String rName = string(ri, "rName");
                    if (rName != null && rName.contains(si)) {
                        if (sb.length() > 0) {
                            sb.append("、");
                        }
                        sb.append(name(ri));
                    }
                }
            }
        }
    }

    public List<String> getRelationQuery() {
        List<String> query = new ArrayList<>();
        query.add("可以访问哪些");
        query.add("能访问哪些");
        query.add("有哪些");
        query.add("有什么");
        query.add("有多少");
        query.add("是什么");
        query.add("是多少");
        query.add("是哪些");
        query.add("是");
        query.add("有");
        return query;
    }

    public void seeEndNodeAuth(StringBuilder sb, List<Map<String, Object>> datas) {
        if (datas != null && !datas.isEmpty()) {
            StringBuilder sbx = new StringBuilder();
            for (Map<String, Object> di : datas) {
                if (sbx.length() > 0) {
                    sbx.append("、");
                }
                sbx.append(neo4jUService.seeNode(di) + "的" + string(di, "HAS_PERMISSION"));
            }
            if (sbx.length() > 0) {
                sb.append(sbx.toString());
            }
        }
    }

    public void seeEndNodePropRel(StringBuilder sb, List<Map<String, Object>> datas) {
        if (datas != null && !datas.isEmpty()) {
            StringBuilder sbx = new StringBuilder();
            for (Map<String, Object> di : datas) {
                if (sbx.length() > 0) {
                    sbx.append("、");
                }
                sbx.append(neo4jUService.seeNode(di) + "的" + string(di, "HAS_"));
            }
            if (sbx.length() > 0) {
                sb.append(sbx.toString());
            }
        }
    }

    public void seeEndNode(StringBuilder sb, List<Map<String, Object>> datas) {
        if (datas != null && !datas.isEmpty()) {
            StringBuilder sbx = new StringBuilder();
            for (Map<String, Object> di : datas) {
                if (sbx.length() > 0) {
                    sbx.append("、");
                }
                sbx.append(neo4jUService.seeNode(di));
            }
            if (sbx.length() > 0) {
                sb.append(sbx.toString());
            }
        }
    }

    public void seeProperty(StringBuilder sb, String startName, String coli, Map<String, Object> oMdi,
                            Map<String, Object> context) {
        List<Map<String, Object>> datas;
        Map<String, Object> selectedOne = selectedData(context, startName);
        String getRelPropi = "MATCH (f:Field)   WHERE  f.field='" + coli + "' and f.objectId=" + id(oMdi)
                + " return f.type,f.valueField";
        List<Map<String, Object>> fieldInfo = neo4jUService.cypher(getRelPropi);
        Long startId = id(selectedOne);
        if (!fieldInfo.isEmpty()) {
            Map<String, Object> fi = fieldInfo.get(0);
            String relTypeLabel = string(fi, "type");
            String relTypeValue = string(fi, "valueField");

            String getPropi = "MATCH (n:" + label(oMdi) + ") " + " WHERE id(n)=" + startId + " return n." + coli;
            datas = neo4jUService.cypher(getPropi);
            if (datas != null && !datas.isEmpty()) {
                if (datas.get(0).get(coli) == null)
                    return;
                try {
                    Long coliValue = longValue(datas.get(0), coli);

                    String getRealValue = "MATCH (n:" + relTypeLabel + ") " + " WHERE id(n) = " + coliValue
                            + "   return n";
                    if (!ID.equals(relTypeValue)) {
                        getRealValue = "MATCH (n:" + relTypeLabel + ") " + " WHERE n." + relTypeValue + " = "
                                + coliValue + "   return n";
                    }
                    datas = neo4jUService.cypher(getRealValue);
                    if (datas != null && !datas.isEmpty()) {
                        sb.append(neo4jUService.seeNode(datas.get(0)));
                    }
                } catch (Exception e) {
                    sb.append(string(datas.get(0), coli));
                }
            }
        } else {
            String getPropi = "MATCH (n:" + label(oMdi) + ") " + " WHERE id(n)=" + startId + " return n." + coli;
            datas = neo4jUService.cypher(getPropi);
            if (datas != null) {
                String coliValue = string(datas.get(0), coli);
                sb.append(coliValue);
            }
        }
    }

    public String getABPath(String msg, Map<String, Object> context) {
        Long idOfStart = null;
        Long idOfEnd = null;

        boolean useAnd = false;
        for (String qie : andRel) {
            if (msg.contains(qie)) {
                String[] resourceAuth = msg.split(qie);
                idOfStart = getIdOfData(resourceAuth[0], context);
                idOfEnd = getIdOfData(resourceAuth[1], context);
                useAnd = true;
            }
        }
        if (useAnd) {
            return adminService.showPathInfo(idOfStart, idOfEnd);
        }
        return null;
    }

    public String handleABPath(String msg, Map<String, Object> context) {
        Long idOfStart = null;
        Long idOfEnd = null;

        boolean useAnd = false;
        for (String qie : andRel) {
            if (msg.contains(qie)) {
                String[] resourceAuth = msg.split(qie);
                for (String ri : resourceAuth) {
                    if (containLabelInfo(ri)) {
                        Map<String, Object> onlyContext = new HashMap<>();
                        ri = onlyName(onlyContext, ri);
                        String dataLabel = string(onlyContext, "dataLabel");
                        Map<String, Object> data2 = getData(ri, dataLabel, context);
                        Long idOfRoleOrUser2 = id(data2);
                        if (idOfStart != null) {
                            idOfEnd = idOfRoleOrUser2;
                        } else {
                            idOfStart = idOfRoleOrUser2;
                        }
                        if (META_DATA.equals(dataLabel)) {
                            data2.put("url", LemodoApplication.MODULE_NAME + "/md/" + dataLabel);
                        } else {
                            // document
                            data2.put("url", LemodoApplication.MODULE_NAME + "/layui/" + dataLabel + "/" + id(data2)
                                    + "/documentRel");
                        }
                    } else {
                        Map<String, Object> mdData = getData(ri, META_DATA, context);

                        if (mdData != null) {
                            idOfEnd = id(mdData);
                            if (META_DATA.equals(label(mdData))) {
                                mdData.put("url", LemodoApplication.MODULE_NAME + "/md/" + label(mdData));
                            } else {
                                // document
                                mdData.put("url", LemodoApplication.MODULE_NAME + "/layui/" + label(mdData) + "/"
                                        + id(mdData) + "/documentRel");
                            }
                        } else {
                            Long idOfRoleOrUser2 = getIdOfRoleOrUser(ri);
                            if (idOfStart != null) {
                                idOfEnd = idOfRoleOrUser2;
                            } else {
                                idOfStart = idOfRoleOrUser2;
                            }
                        }
                    }

                }
                useAnd = true;
            }
        }
        if (useAnd) {
            return adminService.showPathInfo(idOfStart, idOfEnd);
        }
        return null;
    }

    /**
     * 执行，存在分支，原地等待，发送提示信息，获取前端数据，继续执行。
     *
     * @param msg
     * @param data
     * @param prefix
     * @return
     */
    public void handleExecute(String msg, List<Map<String, Object>> data, String prefix, Map<String, Object> context) {
        String obj = msg.replaceFirst(prefix, "");
        boolean useAnd = false;
        for (String qie : andRel) {
            if (obj.contains(qie)) {
                String[] scripts = msg.split(qie);
                for (String si : scripts) {
                    data.add(selectedData(context, si));
                }
                useAnd = true;
            }
        }
        if (!useAnd) {
            data.add(selectedData(context, obj));
        }

        for (Map<String, Object> di : data) {
            String string = string(di, "Content");
            Interpreter in = new Interpreter();
            try {
                in.set("so", this);
                // 得有一个文档说明：
                in.set("repo", neo4jService);
                in.eval(string);
            } catch (EvalError e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * 选择数据，当一个数据出现分支时，需要用户进行判断，具体执行哪一个分支。
     *
     * @param context
     * @param si
     */
    private Map<String, Object> selectedData(Map<String, Object> context, String si) {
        List<Map<String, Object>> sis = getData(si);
        context.put("currentName", si);
        return userSelect(context, sis);
    }

    private Map<String, Object> selectedData(Map<String, Object> context, String si,
                                             List<Map<String, Object>> preLabels) {
        List<String> listMap2List = listMap2ListString("x", preLabels);
        List<Map<String, Object>> dataBy = neo4jUService.getDataBy(listMap2List, si);

        context.put("currentName", si);
        return userSelect(context, dataBy);
    }

    public Map<String, Object> userSelect(Map<String, Object> context, List<Map<String, Object>> sis) {
        if (sis != null && !sis.isEmpty()) {
            if (sis.size() > 1) {
                Object converation = context.get(WebSocketDomain.CONVERSATION);
                if (converation != null) {
                    LoggerTool.info(logger,"出现多个选项，需要用户选择，且用户已连上Websocket");
                    ContextServer cs = (ContextServer) converation;
                    try {
                        String object = string(context, "currentName");
                        if (object == null) {
                            object = "";
                        }
                        cs.sendMessage("\n\n\n" + object + "有多选，请选择：\n<br>" + options(sis) + "\n\n\n");
                        Integer selected = integer(context, "which");
                        int count = 0;

                        while (selected == null && count < 50) {
                            try {
                                Thread.sleep(1000);
                                LoggerTool.info(logger,"等待用户选择");
                                selected = integer(context, "which");
                                count++;
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        context.remove("currentName");
                        context.remove("which");
                        if (selected == null) {
                            return null;
                        }
                        return sis.get(selected);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        LoggerTool.error(logger,e1.getMessage(), e1);
                    }
                } else {
                    return sis.get(0);
                }

            } else {
                return sis.get(0);
            }

        }
        return null;
    }

    private String options(List<Map<String, Object>> sis) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Map<String, Object> mi : sis) {
            if (sb.length() > 0) {
                sb.append(" 、 ");
            }
            String seeNode = neo4jUService.seeNode(mi);
            sb.append("<input type=radio onclick=\"mySelect('" + i + "')\">" + (i + 1) + "【" + label(mi) + "】" + id(mi)
                    + seeNode + "</input>");
            i++;
        }
        return sb.toString();
    }

    public void handleManage(String msg, String labelOf, List<Map<String, Object>> data, String prefix,
                             Map<String, Object> context) {
        String obj = msg.replaceFirst(prefix, "");
        boolean useAnd = false;
        for (String qie : andRel) {
            if (obj.contains(qie)) {
                String[] resourceAuth = msg.split(qie);
                for (String ri : resourceAuth) {
                    Map<String, Object> mdData = getData(ri, labelOf, context);

                    if (mdData != null) {
                        if (META_DATA.equals(labelOf)) {
                            mdData.put("url", LemodoApplication.MODULE_NAME + "/md/" + label(mdData));
                        }
                        data.add(mdData);
                    }
                }
                useAnd = true;
            }
        }
        if (!useAnd) {
            Map<String, Object> data2 = getData(obj, labelOf, context);
            if (data2 != null) {
                if (META_DATA.equals(labelOf)) {
                    data2.put("url", LemodoApplication.MODULE_NAME + "/md/" + label(data2));
                }
                data.add(data2);
            }
        }
    }

    public void handleOpen(String msg, String labelOf, List<Map<String, Object>> data, Map<String, Object> context) {
        boolean useAnd = false;
        for (String qie : andRel) {
            if (msg.contains(qie)) {
                String[] resourceAuth = msg.split(qie);
                for (String ri : resourceAuth) {
                    Map<String, Object> mdData = getData(ri, labelOf, context);
                    processOpenData(labelOf, data, mdData);
                }
                useAnd = true;
            }
        }
        if (!useAnd) {
            Map<String, Object> data2 = getData(msg, labelOf, context);
            processOpenData(labelOf, data, data2);
        }
    }

    public void processOpenData(String labelOf, List<Map<String, Object>> data, Map<String, Object> data2) {
        if (data2 != null) {
            String url2 = url(data2);
            if (url2 == null) {
                // 判断权限，只读和修改权限

                data2.put("url",
                        LemodoApplication.MODULE_NAME + "/layui/" + label(data2) + "/" + id(data2) + "/documentRel");

            } else {
                if (url2.contains("?xxx=")) {
                    String[] split = url2.split("xxx=");
                    String currentJSessionId = adminService.getCurrentJSessionId();
                    if (currentJSessionId == null) {
                        currentJSessionId = adminService.getRequestSessionId();
                    }
                    data2.put("url", split[0] + "xxx=" + currentJSessionId);

                    String userName = adminService.getCurrentUserName();
                    Map<String, Object> attMapBy = neo4jService.getAttMapBy("sessionId", currentJSessionId, "Session");
                    if (attMapBy == null) {
                        Map<String, Object> session = newMap();
                        Long currentPasswordId = adminService.getCurrentPasswordId();
                        if (currentPasswordId.intValue() > 0) {
                            session.put("userName", userName);
                            // 保存会话信息,保存之前，删除所有过期数据。
                            // neo4jService.delete("userName", userName, "Session");
                            session.put("sessionId", currentJSessionId);
                            session.put("createTime", DateTool.nowLong());
                            session.put("accountId", currentPasswordId);
                            neo4jService.save(session, "Session");
                        }

                    }
                }
            }
            if (META_DATA.equals(labelOf)) {
                data2.put("url", LemodoApplication.MODULE_NAME + "/md/" + label(data2));
            }

            data.add(data2);
        }
    }

    /**
     * 将{用户或者角色}的什么的什么权限,删除。
     *
     * @param msg
     */
    public void handleStartWithJiang(String msg, Map<String, Object> context) {
        String prefix2 = "将";
        if (msg.startsWith(prefix2)) {
            context.put(USED, true);
            // 获取默认数据：
            for (String deli : deleteWords) {
                if (msg.contains(deli)) {
                    String[] dels = msg.split(deli);
                    String objectResourceAuth = dels[0].replaceFirst(prefix2, "");
                    Boolean useOwn = false;
                    // 将xxx删除
                    for (String oi : ownWords) {
                        // 资源权限删除
                        if (objectResourceAuth.contains(oi)) {
                            useOwn = true;
                            String[] resourceAuth = objectResourceAuth.split(oi);
                            String objectStr = resourceAuth[0];
                            boolean useBetween = false;
                            for (String ci : between) {
                                if (objectStr.endsWith(ci)) {
                                    objectStr = objectStr.replaceFirst(ci, "");
                                    String betweenRel = resourceAuth[1];
                                    if (betweenRel.endsWith("关系")) {
                                        betweenRel = betweenRel.replaceFirst("关系", "");

                                        List<Long> startIds = new ArrayList<>();
                                        for (String qie : andRel) {
                                            if (objectStr.contains(qie)) {
                                                String[] objs = objectStr.split(qie);
                                                for (String obji : objs) {
                                                    if (containLabelInfo(obji)) {
                                                        Map<String, Object> onlyContext = new HashMap<>();
                                                        obji = onlyName(onlyContext, obji);
                                                        String dataLabel = string(onlyContext, "dataLabel");
                                                        Map<String, Object> data2 = getData(obji, dataLabel, context);
                                                        startIds.add(id(data2));
                                                    } else {
                                                        startIds.add(getIdOfData(obji, context));
                                                    }
                                                }
                                                if (objs.length == 2 && startIds.size() == 2) {
                                                    if ("".equals(betweenRel)) {
                                                        deleteRel(startIds.get(0), startIds.get(1));
                                                    } else {
                                                        deleteRel(startIds.get(0), startIds.get(1));
                                                    }
                                                }
                                            }
                                        }

                                    } else {

                                    }
                                    return;
                                }
                            }
                            if (useBetween) {
                                return;
                            }

                            boolean useAnd = false;
                            List<Long> startIds = new ArrayList<>();
                            for (String qie : andRel) {
                                if (objectStr.contains(qie)) {
                                    String[] objs = objectStr.split(qie);
                                    for (String obji : objs) {
                                        if (containLabelInfo(obji)) {
                                            Map<String, Object> onlyContext = new HashMap<>();
                                            obji = onlyName(onlyContext, obji);
                                            String dataLabel = string(onlyContext, "dataLabel");
                                            Map<String, Object> data2 = getData(obji, dataLabel, context);
                                            startIds.add(id(data2));
                                        } else {
                                            startIds.add(getIdOfRoleOrUser(obji));
                                        }
                                    }
                                    useAnd = true;
                                }
                            }
                            Long startId = null;
                            if (!useAnd) {
                                if (containLabelInfo(objectStr)) {
                                    Map<String, Object> onlyContext = new HashMap<>();
                                    objectStr = onlyName(onlyContext, objectStr);
                                    String dataLabel = string(onlyContext, "dataLabel");
                                    Map<String, Object> data2 = getData(objectStr, dataLabel, context);
                                    startId = id(data2);
                                    if (resourceAuth.length > 2) {
                                        startDelRel(resourceAuth[1], resourceAuth[2], startId, context);
                                    } else {
                                        startDelRel(resourceAuth[0], resourceAuth[1], startId, context);
                                    }
                                } else {
                                    startId = getIdOfRoleOrUser(objectStr);
                                    if (resourceAuth.length > 2) {
                                        startDelRel(resourceAuth[1], resourceAuth[2], startId, context);
                                    } else {
                                        startDelRel(resourceAuth[0], resourceAuth[1], startId, context);
                                    }
                                }

                            } else {
                                for (Long si : startIds) {
                                    if (resourceAuth.length > 2) {
                                        startDelRel(resourceAuth[1], resourceAuth[2], si, context);
                                    } else {
                                        startDelRel(resourceAuth[0], resourceAuth[1], si, context);
                                    }
                                }
                            }

                        }
                    }

                    // if(!useOwn) {
                    //
                    // }
                }
            }
        }
    }


    public Long replacePronoun(Map<String, Object> context, String userRole) {
        boolean useMe = false;
        Long startId = null;
        for (String mi : me) {
            if (userRole.equals(mi)) {
                startId = longValue(context, "MyId");
                useMe = true;
            }
        }
        if (!useMe) {
            startId = getIdOfRoleOrUser(userRole);
        }
        return startId;
    }

    /**
     * 1、A和B是关系C 是朋友关系，恋人，同学，师徒，上级，下级，队友，校友 2、A是B的C /小明是小李的老师，好朋友，兄弟，父母，债务人，买受人
     * <p>
     * <p>
     * A和xxx与xx2是关系1 可以访问动作。 A可以/能：看见\访问\读写\更新\删除 B。 未读 已读 A昨天干了什么 今天要干什么？ 出现了什么问题？
     * <p>
     * 添加关系a-relName(relCode)->b
     *
     * @param msg
     * @param context
     */
    public void handleStartCreateRel(String msg, Map<String, Object> context) {

        if (!bool(context, USED)) {
            for (String ari : newRelation) {
                if (!bool(context, USED)) {
                    processStartNewRel(msg, context, ari);
                }
            }
        }
    }

    /**
     * 解析执行新增关系
     *
     * @param msg
     * @param context
     * @param ari
     */
    public void processStartNewRel(String msg, Map<String, Object> context, String ari) {
        if (msg.startsWith(ari)) {
            msg = msg.replaceFirst(ari, "");
            context.put(USED, true);
            // 获取默认数据：
            boolean used = false;
            for (String isRel : kEqualv) {
                if (msg.contains(isRel)) {
                    used = true;
                    String[] startEndOfRel = msg.split(isRel);
                    // 租户数据授权？该如何授予权限？
                    String start = startEndOfRel[0].replaceFirst(ari, "");
                    boolean useAnd = false;
                    List<Long> startIds = new ArrayList<>();
                    String rightOfIs = startEndOfRel[1];
                    for (String qie : andRel) {
                        if (start.contains(qie)) {
                            String[] starts = rightOfIs.split(qie);
                            for (String si : starts) {
                                if (containLabelInfo(rightOfIs)) {
                                    addStartId(startIds, si, context);
                                } else {
                                    startIds.add(getIdOfData(si, context));
                                }
                            }
                            useAnd = true;
                        }
                    }
                    Long startId = null;
                    if (!useAnd) {
                        if (containLabelInfo(rightOfIs)) {
                            addStartMetaRel2End(start, rightOfIs, context);
                        } else {
                            startId = getIdOfData(start, context);
                            startAddRel(rightOfIs, startId, context);
                        }
                    } else {
                        if (startIds.size() == 2) {
                            createRel(startIds.get(0), startIds.get(1), rightOfIs);
                        }
                        if (startIds.size() > 2) {
                            for (Long objectId : startIds) {
                                for (Long otherId : startIds) {
                                    if (!objectId.equals(otherId)) {
                                        createRel(objectId, otherId, rightOfIs);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (!used) {
                String[] args = {"->", "<-"};
                boolean created = false;
                for (String ai : args) {
                    if (!created && msg.contains(ai)) {
                        String[] split = msg.split(ai);
                        String start = null;
                        String end = null;
                        String rel = null;
                        if (split[0].contains("-")) {
                            end = clearBigkh(split[1]);
                            String[] split2 = split[0].split("-");
                            start = clearBigkh(split2[0]);
                            rel = split2[1];
                        } else if (split[1].contains("-")) {
                            end = clearBigkh(split[0]);
                            String[] split2 = split[1].split("-");
                            start = clearBigkh(split2[1]);
                            rel = split2[0];
                        }
                        Long startId = getIdOfData(start, context);
                        if (end.contains("、")) {
                            String[] split2 = end.split("、");
                            Boolean createi = false;
                            for (String ei : split2) {
                                createi = createOneRel(context, created, ei, rel, startId);
                            }
                            if (createi) {
                                created = true;
                            }
                        } else {
                            created = createOneRel(context, created, end, rel, startId);
                        }

                    }
                }
            }
        }
    }

    public boolean createOneRel(Map<String, Object> context, boolean created, String end, String rel, Long startId) {
        Long endId = getIdOfData(end, context);
        String relCode = null;
        // 判断是否存在中文和应为关系名称和代码
        Map<String, Object> propsMap = null;
        int rightx = rel.indexOf("}");
        int leftX = rel.indexOf("{");
        if (leftX >= 0 && rightx > 0) {
            String props = rel.substring(leftX, rightx + 1);
            propsMap = JSON.parseObject(props);
            rel = rel.split("\\{")[0];
        }

        if (rel.contains("(") && rel.contains(")")) {
            String[] codePart = rel.split("\\(");
            relCode = codePart[1].replaceAll("\\)", "");
            rel = codePart[0];

        } else if (!rel.contains("(") && !rel.contains(")")) {
            Map<String, Object> data2 = getData(rel, context);
            if (data2 != null) {
                relCode = string(data2, "reLabel");
            }
        }
        if (endId != null) {
            updateRelDefine(startId, endId, rel, relCode);
            if (propsMap != null && !propsMap.isEmpty()) {
                createRel(startId, endId, rel, relCode, propsMap);
            } else {
                createRel(startId, endId, rel, relCode);
            }

            created = true;
            context.put(USED, true);
        }
        return created;
    }

    /**
     * 清理大括号
     *
     * @param split2
     * @return
     */
    public String clearBigkh(String split2) {
        return split2.replaceAll("\\}", "").replaceAll("\\{", "");
    }

    /**
     * 删除关系
     *
     * @param msg
     * @param context
     */
    public void handleStartDelRel(String msg, Map<String, Object> context) {
        if (!bool(context, USED)) {
            for (String ari : deleteRels) {
                if (!bool(context, USED)) {
                    processStartDelRel(msg, context, ari);
                }
            }
        }
    }

    /**
     * 解析执行删除关系
     *
     * @param msg
     * @param context
     * @param ari
     */
    public void processStartDelRel(String msg, Map<String, Object> context, String ari) {
        if (msg.startsWith(ari)) {
            msg = msg.replaceFirst(ari, "");
            context.put(USED, true);
            // 获取默认数据：
            boolean used = false;
            for (String isRel : kEqualv) {
                if (msg.contains(isRel)) {
                    used = true;
                    String[] startEndOfRel = msg.split(isRel);
                    // 租户数据授权？该如何授予权限？
                    String start = startEndOfRel[0].replaceFirst(ari, "");
                    boolean useAnd = false;
                    List<Long> startIds = new ArrayList<>();
                    String rightOfIs = startEndOfRel[1];
                    for (String qie : andRel) {
                        if (start.contains(qie)) {
                            String[] starts = rightOfIs.split(qie);
                            for (String ri : starts) {
                                if (containLabelInfo(rightOfIs)) {
                                    addStartId(startIds, ri, context);
                                } else {
                                    startIds.add(getIdOfData(ri, context));
                                }
                            }
                            useAnd = true;
                        }
                    }
                    Long startId = null;
                    if (!useAnd) {
                        if (containLabelInfo(rightOfIs)) {
                            delStartMetaRel2End(start, rightOfIs, context);
                        } else {
                            startId = getIdOfData(start, context);
                            startDelRel(rightOfIs, startId, context);
                        }
                    } else {
                        if (startIds.size() == 2) {
                            delRel(startIds.get(0), startIds.get(1), rightOfIs);
                        }
                        if (startIds.size() > 2) {
                            for (Long objectId : startIds) {
                                for (Long otherId : startIds) {
                                    if (!objectId.equals(otherId)) {
                                        delRel(objectId, otherId, rightOfIs);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (!used) {
                String clearmsg = msg.replaceAll("\\}", "").replaceAll("\\{", "");
                String[] args = {"->", "<-"};
                boolean deleted = false;
                for (String ai : args) {

                    if (!deleted && msg.contains(ai)) {
                        String[] split = clearmsg.split(ai);
                        String start = null;
                        String end = null;
                        String rel = null;
                        if (split[0].contains("-")) {
                            end = split[1];
                            String[] split2 = split[0].split("-");
                            start = split2[0];
                            rel = split2[1];
                        } else if (split[1].contains("-")) {
                            end = split[0];
                            String[] split2 = split[1].split("-");
                            start = split2[1];
                            rel = split2[0];
                        }
                        Long startId = getIdOfData(start, context);
                        Long endId = getIdOfData(end, context);
                        String relCode = null;
                        // 判断是否存在中文和应为关系名称和代码
                        if (rel.contains("(") && rel.contains(")")) {
                            String[] split2 = rel.split("\\(");
                            relCode = split2[1].replaceAll("\\)", "");
                            rel = split2[0];
                        } else if (!rel.contains("(") && !rel.contains(")")) {
                            Map<String, Object> data2 = getData(rel, context);
                            relCode = code(data2);
                        }
                        delRel(startId, endId, rel, relCode);
                        deleted = true;
                    }
                }
            }
        }
    }

    /**
     * 添加关系，开始节点句子中包含(元数据信息)
     *
     * @param start
     * @param rightOfIs
     */
    public void addStartMetaRel2End(String start, String rightOfIs, Map<String, Object> conversation) {
        String[] split = start.split("\\(");
        if (split.length < 2) {
            split = start.split(cnLeftKuoHao);
        }
        String meta = null;
        if (split[1].endsWith(rightKuohao)) {
            meta = split[1].replace(rightKuohao, "");
        } else if (split[1].endsWith(cnRightKuoHao)) {
            meta = split[1].replace(cnRightKuoHao, "");
        }
        List<Map<String, Object>> metaDataByName = getMetaDataByName(meta);
        for (Map<String, Object> mi : metaDataByName) {
            List<Map<String, Object>> dataBy = neo4jUService.getDataBy(label(mi), split[0]);
            for (Map<String, Object> di : dataBy) {
                startAddRel(rightOfIs, id(di), name(mi), conversation);
            }
        }
    }

    /**
     * 删除关系
     *
     * @param start
     * @param rightOfIs
     * @param conversation
     */
    public void delStartMetaRel2End(String start, String rightOfIs, Map<String, Object> conversation) {
        String[] split = start.split("\\(");
        if (split.length < 2) {
            split = start.split(cnLeftKuoHao);
        }
        String meta = null;
        if (split[1].endsWith(rightKuohao)) {
            meta = split[1].replace(rightKuohao, "");
        } else if (split[1].endsWith(cnRightKuoHao)) {
            meta = split[1].replace(cnRightKuoHao, "");
        }
        List<Map<String, Object>> metaDataByName = getMetaDataByName(meta);
        for (Map<String, Object> mi : metaDataByName) {
            List<Map<String, Object>> dataBy = neo4jUService.getDataBy(label(mi), split[0]);
            for (Map<String, Object> di : dataBy) {
                startDelRel(rightOfIs, id(di), name(mi), conversation);
            }
        }
    }

    /**
     * 收集包含括号（元数据）的开始节点ID
     *
     * @param startIds
     * @param start
     */
    public void addStartId(List<Long> startIds, String start, Map<String, Object> context) {
        String[] split = start.split("\\(");
        if (split.length < 2) {
            split = start.split(cnLeftKuoHao);
        }
        String meta = null;
        if (split[1].endsWith(rightKuohao)) {
            meta = split[1].replace(rightKuohao, "");
        } else if (split[1].endsWith(cnRightKuoHao)) {
            meta = split[1].replace(cnRightKuoHao, "");
        }
        List<Map<String, Object>> metaDataByName = getMetaDataByName(meta);
        Map<String, Object> si = userSelect(context, metaDataByName);
        List<Map<String, Object>> dataBy = neo4jUService.getDataBy(label(si), split[0]);
        for (Map<String, Object> di : dataBy) {
            startIds.add(id(di));
        }
    }

    /**
     * 处理以删除为开头的语句,先处理删除数据，再处理删除关系。
     *
     * @param msg
     * @param context
     */
    private void handleDelete(String msg, Map<String, Object> context) {
        for (String deli : deleteWords) {
            if (msg.startsWith(deli)) {
                context.put(USED, true);
                String delContent = msg.replaceFirst(deli, "");

                Long idOfData = getIdOfData(delContent, context);
                if (idOfData != null) {// 删除数据
                    neo4jUService.execute("MATCH(n) where id(n)=" + idOfData + " delete n");
                } else {
                    List<Map<String, Object>> dataByXx = neo4jUService.getDataBy(delContent);
                    if (dataByXx != null && dataByXx.size() > 1) {
                        // 多个的情况盖如和处理：和前端进行确认
                        return;
                    }
                    handelDelNode(delContent, context);
                    handelDelRelOrProp(delContent, context);
                }
            }
        }
    }

    /**
     * update xx's dd =yy 更新小明的性别为女 更新小白的性别为女
     * <p>
     * 更新小白和李白的性别为女
     *
     * @param msg
     * @param context
     */
    private void handleUpdate(String msg, Map<String, Object> context) {
        for (String updatei : updates) {
            if (msg.startsWith(updatei)) {
                context.put(USED, true);
                // 租户数据授权？该如何授予权限？
                String noPrefix = msg.replaceFirst(updatei, "");
                // 找到等于
                for (String eqi : kEqualv) {
                    if (noPrefix.contains(eqi)) {
                        String[] startEndOfRel = noPrefix.split(eqi);
                        // 租户数据授权？该如何授予权限？
                        handleLeftAndRight(startEndOfRel[0], startEndOfRel[1], context);
                    }
                }
                for (String eqi : relName) {
                    if (noPrefix.contains(eqi)) {
                        String[] startEndOfRel = noPrefix.split(eqi);
                        // 租户数据授权？该如何授予权限？
                        addStartMetaRel2End(startEndOfRel[0], startEndOfRel[1], context);
                    }
                }

            }
        }
    }

    /**
     * 处理等式的左右两边
     *
     * @param left
     * @param rightOfIs
     */
    private void handleLeftAndRight(String left, String rightOfIs, Map<String, Object> context) {
        boolean leftHasOi = false;
        for (String oi : ownWords) {
            if (left.contains(oi)) {
                leftHasOi = true;
                String[] leftBelong = left.trim().split(oi);
                String objectStr = leftBelong[0];
                List<Long> startIds = parseObjectsId(context, objectStr);

                String propOrRel = leftBelong[1];

                for (Long startId : startIds) {
                    Map<String, String> nameColById = neo4jUService.getNameColById(startId);
                    String coli = nameColById.get(propOrRel);
                    if (coli != null) {
                        // 判断字段是否是关联字段。管理字段根据
                        JSONObject vo = new JSONObject();
                        Map<String, Object> metaDataById = neo4jUService.getMetaDataById(startId);
                        String labelPo = label(metaDataById);
                        vo.put("poId", labelPo);
                        // 查询自定义字段数据
                        List<Map<String, Object>> fieldInfoList = objectService.getBy(vo, "Field");
                        if (fieldInfoList != null && !fieldInfoList.isEmpty()) {
                            Map<String, Map<String, Object>> customFieldMap = new HashMap<>(fieldInfoList.size());
                            for (Map<String, Object> fi : fieldInfoList) {
                                Object object = fi.get(FIELD);
                                object = object == null ? fi.get(ID) : object;
                                customFieldMap.put(String.valueOf(object), fi);
                            }
                            Map<String, Object> field = customFieldMap.get(coli);

                            if (field != null) {
                                String type = String.valueOf(field.get("type"));
                                if ("true".equals(field.get("isPo"))) {// 更新关联字段
                                    List<Map<String, Object>> dataBy = neo4jUService.getDataBy(type, rightOfIs);
                                    if (dataBy != null) {
                                        String id2 = string(dataBy.get(0), ID);
                                        neo4jUService.updateBy(startId, coli, id2);
                                    }
                                }
                            }
                        } else {// 更新属性
                            neo4jUService.updateBy(startId, coli, rightOfIs);
                        }
                    } else {
                        // 没有字段则直接添加关系
                        startAddRel(rightOfIs, startId, propOrRel, context);
                    }
                }
            }
        }
        if (!leftHasOi) {
            // 没有字段则直接添加关系
            List<Long> startIds = parseObjectsId(context, left);
            for (Long si : startIds) {
                startAddRel(rightOfIs, si, context);
            }
        }
    }

    /**
     * 解析字符串中包含的对象，并返回去对象Id列表
     *
     * @param context
     * @param objectStr
     * @return
     */
    public List<Long> parseObjectsId(Map<String, Object> context, String objectStr) {
        boolean useAnd;
        List<Long> startIds = new ArrayList<>();
        useAnd = handleAnd(context, objectStr, startIds);

        if (!useAnd) {
            if (containLabelInfo(objectStr)) {
                Map<String, Object> onlyContext = new HashMap<>();
                objectStr = onlyName(onlyContext, objectStr);
                String dataLabel = string(onlyContext, "dataLabel");
                Map<String, Object> data2 = getData(objectStr, dataLabel, context);
                startIds.add(id(data2));
            } else {
                Long startId = getIdOfData(objectStr, context);
                if (startId != null) {
                    startIds.add(startId);
                }
            }
        }
        return startIds;
    }

    /**
     * 针对字符串处理是否包含与、和等关键字。
     *
     * @param context
     * @param objectStr
     * @param startIds
     * @return
     */
    public boolean handleAnd(Map<String, Object> context, String objectStr, List<Long> startIds) {
        boolean useAnd = false;
        for (String qie : andRel) {
            if (objectStr.contains(qie)) {
                String[] lefts = objectStr.split(qie);
                for (String li : lefts) {
                    if (containLabelInfo(li)) {
                        Map<String, Object> onlyContext = new HashMap<>();
                        li = onlyName(onlyContext, li);
                        String dataLabel = string(onlyContext, "dataLabel");
                        Map<String, Object> data2 = getData(li, dataLabel, context);
                        startIds.add(id(data2));
                    } else {
                        Long idOfData = getIdOfData(li, context);
                        if (idOfData != null) {
                            startIds.add(idOfData);
                        }
                    }

                }
                useAnd = true;
            }
        }
        return useAnd;
    }

    private boolean handelDelNode(String xx, Map<String, Object> funContext) {
        boolean useAnd = false;
        List<Long> objIds = new ArrayList<>();
        for (String qie : andRel) {
            if (xx.contains(qie)) {
                String[] starts = xx.split(qie);
                for (String ri : starts) {
                    if (containLabelInfo(ri)) {
                        Map<String, Object> onlyContext = new HashMap<>();
                        ri = onlyName(onlyContext, ri);
                        String dataLabel = string(onlyContext, "dataLabel");
                        Map<String, Object> data2 = getData(ri, dataLabel, funContext);
                        Long idOfData = id(data2);
                        objIds.add(idOfData);
                    } else {
                        objIds.add(getIdOfData(ri, funContext));
                    }
                }
                useAnd = true;
            }
        }
        if (useAnd && objIds.size() > 0) {
            neo4jUService.execute("MATCH (n) WHERE ID(n) IN [" + joinLong(objIds) + "] DETACH DELETE n ");
        }
        return useAnd;
    }

    /**
     * 删除关系或者删除属性
     *
     * @param xx
     * @return
     */
    public boolean handelDelRelOrProp(String xx, Map<String, Object> funContext) {
        boolean useAnd = false;
        boolean useOwni = false;
        for (String qie : andRel) {
            if (xx.contains(qie)) {
                String[] ands = xx.split(qie);
                for (String ai : ands) {
                    for (String oi : ownWords) {
                        if (xx.contains(oi)) {
                            useOwni = true;
                            String[] orpi = ai.split(oi);
                            Map<String, Object> oneData = selectedData(funContext, orpi[0]);
                            Long owniId = id(oneData);
                            Map<String, String> nameColById = neo4jUService.getNameColById(owniId);

                            String col = nameColById.get(orpi[1]);
                            if (col != null) {
                                neo4jUService.execute("MATCH (n) WHERE ID(n)  =" + owniId + " REMOVE n." + col);
                            } else {
                                Long idOfMd = getIdOfMd(orpi[1]);
                                if (idOfMd == null) {
                                    Long idOfData = getIdOfData(orpi[1], funContext);
                                    deleteRel(owniId, idOfData);
                                } else {
                                    deleteRel(owniId, idOfMd);
                                }
                            }
                        }
                    }
                }
                useAnd = true;
            }
        }

        if (!useAnd) {
            for (String oi : ownWords) {
                if (xx.contains(oi)) {
                    useOwni = true;
                    String[] orpi = xx.split(oi);
                    Map<String, Object> oneData = getData(orpi[0], funContext);
                    Long owniId = id(oneData);
                    Map<String, String> nameColById = neo4jUService.getNameColById(owniId);

                    String col = nameColById.get(orpi[1]);
                    if (col != null) {
                        neo4jUService.execute("MATCH (n) WHERE ID(n)  =" + owniId + " REMOVE n." + col);
                    } else {
                        Long idOfMd = getIdOfMd(orpi[1]);
                        if (idOfMd == null) {
                            Long idOfData = getIdOfData(orpi[1], funContext);
                            deleteRel(owniId, idOfData);
                        } else {
                            deleteRel(owniId, idOfMd);
                        }
                    }
                }

            }
        }

        return useAnd;
    }

    private void startDelRel(String delJuzi, Long startId) {
        Long endId;
        for (String owni : ownWords) {
            if (delJuzi.contains(owni)) {
                String[] resourceAuth = delJuzi.split(owni);
                String resource = resourceAuth[0];
                endId = getIdOfMd(resource);
                String auth = resourceAuth[1];
                deleteAuthRel(startId, endId, auth);
            }
        }
    }

    @ServiceLog(description = "删除权限关系，开始节点和结束节点，权限关系")
    private void startDelRel(String end, String rel, Long startId, Map<String, Object> context) {
        Long endId = getIdOfMd(end);
        if (endId == null || endId.equals(startId)) {
            if (containLabelInfo(rel)) {
                Map<String, Object> onlyContext = new HashMap<>();
                rel = onlyName(onlyContext, rel);
                String dataLabel = string(onlyContext, "dataLabel");
                // Map<String, Object> mapObject = mapObject(onlyContext, "dataMd");
                endId = getIdOfData(rel, dataLabel, context);
            } else {
                endId = getIdOfData(rel, context);
            }
        }
        deleteAuthRel(startId, endId, rel);
    }

    @ServiceLog(description = "删除权限关系，开始节点和结束节点，权限关系")
    public void deleteAuthRel(Long startId, Long endId, String auth) {
        String relCode;
        Map<String, Object> authMap = neo4jUService.getAttMapBy(NAME, auth, "permission");
        relCode = code(authMap);
        String cypher = " MATCH(s)-[r:" + relCode + "{name:\"" + auth + "\"}]->(e)  where id(s)=" + startId
                + " and id(e)=" + endId + " delete r";
        neo4jService.execute(cypher);
    }

    @ServiceLog(description = "删除关系，开始节点和结束节点的所有关系")
    public void deleteRel(Long startId, Long endId) {
        String cypher = " MATCH(s)-[r]->(e)  where id(s)=" + startId + " and id(e)=" + endId + " delete r";
        neo4jService.execute(cypher);
    }

    @ServiceLog(description = "删除关系，开始节点和结束节点的某个关系")
    public void deleteRel(Long startId, Long endId, String name) {
        String cypher = " MATCH(s)-[r]->(e)  where id(s)=" + startId + " and id(e)=" + endId + " delete r";
        if (name != null && !"".equals(name.trim())) {
            cypher = " MATCH(s)-[r]->(e)  where id(s)=" + startId + " and r.name=\"" + name + "\" and id(e)=" + endId
                    + " delete r";
        }
        neo4jService.execute(cypher);
    }

    /**
     * 添加关系
     *
     * @param rightOfIs
     * @param startId
     */
    @ServiceLog(description = "给开始节点添加关系")
    public void startAddRel(String rightOfIs, Long startId, Map<String, Object> userContext) {
        Long endId;
        boolean rigthHasOwniWord = false;
        for (String owni : ownWords) {
            if (rightOfIs.contains(owni)) {
                rigthHasOwniWord = true;
                String[] whoSResource = rightOfIs.split(owni);
                String who = whoSResource[0];

                String propOrRel = whoSResource[1];

                if (containLabelInfo(who)) {
                    addAuthRelWithLabelInfo(startId, who, propOrRel, zuoKuohao);
                    addAuthRelWithLabelInfo(startId, who, propOrRel, cnLeftKuoHao);
                } else {
                    endId = getIdOfMd(who);
                    createRel(startId, endId, propOrRel);
                }
            }
        }
        if (!rigthHasOwniWord) {
            // 获取结束节点的名称
            if (containLabelInfo(rightOfIs)) {
                startAddRel(startId, cnLeftKuoHao, rightOfIs);
                startAddRel(startId, zuoKuohao, rightOfIs);
            } else {
                // 不应该出现A是B？
                endId = id(getData(rightOfIs, userContext));
                if (endId != null) {// 有关系节点
                    createRel(startId, endId, rightOfIs);
                }
            }

        }
    }

    public void startDelRel(String rightOfIs, Long startId, Map<String, Object> userContext) {
        Long endId;
        boolean rigthHasOwniWord = false;
        for (String owni : ownWords) {
            if (rightOfIs.contains(owni)) {
                rigthHasOwniWord = true;
                String[] whoSResource = rightOfIs.split(owni);
                String who = whoSResource[0];

                String propOrRel = whoSResource[1];

                if (containLabelInfo(who)) {
                    delAuthRelWithLabelInfo(startId, who, propOrRel, zuoKuohao);
                    delAuthRelWithLabelInfo(startId, who, propOrRel, cnLeftKuoHao);
                } else {
                    endId = getIdOfMd(who);
                    delRel(startId, endId, propOrRel);
                }
            }
        }
        if (!rigthHasOwniWord) {
            // 获取结束节点的名称
            if (containLabelInfo(rightOfIs)) {
                startDelRel(startId, cnLeftKuoHao, rightOfIs);
                startDelRel(startId, zuoKuohao, rightOfIs);
            } else {
                // 不应该出现A是B？
                endId = id(getData(rightOfIs, userContext));
                if (endId != null) {// 有关系节点
                    delRel(startId, endId, rightOfIs);
                }
            }

        }
    }

    public void startAddRel(String rightOfIs, Long startId, String relName, Map<String, Object> userContext) {
        Long endId;
        boolean rightHasOwniWord = false;
        for (String owni : ownWords) {
            if (rightOfIs.contains(owni)) {
                rightHasOwniWord = true;
                String[] whoSResource = rightOfIs.split(owni);
                String who = whoSResource[0];

                String propOrRel = whoSResource[1];

                if (containLabelInfo(who)) {
                    addAuthRelWithLabelInfo(startId, who, propOrRel, zuoKuohao);
                    addAuthRelWithLabelInfo(startId, who, propOrRel, cnLeftKuoHao);
                } else {
                    endId = getIdOfMd(who);
                    createRel(startId, endId, propOrRel);
                }
            }
        }
        if (!rightHasOwniWord) {
            // 不应该出现A是B？
            endId = id(getData(rightOfIs, userContext));
            if (endId != null) {// 有关系节点
                createRel(startId, endId, relName);
            }
        }
    }

    /**
     * 删除关系
     *
     * @param rightOfIs
     * @param startId
     * @param relName
     * @param userContext
     */
    public void startDelRel(String rightOfIs, Long startId, String relName, Map<String, Object> userContext) {
        Long endId;
        boolean rightHasOwniWord = false;
        for (String owni : ownWords) {
            if (rightOfIs.contains(owni)) {
                rightHasOwniWord = true;
                String[] whoSResource = rightOfIs.split(owni);
                String who = whoSResource[0];

                String propOrRel = whoSResource[1];

                if (containLabelInfo(who)) {
                    delAuthRelWithLabelInfo(startId, who, propOrRel, zuoKuohao);
                    delAuthRelWithLabelInfo(startId, who, propOrRel, cnLeftKuoHao);
                } else {
                    endId = getIdOfMd(who);
                    deleteRel(startId, endId, propOrRel);
                }
            }
        }
        if (!rightHasOwniWord) {
            // 不应该出现A是B？
            endId = id(getData(rightOfIs, userContext));
            if (endId != null) {// 有关系节点
                deleteRel(startId, endId, relName);
            }
        }
    }

    /**
     * 给开始节点添加xxxxxxxxxx
     *
     * @param rightOfIs
     * @param startId
     */
    @ServiceLog(description = "给开始节点添加权限")
    public void startAddAuth(String rightOfIs, Long startId) {
        Long endId;
        boolean hasOwniWord = false;

        for (String owni : ownWords) {// 谁的什么
            if (rightOfIs.contains(owni)) {
                hasOwniWord = true;
                String[] whoSResource = rightOfIs.split(owni);
                String who = whoSResource[0];

                String por = whoSResource[1];
                if (containLabelInfo(who)) {// 包含Label信息，
                    addAuthRelWithLabelInfo(startId, zuoKuohao, who, por);
                    addAuthRelWithLabelInfo(startId, cnLeftKuoHao, who, por);
                } else {
                    endId = getIdOfMd(who);
                    addAuthRel(startId, endId, por);
                }
            }
        }
        if (!hasOwniWord) {
            // 不应该出现A是B？
            if (containLabelInfo(rightOfIs)) {
                relateEndWithLabelInfo(rightOfIs, startId, zuoKuohao);
                relateEndWithLabelInfo(rightOfIs, startId, cnLeftKuoHao);
            } else {
                endId = neo4jUService.getIdBy(rightOfIs);
                if (endId != null) {
                    addAuthRel(startId, endId, rightOfIs);
                }
            }
        }
    }

    /**
     * 关联终点包含括号括起来的标签Label信息
     *
     * @param rightOfIs
     * @param startId
     * @param zuoKuohao2
     */
    @ServiceLog(description = "给开始节点添加关系，终点有元数据标识")
    public void relateEndWithLabelInfo(String rightOfIs, Long startId, String zuoKuohao2) {
        if (rightOfIs.contains(zuoKuohao2)) {
            String[] split = rightOfIs.split(zuoKuohao2);
            String meta = null;
            if (split[1].endsWith(rightKuohao)) {
                meta = split[1].replace(rightKuohao, "");
            }
            if (split[1].endsWith(cnRightKuoHao)) {
                meta = split[1].replace(cnRightKuoHao, "");
            }
            List<Map<String, Object>> metaDataByName = getMetaDataByName(meta);
            for (Map<String, Object> mi : metaDataByName) {
                List<Map<String, Object>> dataBy = neo4jUService.getDataBy(label(mi), split[0]);
                for (Map<String, Object> di : dataBy) {
                    addAuthRel(startId, id(di), meta);
                }
            }
        }
    }

    /**
     * 给开始节点添加权限信息，包含标签，Label信息
     *
     * @param startId
     * @param zuoKuohao2
     * @param who
     * @param por
     */
    @ServiceLog(description = "给开始节点添加权限关系，终点有元数据信息，带着终点和气属性或者关系参数")
    public void addAuthRelWithLabelInfo(Long startId, String zuoKuohao2, String who, String por) {
        if (who.contains(zuoKuohao2)) {
            String[] split = who.split(zuoKuohao2);
            String meta = null;
            if (split[1].endsWith(rightKuohao)) {
                meta = split[1].replace(rightKuohao, "");
            }
            if (split[1].endsWith(cnRightKuoHao)) {
                meta = split[1].replace(cnRightKuoHao, "");
            }
            List<Map<String, Object>> metaDataByName = getMetaDataByName(meta);
            for (Map<String, Object> mi : metaDataByName) {
                List<Map<String, Object>> dataBy = neo4jUService.getDataBy(label(mi), split[0]);
                for (Map<String, Object> di : dataBy) {
                    addAuthRel(startId, id(di), por);
                }
            }
        }
    }

    /**
     * 删除权限关系
     *
     * @param startId
     * @param zuoKuohao2
     * @param who
     * @param por
     */
    public void delAuthRelWithLabelInfo(Long startId, String zuoKuohao2, String who, String por) {
        if (who.contains(zuoKuohao2)) {
            String[] split = who.split(zuoKuohao2);
            String meta = null;
            if (split[1].endsWith(rightKuohao)) {
                meta = split[1].replace(rightKuohao, "");
            }
            if (split[1].endsWith(cnRightKuoHao)) {
                meta = split[1].replace(cnRightKuoHao, "");
            }
            List<Map<String, Object>> metaDataByName = getMetaDataByName(meta);
            for (Map<String, Object> mi : metaDataByName) {
                List<Map<String, Object>> dataBy = neo4jUService.getDataBy(label(mi), split[0]);
                for (Map<String, Object> di : dataBy) {
                    delRel(startId, id(di), por);
                }
            }
        }
    }

    /**
     * @param startId
     * @param zuoKuohao2
     * @param who
     */
    public void startAddRel(Long startId, String zuoKuohao2, String who) {
        if (who.contains(zuoKuohao2)) {
            String[] split = who.split(zuoKuohao2);
            String meta = null;
            if (split[1].endsWith(rightKuohao)) {
                meta = split[1].replace(rightKuohao, "");
            }
            if (split[1].endsWith(cnRightKuoHao)) {
                meta = split[1].replace(cnRightKuoHao, "");
            }

            List<Map<String, Object>> metaDataByName = getMetaDataByName(meta);
            for (Map<String, Object> mi : metaDataByName) {
                List<Map<String, Object>> dataBy = neo4jUService.getDataBy(label(mi), split[0]);
                for (Map<String, Object> di : dataBy) {
                    createRel(startId, id(di), meta);
                }
            }
        }
    }

    /**
     * 删除关系
     *
     * @param startId
     * @param zuoKuohao2
     * @param who
     */
    public void startDelRel(Long startId, String zuoKuohao2, String who) {
        if (who.contains(zuoKuohao2)) {
            String[] split = who.split(zuoKuohao2);
            String meta = null;
            if (split[1].endsWith(rightKuohao)) {
                meta = split[1].replace(rightKuohao, "");
            }
            if (split[1].endsWith(cnRightKuoHao)) {
                meta = split[1].replace(cnRightKuoHao, "");
            }

            List<Map<String, Object>> metaDataByName = getMetaDataByName(meta);
            for (Map<String, Object> mi : metaDataByName) {
                List<Map<String, Object>> dataBy = neo4jUService.getDataBy(label(mi), split[0]);
                for (Map<String, Object> di : dataBy) {
                    delRel(startId, id(di), meta);
                }
            }
        }
    }

    @ServiceLog(description = "带着括号的字符串，获取其元数据")
    public String getMetaInfo(String zuoKuohao2, String who, Map<String, Object> context) {
        if (who.contains(zuoKuohao2)) {
            String[] split = who.split(zuoKuohao2);
            String meta = null;
            if (split[1].endsWith(rightKuohao)) {
                meta = split[1].replace(rightKuohao, "");
            }
            if (split[1].endsWith(cnRightKuoHao)) {
                meta = split[1].replace(cnRightKuoHao, "");
            }
            List<Map<String, Object>> metaDataByName = getMetaDataByName(meta);
            for (Map<String, Object> mi : metaDataByName) {
                List<Map<String, Object>> dataBy = neo4jUService.getDataBy(label(mi), split[0]);
                for (Map<String, Object> di : dataBy) {
                    context.put("dataLabel", label(di));
                    context.put("dataMd", di);
                }
            }
            return meta;
        }
        return who;
    }

    /**
     * 判断句子是否包含左右括号，包括中引文括号。
     *
     * @param msg
     * @return 包含括号返回true
     */
    @ServiceLog(description = "判断是否包含括号")
    public boolean containLabelInfo(String msg) {
        Boolean metaStart = msg.contains(zuoKuohao) || msg.contains(cnLeftKuoHao);
        Boolean metaEnd = msg.contains(rightKuohao) || msg.contains(cnRightKuoHao);
        boolean hasMetaInfo = metaStart && metaEnd;
        return hasMetaInfo;
    }

    @ServiceLog(description = "给开始、结束节点添加关系：参数Long startId, Long endId, String relName")
    public void createRel(Long startId, Long endId, String relName) {
        Map<String, Object> authMap = neo4jUService.getAttMapBy(NAME, relName, "RelationDefine");
        if (authMap == null) {
            return;
        }
        String relCode = string(authMap, "reLabel");
        String cypher = "MATCH (s),(e) where id(s)=" + startId + " and id(e)=" + endId + " create (s)-[:" + relCode
                + "{name:\"" + relName + "\",code:\"" + relCode + "\"}]->(e)";
        neo4jService.execute(cypher);
    }

    public void createRel(Long startId, Long endId, String relName, String relCode) {
        Map<String, Object> authMap = neo4jUService.getAttMapBy(NAME, relName, "RelationDefine");
        if (authMap != null) {
            relCode = string(authMap, "reLabel");
        }
        String cypher = "MATCH (s),(e) where id(s)=" + startId + " and id(e)=" + endId + " create (s)-[:" + relCode
                + "{name:\"" + relName + "\",code:\"" + relCode + "\"}]->(e)";
        neo4jService.execute(cypher);
    }

    public void createRel(Long startId, Long endId, String relName, String relCode, Map<String, Object> props) {
        Map<String, Object> authMap = neo4jUService.getAttMapBy(NAME, relName, "RelationDefine");
        if (authMap != null) {
            relCode = string(authMap, "reLabel");
        }
        props.put(CODE, relCode);
        props.put(NAME, relName);
        // +"{name:\"" + relName + "\",code:\"" + relCode + "\"}
        String mapString = mapString(props);

        String cypher = "MATCH (s),(e) where id(s)=" + startId + " and id(e)=" + endId + " create (s)-[:" + relCode
                + "{" + mapString + "}]->(e)";
        neo4jService.execute(cypher);
    }

    /**
     * 更新关系定义信息
     *
     * @param startId
     * @param endId
     * @param relName
     * @param relCode
     */
    public void updateRelDefine(Long startId, Long endId, String relName, String relCode) {
        String endLabel = neo4jUService.getLabelByNodeId(endId);
        String startLabel = neo4jUService.getLabelByNodeId(startId);
        Map<String, Object> authMap = neo4jUService.getAttMapBy(NAME, relName, "RelationDefine");

        Map<String, Object> map = newMap();
        // id,reLabel,name,startLabel,endLabel
        // 编码,标签,名称,关系方,被关系方,查询语句,查询列
        map.put("startLabel", startLabel);
        map.put("endLabel", endLabel);
        map.put("reLabel", relCode);
        map.put(NAME, relName);
        if (authMap == null) {
            neo4jUService.saveByBody(map, "RelationDefine");
        } else {
            boolean startNe = !string(authMap, "startLabel").equals(startLabel);
            boolean endLabelNE = !string(authMap, "endLabel").equals(endLabel);
            if (startNe || endLabelNE) {
                neo4jUService.saveByBody(map, "RelationDefine");
            }
        }
    }

    public void delRel(Long startId, Long endId, String relName, String relCode) {
        Map<String, Object> authMap = neo4jUService.getAttMapBy(NAME, relName, "RelationDefine");
        if (authMap != null) {
            relCode = string(authMap, "reLabel");
        }
        String cypher = "MATCH (s)-[r:" + relCode + "{name:\"" + relName + "\",code:\"" + relCode + "\"}]->(e) "
                + " where id(s)=" + startId + " and id(e)=" + endId + " delete r";
        neo4jService.execute(cypher);
    }

    @ServiceLog(description = "给开始、结束节点删除关系：参数Long startId, Long endId, String relName")
    public void delRel(Long startId, Long endId, String relName) {
        Map<String, Object> authMap = neo4jUService.getAttMapBy(NAME, relName, "RelationDefine");
        if (authMap == null) {
            return;
        }
        String relCode = string(authMap, "reLabel");
        String cypher = "MATCH (s)-[r:" + relCode + "{name:\"" + relName + "\"}]->(e) where id(s)=" + startId
                + " and id(e)=" + endId + " delete r";
        neo4jService.execute(cypher);
    }

    /**
     * 给开始节点和结束节点添加权限关系
     *
     * @param startId
     * @param endId
     * @param relName
     */
    @ServiceLog(description = "给开始、结束节点添加权限关系：参数Long startId, Long endId, String relName")
    public void addAuthRel(Long startId, Long endId, String relName) {
        String relCode;
        Map<String, Object> authMap = neo4jUService.getAttMapBy(NAME, relName, "permission");
        if (authMap != null) {
            relCode = code(authMap);
            String cypher = "MATCH (s),(e) where id(s)=" + startId + " and id(e)=" + endId
                    + " create (s)-[:HAS_PERMISSION{code:\"" + relCode + "\",name:\"" + relName + "\"}]->(e)";
            neo4jService.execute(cypher);
            return;
        } else {
            Map<String, Object> metaDataById = neo4jUService.getMetaDataById(endId);
            String code2 = code(metaDataById);
            StringBuilder cypher = new StringBuilder(
                    "MATCH (s),(e) where id(s)=" + startId + " and id(e)=" + endId + " create (s)-[:HAS_PERMISSION{");
            if (code2 != null) {
                cypher.append("code:\"" + code2 + "\",");
            }
            cypher.append("name:\"" + name(metaDataById) + "\"}]->(e)");
            neo4jService.execute(cypher.toString());
        }

    }

    @ServiceLog(description = "根据参数获取元数据的Id")
    public Long getIdOfMd(String resource) {
        Long startId = null;
        List<Map<String, Object>> metaDataBy = neo4jUService.getMetaDataBy(resource);
        if (metaDataBy.size() >= 1) {
            startId = id(metaDataBy.get(0));
        }
        return startId;
    }

    @ServiceLog(description = "根据参数获取元数据的Label")
    public String getLabelOfMd(String resource) {
        String labelData = null;
        List<Map<String, Object>> metaDataBy = neo4jUService.getMetaDataBy(resource);
        if (metaDataBy.size() == 1) {
            labelData = label(metaDataBy.get(0));
        }
        return labelData;
    }

    @ServiceLog(description = "根据参数获取用户或者角色的ID")
    public Long getIdOfRoleOrUser(String resource) {
        Long startId = null;
        List<Map<String, Object>> metaDataBy = neo4jUService.getDataBy("Role", resource);
        if (!metaDataBy.isEmpty() && metaDataBy.size() > 0) {
            startId = id(metaDataBy.get(0));
        } else {
            metaDataBy = neo4jUService.getDataBy("User", resource);
            if (!metaDataBy.isEmpty() && metaDataBy.size() > 0) {
                startId = id(metaDataBy.get(0));
            }
        }
        return startId;
    }

    @ServiceLog(description = "根据参数：name，label，获取数据")
    public Map<String, Object> getData(String name, String labelOf, Map<String, Object> context) {
        List<Map<String, Object>> metaDataBy = neo4jUService.getDataBy(labelOf, name);
        return userSelect(context, metaDataBy);
    }

    @ServiceLog(description = "根据参数：name，获取数据,Context中用户选择信息")
    public Map<String, Object> getData(String name, Map<String, Object> context) {
        // String dataOfKuohao = getDataOfKuohao(name);
        List<Map<String, Object>> data = new ArrayList<>();
        if (containLabelInfo(name)) {
            Map<String, Object> dataOfKuohao = getDataOfKuohao(name);
            String meta = string(dataOfKuohao, "meta");
            String[] split = strArray(dataOfKuohao, "split");
            List<Map<String, Object>> metaDataByName = getMetaDataByName(meta);
            for (Map<String, Object> mi : metaDataByName) {
                List<Map<String, Object>> dataBy = neo4jUService.getDataBy(label(mi), split[0]);
                if (dataBy != null) {
                    data.addAll(dataBy);
                }
            }
        } else {
            data = neo4jUService.getDataBy(name);
        }
        if (data.size() == 1 || isSameList(data)) {
            return data.get(0);
        }
        return userSelect(context, data);
    }

    @ServiceLog(description = "根据参数：name，label，获取数据的ID")
    public Long getIdOfData(String name, String labelOf, Map<String, Object> context) {
        return id(getData(name, labelOf, context));
    }

    public List<Map<String, Object>> getData(String name) {
        return neo4jUService.getDataBy(name);
    }

    @ServiceLog(description = "根据参数：name，获取第一个数据的ID")
    public Long getIdOfData(String resource, Map<String, Object> context) {
        for (String mi : me) {
            if (resource.equals(mi)) {
                return longValue(context, "MyId");
            }
        }
        return id(getData(resource, context));
    }

    /**
     * 默认增删改查
     *
     * @param message
     */
    @ServiceLog(description = "解析会话sessioinId的命令message")
    public Map<String, Object> parseAndexcute(String message, String sessionId) {
        // 替换掉声音助词
        String msg = clearVoiceWord(message);
        // huoq唤醒词：
        // 默认的唤醒词
        if (msg.length() <= 10) {
            // 获取默认数据：
            boolean use = false;
            List<String> xx = new ArrayList<>();
            xx.addAll(getUseWords);
            xx.addAll(stackQuit);
            xx.addAll(operateStack);
            xx.addAll(newUpdate);
            xx.addAll(newRelation);
            xx.addAll(newNode);
            xx.addAll(andRel);
            xx.addAll(removes);
            xx.addAll(ownWords);
            xx.addAll(kEqualv);
            xx.addAll(relProp);
            xx.addAll(relName);
            xx.addAll(isRel);
            xx.addAll(manageNode);

            for (String ni : xx) {
                if (msg.startsWith(ni)) {
                    use = true;
                }
            }
            if (!use) {
                Map<String, Object> parseNoReservedWord = parseNoReservedWord(sessionId, msg);
                if (parseNoReservedWord != null) {
                    return parseNoReservedWord;
                }
            }

        }

        Map<String, Object> operateMeta = enterMeta(sessionId, msg);
        if (operateMeta != null) {
            return operateMeta;
        }
        Map<String, Object> manage = manage(sessionId, msg);
        if (manage != null) {
            return manage;
        }
        Map<String, Object> operateObject = useIt(sessionId, msg);
        if (operateObject != null) {
            return operateObject;
        }

        Map<String, Object> handleStartWithTa = handleStartWithTa(sessionId, msg);
        if (handleStartWithTa != null) {
            return handleStartWithTa;
        }
        for (String ni : removes) {
            Map<String, Object> delObject = deleteOne(msg, ni, sessionId);
            if (delObject != null) {
                return delObject;
            }
        }
        //
        for (String hi : relProp) {
            addRelationProp(msg, hi, sessionId);
        }
        return singleSentence(msg, sessionId);
    }

    /**
     * 解析没有使用关键字的句子
     *
     * @param sessionId
     * @param msg
     * @return
     */
    private Map<String, Object> parseNoReservedWord(String sessionId, String msg) {
        Map<String, Object> myContext = getMyContext(sessionId);
        Map<String, Object> data = new HashMap<>();

        // 当前节点为空，或者当前节点名称有
        Map<String, Object> metaMap = mapObject(myContext, OPERATE_META);
        if (metaMap == null) {
            return newFromMeta(sessionId, msg);
        } else {// 当前已有对象，则获取当前的标签，查询当前元数据的实例数据
            String operateLabel = getOperateLabel(sessionId);
            Map<String, Object> objectNode = getNode(operateLabel, "name", msg.trim());
            if (objectNode != null) {// 在当前的元数据下查询到实例数据，精确查找实例数据
                setMayConextProp(OPERATE_OBJECT, objectNode, sessionId);

                Map<String, Object> objectShowCol = neo4jUService.onlyShowCol(objectNode, operateLabel);
                if (objectShowCol == null) {
                    try {
                        crudUtil.simplification(objectNode);
                        deSensitive(operateLabel, objectNode);
                        Map<String, String> colHeader = neo4jUService.getColHeadById(id(objectNode));
                        objectShowCol = visualData(colHeader, objectNode);
                    } catch (DefineException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                if (label(objectShowCol) == null) {
                    objectShowCol.put(LABEL, operateLabel);
                }
                neo4jUService.visulRelation(objectShowCol);
                StringBuilder sb = new StringBuilder();
                nodeDataRelView(sb, objectShowCol);
                data.put("data", sb.toString());
                return data;
            } else {// 当前的元数据中，没有精确的实例数据
                // 重新走一遍查询，去掉当前：元数据，实例数据，标签
                clearMetaObject(myContext);
                return newFromMeta(sessionId, msg);
            }
        }
    }

    private Map<String, Object> parseObjectWord(String sessionId, String msg) {
        Map<String, Object> myContext = getMyContext(sessionId);
        Map<String, Object> data = new HashMap<>();

        // 当前节点为空，或者当前节点名称有
        Map<String, Object> metaMap = mapObject(myContext, OPERATE_META);
        if (metaMap == null) {
            return newFromMeta(sessionId, msg);
        } else {// 当前已有对象，则获取当前的标签，查询当前元数据的实例数据
            String operateLabel = getOperateLabel(sessionId);
            Map<String, Object> objectNode = getNode(operateLabel, "name", msg.trim());
            if (objectNode != null) {// 在当前的元数据下查询到实例数据，精确查找实例数据
                setMayConextProp(OPERATE_OBJECT, objectNode, sessionId);

                Map<String, Object> objectShowCol = neo4jUService.onlyShowCol(objectNode, operateLabel);
                if (objectShowCol == null) {
                    try {
                        crudUtil.simplification(objectNode);
                        deSensitive(operateLabel, objectNode);
                        Map<String, String> colHeader = neo4jUService.getColHeadById(id(objectNode));
                        objectShowCol = visualData(colHeader, objectNode);
                    } catch (DefineException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                if (label(objectShowCol) == null) {
                    objectShowCol.put(LABEL, operateLabel);
                }
                neo4jUService.visulRelation(objectShowCol);
                StringBuilder sb = new StringBuilder();
                nodeDataRelView(sb, objectShowCol);
                data.put("data", sb.toString());
                return data;
            } else {// 当前的元数据中，没有精确的实例数据
                // 重新走一遍查询，去掉当前：元数据，实例数据，标签
                clearMetaObject(myContext);
                return newFromMeta(sessionId, msg);
            }
        }
    }

    /**
     * 根据实例数据，返回元数据
     *
     * @param di
     * @return
     */
    public String metaInfoByData(Map<String, Object> di) {
        Map<String, Object> metaData = neo4jUService.getAttMapBy(LABEL, label(di), META_DATA);
        return metaDataView(metaData);
    }

    /**
     * 可视化元数据，链接元数据
     *
     * @param metaData
     * @return
     */
    public String metaDataView(Map<String, Object> metaData) {
        String metai = "<a href=\"javascript:;\" onclick=\"window.open('" + LemodoApplication.MODULE_NAME + "/layui/MetaData/documentRead?id="
                + id(metaData) + "')\">" + name(metaData) + "</a>";
        return metai;
    }

    private Map<String, Object> newFromMeta(String sessionId, String msg) {
        // 全局查询元数据
        Map<String, Object> metaNode = getNode(META_DATA, "name", msg.trim());
        if (metaNode != null) {// 精确找到元数据
            String label = label(metaNode);
            getInto(label, sessionId);
            setMayConextProp(OPERATE_META, metaNode, sessionId);
        } else {// 没有精确的元数据
            // 查询所有名称或者编码与查询字段匹配的所有节点。
            String queryByNameOrCode = "Match(n) where n.name  CONTAINS '" + msg + "' OR n.code  CONTAINS '" + msg
                    + "' return n";
            List<Map<String, Object>> queryList = neo4jService.cypher(queryByNameOrCode);
            Map<String, Object> data = new HashMap<>();
            // crudUtil.simplifiList(queryList);
            neo4jUService.visualRelList(queryList);
            StringBuilder sb = new StringBuilder();
            for (Map<String, Object> di : queryList) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                nodeDataRelView(sb, di);
            }
            data.put("data", sb.toString());
            return data;
        }
        // 只显示设置过的可视化字段
        Map<String, Object> metaShowCol = neo4jUService.onlyShowCol(metaNode, label(metaNode));
        if (metaShowCol != null) {
            neo4jUService.visulRelation(metaShowCol);
            return metaShowCol;
        }
        // 没有设置显示的数据，默认的数据显示功能。
        crudUtil.simplification(metaNode);
        neo4jUService.visulRelation(metaNode);

        return metaNode;
    }

    public void nodeDataRelView(StringBuilder sb, Map<String, Object> di) {
        metaAndDataView(sb, di);
        String string = string(di, "出关系");
        if (string != null) {
            sb.append("\n" + name(di) + "->" + string);
        }
        String r = string(di, "入关系");
        if (r != null) {
            sb.append("\n" + r + "->" + name(di));
        }
    }

    /**
     * 根据实例数据读取实例数据，元数据，
     *
     * @param sb
     * @param di
     */
    public void metaAndDataView(StringBuilder sb, Map<String, Object> di) {
        String label2 = label(di);
        if (label2 != null) {
            sb.append(metaInfoByData(di));// 元数据链接
            sb.append(":");
            String nodeName;
            if (label2.equals(LABEL_FIELD)) {
                nodeName = "【" + string(di, FIELD) + "】";
            } else {
                String name2 = name(di);
                if (label2.equals(META_DATA)) {
                    nodeName = "【" + label2 + "】";
                    if (name2 != null || "null".equals(name2)) {
                        nodeName = nodeName + name2;
                    }
                } else {
                    nodeName = neo4jUService.showNode(di, label2);
                }
            }

            sb.append("<a href=\"javascript:;\" onclick=\"window.open('" + LemodoApplication.MODULE_NAME + "/layui/" + label2 + "/documentRead?id="
                    + id(di) + "')\">" + nodeName + "</a>");// 实例数据连接
        }
    }

    /**
     * 清理对象和元数据，pop
     *
     * @param myContext
     */
    private void clearMetaObject(Map<String, Object> myContext) {
        myContext.remove(OPERATE_META);
        myContext.remove(OPERATE_OBJECT);
        myContext.remove(OPERATE_LABEL);

    }

    private void deSensitive(String label, Map<String, Object> di) throws DefineException {
        String[] sensitiveColumn = crudUtil.getSensitiveColumn(label);
        if (sensitiveColumn != null && sensitiveColumn.length > 0) {
            for (String si : sensitiveColumn) {
                String string = string(di, si);
                String dsValue = string.substring(0, 1);
                di.put(si, dsValue + "***");
            }
        }
    }

    private Map<String, Object> visualData(Map<String, String> colHeader, Map<String, Object> di)
            throws DefineException {
        Map<String, Object> copy = copy(di);
        for (Entry<String, String> si : colHeader.entrySet()) {
            if (di.containsKey(si.getKey())) {
                copy.put(si.getValue(), di.get(si.getKey()));
            }

        }
        return copy;
    }

    /**
     * @param sessionId
     * @param string
     * @return
     */
    public String replaceCount(String msg, Map<String, Object> context) {
        StringBuilder sb = new StringBuilder();
        for (String ci : count) {
            if (msg.contains(ci)) {
                String[] split = msg.split(ci);
                sb.append(split[0]);
                //
                String countWorld = "个";
                String replace = split[1];
                for (String ni : countWord) {
                    if (split[1].startsWith(ni)) {
                        replace = split[1].replace(ni, "");
                        countWorld = ni;
                        break;
                    }

                }
                List<String> countNames = new ArrayList<>();

                boolean hasAnd = false;
                for (String ai : andRel) {
                    if (replace.contains(ai)) {
                        hasAnd = true;
                        String[] split2 = replace.split(ai);
                        for (String li : split2) {
                            boolean xx = false;
                            for (String aj : andRel) {
                                if (li.contains(aj)) {
                                    xx = true;
                                }
                            }
                            if (!xx) {
                                countNames.add(li);
                            }
                        }
                    }
                }
                if (!hasAnd) {
                    countNames.add(replace);
                }
                StringBuilder countStr = new StringBuilder();
                for (String cni : countNames) {
                    if (countStr.length() > 1) {
                        countStr.append("、");
                    }
                    final String cw = countWorld;
                    Callable<String> callabel = () -> {
                        StringBuilder sbx = new StringBuilder();
                        Map<String, Object> attMapBy = neo4jUService.getAttMapBy(NAME, cni, META_DATA);
                        if (attMapBy != null) {
                            String str = fun.count(label(attMapBy)) + cw + cni;
                            sbx.append(str);
                        }

                        return sbx.toString();
                    };
                    String vt = VtPool.vt(callabel);
                    countStr.append(vt);
                }
                if (countStr.length() > 0) {
                    sb.append(countStr);
                }
                break;
            }
        }

        return sb.toString();
    }

    /**
     * 简化字段
     *
     * @throws DefineException
     */
    private Map<String, Object> handleStartWithTa(String sessionId, String string) {
        if (getOperateLabel(sessionId) != null && getOperateObject(sessionId) != null
                || getOperateRelation(sessionId) != null) {
            List<String> heSheIt = Arrays.asList("他", "她", "它");
            for (String hi : heSheIt) {
                // 新增关系 xx and xx is xxRel

                for (String ni : andRel) {
                    addTaRealtion(string, hi + ni, sessionId);
                }
                // 新增关系 xx 是 bb de xxRel
                for (String ni : kEqualv) {
                    addIsTaDeRealtion(string, hi + ni, sessionId);
                }
                // 新增属性 o的xxx是bbb
                for (String ni : ownWords) {
                    Map<String, Object> addTaDeProp = addTaDeProp(string, hi + ni, sessionId);
                    if (addTaDeProp != null) {
                        return addTaDeProp;
                    }
                }
            }
        }

        return null;
    }

    public Map<String, Object> multiParseAndexcute(String string2, String sessionId) {
        // 替换掉声音助词
        String string = clearVoiceWord(string2);
        // huoq唤醒词：
//	neo4jService.listDataByLabel("w");

        Map<String, Object> operateMeta = enterMeta(sessionId, string);
        if (operateMeta != null) {
            return operateMeta;
        }

        Map<String, Object> operateObject = useIt(sessionId, string);
        if (operateObject != null) {
            return operateObject;
        }
        handleStartWithTa(sessionId, string);
        List<String> relProp = Arrays.asList("关系属性");
        for (String hi : relProp) {
            addRelationProp(string, hi, sessionId);
        }
        return singleSentence(string, sessionId);
    }

    private Map<String, Object> enterMeta(String sessionId, String string) {
        Map<String, Object> operateMeta = null;

        for (String ni : getUseWords) {
            operateMeta = useSomething(string, ni, sessionId);
            if (operateMeta != null) {
                return operateMeta;
            }
        }
        return operateMeta;
    }

    private Map<String, Object> manage(String sessionId, String string) {
        Map<String, Object> operateMeta = new HashMap<>();

        for (String ni : manageNode) {
            operateMeta = manageSomething(string, ni, sessionId);
            if (operateMeta != null) {
                return operateMeta;
            }
        }
        return operateMeta;
    }

    private Map<String, Object> useIt(String sessionId, String string) {
        Map<String, Object> operateObject = null;
        for (String ni : getUseWords) {
            operateObject = useObject(string, ni, sessionId);
            if (operateObject != null) {
                return operateObject;
            }
        }
        return null;
    }

    /**
     * 批量操作，同一种操作，CUD
     *
     * @param string
     * @param sessionId
     * @return
     */
    private Map<String, Object> singleSentence(String string, String sessionId) {
        List<Map<String, Object>> dataCreate = new ArrayList<>();
        for (String ni : newNode) {
            List<Map<String, Object>> createSomething = createSomething(string, ni, sessionId);
            dataCreate.addAll(createSomething);
        }
        if (dataCreate != null && !dataCreate.isEmpty()) {
            Map<String, Object> data = new HashMap<>();
            data.put("data", dataCreate);
            return data;
        }
        List<Map<String, Object>> updateCreate = new ArrayList<>();

        for (String ni : newUpdate) {
            List<Map<String, Object>> updates = updateSomething(string, ni);
            updateCreate.addAll(updates);
        }
        if (updateCreate != null && !updateCreate.isEmpty()) {
            Map<String, Object> data = new HashMap<>();
            data.put("data", dataCreate);
            return data;
        }

        for (String ni : newRelation) {
            addSomeRealtion(string, ni, sessionId);
        }

        for (String ni : removes) {
            deleteSomething(string, ni, sessionId);
        }
        return null;
    }

    private String clearVoiceWord(String string2) {
        String replaceAll = string2.replaceAll("嗯", "");
        List<String> wcSet = Arrays.asList("诶", "呃", "乌", "阿", "偌", "得", "叱", "吓", "吁", "呔", "呐", "呜", "呀", "呵", "哎",
                "咄", "咍", "呣", "呶", "呸", "呦", "哈", "咳", "哑", "咦", "哟", "咨", "啊", "唉", "唗", "哦", "哼", "唦", "喏", "啧", "嗏",
                "喝", "嗟", "喂", "喔", "嗄", "嗳", "嗤", "嘟", "嗨", "嗐", "嗯", "嘘", "嘿", "噢", "嘻", "噫", "嚄", "嚯", "於", "欸", "恶",
                "究竟", "终究", "他妈的", "日你的妈");
        for (String ni : wcSet) {
            if (string2.indexOf("ni") > -1) {
                replaceAll = replaceAll.replaceAll(ni, "");
            }
        }

        return replaceAll;
    }

    /**
     * 创建相关的事情
     *
     * @param string
     */
    private List<Map<String, Object>> createSomething(String string, String newCreate, String sessionId) {
        string = clearFuhao(string);
        List<Map<String, Object>> createObjects = new ArrayList<>();
        if (string.indexOf(newCreate) > -1) {
            String[] create = string.split(newCreate);
            for (String createObject : create) {
                if (createObject != null && !"".equals(createObject.trim()) && !createObject.startsWith("关系")) {
                    Map<String, Object> createOne = createOne(createObject, sessionId);
                    if (createOne != null) {
                        createObjects.add(createOne);
                    }
                }
            }
        }
        if (createObjects.size() == 1) {
            setMayConextProp(OPERATE_OBJECT, createObjects.get(0), sessionId);
        }

        return createObjects;
    }

    /**
     * 操作：元数据（用户，项目）。 更新某个东西 do（将，把）xxx的sss修改为（成）bb
     *
     * @param string
     * @param updateWord
     */
    private List<Map<String, Object>> updateSomething(String string, String updateWord) {
        string = clearFuhao(string);
        List<Map<String, Object>> updateObjects = new ArrayList<>();
        if (string.indexOf(updateWord) > -1) {
            String[] updates = string.split(updateWord);
            String operatObject = updates[0];
            if (operatObject != null && operatObject.startsWith("操作")) {
                String label = operatObject.replace("操作", "");
                if (label.length() > 1) {
                    for (String updateObject : updates) {
                        if (updateObject != null && !"".equals(updateObject.trim()) && !updateObject.startsWith("关系")) {
                            List<Map<String, Object>> updateOne = updateOne(updateObject, label);
                            updateObjects.addAll(updateOne);
                        }
                    }
                }
            }
        }
        return updateObjects;
    }

    /**
     * @param string
     * @param useWordi
     */
    private Map<String, Object> useSomething(String string, String useWordi, String sessionId) {
        string = clearFuhao(string);
        if (string.indexOf(useWordi) > -1) {
            if (string != null && string.startsWith(useWordi)) {
                String[] split = string.split(useWordi);
                if (split.length > 2) {// 进入操作，进入操作
                    for (String si : split) {

                    }
                } else {
                    String metaName = string.replace(useWordi, "");
                    if (metaName.length() > 1) {
                        Map<String, Object> metaNode = getNode(META_DATA, "name", metaName);
                        if (metaNode != null) {
                            String label = label(metaNode);
                            getInto(label, sessionId);
                            setMayConextProp(OPERATE_META, metaNode, sessionId);
                        }
                        return metaNode;
                    }
                }
            }
        }
        return null;
    }

    private Map<String, Object> manageSomething(String string, String manageWordi, String sessionId) {
        string = clearFuhao(string);
        if (string.indexOf(manageWordi) > -1) {
            if (string != null && string.startsWith(manageWordi)) {
                String[] split = string.split(manageWordi);
                if (split.length > 2) {// 进入操作，进入操作

                } else {
                    String metaName = string.replace(manageWordi, "");
                    if (metaName.length() > 1) {
                        String queryByNameOrCode = "Match(n:MetaData) where n.name  CONTAINS '" + metaName
                                + "' OR n.code  CONTAINS '" + metaName + "'  OR n.label  CONTAINS '" + metaName
                                + "' return n";
                        List<Map<String, Object>> queryList = neo4jService.cypher(queryByNameOrCode);
                        List<String> manageHref = new ArrayList<>();
                        if (queryList != null && !queryList.isEmpty()) {
                            setMayConextProp(OPERATE_META_LIST, queryList, sessionId);
                            for (Map<String, Object> mi : queryList) {

                                String xxi = "<a href=\"javascript:;\" onclick=\"window.open('" + LemodoApplication.MODULE_NAME + "/po/" + label(mi)
                                        + "')\"> 【" + name(mi) + "】</a>";
                                if (manageHref.size() > 0) {
                                    manageHref.add("、" + xxi);
                                } else {
                                    manageHref.add(xxi);
                                }
                            }
                        }
                        Map<String, Object> data = new HashMap<>();
                        data.put("data", manageHref);
                        return data;
                    }
                }
            }
        }
        return null;
    }

    private Map<String, Object> useObject(String string, String useWordi, String sessionId) {
        string = clearFuhao(string);
        if (string.indexOf(useWordi) > -1) {
            if (string != null && string.startsWith(useWordi)) {
                String objectName = string.replace(useWordi, "");
                String trimName = objectName.trim();
                if (trimName.length() > 1) {
                    Map<String, Object> objectNode = getNode(getOperateLabel(sessionId), "name", trimName);
                    if (objectNode != null) {
                        setMayConextProp(OPERATE_OBJECT, objectNode, sessionId);
                    }
                    return objectNode;
                }
            }
        }
        return null;
    }

    private void getInto(String label, String sessionId) {
        Map<String, Object> myContext = getMyContext(sessionId);
        myContext.put(OPERATE_LABEL, label);
    }

    private void setMayConextProp(String key, Object value, String sessionId) {
        getMyContext(sessionId).put(key, value);
    }

    /**
     * 根据会话ID 获取上下文
     *
     * @param sessionId
     * @return
     */
    public Map<String, Object> getMyContext(String sessionId) {
        Map<String, Object> myContext = context.get(sessionId);
        if (myContext == null) {
            myContext = new HashMap<>();
            context.put(sessionId, myContext);
        }
        if (myContext.get("MyId") == null) {
            myContext.put("MyId", sessionId);
        }
        return myContext;
    }

    public Map<String, Map<String, Object>> getContextmap() {
        return context;
    }

    private String getMyKey() {
        String currentUserName = adminService.getCurrentAccount();
        String currentUserId = adminService.getCurrentPasswordId() + "";
        String userkey = currentUserId + "-" + currentUserName;
        return userkey;
    }

    private String getOperateLabel(String sessionId) {
        return string(getMyContext(sessionId), OPERATE_LABEL);
    }

    private String getColumnByHeader(String headeri, String sessionId) {
        Map<String, Object> metaMap = mapObject(getMyContext(sessionId), OPERATE_META);
        return getColByHeader(metaMap, headeri);
    }

    private Map<String, Object> getOperateObject(String sessionId) {
        return mapObject(getMyContext(sessionId), OPERATE_OBJECT);
    }

    private Map<String, Object> getOperateRelation(String sessionId) {
        return mapObject(getMyContext(sessionId), OPERATE_RELATION);
    }

    private void addSomeRealtion(String string, String newRelWord, String sessionId) {
        string = clearFuhao(string);
        if (string.indexOf(newRelWord) > -1) {
            String[] create = string.split(newRelWord);
            for (String createObject : create) {
                if (createObject != null && !"".equals(createObject.trim()) && createObject.startsWith("关系")) {
                    addOneRelation(createObject, sessionId);
                }
            }
        }
    }

    private void addTaRealtion(String string, String newRelWord, String sessionId) {
        string = clearFuhao(string);
        if (string.indexOf(newRelWord) > -1 && string.startsWith(newRelWord)) {
            String[] create = string.split(newRelWord);
            if (create[1] != null && !"".equals(create[1].trim())) {
                addTaAndXIsRelation(create[1], sessionId);
            }
        }
    }

    private void addIsTaDeRealtion(String string, String newRelWord, String sessionId) {
        string = clearFuhao(string);
        if (string.indexOf(newRelWord) > -1 && string.startsWith(newRelWord)) {
            String[] relOne = string.split(newRelWord);
            if (relOne[1] != null && !"".equals(relOne[1].trim())) {
                isTadeXRelation(relOne[1], sessionId);
            }
        }
    }

    private Map<String, Object> addTaDeProp(String string, String newRelWord, String sessionId) {
        string = clearFuhao(string);
        if (string.indexOf(newRelWord) > -1 && string.startsWith(newRelWord)) {
            String[] prop = string.split(newRelWord);
            if (prop[1] != null && !"".equals(prop[1].trim())) {
                return addTadeProp(prop[1], sessionId);
            }
        }
        return null;
    }

    private void addRelationProp(String string, String newRelWord, String sessionId) {
        string = clearFuhao(string);
        if (string.indexOf(newRelWord) > -1 && string.startsWith(newRelWord)) {
            String[] prop = string.split(newRelWord);
            if (prop[1] != null && !"".equals(prop[1].trim())) {
                addRelDeProp(prop[1], sessionId);
            }
        }
    }

    private void addRelDeProp(String itIs, String sessionId) {
        String key = "";
        String value = "";

        for (String ui : kEqualv) {
            if (itIs.indexOf(ui) > -1) {
                String[] prop = itIs.split(ui);
                key = prop[0].trim();
                value = prop[1].trim();

                Map<String, Object> nodeMap = getOperateRelation(sessionId);
                if (nodeMap != null) {
                    Map<String, Object> data = new HashMap<>();
                    data.put(key, value);
                    Long startId = id(nodeMap);
                    neo4jService.saveRelById(startId, nodeMap);
                }
            }
        }

    }

    private Map<String, Object> addTadeProp(String itIs, String sessionId) {
        String key = "";
        String value = "";
        Map<String, Object> startNode = getOperateObject(sessionId);

        for (String ui : kEqualv) {
            if (itIs.indexOf(ui) > -1) {
                String[] prop = itIs.split(ui);
                key = prop[0].trim();
                // 判断可以是否是关系，如朋友，父亲，上级，下级，后续，前序。
                Map<String, Object> endNode = null;
                if (relName.contains(key)) {
                    // 添加关系,同类中找
                    endNode = getNode(getOperateLabel(sessionId), NAME, value);
                } else {// 跨元数据关系
                    Map<String, Object> node = getNode(META_DATA, NAME, key);
                    if (node != null) {
                        endNode = getNode(label(node), NAME, value);
                    }
                }
                if (endNode != null) {
                    relationService.addRel(key, id(startNode), id(endNode));
                    return new HashMap<>();
                }

                value = prop[1].trim();

                if (startNode != null) {
                    String columnKey = getColumnByHeader(key, sessionId);
                    startNode.put(columnKey, value);
                    neo4jService.saveById(string(startNode, ID), startNode);
                    return startNode;
                }
            }
        }
        return null;

    }

    /**
     * 是xxx的朋友
     *
     * @param itIs
     * @param sessionId
     */
    private void isTadeXRelation(String itIs, String sessionId) {
        String endName = "";
        String relName = "";

        for (String ui : ownWords) {
            if (itIs.indexOf(ui) > -1) {
                String[] prop = itIs.split(ui);
                endName = prop[0].trim();
                relName = prop[1].trim();

                addRelateInCurrentMeta(sessionId, endName, relName);
            }
        }

    }

    /**
     * 在当前元数据中添加关系
     *
     * @param sessionId
     * @param endName
     * @param relName
     */
    private void addRelateInCurrentMeta(String sessionId, String endName, String relName) {
        Map<String, Object> endNode = getNode(getOperateLabel(sessionId), NAME, endName);

        Map<String, Object> startNode = getOperateObject(sessionId);
        if (endNode != null && startNode != null) {
            Map<String, Object> data = new HashMap<>();
            data.put(NAME, relName);
            Long endId = id(endNode);
            Long startId = id(startNode);
            relationService.addRel(relName, startId, endId, data);
            setMayConextProp(OPERATE_RELATION, data, sessionId);
        }
    }

    /**
     * ta和tb是朋友
     *
     * @param itIs
     * @param sessionId
     */
    private void addTaAndXIsRelation(String itIs, String sessionId) {
        String endName = "";
        String relName = "";
        for (String ui : isRel) {
            if (itIs.indexOf(ui) > -1) {
                String[] prop = itIs.split(ui);
                endName = prop[0].trim();
                relName = prop[1].trim();

                Map<String, Object> endNode = getNode(getOperateLabel(sessionId), "name", endName);

                Map<String, Object> startNode = getOperateObject(sessionId);
                if (endNode != null && startNode != null) {
                    Map<String, Object> data = new HashMap<>();
                    data.put(NAME, relName);
                    relationService.addRel(relName, id(startNode), id(endNode), data);
                }
            }
        }

    }

    /**
     * xx的relation是bb xx是bb的relation
     * <p>
     * 操作关系：同类关系，不同元数据关系。 关系属性。 给用户xx添加一个账号。账号是名称全拼。
     * <p>
     * 方向关系：出关系，入关系。 开始节点是sss叫sp,ta的属性x是dd 结束节点是eee叫ep,ta的属性x是dd 关系属性： 开始时间，结束时间，状态
     * 如何写脚本识别？
     *
     * @param createObject
     */
    private void addOneRelation(String createObject, String sessionId) {
        String[] subject = createObject.split("的");
        String so = subject[1];
        String si = subject[1];
        String relationName = "";
        String relEndObject = "";
        for (String ki : kEqualv) {
            if (si.indexOf(ki) > -1) {
                String[] prop = si.split(ki);
                relationName = prop[0].trim();
                relEndObject = prop[1].trim();
                // relEndObject = splitByPronoun(relEndObject, sessionId);

                Map<String, Object> node = getNode(META_DATA, "name", relationName);
                if (node != null) {
                    String label = label(node);
                    Map<String, Object> data = new HashMap<>();
                    data.put(NAME, relEndObject);
                    data.put(LABEL, label);
                    Node saveByBody = neo4jService.saveByBody(data, label);
                    break;
                }
            }
        }

    }

    /**
     * 属性中文名是叫为等于xx的xx
     *
     * @param createObject
     */
    private Map<String, Object> recognitDifferntObject(String createObject) {
        String[] subject = createObject.split("的");
        String so = subject[1];
        String metaName = subject[1];
        String key = "";
        String value = "";
        for (String ni : kEqualv) {
            if (so.indexOf("ni") > -1) {
                String[] prop = so.split(ni);
                key = prop[0].trim();
                value = prop[1].trim();

                Map<String, Object> metaNode = getNode(META_DATA, "name", metaName);

                if (metaNode != null) {
                    String label = label(metaNode);
                    Map<String, String> nameColumn = nameColumn(metaNode);
                    String keyCode = nameColumn.get(key);
                    return getNode(label, keyCode, value);
                }
            }
        }
        return null;

    }

    /**
     * xxx的属性kkkk修改为vvv
     *
     * @param createObject
     * @param metaName
     */
    private List<Map<String, Object>> updateOne(String createObject, String metaName) {
        String[] subject = createObject.split("的");
        String objectName = subject[0];
        String propName = subject[1];
        propName = clearFuhao(propName);
        List<String> wcSet = Arrays.asList("修改为", "修改成", "改为", "改成", "变更为", "变更成", "更新成", "更新为", "刷新成", "刷新为");
        String key = null;
        String value = null;
        for (String ni : wcSet) {
            if (propName.indexOf(ni) > -1) {
                String[] prop = propName.split(ni);
                key = prop[0].trim();
                value = prop[1].trim();
            }
        }
        List<Map<String, Object>> retUpdate = new ArrayList<>();
        Map<String, Object> metaNode = getNode(META_DATA, "name", metaName);
        if (metaNode != null) {
            String label = label(metaNode);
            Map<String, Object> objectNode = getNode(label, "name", objectName);
            if (objectNode != null) {
                Long id = id(objectNode);
                Map<String, Object> data = new HashMap<>();
                String colByHeader = getColByHeader(metaNode, key);
                data.put(colByHeader, value);
                neo4jService.update(data, id);
                retUpdate.add(data);
            }
        }
        return retUpdate;
    }

    private Map<String, Object> createOne(String createObject, String sessionId) {
        String propName = getPropName(createObject);
        propName = clearFuhao(propName);
        String metaName = "";
        String name = "";
        Map<String, Object> node = null;
        if (propName.indexOf("叫") > -1) {
            String[] prop = propName.split("叫");
            metaName = prop[0].trim();
            name = prop[1].trim();
            name = splitByPronoun(name, sessionId);
            node = getNode(META_DATA, "name", metaName);
        } else {

            String operateLabel = getOperateLabel(sessionId);
            Map<String, Object> data = new HashMap<>();
            data.put(NAME, propName);
            data.put(LABEL, operateLabel);
            Node saveByBody = neo4jService.saveByBody(data, operateLabel);
            return data;
        }
        if (node != null) {
            String label = label(node);
            Map<String, Object> data = new HashMap<>();
            data.put(NAME, name);
            data.put(LABEL, label);
            Node saveByBody = neo4jService.saveByBody(data, label);
            return data;
        }
        return null;
    }

    private String getPropName(String createObject) {
        if (createObject.indexOf("个") < 0) {// 没有两次
            return createObject;
        }
        String[] subject = createObject.split("一个");
        String propName = subject[1];
        return propName;
    }

    private String clearFuhao(String propName) {
        propName = propName.replaceAll(",", "");
        propName = propName.replaceAll("，", "");
        propName = propName.replaceAll("、", "");
        propName = propName.replaceAll("。", "");
        propName = propName.replaceAll("<div><br></div>", "");
        propName = propName.replaceAll("</pre>", "");

        return propName;
    }

    private Map<String, Object> deleteOne(String string, String delKey, String sessionId) {
        string = clearFuhao(string);
        if (string.indexOf(delKey) > -1) {
            String[] dels = string.split(delKey);
            String delObject = dels[1];
            String objName = "";
            if (delObject.indexOf("个") > -1) {
                String[] subject = delObject.split("个");
                objName = subject[1];
            } else {
                objName = delObject;
            }

            String name = objName;
            String label = getOperateLabel(sessionId);
            Map<String, Object> data = new HashMap<>();
            data.put(NAME, name);
            neo4jService.removeNodeByPropAndLabel(data, label);
            if (string(data, ID) != null) {
                return data;
            }
        }
        return null;
    }

    private void deleteSomething(String string, String delKey, String sessionId) {
        string = clearFuhao(string);
        if (string.indexOf(delKey) > -1) {
            String[] create = string.split(delKey);
            String createObject = create[1];
            String propName = "";
            if (createObject.indexOf("个") > -1) {
                String[] subject = createObject.split("一个");
                propName = subject[1];
            } else {
                propName = createObject;
            }

            String metaName = "";
            String name = "";
            if (propName.indexOf("叫") > -1) {
                String[] prop = propName.split("叫");
                metaName = prop[0].trim();
                name = prop[1].trim();
                name = splitByPronoun(name, sessionId);
            } else {
                metaName = splitByPronoun(propName, sessionId);
            }

            Map<String, Object> node = getNode(META_DATA, "name", metaName);
            String label = label(node);
            Map<String, Object> data = new HashMap<>();
            data.put(NAME, name);
            data.put(LABEL, label);
            neo4jService.removeNodeByPropAndLabel(data, label);
        }
    }

    private String splitByPronoun(String name, String sessionId) {
        if (name.indexOf("他") > -1) {
            String[] split = name.split("他");
            name = split[0];
            parseAndexcute(split[1], sessionId);
        }
        if (name.indexOf("它") > -1) {
            String[] split = name.split("它");
            name = split[0];
            parseAndexcute(split[1], sessionId);
        }
        if (name.indexOf("她") > -1) {
            String[] split = name.split("她");
            name = split[0];
            parseAndexcute(split[1], sessionId);
        }
        return name;
    }

}
