package com.wldst.ruder.module.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


// 模拟解析后的数据结构
public class ParsedData {
    private List<String> words = new ArrayList<>();

    public void addWord(String word) {
        this.words.add(word);
    }

    public List<String> getWords() {
        return words;
    }
}
