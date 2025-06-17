package com.wldst.ruder.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.MediaType;
import com.wldst.ruder.constant.CruderConstant;

/**
 * 导出文件名称
 * 
 * @author liuqiang
 *
 */
public class ExportExcel {
    private static final Logger logger = LoggerFactory.getLogger(ExportExcel.class);

    // excel文件名称
    private String fileName;

    // 显示的导出表的标题
    private String title;

    // 导出表的列名
    private String[] headerNames;

    private List<Object[]> dataList = new ArrayList<Object[]>();

    private HttpServletResponse response;

    // 构造方法，传入要导出的数据
    /**
     * 
     * @param fileName
     * @param title
     * @param headers
     * @param dataList
     * @param response
     */
    public ExportExcel(String fileName, String title, String[] headers, List<Object[]> dataList,
	    HttpServletResponse response) {
	this.fileName = fileName;
	this.dataList = dataList;
	this.headerNames = headers;
	this.title = title;
	this.response = response;
    }

    // 构造方法，传入要导出的数据
    public ExportExcel(String fileName, String title, String[] headers, List<Object[]> dataList) {
	this.fileName = fileName;
	this.dataList = dataList;
	this.headerNames = headers;
	this.title = title;
    }

    /**
     * 导出数据
     */
    public File export() {
	try {
	    HSSFWorkbook workbook = new HSSFWorkbook(); // 创建工作簿对象
	    HSSFSheet sheet = workbook.createSheet(title); // 创建工作表

	    // 产生表格标题行
	    HSSFRow rowm = sheet.createRow(0);
	    HSSFCell cellTiltle = rowm.createCell(0);

	    // sheet样式定义【getColumnTopStyle()/getStyle()均为自定义方法 - 在下面 - 可扩展】
	    HSSFCellStyle columnTopStyle = this.getColumnTopStyle(workbook);// 获取列头样式对象
	    HSSFCellStyle style = this.getStyle(workbook); // 单元格样式对象

	    sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, (headerNames.length - 1)));
	    cellTiltle.setCellStyle(columnTopStyle);
	    cellTiltle.setCellValue(title);

	    // 定义所需列数
	    int columnNum = headerNames.length;
	    HSSFRow rowRowName = sheet.createRow(2); // 在索引2的位置创建行(最顶端的行开始的第二行)

	    // 将列头设置到sheet的单元格中
	    for (int n = 0; n < columnNum; n++) {
		HSSFCell cellRowName = rowRowName.createCell(n); // 创建列头对应个数的单元格
		cellRowName.setCellType(HSSFCell.CELL_TYPE_STRING); // 设置列头单元格的数据类型
		HSSFRichTextString text = new HSSFRichTextString(headerNames[n]);
		cellRowName.setCellValue(text); // 设置列头单元格的值
		cellRowName.setCellStyle(columnTopStyle); // 设置列头单元格样式
	    }

	    // 将查询出的数据设置到sheet对应的单元格中
	    for (int i = 0; i < dataList.size(); i++) {

		Object[] obj = dataList.get(i);// 遍历每个对象
		HSSFRow row = sheet.createRow(i + 3);// 创建所需的行数

		for (int j = 0; j < obj.length; j++) {
		    HSSFCell cell = row.createCell(j, HSSFCell.CELL_TYPE_STRING);
		    if (obj[j] != null) {
			if (obj[j] instanceof Date) {
			    Date d = (Date) obj[j];
			    cell.setCellValue(DateTool.format(d, "yyyy-MM-dd HH:mm:ss"));
			} else {
			    cell.setCellValue(obj[j].toString()); // 设置单元格的值
			}
		    } else {
			cell.setCellValue(""); // 设置单元格的值
		    }
		    cell.setCellStyle(style); // 设置单元格样式
		}
	    }
	    // 让列宽随着导出的列长自动适应
	    CommonUtil.autoWidth(sheet, headerNames);

	    if (workbook != null) {
		OutputStream out = null;
		try {
		    if (response != null) {
			response.setCharacterEncoding("utf-8");
			response.setContentType(MediaType.MICROSOFT_EXCEL.toString());
			// response.setContentType("application/msexcel");

			if (fileName == null) {
			    fileName = "Excel-" + String.valueOf(System.currentTimeMillis()).substring(4, 13)
				    + CruderConstant.FILE_TYPE_XLS;
			}
			// fileName = URLEncoder.encode(fileName,"utf-8").replaceAll("\\+", "%20");
			fileName = new String(fileName.getBytes("gb2312"), "iso8859-1");

			response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
			// response.setHeader("Content-Disposition", "attachment; filename=\"" +
			// fileName + "\"");
			out = response.getOutputStream();
			workbook.write(out);
			return null;
		    } else {
			File file = new File(fileName);
			out = new FileOutputStream(file);
			workbook.write(out);
			return file;
		    }
		} catch (IOException e) {
		    LoggerTool.error(logger,e.getMessage(), e);
		} finally {
		    try {
			workbook.close();
			if (out != null) {
			    out.flush();
			    out.close();
			}
		    } catch (IOException e) {
			LoggerTool.error(logger,e.getMessage(), e);
		    }
		}
	    }
	} catch (Exception e) {
	    LoggerTool.error(logger,e.getMessage(), e);
	}
	return null;
    }

    /**
     * 处理单元格格式的简单方式
     * 
     * @param hssfCell
     * @return
     */
    public static String formatCell(HSSFCell hssfCell) {
	if (hssfCell == null) {
	    return "";
	} else {
	    if (hssfCell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN) {
		return String.valueOf(hssfCell.getBooleanCellValue());
	    } else if (hssfCell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
		return String.valueOf(hssfCell.getNumericCellValue());
	    } else {
		return String.valueOf(hssfCell.getStringCellValue());
	    }
	}
    }

    /**
     * 处理单元格格式的第二种方式: 包括如何对单元格内容是日期的处理
     * 
     * @param cell
     * @return
     */
    public static String formatCell2(HSSFCell cell) {
	if (cell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN) {
	    return String.valueOf(cell.getBooleanCellValue());
	} else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {

	    // 针对单元格式为日期格式
	    if (HSSFDateUtil.isCellDateFormatted(cell)) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(HSSFDateUtil.getJavaDate(cell.getNumericCellValue())).toString();
	    }
	    return String.valueOf(cell.getNumericCellValue());
	} else {
	    return cell.getStringCellValue();
	}
    }

    /**
     * 处理单元格格式的第三种方法:比较全面
     * 
     * @param cell
     * @return
     */
    public static String formatCell3(HSSFCell cell) {
	if (cell == null) {
	    return "";
	}
	switch (cell.getCellType()) {
	case HSSFCell.CELL_TYPE_NUMERIC:

	    // 日期格式的处理
	    if (HSSFDateUtil.isCellDateFormatted(cell)) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(HSSFDateUtil.getJavaDate(cell.getNumericCellValue())).toString();
	    }

	    return String.valueOf(cell.getNumericCellValue());

	// 字符串
	case HSSFCell.CELL_TYPE_STRING:
	    return cell.getStringCellValue();

	// 公式
	case HSSFCell.CELL_TYPE_FORMULA:
	    return cell.getCellFormula();

	// 空白
	case HSSFCell.CELL_TYPE_BLANK:
	    return "";

	// 布尔取值
	case HSSFCell.CELL_TYPE_BOOLEAN:
	    return cell.getBooleanCellValue() + "";

	// 错误类型
	case HSSFCell.CELL_TYPE_ERROR:
	    return cell.getErrorCellValue() + "";
	}

	return "";
    }

    /**
     * 列头单元格样式
     */
    public HSSFCellStyle getColumnTopStyle(HSSFWorkbook workbook) {
	// 设置字体
	HSSFFont font = workbook.createFont();
	// 设置字体大小
	font.setFontHeightInPoints((short) 11);
	// 字体加粗
	font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
	// 设置字体名字
	font.setFontName("Courier New");
	// 设置样式;
	HSSFCellStyle style = workbook.createCellStyle();
	// 设置底边框;
	style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
	// 设置底边框颜色;
	style.setBottomBorderColor(HSSFColor.BLACK.index);
	// 设置左边框;
	style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
	// 设置左边框颜色;
	style.setLeftBorderColor(HSSFColor.BLACK.index);
	// 设置右边框;
	style.setBorderRight(HSSFCellStyle.BORDER_THIN);
	// 设置右边框颜色;
	style.setRightBorderColor(HSSFColor.BLACK.index);
	// 设置顶边框;
	style.setBorderTop(HSSFCellStyle.BORDER_THIN);
	// 设置顶边框颜色;
	style.setTopBorderColor(HSSFColor.BLACK.index);
	// 在样式用应用设置的字体;
	style.setFont(font);
	// 设置自动换行;
	style.setWrapText(false);
	// 设置水平对齐的样式为居中对齐;
	style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
	// 设置垂直对齐的样式为居中对齐;
	style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
	return style;
    }

    /**
     * 列数据信息单元格样式
     */
    public HSSFCellStyle getStyle(HSSFWorkbook workbook) {
	// 设置字体
	HSSFFont font = workbook.createFont();
	// 设置字体大小
	// font.setFontHeightInPoints((short)10);
	// 字体加粗
	// font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
	// 设置字体名字
	font.setFontName("Courier New");
	// 设置样式;
	HSSFCellStyle style = workbook.createCellStyle();
	// 设置底边框;
	style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
	// 设置底边框颜色;
	style.setBottomBorderColor(HSSFColor.BLACK.index);
	// 设置左边框;
	style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
	// 设置左边框颜色;
	style.setLeftBorderColor(HSSFColor.BLACK.index);
	// 设置右边框;
	style.setBorderRight(HSSFCellStyle.BORDER_THIN);
	// 设置右边框颜色;
	style.setRightBorderColor(HSSFColor.BLACK.index);
	// 设置顶边框;
	style.setBorderTop(HSSFCellStyle.BORDER_THIN);
	// 设置顶边框颜色;
	style.setTopBorderColor(HSSFColor.BLACK.index);
	// 在样式用应用设置的字体;
	style.setFont(font);
	// 设置自动换行;
	style.setWrapText(false);
	// 设置水平对齐的样式为居中对齐;
	style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
	// 设置垂直对齐的样式为居中对齐;
	style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);

	return style;

    }
}