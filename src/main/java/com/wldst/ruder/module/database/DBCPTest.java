package com.wldst.ruder.module.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;

public class DBCPTest {

    public void init() throws Exception {

	Properties p = new Properties();

	p.setProperty("driverClassName", "com.mysql.jdbc.Driver");
	p.setProperty("url", "jdbc:mysql://localhost:3306/test");
	p.setProperty("username", "root");
	p.setProperty("password", "root");

	p.setProperty("maxActive", "50");// 设置最大并发数
	p.setProperty("initialSize", "2");// 数据库初始化时，创建的连接个数
	p.setProperty("minIdle", "10");// 最小空闲连接数
	p.setProperty("maxIdle", "10");// 数据库最大连接数
	p.setProperty("maxWait", "1000");// 超时等待时间(毫秒）
	p.setProperty("removeAbandoned", "false");// 是否自动回收超时连接
	p.setProperty("removeAbandonedTimeout", "120");// 超时时间(秒)
	p.setProperty("testOnBorrow", "true");// 取得连接时进行有效性验证
	p.setProperty("logAbandoned", "true");// 是否在自动回收超时连接的时候打印连接的超时错误

	BasicDataSource dataSource = BasicDataSourceFactory.createDataSource(p);

	Connection connection = dataSource.getConnection();

    }

    public void init2() throws SQLException {

	BasicDataSource dataSource = new BasicDataSource();
	dataSource.setDriverClassName("com.mysql.jdbc.Driver");
	dataSource.setUrl("jdbc:mysql://localhost:3306/test");
	dataSource.setUsername("root");
	dataSource.setPassword("root");

	Connection connection = dataSource.getConnection();
	connection.prepareStatement(null);
    }

}