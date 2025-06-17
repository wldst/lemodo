package com.wldst.ruder.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseKeyword {

    public static List<String> getKeywords(String txt) {
	String pattern = "\\$\\{(.+?)\\}";
	Pattern p = Pattern.compile(pattern);
	Matcher m = p.matcher(txt);
	List<String> list = new ArrayList<String>();
	while (m.find()) {
	    list.add(m.group());
	}
	return list;
    }

    public static String parse(String content, Map<String, Object> data) {
	String pattern = "\\$\\{(.+?)\\}";
	Pattern p = Pattern.compile(pattern);
	Matcher m = p.matcher(content);
	StringBuffer sb = new StringBuffer();
	while (m.find()) {
	    String key = m.group(1);
	    String value = String.valueOf(data.get(key));
	    m.appendReplacement(sb, data.get(key) == null ? "" : value);
	}
	m.appendTail(sb);
	return sb.toString();
    }

}