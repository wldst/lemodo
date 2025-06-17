package com.wldst.ruder.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ServiceException;

/**
 * 通用功能
 * 
 * @author wldst
 *
 */
public class CommonUtil {
    private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);

    /**
     * 判断是否是数字
     * 
     * @param value
     * @return
     */
    public static boolean isNumber(String value) {
	if(value==null||"".equals(value.trim())) {
	    return false;
	}
	Pattern pattern = Pattern.compile("^[\\d]+$");
	boolean matches = pattern.matcher(value).matches();
	return matches;
    }

    /**
     * 判断是否是浮点数据
     * 
     * @param value
     * @return
     */
    public static boolean isFloatNumber(String value) {
	Pattern pattern = Pattern.compile("^\\d+\\.\\d+&");
	boolean matches = pattern.matcher(value).matches();
	return matches;
    }

    /**
     * 让列宽随着导出的列长自动适应
     * 
     * @Title: autoWidth
     * @Description:
     * @param sheet
     * @param columnHearNames void
     * @author sunjiankuo
     * @throws @date 2018年12月4日 上午8:48:42
     * @version V1.0
     */
    public static void autoWidth(HSSFSheet sheet, String[] columnHearNames) {
	int columnWidth = 0;
	for (int colNum = 0; colNum < columnHearNames.length; colNum++) {
	    columnWidth = autoWidthColumni(sheet, columnWidth, colNum);
	}
    }

    /**
     * 自适应列宽度
     * 
     * @author liuqiang
     * @date 2019年4月19日 上午10:21:50
     * @version V1.0
     * @param sheet
     * @param columnWidth
     * @param colNum
     * @return
     */
    private static int autoWidthColumni(HSSFSheet sheet, int columnWidth, int colNum) {
	int colWidth = sheet.getColumnWidth(colNum) / 256;
	for (int rowNum = 0; rowNum < sheet.getLastRowNum(); rowNum++) {
	    HSSFRow currentRow;
	    // 当前行未被使用过
	    if (sheet.getRow(rowNum) == null) {
		currentRow = sheet.createRow(rowNum);
	    } else {
		currentRow = sheet.getRow(rowNum);
	    }
	    if (currentRow.getCell(colNum) != null) {
		columnWidth = currentRow.getCell(colNum).getStringCellValue().length();
		try {
		    HSSFCell currentCell = currentRow.getCell(colNum);
		    if (currentCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
			if (colWidth > columnWidth) {
			    columnWidth = colWidth;
			}
		    }
		} catch (Exception e) {
		    LoggerTool.error(logger,e.getMessage(), e);
		}
	    }
	}
	try {
	    int cw = (columnWidth + 4) * 256;

	    if (cw < 255 * 256) {

		sheet.setColumnWidth(colNum, cw);
	    } else {
		sheet.setColumnWidth(colNum, 6000);
	    }

	} catch (Exception e) {
	    LoggerTool.error(logger,e.getMessage(), e);
	}
	return columnWidth;
    }

    // 从类unix机器上获取mac地址
    public static String getMac(String ip) throws IOException {
	String mac = "";
	if (ip != null) {
	    try {
		Process process = Runtime.getRuntime().exec("arp " + ip);
		InputStreamReader ir = new InputStreamReader(process.getInputStream());
		LineNumberReader input = new LineNumberReader(ir);
		String line;
		StringBuffer s = new StringBuffer();
		while ((line = input.readLine()) != null) {
		    s.append(line);
		}
		mac = s.toString();
		if (!mac.isBlank()) {
		    mac = mac.substring(mac.indexOf(":") - 2, mac.lastIndexOf(":") + 3);
		}
		return mac;
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	return mac;
    }

    // 从windows机器上获取mac地址
    public static String getMacInWindows(final String ip) {
	String result = "";
	String[] cmd = { "cmd", "/c", "ping " + ip };
	String[] another = { "cmd", "/c", "ipconfig -all" };
	// 获取执行命令后的result
	String cmdResult = callCmd(cmd, another);
	// 从上一步的结果中获取mac地址
	result = filterMacAddress(ip, cmdResult, "-");
	return result;
    }

    // 命令执行
    public static String callCmd(String[] cmd, String[] another) {
	String result = "";
	String line = "";
	try {
	    Runtime rt = Runtime.getRuntime();
	    // 执行第一个命令
	    Process proc = rt.exec(cmd);
	    proc.waitFor();
	    // 执行第二个命令
	    proc = rt.exec(another);
	    InputStreamReader is = new InputStreamReader(proc.getInputStream());
	    BufferedReader br = new BufferedReader(is);
	    while ((line = br.readLine()) != null) {
		result += line;
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return result;
    }

    // 获取mac地址
    public static String filterMacAddress(final String ip, final String sourceString, final String macSeparator) {
	String result = "";
	String regExp = "((([0-9,A-F,a-f]{1,2}" + macSeparator + "){1,5})[0-9,A-F,a-f]{1,2})";
	Pattern pattern = Pattern.compile(regExp);
	Matcher matcher = pattern.matcher(sourceString);
	while (matcher.find()) {
	    result = matcher.group(1);
	    // 因计算机多网卡问题，截取紧靠IP后的第一个mac地址
	    int num = sourceString.indexOf(ip) - sourceString.indexOf(": " + result + " ");
	    if (num > 0 && num < 300) {
		break;
	    }
	}
	return result;
    }
    
   public static String getMacInfo(String ip)
    {
        //获取ip地址
        String macInfo = null;
        try
        {
            //linux下获取mac地址
           String macAddr = getMac(ip);
            //windows下获取mac地址
            if(macAddr!=null&&macAddr.isBlank()){
                macAddr = getMacInWindows(ip).trim();
            }
        }
        catch (Exception e)
        {
            return null;
        }
        return macInfo;
    }
   
  

	/**
	 * 实现 double 类型精确计算
	 */
	public static Double calculateDouble(double m, double n, String pattern) {
		BigDecimal a = new BigDecimal(Double.toString(m));
		BigDecimal b = new BigDecimal(Double.toString(n));
		// 计算结果
		double result = 0;
		// 逻辑处理
		switch (pattern) {
		case "+":
			result = a.add(b).doubleValue();
			break;
		case "-":
			result = a.subtract(b).doubleValue();
			break;
		case "*":
			result = a.multiply(b).doubleValue();
			break;
		case "/":
			result = a.divide(b).doubleValue();
			break;
		}
		return result;
	}

	/**
	 * 根据文件路径截取文件名
	 */
	public static String getFileName(String filePath) {
		int index = filePath.lastIndexOf("\\");
		if (index <= 0) {
			index = filePath.lastIndexOf("/");
		}
		return filePath.substring(index + 1, filePath.length());
	}

	/**
	 * 根据文件路径截取文件后缀名
	 */
	public static String getFileSuffix(String filePath) {
		return filePath.substring(filePath.lastIndexOf(".") + 1, filePath.length());
	}

	/**
	 * 根据文件路径获取文件大小, 返回字节大小
	 */
	public static Long getExactFileSize(String filePath) {
		return new File(filePath).length();
	}

	/**
	 * 根据文件路径获取文件大小, 返回值单位MB
	 */
	public static Double getFileSize(String filePath) {
		// 保留两位小数
		DecimalFormat df = new DecimalFormat("#.00");
		Long byteSize = getExactFileSize(filePath);
		Double realSize = calculateDouble((double) byteSize, 1024 * 1024, "/");
		return Double.parseDouble(df.format(realSize));
	}

	/**
	 * 根据文件路径删除文件
	 */
	public static void removeFile(String filePath) {
		File targetFile = new File(filePath);
		if (targetFile.exists()) {
			targetFile.delete();
		}
	}

	/**
	 * 解压 gzip 文件
	 * @throws IOException 
	 * @throws ServiceException 
	 */
	public static String uncompressGzipFile(String filePath) throws IOException, ServiceException {
		FileOutputStream outputStream = null;
		FileInputStream inputStream = null;
		GZIPInputStream gInputStream = null;
		String targetFilePath = "";
		try {
			inputStream = new FileInputStream(filePath);
			gInputStream = new GZIPInputStream(inputStream);
			targetFilePath = filePath.substring(0, filePath.lastIndexOf("."));
			targetFilePath = targetFilePath.substring(0, targetFilePath.lastIndexOf("."));
			outputStream = new FileOutputStream(targetFilePath);
			byte[] buf = new byte[1024];
			int num;
			while ((num = gInputStream.read(buf, 0, buf.length)) != -1) {
				outputStream.write(buf, 0, num);
			}
		} catch (Exception e) {
			throw new ServiceException("解压 GZIP 文件失败!");
		} finally {
			closeIO(outputStream, gInputStream, inputStream);
		}
		return targetFilePath;
	}

	/**
	 * 获取系统换行符
	 */
	public static String getSystemNewline() {
		return System.getProperty("line.separator");
	}

	/**
	 * 生成一个 UUID 随机字符串
	 */
	public static String generateUUID() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	/**
	 * 将BigDecimal转换为int
	 */

	public static int convertBigDecimalToInt(Object object) {
		int val = 0;
		try {
			val = Integer.valueOf(((BigDecimal) object).toString());
		} catch (Exception e) {
		}
		return val;
	}

	/**
	 * 关闭流
	 * @throws IOException 
	 */
	public static void closeIO(Closeable... ios) throws IOException {
		try {
			for (Closeable io : ios) {
				if (null != io) {
					io.close();
				}
			}
		} catch (IOException e) {
			throw new IOException("关闭流异常!");
		}
	}
   
   public static void main(String args[]) {
       System.out.println(getMacInfo("192.168.0.6"));
   }

}
