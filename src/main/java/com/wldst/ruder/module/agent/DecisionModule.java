package com.wldst.ruder.module.agent;

public class DecisionModule {
    private KnowledgeBase knowledgeBase;

    public DecisionModule(KnowledgeBase knowledgeBase){
        this.knowledgeBase=knowledgeBase;
    }
    public DecisionModule(){
    }



    public KnowledgeBase getKnowledgeBase(){
        return knowledgeBase;
    }

    public void setKnowledgeBase(KnowledgeBase knowledgeBase){
        this.knowledgeBase=knowledgeBase;
    }

    public String makeDecision(String inference) {
        // 这里是一个简单的决策逻辑示例，根据推理结果做出决策
        if (inference.contains("分析")) {
            return "执行数据分析流程。";
        } else if (inference.contains("情况")) {
            return "评估当前环境状态并制定应对策略。";
        }

        return "没有足够的信息进行决策。";
    }
}
