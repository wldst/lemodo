package com.wldst.ruder.module.ai.container;

import java.util.List;
import java.util.Map;

public class BigModelClient implements IBigModel {
    @Override
    public String callModel(String agentId,String input) {
        // 实现调用大模型的逻辑
        // 可能需要网络请求、API调用等
        return "Response from the big model";
    }

    @Override
    public String callModel(String agentId,String input, List<Map<String, Object>> chatItems) {
        return null;
    }
}