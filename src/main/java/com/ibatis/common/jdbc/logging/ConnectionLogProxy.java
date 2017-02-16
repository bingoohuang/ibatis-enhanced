package com.ibatis.common.jdbc.logging;

import com.github.bingoohuang.ibatis.BlackcatUtils;
import com.github.bingoohuang.ibatis.IbatisTrace;
import com.ibatis.common.beans.ClassInfo;
import com.ibatis.common.logging.Log;
import com.ibatis.common.logging.LogFactory;
import lombok.Getter;
import lombok.val;

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

    @Getter private Connection connection;

    private ConnectionLogProxy(Connection conn) {
        super();
        this.connection = conn;
    }

    public Object invoke(Object proxy, Method method, Object[] params)
            throws Throwable {
        IbatisTrace ibatisTrace = new IbatisTrace();
        try {
            if ("prepareStatement".equals(method.getName()) || "prepareCall".equals(method.getName())) {
                String param0 = (String) params[0];
                String oneLineSql = BlackcatUtils.oneLineSql(param0);
                ibatisTrace.setPrepared(oneLineSql);

                if (log.isDebugEnabled()) {
                    log.debug("{conn-" + id + "} " + method.getName() + ": " + oneLineSql);
                }
                val stmt = (PreparedStatement) method.invoke(connection, params);
                return PreparedStatementLogProxy.newInstance(stmt, param0, ibatisTrace);
            }

            if ("createStatement".equals(method.getName())) {
                val stmt = (Statement) method.invoke(connection, params);
                return StatementLogProxy.newInstance(stmt, ibatisTrace);
            }

            return method.invoke(connection, params);
        } catch (Throwable t) {
            Throwable t1 = ClassInfo.unwrapThrowable(t);
            ibatisTrace.setThrowable(t1);
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
}
