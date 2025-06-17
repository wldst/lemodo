package com.wldst.ruder.util;

import com.wldst.ruder.constant.CruderConstant;
import com.wldst.ruder.exception.DefineException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LabelValidator {

    // 预编译正则表达式以提高性能，特别是当这个方法被频繁调用时
    private static final Pattern LABEL_PATTERN = Pattern.compile(CruderConstant.REGEX_NODE_LABEL);

    /**
     * 校验给定的label是否符合Cypher节点的label命名规范。
     *
     * @param label 待校验的标签字符串。
     * @throws DefineException 如果标签不符合规范，则抛出此异常。
     */
    public static void validateLabel(String label) throws DefineException {
        // 检查label是否为null或空，以避免NullPointerException
        if (label == null || label.isEmpty()) {
            throw new DefineException("标签不能为空");
        }

        // 使用预编译的正则表达式进行匹配
        Matcher matcher = LABEL_PATTERN.matcher(label);
        if (!matcher.matches()) {
            // 提供更详细的错误信息，指出哪个字符不符合规范
            // 由于无法直接获取到不符合规范的字符信息，因此提供一般性建议
            throw new DefineException(label+"节点标签不符合命名规范，请确保标签仅包含字母、数字、下划线（_）和冒号（:）。");
        }
    }
}
