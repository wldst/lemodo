package com.wldst.ruder.module.cluster.handle;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.qliu6.OSUtil;

public class UpdateDialog extends JDialog {
	private JButton cancelButton;
	private JButton okBJButton;
	private JLabel status = new JLabel();
	private static UpdateDialog instanceUpdateDialog;

	public UpdateDialog(JFrame parent) {
		super(parent, "网络异常", true);
		super.setSize(200, 120);
		super.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setResizable(false);
		instanceUpdateDialog = this;
		centered(this);
		init();
	}

	private void init() {
		okBJButton = new JButton("");
		cancelButton = new JButton("取消");
		this.setLayout(null);

		status.setText(" 有新版本,更新吗?");
		status.setBounds(new Rectangle(20, 12, 150, 30));
		okBJButton.setBounds(new Rectangle(40, 50, 60, 30));
		// cancelButton.setBounds(new Rectangle(110, 50, 60, 30));
		okBJButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
				OSUtil.runCmd("killall webshow&&cd /usr/q6/ovd/&&./start.sh");
			}
		});
		// cancelButton.addActionListener(new ActionListener() {
		//
		// @Override
		// public void actionPerformed(ActionEvent e) {
		// dispose();
		//
		// }
		// });

		this.add(status);
		this.add(okBJButton);
		this.add(cancelButton);
	}

	public void centered(Container container) {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		int w = container.getWidth();
		int h = container.getHeight();
		container.setBounds((screenSize.width - w) / 2,
				(screenSize.height - h) / 2, w, h);
	}

}
