package com.wldst.ruder.domain;

import com.wldst.ruder.util.MapTool;
/**
 * Layui界面模板参数
 * @author wldst
 *
 */
public class LayUIDomain extends FileDomain{
    public static final String  COLUMN_TYPE="colTypes";
    public static final String  COLUMN_SIZE="colSize";
    public static final String  COLUMN_NULL_ABLE="nullAble";
    
    public static final String  JS_CODE="jsCode";
    public static final String  HTML="Html";
    public static final String  ICON="icon";
    
    public static final String  is_search_input= "isSearchInput";
    public static final String  show_type= "showType";
    public static final String  is_po= "isPo";
    public static final String  module_dropdown= "dropdown";
    //取值字段
    public static final String  value_field= "valueField";
    public static final String  unicode="unicode";
    
    //Layui界面模板参数
    public static final String  BTN_CREATE= "createBtn";
    public static final String  BTN_TABLE_CREATE= "addBtn";
    public static final String  BTN_REMOVE="removeBtn";
    public static final String  BTN_REMOVE_REL="removeRelBtn";
    public static final String  OPT_TOOL_BAR="toolbarOpt";
    public static final String  TOOL_FUN="toolFun";
    public static final String  ACTIVE_LOGIC="activLogic";
    
    public static final String  LAYUI_TABLE_TOOL_BTN= "layTableToolOpt";
    
    public static final String layuiConfig = """ 
    		layui.config({ 
        		dir: '/static/layui/',
        		version: false ,debug: false,
        		base: '/static/layui/lay/modules/'
    		})
    		""";
    public void searchKeyWord(StringBuilder searchValueJs) {
	searchKeyWord(searchValueJs,KEY_WORD);
    }
    public void searchKeyWord(StringBuilder searchValueJs, String code) {
   	searchValueJs.append("var " + code + "=$('#" + code + "').val();\n");
   	searchValueJs.append(" searchForm['" + code + "']=" + code + ";\n");
       }
    
    /**
     * 定义和读取数据
     * 
     * @param searchValueJs
     * @param code
     */
    public void searchInput(StringBuilder searchValueJs, String code) {
	searchValueJs.append("var " + code + "=$('#" + code + "Reload').val();\n");
	searchValueJs.append(" searchForm['" + code + "']=" + code + ";\n");
    }
}
