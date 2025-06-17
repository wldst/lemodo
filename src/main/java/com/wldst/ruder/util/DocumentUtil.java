package com.wldst.ruder.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 */
public class DocumentUtil {
	private static String xmlStr = "";
	private static List<Map<String, Object>> nodeMapList = new ArrayList<Map<String, Object>>();

	/**
	 * 将指定的document解析成xml字符串
	 * 
	 * @param doc
	 * @return
	 */
	public static String getXmlStrByDocument(Document doc) {
		xmlStr = "";
		// 根节点名称
		String rootName = doc.getDocumentElement().getTagName();
		// 递归解析Element
		Element element = doc.getDocumentElement();
		return getElementStr(element);
	}

	/**
	 * 将指定的节点解析成xml字符串
	 * 
	 * @param element
	 * @return
	 */
	public static String getElementStr(Element element) {
		String TagName = element.getTagName();
		boolean flag = true;

		xmlStr = xmlStr + "<" + TagName;
		NamedNodeMap attris = element.getAttributes();
		for (int i = 0; i < attris.getLength(); i++) {
			Attr attr = (Attr) attris.item(i);
			xmlStr = xmlStr + " " + attr.getName() + "=\"" + attr.getValue() + "\"";
		}
		xmlStr = xmlStr + ">";

		NodeList nodeList = element.getChildNodes();
		Node childNode;
		for (int temp = 0; temp < nodeList.getLength(); temp++) {
			childNode = nodeList.item(temp);
			// 判断是否属于节点
			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
				// 判断是否还有子节点
				getElementStr((Element) childNode);
				if (childNode.getNodeType() != Node.COMMENT_NODE) {
					xmlStr = xmlStr + childNode.getTextContent();
				}
			}
		}
		xmlStr = xmlStr + "</" + element.getTagName() + ">";
		return xmlStr;
	}

	/**
	 * 解析节点
	 * 
	 * @param element
	 * @param graphId 所属图的id
	 */
	public static void parseElement(Element element, String graphId) {
		NodeList nodeList = element.getChildNodes();
		Node childNode;
		for (int temp = 0; temp < nodeList.getLength(); temp++) {

			childNode = nodeList.item(temp);
			String id = getUUID32();
			String nodeId = getNodeAttrValue(childNode, "id");
			if (!"0".equals(nodeId) && !"1".equals(nodeId) && "mxCell".equals(childNode.getNodeName())) {
				System.out.println(childNode.getNodeName());
				System.out.println("graphid:" + graphId);
				System.out.println("nodeId:" + getNodeAttrValue(childNode, "id"));
				System.out.println("parent:" + getNodeAttrValue(childNode, "parent"));
				System.out.println("value:" + getNodeAttrValue(childNode, "value"));
				System.out.println("source:" + getNodeAttrValue(childNode, "source"));
				System.out.println("target:" + getNodeAttrValue(childNode, "target"));
				System.out.println("vertex:" + getNodeAttrValue(childNode, "vertex"));
				System.out.println("edge:" + getNodeAttrValue(childNode, "edge"));
				parseElement2((Element) childNode, nodeId, graphId);
				System.out.println("****end*****");
				Map<String, Object> node = new HashMap<String, Object>();
				node.put("id", id);
				node.put("nodeId", nodeId);
				node.put("graphId", graphId);
				node.put("parent", getNodeAttrValue(childNode, "parent"));
				node.put("nodeValue", getNodeAttrValue(childNode, "value"));
				node.put("source", getNodeAttrValue(childNode, "source"));
				node.put("target", getNodeAttrValue(childNode, "target"));
				node.put("edge", getNodeAttrValue(childNode, "edge"));
				node.put("vertex", getNodeAttrValue(childNode, "vertex"));
				node.put("style", getNodeAttrValue(childNode, "style"));
				node.put("ass", getNodeAttrValue(childNode, "as"));
				node.put("nodeName", childNode.getNodeName());
				nodeMapList.add(node);
			}

			// 判断是否属于节点
			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
				// 判断是否还有子节点
				parseElement((Element) childNode, graphId);
			}
		}
	}

	/**
	 * 解析mxGeometry节点
	 * 
	 * @param element
	 * @param parentId
	 * @param graphId
	 */
	private static void parseElement2(Element element, String parentId, String graphId) {
		NodeList nodeList = element.getChildNodes();
		Node childNode;

		for (int temp = 0; temp < nodeList.getLength(); temp++) {
			childNode = nodeList.item(temp);
			String nodeName = childNode.getNodeName();
			if ("mxGeometry".equals(nodeName)) {

				String nodeId = getNodeAttrValue(childNode, "id");
				String id = getUUID32();

				System.out.println("--name:" + nodeName);
				System.out.println("--height:" + getNodeAttrValue(childNode, "height"));
				System.out.println("--width:" + getNodeAttrValue(childNode, "height"));
				System.out.println("--x:" + getNodeAttrValue(childNode, "x"));
				System.out.println("--y:" + getNodeAttrValue(childNode, "y"));
				System.out.println("--as:" + getNodeAttrValue(childNode, "as"));
				System.out.println("--relative:" + getNodeAttrValue(childNode, "relative"));

				Map<String, Object> node = new HashMap<String, Object>();
				node.put("id", id);
				node.put("nodeId", nodeId);
				node.put("parent", parentId);
				node.put("nodeName", childNode.getNodeName());
				node.put("height", getNodeAttrValue(childNode, "height"));
				node.put("width", getNodeAttrValue(childNode, "width"));
				node.put("x", getNodeAttrValue(childNode, "x"));
				node.put("y", getNodeAttrValue(childNode, "y"));
				node.put("ass", getNodeAttrValue(childNode, "as"));
				node.put("relative", getNodeAttrValue(childNode, "relative"));
				node.put("graphId", graphId);
				node.put("style", getNodeAttrValue(childNode, "style"));
				// node.put("value", getNodeAttrValue(childNode, "value"));
				// node.put("source", getNodeAttrValue(childNode, "source"));
				// node.put("target", getNodeAttrValue(childNode, "target"));
				// node.put("edge", getNodeAttrValue(childNode, "edge"));
				// node.put("vertex", getNodeAttrValue(childNode, "vertex"));
				nodeMapList.add(node);

				// 判断是否属于节点
				if (childNode.getNodeType() == Node.ELEMENT_NODE) {
					// 判断是否还有子节点
					parseElement((Element) childNode, "");
				}
			}
		}

	}

	/**
	 * 获取指定节点的指定属性的值
	 * 
	 * @param node
	 * @param attrName
	 * @return
	 */
	public static String getNodeAttrValue(Node node, String attrName) {
		NamedNodeMap attr = node.getAttributes();
		if (attr != null) {
			Node attrNode = attr.getNamedItem(attrName);
			if (attrNode != null) {
				return attrNode.getNodeValue();
			}
		}
		return "";

	}

	/**
	 * 获取指定的document对象中要保存的节点对象
	 * 
	 * @param doc
	 * @return
	 */
	public static List<Map<String, Object>> parseDocument(Document doc) {
		String id = "6ed10c4036f245b8bf78e1141d85e23b";// doc.getDocumentElement().getAttribute("id");
		if ("".equals(id)) {
			id = getUUID32();
		}
		// 递归解析Element
		Element element = doc.getDocumentElement();
		nodeMapList.clear();
		parseElement(element, id);
		return nodeMapList;
	}

	/**
	 * 根据图的id获取图的xml字符串
	 * 
	 * @param graphId
	 * @return
	 */
	public static String getXmlByGraphId(String graphId) {
		xmlStr = "";
		// 根节点名称
		/*
		 * String rootName = doc.getDocumentElement().getTagName(); // 递归解析Element
		 * Element element = doc.getDocumentElement();
		 */
		return getElementStr(null);

	}

	/**
	 * 生成32位主键
	 * 
	 * @return
	 */
	public static String getUUID32() {
		return UUID.randomUUID().toString().replace("-", "").toLowerCase();
	}

	public static Document createDocument() {
		// 初始化xml解析工厂
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		// 创建DocumentBuilder
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		// 创建Document
		Document doc = builder.newDocument();

		// standalone用来表示该文件是否呼叫其它外部的文件。若值是 ”yes” 表示没有呼叫外部文件
		doc.setXmlStandalone(true);

		// 创建一个根节点
		// 说明:
		// doc.createElement("元素名")、element.setAttribute("属性名","属性值")、element.setTextContent("标签间内容")
		Element diagram = doc.createElement("diagram");
		diagram.setAttribute("id", "");
		diagram.setAttribute("tcn", "");

		// 创建根节点第一个子节点
		Element mxGraphModel = doc.createElement("mxGraphModel");
		diagram.appendChild(mxGraphModel);

		Element root = doc.createElement("root");
		mxGraphModel.appendChild(root);

		Element mxCell1 = doc.createElement("mxCell");
		mxCell1.setAttribute("id", "0");
		root.appendChild(mxCell1);

		Element mxCell2 = doc.createElement("mxCell");
		mxCell2.setAttribute("id", "1");
		mxCell2.setAttribute("parent", "0");
		root.appendChild(mxCell2);

		// 根据图的id获取图中节点

		/*
		 * Element mxCell3 = doc.createElement("mxCell"); mxCell3.setAttribute("id",
		 * "2"); mxCell3.setAttribute("parent", "1"); mxCell3.setAttribute("vertex",
		 * "1"); mxCell3.setAttribute("value", "songyan"); root.appendChild(mxCell3);
		 * 
		 * Element mxGeometry = doc.createElement("mxGeometry");
		 * mxGeometry.setAttribute("x", "20"); mxGeometry.setAttribute("y", "20");
		 * mxGeometry.setAttribute("width", "80"); mxGeometry.setAttribute("height",
		 * "30"); mxGeometry.setAttribute("as", "geometry");
		 * mxCell3.appendChild(mxGeometry);
		 */

		// 添加根节点
		doc.appendChild(diagram);

		return doc;
	}

	/**
	 * 根据图的id获取document对象
	 * 
	 * @param graphId 图的id
	 * @return
	 */
	public static Document getDocumentByGraphId(String graphId) {
		// 初始化xml解析工厂
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		// 创建DocumentBuilder
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		// 创建Document
		Document doc = builder.newDocument();

		// standalone用来表示该文件是否呼叫其它外部的文件。若值是 ”yes” 表示没有呼叫外部文件
		doc.setXmlStandalone(true);

		// 创建一个根节点
		// 说明:
		// doc.createElement("元素名")、element.setAttribute("属性名","属性值")、element.setTextContent("标签间内容")
		Element diagram = doc.createElement("diagram");
		diagram.setAttribute("id", "");
		diagram.setAttribute("tcn", "");

		// 创建根节点第一个子节点
		Element mxGraphModel = doc.createElement("mxGraphModel");
		diagram.appendChild(mxGraphModel);

		Element root = doc.createElement("root");
		mxGraphModel.appendChild(root);

		Element mxCell1 = doc.createElement("mxCell");
		mxCell1.setAttribute("id", "0");
		root.appendChild(mxCell1);

		Element mxCell2 = doc.createElement("mxCell");
		mxCell2.setAttribute("id", "1");
		mxCell2.setAttribute("parent", "0");
		root.appendChild(mxCell2);

		// 根据图的id获取图中节点
		// List<Map<String,Object>> transList = trans

		// 添加根节点
		doc.appendChild(diagram);

		return doc;
	}

	public static void main(String[] args) {
		Document document = createDocument();
		System.out.println(getXmlStrByDocument(document));
	}

	public static Document getDocument(List<Map<String, Object>> newNodeList, String graphId) {
		// 初始化xml解析工厂
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		// 创建DocumentBuilder
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	if (builder == null) {
	    return null;
	}
		// 创建Document
		Document doc = builder.newDocument();

		// standalone用来表示该文件是否呼叫其它外部的文件。若值是 ”yes” 表示没有呼叫外部文件
		doc.setXmlStandalone(true);

		// 创建一个根节点
		// 说明:
		// doc.createElement("元素名")、element.setAttribute("属性名","属性值")、element.setTextContent("标签间内容")
		Element diagram = doc.createElement("diagram");
		diagram.setAttribute("id", graphId);
		diagram.setAttribute("tcn", "");

		// 创建根节点第一个子节点
		Element mxGraphModel = doc.createElement("mxGraphModel");
		diagram.appendChild(mxGraphModel);

		Element root = doc.createElement("root");
		mxGraphModel.appendChild(root);

		Element mxCell1 = doc.createElement("mxCell");
		mxCell1.setAttribute("id", "0");
		root.appendChild(mxCell1);

		Element mxCell2 = doc.createElement("mxCell");
		mxCell2.setAttribute("id", "1");
		mxCell2.setAttribute("parent", "0");
		root.appendChild(mxCell2);

		for (Map<String, Object> node : newNodeList) {
			handleNode(root, doc, node);
		}

		// 添加根节点
		doc.appendChild(diagram);

		return doc;
	}

	private static void handleNode(Element root, Document doc, Map<String, Object> node) {
		Element mxCell = doc.createElement((String) node.get("nodeName"));
		Object as = node.get("as");
		Object width = node.get("width");
		Object x = node.get("x");
		Object y = node.get("y");
		Object style = node.get("style");
		Object nodeId = node.get("nodeId");
		Object height = node.get("height");
		Object parent = node.get("parent");
		Object relative = node.get("relative");
		Object vertex = node.get("vertex");

		Object value = node.get("value");
		Object edge = node.get("edge");
		Object source = node.get("source");
		Object target = node.get("target");

		if (value != null && !"".equals(value)) {
			mxCell.setAttribute("value", (String) value);
		}
		if (edge != null && !"".equals(edge)) {
			mxCell.setAttribute("edge", (String) edge);
		}
		if (source != null && !"".equals(source)) {
			mxCell.setAttribute("source", (String) source);
		}
		if (target != null && !"".equals(target)) {
			mxCell.setAttribute("target", (String) target);
		}
		if (as != null && !"".equals(as)) {
			mxCell.setAttribute("as", (String) as);
		}
		if (width != null && !"".equals(width)) {
			mxCell.setAttribute("width", (String) width);
		}
		if (x != null && !"".equals(x)) {
			mxCell.setAttribute("x", (String) x);
		}

		if (y != null && !"".equals(y)) {
			mxCell.setAttribute("y", (String) y);
		}
		if (style != null && !"".equals(style)) {
			mxCell.setAttribute("style", (String) style);
		}
		if (nodeId != null && !"".equals(nodeId)) {
			mxCell.setAttribute("id", (String) nodeId);
		}
		if (parent != null && !"".equals(parent)) {
			mxCell.setAttribute("parent", (String) parent);
		}
		if (height != null && !"".equals(height)) {
			mxCell.setAttribute("height", (String) height);
		}
		if (relative != null && !"".equals(relative)) {
			mxCell.setAttribute("relative", (String) relative);
		}
		if (vertex != null && !"".equals(vertex)) {
			mxCell.setAttribute("vertex", (String) vertex);
		}
		root.appendChild(mxCell);

		Object child = node.get("child");
		if (child != null) {
			List<Map<String, Object>> childNodeList = (List<Map<String, Object>>) child;
			for (Map<String, Object> map : childNodeList) {
				handleNode(mxCell, doc, map);
			}
		}

	}

}
