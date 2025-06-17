package com.wldst.ruder.module.database.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.wldst.ruder.exception.ApplicationException;

/**
 * 这是一个管理JDBC connection的类 (从连接池中拿Connection).
 * 浏览器的每一次访问服务器,服务器都会开启一个线程,即每一个request,对应一个线程.
 * 每一个线程即是一个ThreadLocal,放在ThreadLocal中的东西是属于同一线程共享的,所以在ThreadLocal放Connection,
 * 同一个request取出来的Connection是属于同一个的.
 * 
 * @author Kevin
 *
 */
public class ConnectionManager {

    /**
     * ThreadLocal存放Connection
     */
    private static ThreadLocal<Connection> connectionHolder = new ThreadLocal<Connection>();

    /**
     * 从连接池拿Connection
     * 
     * getConnection和connectionHolder.get()的区别
     * connectionHolder.get()是尝试从ThreadLocal中获取Connection,如果没有,返回null,如果有,直接返回.
     * getConnection也是尝试从ThreadLocal中获取Connection,如果没有,则创建一个,然后返回,如果有,直接返回.
     */
    public static Connection getConnection() {

	Connection connection = connectionHolder.get();

	if (connection == null) {
	    // 1.连接池可以理解是一个java类,必须实现接口DateSource
	   connectionHolder.set(connection);
	}

	return connection;
    }

    /**
     * Connection使用完毕,关闭 此处的Connection是从连接池中拿出来的,关闭Connection实质上是让Connection恢复空闲状态
     * 
     * @throws ApplicationException
     */
    public static void closeConnection() throws ApplicationException {
	// 尝试从ThreadLocal获取Connection,如果没有,关闭Connection失去意义.
	Connection connection = connectionHolder.get();

	if (connection != null) {
	    try {
		connection.close();
		connectionHolder.remove();

	    } catch (SQLException e) {
		e.printStackTrace();
		throw new ApplicationException("系统错误,请联系系统管理员!");
	    }
	}
    }

    /**
     * Statement使用完毕,关闭
     * 
     * @throws ApplicationException
     */
    public static void closeStatement(Statement statement) throws ApplicationException {

	if (statement != null) {
	    try {
		statement.close();

	    } catch (SQLException e) {
		e.printStackTrace();
		throw new ApplicationException("系统错误,请联系系统管理员!");
	    }
	}
    }

    /**
     * ResultSet使用完毕,关闭
     * 
     * @throws ApplicationException
     */
    public static void closeResultSet(ResultSet resultSet) throws ApplicationException {

	if (resultSet != null) {
	    try {
		resultSet.close();

	    } catch (SQLException e) {
		e.printStackTrace();
		throw new ApplicationException("系统错误,请联系系统管理员!");
	    }
	}
    }

    /**
     * 获取connection,把事务设置为手动提交,自己控制事务
     * 
     * @throws ApplicationException
     */
    public static void manualCommitTransaction() throws ApplicationException {

	// 看看ThreadLocal中是否有Connection,如果没有(因为这是第一次跟Connection打交道,所以没有Connection),必须创建一个.
	// 如果此处不创建Connection,将无法保证事务.
	Connection connection = getConnection();

	if (connection != null) {
	    try {
		if (connection.getAutoCommit()) {
		    connection.setAutoCommit(false);
		}

	    } catch (SQLException e) {
		e.printStackTrace();
		throw new ApplicationException("系统错误,请联系系统管理员!");
	    }
	}
    }

    /**
     * 一切操作正常,提交事务
     * 
     * @throws ApplicationException
     */
    public static void commitTransaction() throws ApplicationException {
	// 尝试从ThreadLocal获取Connection,如果没有,提交事务失去意义.
	Connection connection = connectionHolder.get();

	if (connection != null) {
	    try {
		if (!connection.getAutoCommit()) {
		    connection.commit();
		}

	    } catch (SQLException e) {
		e.printStackTrace();
		throw new ApplicationException("系统错误,请联系系统管理员!");
	    }
	}
    }

    /**
     * 操作发生异常,回滚事务
     * 
     * @throws ApplicationException
     */
    public static void rollbackTransaction() throws ApplicationException {
	// 尝试从ThreadLocal获取Connection,如果没有,回滚事务失去意义.
	Connection connection = connectionHolder.get();

	if (connection != null) {
	    try {
		if (!connection.getAutoCommit()) {
		    connection.rollback();
		}

	    } catch (SQLException e) {
		e.printStackTrace();
		throw new ApplicationException("系统错误,请联系系统管理员!");
	    }
	}
    }

    /**
     * 重置事务,如果事务是手动提交的,重置为自动提交,如果是自动提交的,重置为手动提交.
     * 
     * @throws Exception
     */
    public static void resetConnection() throws ApplicationException {
	// 尝试从ThreadLocal获取Connection,如果没有,重置事务失去意义.
	Connection connection = connectionHolder.get();

	if (connection != null) {
	    try {
		if (connection.getAutoCommit()) {
		    connection.setAutoCommit(false);

		} else {
		    connection.setAutoCommit(true);
		}

	    } catch (SQLException e) {
		e.printStackTrace();
		throw new ApplicationException("系统错误,请联系系统管理员!");
	    }
	}
    }
}