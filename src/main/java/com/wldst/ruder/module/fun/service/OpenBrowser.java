package com.wldst.ruder.module.fun.service;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class OpenBrowser {
    public static void main(String[] args) {
        String url = "https://www.example.com"; // 替换为你想要打开的网站URL
        
//        openBroswer(url);
        openWPSFile("E:\\360MoveData\\Users\\wldst\\Desktop\\nginx.conf");
//        D:\liuqiang\work\pmis\桌面
    }

    public static void openBroswer(String url) {
	if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("当前系统不支持桌面操作");
        }
    }
    
    public static void openWPSFile(String filePath) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                File file = new File(filePath);
                desktop.open(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("当前系统不支持桌面操作");
        }
    }
}

