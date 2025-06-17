package com.wldst.ruder.module.ai.container;

import java.util.List;
import java.util.Map;

public interface IBigModel {
    String callModel(String agentId,String input);

    String callModel(String agentId,String input, List<Map<String, Object>> chatItems);
}