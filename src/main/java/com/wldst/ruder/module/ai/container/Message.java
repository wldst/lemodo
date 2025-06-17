package com.wldst.ruder.module.ai.container;
import  java.lang.Record;
public record Message(String sender, String receiver, String content) {
    public String toString() {
        return "Message{" +
                "sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", content=" + content +
                ", createdOn=" + java.time.LocalDateTime.now() + // 假设我们想添加当前时间戳
                '}';
    }

}
