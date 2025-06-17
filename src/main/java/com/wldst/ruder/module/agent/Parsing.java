package com.wldst.ruder.module.agent;

public class Parsing {

    // 解析数据的函数，接收来自感知模块的数据
    public ParsedData parseInput(String data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Input data cannot be empty or null");
        }

        // 模拟解析过程，例如简单的字符串分割或提取关键信息
        String[] words = data.split("\\s+"); // 分割空格创建单词数组
        ParsedData parsedData = new ParsedData();

        for (String word : words) {
            if (!word.isEmpty()) { // 忽略空的分割结果
                parsedData.addWord(word);
            }
        }

        return parsedData;
    }

    
}
