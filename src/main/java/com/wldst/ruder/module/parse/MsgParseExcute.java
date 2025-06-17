package com.wldst.ruder.module.parse;

/**
 * 消息处理
 * @author wldst
 *
 */
public interface MsgParseExcute {
    Object parseAndexcute(String message, String sessionId);
}
