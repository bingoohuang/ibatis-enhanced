package com.ibatis.common.jdbc.logging;

import com.github.bingoohuang.ibatis.BlackcatUtils;
import com.google.common.base.Throwables;
import com.ibatis.common.beans.ClassInfo;
import com.ibatis.common.logging.Log;
import com.ibatis.common.logging.LogFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * Connection proxy to add logging
 */
public class ConnectionLogProxy extends BaseLogProxy implements InvocationHandler {

    private static final Log log = LogFactory.getLog(Connection.class);

    private Connection connection;

    private ConnectionLogProxy(Connection conn) {
        super();
        this.connection = conn;
        if (log.isDebugEnabled()) {
            log.debug("{conn-" + id + "} Connection");
        }
    }

    public Object invoke(Object proxy, Method method, Object[] params)
            throws Throwable {
        try {

            if ("prepareStatement".equals(method.getName())) {
                String param0 = (String) params[0];
                String oneLineSql = BlackcatUtils.oneLineSql(param0);
                BlackcatUtils.log("SQL.Preparing", oneLineSql);

                if (log.isDebugEnabled()) {
                    log.debug("{conn-" + id + "} Preparing Statement: " + oneLineSql);
                }
                PreparedStatement stmt = (PreparedStatement) method.invoke(connection, params);
                return PreparedStatementLogProxy.newInstance(stmt, param0);
            }

            if ("prepareCall".equals(method.getName())) {
                String param0 = (String) params[0];
                String oneLineSql = BlackcatUtils.oneLineSql(param0);
                BlackcatUtils.log("SQL.Preparing", oneLineSql);

                if (log.isDebugEnabled()) {
                    log.debug("{conn-" + id + "} Preparing Call: " + oneLineSql);
                }
                PreparedStatement stmt = (PreparedStatement) method.invoke(connection, params);
                return PreparedStatementLogProxy.newInstance(stmt, param0);
            }

            if ("createStatement".equals(method.getName())) {
                Statement stmt = (Statement) method.invoke(connection, params);
                return StatementLogProxy.newInstance(stmt);
            }

            return method.invoke(connection, params);
        } catch (Throwable t) {
            Throwable t1 = ClassInfo.unwrapThrowable(t);
            BlackcatUtils.log("SQL.ERROR", Throwables.getStackTraceAsString(t1));
            log.error("Error calling Connection." + method.getName() + ':', t1);
            throw t1;
        }

    }

    /**
     * Creates a logging version of a connection
     *
     * @param conn - the original connection
     * @return - the connection with logging
     */
    public static Connection newInstance(Connection conn) {
        InvocationHandler handler = new ConnectionLogProxy(conn);
        ClassLoader cl = Connection.class.getClassLoader();
        return (Connection) Proxy.newProxyInstance(cl, new Class[]{Connection.class}, handler);
    }

    /**
     * return the wrapped connection
     *
     * @return the connection
     */
    public Connection getConnection() {
        return connection;
    }

}
