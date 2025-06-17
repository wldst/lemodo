package com.wldst.ruder.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class ReadXml {

    public static void main(String[] args) throws DocumentException {
	// TODO Auto-generated method stub
	String svgFileName = "/staticRes/desktop/style/fonts/iconfont2.svg";
	List<Map<String, Object>> readSvgIconFont = readSvgIconFont(svgFileName);
	for (Map<String, Object> icons : readSvgIconFont) {
	    System.out.println(icons);
	}
    }

    public static List<Map<String, Object>> readSvgIconFont(String svgFileName) throws DocumentException {
	SAXReader reader = new SAXReader();
	InputStream resourceAsStream = ReadXml.class.getResourceAsStream(svgFileName);
	Document document = reader.read(resourceAsStream);
	Element root = document.getRootElement();
	List<Element> childElements = root.elements();
	List<Map<String, Object>> glyMaps = new ArrayList<Map<String, Object>>();
	for (Element child : childElements) {
	    if ("defs".equals(child.getQName().getName())) {
		List<Element> gElements = child.elements();
		for (Element gEle : gElements) {
		    String name = gEle.getQName().getName();
		    if ("font".equals(name)) {
			List<Element> elements = gEle.elements();
			for (Element e : elements) {
			    String name2 = e.getQName().getName();
			    String attributeValue = e.attributeValue("unicode");

			    String glyphName = e.attributeValue("glyph-name");

			    if ("glyph".equals(name2) && attributeValue != null) {
				Map<String, Object> glyMap = new HashMap<>();
				glyMap.put("unicode", attributeValue);
				glyMap.put("glyphName", glyphName);
				glyMap.put("name", glyphName);
				glyMaps.add(glyMap);
			    }
			}

		    }
		}
	    }
	}
	return glyMaps;
    }

}
