package com.wldst.ruder.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class StringGet {
    public static  String getColumnSize(String type) {
	if(!type.contains("(")) {
	    return "";
	}
	String[] split = type.split("\\(");
	String ddString =  split[1].split("\\)")[0];
	if(ddString.contains(",")) {
	    return ddString.split(",")[0];
	}
	return ddString;
    }
    
    public static  Long getLong(String str) {
	if(isNumeric(str)) {
	  return   Long.valueOf(str);
	}
	
	return null;
    }
    
    public static boolean isNumeric(String str){
	    Pattern pattern = Pattern.compile("[0-9]*");
	    return pattern.matcher(str).matches();   
    }
    
    public static boolean isLetterDigit(String str) {
	  String regex = "^[a-z0-9A-Z]+$";
	  return str.matches(regex);
    }
    public static boolean isChinese(String str) {
	  String regex = "^[\u4e00-\u9fa5]+$";
	  return str.matches(regex);
  }
    public static String firstLow(String labeli) {
	return labeli.substring(0, 1).toLowerCase() + labeli.substring(1);
    }
    public static String firstBig(String labeli) {
	return labeli.substring(0, 1).toUpperCase() + labeli.substring(1);
    }
    
    public static String fixLabel(String labeli,String prefix) {
	int indexOf = labeli.toUpperCase().indexOf(prefix.toUpperCase());
	if(indexOf==0) {//前缀
	    return firstLow(labeli.substring(indexOf+prefix.length()));
	}
	if(indexOf>0) {//后缀
	    return firstLow(labeli.substring(0,indexOf));
	}
	return labeli;
	
    }
    
    public static String joins(long[] es) {
	StringBuilder joins = new StringBuilder();
	for(long di: es) {
	    if(joins.length()>0) {
		joins.append(",");
	    }
	    joins.append(di);
	}
	return joins.toString();
    }
    
    public static String joins(Object[] es) {
	StringBuilder joins = new StringBuilder();
	for(Object di: es) {
	    if(joins.length()>0) {
		joins.append(",");
	    }
	    joins.append(di);
	}
	return joins.toString();
    }
    
    public static String[] split(String header) {
	if(header==null) {
	    return null;
	}
	String[] mergOpenClose = mergOpenClose(header, "(", ")");
	if (mergOpenClose != null) {
	    String[] mergOpenClose2 = mergOpenClose(header, mergOpenClose, "（", "）");
	    if (mergOpenClose2 != null) {
		return mergOpenClose2;
	    }else {
		return mergOpenClose;
	    }
	} else {
	    String[] mergOpenClose2 = mergOpenClose(header, "（", "）");
	    if (mergOpenClose2 != null) {
		return mergOpenClose2;
	    }
	}

	return header.split(",");
    }
    private static String[] mergOpenClose(String header, String open, String close) {
	return mergOpenClose(header,null,open,close);
    }


    private static String[] mergOpenClose(String header,String[] split, String open, String close) {
	if(split==null) {
	    split= header.split(",");
	}
	boolean containBrankets = header.indexOf(open)>=0&&header.indexOf(close)>0;
	if(containBrankets) {
	    List<String> arList= new ArrayList<>();
	    StringBuilder sBuilder=new StringBuilder();
	    boolean merge=false;	   
	    for(String ci:split) {
		if(ci.indexOf(open)>=0||ci.indexOf(close)>0) {
		    if(!sBuilder.isEmpty()) {
			sBuilder.append(",");
		    }
		    sBuilder.append(ci);
		    
		    if(ci.indexOf(open)>=0) {
			merge=true;
		    }
		    if(ci.indexOf(close)>=0) {
			merge=false;
			arList.add(sBuilder.toString());
			sBuilder.delete(0, sBuilder.length());
		    }
		}else {
		    if(merge) {
			sBuilder.append(ci);
		    }else {
			arList.add(ci);
		    }
		}
	    }
	    String[] retStrings = new String[arList.size()];
	    return arList.toArray(retStrings);
	    
	}
	return null;
    }
    
    public static void main (String args[]) {
	String teString="1--==代码类型(1代码,2代码分组),代码ID,dd（主码,代码意义）,次吗,显示,zh状态(0否,1是)";
	testdata(teString);
	teString="2===代码类型（1代码,2代码分组）,代码ID,主码,代码意义,dd(次吗,显示),zh状态（0否,1是）";
	testdata(teString);
    }


    private static void testdata(String teString) {
	String[] splitHeaders = split(teString);
	for(String di:splitHeaders) {
	    System.out.println(di);
	}
    }

//    public static String join(String string, List<Long> oneList) {
//	List<String> sList = new ArrayList<>(oneList.size());
//	for(Long oi: oneList) {
//	    sList.add(String.valueOf(oi));
//	}
//	return String.join(string, sList);
//    }
    public static String join(String string, List<Object> oneList) {
	List<String> sList = new ArrayList<>(oneList.size());
	for(Object oi: oneList) {
	    sList.add(String.valueOf(oi));
	}
	return String.join(string, sList);
    }

}
