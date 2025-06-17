package com.wldst.ruder.module.agent;

public class Perception {

    // 模拟从用户获取数据的函数
    public String perceiveData(String userInput) {
        // 这里模拟了一个用户通过命令行输入数据的情况
        System.out.println("User input: " + userInput);

        // 实际应用中，可能会包含更复杂的逻辑来处理不同类型的数据
        return sanitizeInput(userInput);
    }

    // 简单地清理和验证用户输入的函数
    private String sanitizeInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Input cannot be empty or null");
        }
        // 在这里可以添加更多的数据清洗逻辑，例如去除特殊字符、转换大小写等
        return input.trim();
    }
}