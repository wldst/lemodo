package com.wldst.ruder.module.workflow.template.beans;

import java.util.HashMap;
import java.util.Map;

import com.wldst.ruder.util.MapTool;

/**
 * 模板任务位置及大小信息
 * 
 * @author wldst
 */
public class Tshape {
    // x坐标值
     public  static String  nodeX="nodeX";

    // y坐标值
     public  static String  nodeY="nodeY";

    // 任务节点宽度
     public static String  nodeW="nodeW";

    // 任务节点高度
     public static String  nodeH="nodeH";

    /**
     * 构造函数
     * 
     * @param x x坐标
     * @param y y坐标
     * @param w 宽度
     * @param h 高度
     */
    public static Map<String, Object> data(int x, int y, int w, int h) {
	Map<String, Object>  shap = new HashMap<>();
	shap.put(nodeX,x);
	shap.put(nodeY,y);
	shap.put(nodeW,w);
	shap.put(nodeH,h);
	return shap;
    }

    public static int getNodeX(Map<String, Object> data) {
	return MapTool.integer(data,nodeX);
    }

    public static void setNodeX(int d, Map<String, Object> data) {
	data.put(nodeX,d);
    }

    public static int getNodeY(Map<String, Object> data) {
	return MapTool.integer(data,nodeY);
    }

    public static void setNodeY(int d, Map<String, Object> data) {
	data.put(nodeY,d);
    }

    public static int getNodeW(Map<String, Object> data) {
	return MapTool.integer(data,nodeW);
    }

    public static void setNodeW(int d, Map<String, Object> data) {
	data.put(nodeW, d);
    }

    public static int getNodeH(Map<String, Object> data) {
	return MapTool.integer(data,nodeH);
    }

    public static void setNodeH(int d, Map<String, Object> data) {
	data.put(nodeH ,d);
    }
    
    

}
