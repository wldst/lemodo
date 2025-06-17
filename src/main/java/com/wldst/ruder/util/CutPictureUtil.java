package com.wldst.ruder.util;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.imageio.ImageIO;
/**
 * @author pibigstar
 * @desc 网页截图工具类
 **/
public class CutPictureUtil {
    public static void main(String[] args) throws IOException, URISyntaxException, AWTException {
        // 控制浏览器打开网页，仅适用于JdK1.6及以上版本
        Desktop.getDesktop().browse(new URL("https://www.toutiao.com/").toURI());
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        
	    GraphicsDevice[] gs = ge.getScreenDevices();
	    for (int j = 0; j < gs.length; j++) {
	       GraphicsDevice gd = gs[j];
	       GraphicsConfiguration[] gc = gd.getConfigurations();
	       for (int i=0; i < gc.length; i++) {
//	          JFrame f = new JFrame(gs[j].getDefaultConfiguration());
//	          Canvas c = new Canvas(gc[i]);
//	          Rectangle gcBounds = gc[i].getBounds();
//	          int xoffs = gcBounds.x;
//	          int yoffs = gcBounds.y;
//	          f.getContentPane().add(c);
//	          f.setLocation((i*50)+xoffs, (i*60)+yoffs);
//	          f.show();
	          sreenPrint(new Robot(gd));
	       }
	    }
        
        
    }

	private static void sreenPrint(Robot robot) throws IOException {
		// 延迟一秒
        robot.delay(1000);
        // 获取屏幕宽度和高度
        Dimension d = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
        int width = (int) d.getWidth();
        int height = (int) d.getHeight();
        // 最大化浏览器
        robot.keyRelease(KeyEvent.VK_F11);
        robot.delay(1000);
        // 对屏幕进行截图
        Image image = robot.createScreenCapture(new Rectangle(0, 0, width, height));
        // 通过图形绘制工具类将截图保存
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = img.createGraphics();
        g.drawImage(image, 0, 0, width, height, null);
        // 保存图片
        ImageIO.write(img, "jpg", new File("D:\\media\\"+System.currentTimeMillis()+".jpg"));
        System.out.println("done!");
	}
}
