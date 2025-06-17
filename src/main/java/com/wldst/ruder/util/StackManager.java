package com.wldst.ruder.util;

import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 栈管理器
 * @author wldst
 *
 */
public class StackManager {
    final static Logger logger = LoggerFactory.getLogger(StackManager.class);
    private Stack<String> lefts;
    private Stack<String> rights;
    private String current;
    private String homePage;

    public StackManager() {
	lefts = new Stack<>();
	rights = new Stack<>();
	current = getHomePage();
	lefts.push(current);
    }

    public void goingTo(String step) {
	current = step;
	if("top".equals(step)) {
	    goToStart();
	    return;
	}
	if (lefts.isEmpty()) {
	    lefts.push(current);
	} else {
	    String peek = lefts.peek();
	    if (peek == null || peek != null && !step.equals(peek)) {
		lefts.push(current);
	    }
	}
	LoggerTool.info(logger,"goingTo =="+step+" lefts="+String.valueOf(lefts)+"rights="+String.valueOf(rights));

    }

    public void rightReceived(String step) {
	if (rights.isEmpty()&&!step.equals(getHomePage())) {
	    rights.push(step);
	} else {
	    String peek = rights.peek();
	    if (peek == null || peek != null && !step.equals(peek)) {
		rights.push(step);
	    }
	}
	LoggerTool.info(logger,"backTo "+step+" lefts="+String.valueOf(lefts)+"rights="+String.valueOf(rights));

    }

    public String toRight() {
	if (lefts.size() <= 1) {
	    return lefts.peek(); // 已经在主页，无法再返回上一步
	}	
	rightReceived(lefts.pop());
	String current = lefts.peek();
	LoggerTool.info(logger," after toRight ,current= "+current+" lefts="+String.valueOf(lefts)+"rights="+String.valueOf(rights));
	return current;
    }

    public String toLeft() {
	if (rights.size() < 1) {
	    return null; // 已经在主页，无法再返回上一步
	}
	goingTo(rights.pop());
	LoggerTool.info(logger," after toLeft ,current="+current+" lefts="+String.valueOf(lefts)+"rights="+String.valueOf(rights));

	return current;
    }

    public String next() {
	if (rights.isEmpty()) {
	    return null; // 栈为空，没有下一个场景
	}
	String current=rights.peek();
	LoggerTool.info(logger,"next "+current+" lefts="+String.valueOf(lefts)+"rights="+String.valueOf(rights));

	return current;
    }

    public String pre() {
	toRight();
	if (lefts.isEmpty()) {
	    return null; // 栈为空，没有下一个场景
	}
	String pre = lefts.peek();
	toLeft();
	LoggerTool.info(logger,"pre "+pre+" lefts="+String.valueOf(lefts)+"rights="+String.valueOf(rights));
	return pre;
    }

    public String goToStart() {
	lefts.clear();
	rights.clear();
	current = "homePage";
	lefts.push(current);
	return current;
    }

    public static void main(String[] args) {
	StackManager manager = new StackManager();
	manager.goingTo("scene1");
	manager.goingTo("scene2");
	manager.goingTo("scene3");

	System.out.println("currentScene:" + manager.current); // 输出：场景3
	System.out.println("preScene:" + manager.toRight()); // 输出：场景2
	System.out.println("nextScene:" + manager.next()); // 输出：场景3
	System.out.println("return HomePage"); // 输出：无输出
	manager.goToStart();
	System.out.println("currentScene:" + manager.current); // 输出：主页
    }

    public String getHomePage() {
	return homePage;
    }

    public void setHomePage(String homePage) {
	this.homePage = homePage;
    }
}