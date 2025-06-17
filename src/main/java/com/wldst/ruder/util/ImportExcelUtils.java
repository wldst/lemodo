package com.wldst.ruder.util;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wldst.ruder.constant.Msg;
import com.wldst.ruder.domain.FileDomain;
/**
 * excel导入工具
 * @author liuqiang
 *
 */
public class ImportExcelUtils {
	
	public static final String OFFICE_EXCEL_2003_POSTFIX = "xls";
	public static final String OFFICE_EXCEL_2010_POSTFIX = "xlsx";
	public static final String EMPTY = "";
	public static final String POINT = ".";
	private static final Logger logger = LoggerFactory.getLogger(ImportExcelUtils.class);
	/**
	 * 导入文件(read the Excel .xlsx,.xls)
	 * @param file 
	 * @return List<Map<String,String>>
	 * @throws IOException
	 */
	public static List<Map<String, String>> readExcel(File file, int startRowNumber,int sheetNum, Map<Integer, String> fieldMap) throws IOException{
	    String originalFileName = file.getName();
        if(!originalFileName.endsWith(FileDomain.FILE_TYPE_XLS) && (!originalFileName.endsWith(FileDomain.FILE_TYPE_XLSX))){
            throw new IllegalArgumentException(Msg.IMPORT_ERRFORMAT_FILE);
        }
       FileInputStream is = new FileInputStream(file);
	    List<Map<String, String>> dataList = new ArrayList<Map<String,String>>();
	        try {
	            Workbook wb = create(is);
	            if(wb != null){
	            	Sheet sheet = wb.getSheetAt(sheetNum);
	 	            int count_row = sheet.getLastRowNum();
	 	            if (count_row >= startRowNumber-1) {
	 	                for (int i=startRowNumber-1; i<=count_row; i++) {
	 	                    Row row = sheet.getRow(i);
	 	                    if (!isRowEmpty(row)) {    
	 	                        boolean flag = false;
	 	                        Map<String, String> map = new HashMap<String, String>();
	 	                        for(Integer j: fieldMap.keySet()){
	 	                            Cell cell = row.getCell(j-1);
	 	                            String value = getCellValue(cell, null);
	 	                            map.put(fieldMap.get(j), value);
	 	                            if(value!=null){
	 	                                flag = true;
	 	                            }
	 	                        }
	 	                        if(!flag){
	 	                            break;
	 	                        }
	 	                        dataList.add(map);
	 	                    }
	 	                }
	 	            }
	            }           
	        } catch (Exception e) {
	        	LoggerTool.error(logger,e.getMessage(),e);
	        } finally{
	        	if(is !=null){
	        		is.close();
	        	}
	        }
	        return dataList;
	    }
	
	/**
	 * 导入文件(read the Excel .xlsx,.xls)
	 * @param file 
	 * @return List<List<String>>
	 * @throws IOException
	 */
	public static List<List<String>> readExcel(File file) throws IOException {
		if (file == null || EMPTY.equals(file.getName().trim())) {
			return null;
		} else {
			String postfix = getPostfix(file.getName());
			if (!EMPTY.equals(postfix)) {
				if (OFFICE_EXCEL_2003_POSTFIX.equals(postfix)) {
					return readXls(file);
				} else if (OFFICE_EXCEL_2010_POSTFIX.equals(postfix)) {
					return readXlsx(file);
				} else {
					return null;
				}
			}
		}
		return null;
	}

	
	@SuppressWarnings("resource")
	private static List<List<String>> readXlsx(File file) {
		int totalRows;  // sheet中总行数
		int totalCells; // 每一行总单元格数
		List<List<String>> list = new ArrayList<>();
		// IO流读取文件
		InputStream input = null;
		XSSFWorkbook wb = null;
		List<String> rowList = null;
		try {
			input = new FileInputStream(file);
			// 创建文档
			wb = new XSSFWorkbook(input);
			// 读取sheet(页)
			for (int numSheet = 0; numSheet < wb.getNumberOfSheets(); numSheet++) {
				XSSFSheet xssfSheet = wb.getSheetAt(numSheet);
				if (xssfSheet == null) {
					continue;
				}
				totalRows = xssfSheet.getLastRowNum();
				// 读取Row,从第二行开始
				for (int rowNum = 1; rowNum <= totalRows; rowNum++) {
					XSSFRow xssfRow = xssfSheet.getRow(rowNum);
					  if (xssfRow != null&&!isRowEmpty(xssfRow)) { 
						rowList = new ArrayList<>();
						totalCells = xssfRow.getLastCellNum();
						// 读取列，从第一列开始
						for (int c = 0; c <= totalCells + 1; c++) {
							XSSFCell cell = xssfRow.getCell(c);
							if (cell == null) {
								rowList.add(EMPTY);
								continue;
							}
							rowList.add(getXValue(cell).trim());
						}
						list.add(rowList);
					}
				}
			}
			return list;
		} catch (IOException e) {
			LoggerTool.error(logger,e.getMessage(),e);
		} finally {
			if(input!=null){
				try {
					input.close();
					input=null;
				} catch (IOException e) {
					LoggerTool.error(logger,e.getMessage(),e);
				}
			}
		}
		return null;

	}

	/**
	 * 读取Excel 2003-2007 .xls
	 * @param beanclazz
	 * @param titleExist
	 * @return List<List<String>>
	 * @throws IOException
	 */
	@SuppressWarnings({ "resource", "deprecation" })
	private static List<List<String>> readXls(File file) {
		int totalRows;  // sheet中总行数
		int totalCells; // 每一行总单元格数
		List<List<String>> list = new ArrayList<>();
		// IO流读取文件
		FileInputStream input = null;
		
		HSSFWorkbook wb = null;
		List<String> rowList = null;
		try {
			input = new FileInputStream(file);
			// 创建文档
			wb = new HSSFWorkbook(input);
			// 读取sheet(页)
			for (int numSheet = 0; numSheet < wb.getNumberOfSheets(); numSheet++) {
				HSSFSheet hssfSheet = wb.getSheetAt(numSheet);
				if (hssfSheet == null) {
					continue;
				}
				totalRows = hssfSheet.getLastRowNum();
				// 读取Row,从第二行开始
				for (int rowNum = 1; rowNum <= totalRows; rowNum++) {
					HSSFRow hssfRow = hssfSheet.getRow(rowNum);
					if (hssfRow != null&&!isRowEmpty(hssfRow)) {
						rowList = new ArrayList<>();
						totalCells = hssfRow.getLastCellNum();
						// 读取列，从第一列开始
						for (short c = 0; c <= totalCells + 1; c++) {
							HSSFCell cell = hssfRow.getCell(c);
							if (cell == null) {
								rowList.add(EMPTY);
								continue;
							}
							rowList.add(getHValue(cell).trim());
						}
						list.add(rowList);
					}
				}
			}
			return list;
		} catch (IOException e) {
			LoggerTool.error(logger,e.getMessage(),e);
		} finally {
			if(input!=null){
				try {
					input.close();
					input=null;
				} catch (IOException e) {
					LoggerTool.error(logger,e.getMessage(),e);
				}
			}
			
		}
		return null;
	}
	
	
	/**
	 * 获得path的后缀名
	 * @param path
	 * @return String
	 */
	private static String getPostfix(String path) {
		if (path == null || EMPTY.equals(path.trim())) {
			return EMPTY;
		}
		if (path.contains(POINT)) {
			return path.substring(path.lastIndexOf(POINT) + 1, path.length());
		}
		return EMPTY;
	}
	
	

	/**
	 * 单元格格式
	 * @param hssfCell
	 * @return String
	 */
	@SuppressWarnings("static-access")
	private static String getHValue(HSSFCell hssfCell) {
		if (hssfCell.getCellType() == hssfCell.CELL_TYPE_BOOLEAN) {
			return String.valueOf(hssfCell.getBooleanCellValue());
		} else if (hssfCell.getCellType() == hssfCell.CELL_TYPE_NUMERIC) {
			String cellValue = "";
			if (HSSFDateUtil.isCellDateFormatted(hssfCell)) {
				Date date = HSSFDateUtil.getJavaDate(hssfCell.getNumericCellValue());
				 SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
				cellValue = sdf.format(date);
			} else {
				DecimalFormat df = new DecimalFormat("#.##");
				cellValue = df.format(hssfCell.getNumericCellValue());
				String strArr = cellValue.substring(cellValue.lastIndexOf(POINT) + 1, cellValue.length());
				if (strArr.equals("00")) {
					cellValue = cellValue.substring(0, cellValue.lastIndexOf(POINT));
				}
			}
			return cellValue;
		} else {
			return String.valueOf(hssfCell.getStringCellValue());
		}
	}

	/**
	 * 单元格格式
	 * @param xssfCell
	 * @return String
	 */
	private static String getXValue(XSSFCell xssfCell) {
		if (xssfCell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
			return String.valueOf(xssfCell.getBooleanCellValue());
		} else if (xssfCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			String cellValue = "";
			if (XSSFDateUtil.isCellDateFormatted(xssfCell)) {
				Date date = XSSFDateUtil.getJavaDate(xssfCell.getNumericCellValue());
				 SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
				cellValue = sdf.format(date);
			} else {
				DecimalFormat df = new DecimalFormat("#.##");
				cellValue = df.format(xssfCell.getNumericCellValue());
				String strArr = cellValue.substring(cellValue.lastIndexOf(POINT) + 1, cellValue.length());
				if (strArr.equals("00")) {
					cellValue = cellValue.substring(0, cellValue.lastIndexOf(POINT));
				}
			}
			return cellValue;
		} else {
			return String.valueOf(xssfCell.getStringCellValue());
		}
	}
	
	
    private static Workbook create(InputStream inp) throws IOException,InvalidFormatException {
	        if (!inp.markSupported()) {
	            inp = new PushbackInputStream(inp, 8);
	        }
	        if (POIFSFileSystem.hasPOIFSHeader(inp)) {
	            return new HSSFWorkbook(inp);
	        }
	        if (POIXMLDocument.hasOOXMLHeader(inp)) {
	            return new XSSFWorkbook(OPCPackage.open(inp));
	        }
	        throw new IllegalArgumentException("你的excel版本目前poi解析不了");
	}
	    
	private static String getCellValue(Cell cell, FormulaEvaluator evaluator) {
        String value = "";
        if (cell != null) {
            int type = cell.getCellType(); // 得到单元格数据类型
            switch (type) { // 判断数据类型
                case Cell.CELL_TYPE_BLANK:
                    value = "";
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                    value = cell.getBooleanCellValue() + "";
                    break;
                case Cell.CELL_TYPE_ERROR:
                    value = cell.getErrorCellValue() + "";
                    break;
                case Cell.CELL_TYPE_FORMULA:
                    if(evaluator == null){
                        value = cell.getCellFormula();
                    }else{
                        CellValue cellValue = evaluator.evaluate(cell); 
                        value = cellValue.getNumberValue() + "";
                    }
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        value = new DataFormatter().formatRawCellContents(cell.getNumericCellValue(), 0, "yyyy-MM-dd");// 格式化日期
                    } else {
                        value = cell.getNumericCellValue() + "";
                    }
                    break;
                case Cell.CELL_TYPE_STRING:
                    value = cell.getStringCellValue();
                    break;
                default:
                    break;
            }
        }
        return value;
	 }
	    
	/**
	 * 自定义xssf日期工具类
	 */
	private static class XSSFDateUtil extends DateUtil {
		protected static int absoluteDay(Calendar cal, boolean use1904windowing) {
			return DateUtil.absoluteDay(cal, use1904windowing);
		}
	}
	
	public static boolean isRowEmpty(Row row){
		for(int c=row.getFirstCellNum();c<row.getLastCellNum();c++){
			Cell cell = row.getCell(c);
			if(cell!=null&& cell.getCellType()!=cell.CELL_TYPE_BLANK){
				return false;
			}
		}
		return true;
	}
}
