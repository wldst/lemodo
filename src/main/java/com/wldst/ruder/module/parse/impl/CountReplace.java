package com.wldst.ruder.module.parse.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wldst.ruder.domain.ParseExcuteDomain;
import com.wldst.ruder.module.bs.Function;
import com.wldst.ruder.module.parse.MsgProcess;
import com.wldst.ruder.util.VtPool;
/**
 * CountReplace类实现了MsgProcess接口，用于处理消息中的统计请求。
 * 它通过解析输入消息，识别出需要统计的对象，并生成相应的统计结果字符串。
 *
 * @author wldst
 *
 */
@Component
public class CountReplace extends ParseExcuteDomain implements MsgProcess{

    // 注入Function服务，用于执行统计操作
    @Autowired
    private Function fun;

    /**
     * 处理：统计
     *
     * 本方法通过解析输入消息，识别出统计请求中的关键元素，如统计对象和统计类型，
     * 并生成相应的统计结果字符串。如果消息中包含有效的统计请求，会将统计结果添加到输出中。
     *
     * @param msg 输入的消息字符串，可能包含统计请求。
     * @param context 上下文对象，用于传递额外的信息。
     * @return 统计结果字符串。
     */
    @Override
    public String process(String msg, Map<String, Object> context) {
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
                    final String cw=countWorld;
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
                    context.put(USED, true);
                    sb.append(countStr);
                }
                break;
            }
        }


        return sb.toString();
    }
}
