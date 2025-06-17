package com.wldst.ruder.module.workflow.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.wldst.ruder.util.DateTool;
import com.wldst.ruder.util.DateUtil;

public class TextUtil {
	public static final String EMPTY_STRING = "";

	public static final void assertNotBlank(String theString, String theMessage) {
		if (theString == null) {
			throw new IllegalArgumentException("Null argument not allowed: "
					+ theMessage);
		}

		if (!(theString.trim().equals("")))
			return;
		throw new IllegalArgumentException("Blank argument not allowed: "
				+ theMessage);
	}

	public static final void assertBoolean(String theString, String theMessage) {
		assertNotBlank(theString, theMessage);
		if ((theString.equalsIgnoreCase("yes"))
				|| (theString.equalsIgnoreCase("true"))
				|| (theString.equalsIgnoreCase("no"))
				|| (theString.equalsIgnoreCase("false"))
				|| (theString.equalsIgnoreCase("y"))
				|| (theString.equalsIgnoreCase("n"))
				|| (theString.equalsIgnoreCase("1"))
				|| (theString.equalsIgnoreCase("0"))) {
			return;
		}

		throw new IllegalArgumentException(theMessage);
	}

	public static final boolean isBlank(String str) {
		return ((str == null) || (str.trim().length() <= 0));
	}

	public static String filterChar(String srcStr) {
		String rtnStr = srcStr.replaceAll("'", "\\\\'");
		rtnStr = rtnStr.replaceAll("\"", "\\\\\"");
		return rtnStr;
	}

	public static boolean toBoolean(String theString) {
		if (theString == null) {
			return false;
		}
		theString = theString.trim();

		return ((theString.equalsIgnoreCase("y"))
				|| (theString.equalsIgnoreCase("yes"))
				|| (theString.equalsIgnoreCase("true")) || (theString
				.equalsIgnoreCase("1")));
	}

	public static final String nvl(String s) {
		return nvl(s, "");
	}

	public static final String nvl(String s, String sDef) {
		return ((s == null) ? sDef : s);
	}

	public static final List<String> splitStrList(String str, String sDef) {
		List<String> retList = null;
		if ((str != null) && (str.length() > 0) && (sDef != null)
				&& (sDef.length() > 0)) {
			String[] retArray = splitStrArray(str, sDef);
			if ((retArray != null) && (retArray.length > 0)) {
				retList = new ArrayList<String>();
				for (int i = 0; i < retArray.length; ++i) {
					retList.add(retArray[i]);
				}
			}
		}
		return retList;
	}

	public static final String[] splitStrArray(String str, String sDef) {
		String[] retArray = null;
		if ((str != null) && (str.length() > 0) && (sDef != null)
				&& (sDef.length() > 0)) {
			retArray = str.split(sDef);
		}
		return retArray;
	}

	public static final long[] splitLongArray(String str, String sDef) {
		long[] retArray = null;
		String[] tempArray = splitStrArray(str, sDef);
		if ((tempArray != null) && (tempArray.length > 0)) {
			int length = tempArray.length;
			retArray = new long[length];
			for (int i = 0; i < length; ++i) {
				try {
					retArray[i] = Long.parseLong(tempArray[i]);
				} catch (Exception ex) {
					retArray[i] = -1L;
				}
			}
		}
		return retArray;
	}

	public static final long[] toLongArray(String[] str) {
		if (str == null) {
			return null;
		}

		long[] l = new long[str.length];
		for (int i = 0; i < str.length; ++i) {
			l[i] = Long.parseLong(str[i]);
		}
		return l;
	}

	public static final String replaceString(String str1, String str2,
			String str3) {
		if (str1 == null) {
			return "";
		}

		return str1.replaceAll(str2, str3);
	}

	public static final String subString(String text, int length) {
		String retStr = null;
		if (text != null) {
			if (length < text.length()) {
				retStr = text.substring(0, length) + " ...";
			} else {
				retStr = text;
			}
		}
		return retStr;
	}

	public static final String fixLengthText(int num, int fixLength,
			String fixChar) {
		StringBuffer retStr = new StringBuffer();
		String tempStr = String.valueOf(num);
		if ((tempStr != null) && (tempStr.length() <= fixLength)) {
			for (int i = tempStr.length(); i < fixLength; ++i) {
				retStr.append(fixChar);
			}
			retStr.append(tempStr);
		} else if (tempStr != null) {
			retStr.append(tempStr.substring(0, fixLength));
		}
		return retStr.toString();
	}

	public static final String fixLengthText(String convertStr, int fixLength,
			String fixChar) {
		StringBuffer retStr = new StringBuffer();
		if ((convertStr != null) && (convertStr.length() <= fixLength)) {
			for (int i = convertStr.length(); i < fixLength; ++i) {
				retStr.append(fixChar);
			}
			retStr.append(convertStr);
		} else if (convertStr != null) {
			retStr.append(convertStr.substring(0, fixLength));
		}
		return retStr.toString();
	}

	public static String substringBetween(String str, String open, String close) {
		if (str == null || open == null || close == null) {
			return null;
		}
		int start = str.indexOf(open);
		if (start != -1) {
			int end = str.indexOf(close, start + open.length());
			if (end != -1) {
				return str.substring(start + open.length(), end);
			}
		}
		return null;
	}

	/**
	 * 获取指定字符串后得所有子字符串
	 * 
	 * @param str
	 *            需要截取得字符串
	 * @param start
	 *            需要开始截取得字符串
	 * @return 结果
	 */
	public static String substring(String str, String start) {
		if (str == null) {
			return null;
		}
		if (isBlank(start)) {
			return str;
		}
		int startIndex = str.indexOf(start);
		if (startIndex != -1) {
			return str.substring(startIndex + start.length());
		}
		return null;
	}

	public static String substr(String str, int startIdx, int endIdx) {
		if (StringUtils.isNotBlank(str)) {
			if (str.length() >= endIdx && str.length() >= startIdx) {
				return str.substring(startIdx, endIdx);
			} else {
				return str;
			}
		}
		return "";
	}

	/**
	 * 获取编码(头字符+中字符+尾字符后四位)
	 * 
	 * @param appendF
	 *            头字符
	 * @param appendM
	 *            中字符
	 * @param appendL
	 *            尾字符
	 * @return
	 */
	public static String jointCode(String appendF, String appendM,
			String appendL) {
		StringBuffer s = new StringBuffer(TextUtil.nvl(appendF));
		s.append(TextUtil.nvl(appendM));
		if (TextUtil.isBlank(appendL))
			s.append(DateTool.format(DateUtil.getCurrentDate(), "mmss"));
		if (!TextUtil.isBlank(appendL) && appendL.length() < 4)
			s.append(TextUtil.fixLengthText(appendL, 4, "0"));
		if (!TextUtil.isBlank(appendL) && appendL.length() > 4)
			s.append(TextUtil.substr(appendL, appendL.length() - 4, appendL
					.length()));
		return s.toString();
	}

	/**
	 * 获取编码(头字符+系统当前日期[例如20130301]+尾字符后四位)
	 * 
	 * @param appendF
	 *            头字符
	 * @param appendL
	 *            尾字符
	 * @return
	 */
	public static String jointCode(String appendF, String appendL) {
		StringBuffer s = new StringBuffer(TextUtil.nvl(appendF));
		s.append(DateTool.format(DateUtil.getCurrentDate(), "yyyyMMdd"));
		if (TextUtil.isBlank(appendL))
			s.append(DateTool.format(DateUtil.getCurrentDate(), "mmss"));
		if (!TextUtil.isBlank(appendL) && appendL.length() < 4)
			s.append(TextUtil.fixLengthText(appendL, 4, "0"));
		if (!TextUtil.isBlank(appendL) && appendL.length() > 4)
			s.append(TextUtil.substr(appendL, appendL.length() - 4, appendL
					.length()));
		return s.toString();
	}

	public static void main(String[] args) {
		System.out.println(TextUtil.jointCode("PCR", "300000000041070205"));
	}

	public static String convertStr(String valueOf) {
		// TODO Auto-generated method stub
		if (valueOf == "null") {
			return null;
		} else {
			return valueOf;
		}

	}

	
	/**
	 * 将数组转换为指定字符串隔开的字符窜
	 * @param longAry
	 * @param splitStr
	 * @return
	 */
	public static String convertAryToStr(long[] longAry,String splitStr) {
		String retStr = "";
		if (longAry != null) {
			int aryLen = longAry.length;
			for (int i = 0; i < aryLen; i++) {
				if (i != aryLen)
					retStr += longAry[i] + splitStr;
				else
					retStr += longAry[i];
			}
		}
		return  retStr;
	}
	/**
	 * 将字符串中的html标签替换
	 * @param message
	 * @return
	 */
	public static String  replaceTheHtmlLabelFilter(String message){
		StringBuffer  result = new StringBuffer();
		if(StringUtils.isBlank(message)){
			return "";
		}else{
			char content []  = new char[message.length()];
			message.getChars(0, message.length(), content, 0);
			for (int  i= 0;  i < content.length; i++) {
				switch(content[i]){
				case '<':
					result.append("&lt;");
					break;
				case '>':
					result.append("&gt;");
					break;
				case '&':
					result.append("&amp;");
					break;
				case '"':
					result.append("&quot;");
					break;
				default:
					result.append(content[i]);
					break;
				}
			}
			
		}
		return result.toString();
	}
	/**
	 * 将字符串中的标签代码替换为标签
	 * @param message
	 * @return
	 */
	public static String  replaceTheHtmlStringFilter(String message){
		if(StringUtils.isBlank(message)){
			return "";
		}
		return message.replace("&lt;", "<").replace("&gt;", ">")
			   .replace("&amp;", "&").replace("&quot;", "\"");
	}
}
