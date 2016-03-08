package com.ibatis.common.jdbc.logging;

import com.github.bingoohuang.ibatis.BlackcatUtils;
import com.ibatis.common.beans.ClassInfo;
import com.ibatis.common.logging.Log;
import com.ibatis.common.logging.LogFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Statement proxy to add logging
 */
public class StatementLogProxy extends BaseLogProxy implements InvocationHandler {

    private static final Log log = LogFactory.getLog(Statement.class);

    private Statement statement;

    private StatementLogProxy(Statement stmt) {
        super();
        this.statement = stmt;
    }

    public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
        try {
            if (EXECUTE_METHODS.contains(method.getName())) {
                String oneLineSql = BlackcatUtils.oneLineSql((String) params[0]);
                BlackcatUtils.log("SQL.Statement", oneLineSql);

                if (log.isDebugEnabled()) {
                    log.debug("{stmt-" + id + "} Statement: " + oneLineSql);
                }
                if ("executeQuery".equals(method.getName())) {
                    ResultSet rs = (ResultSet) method.invoke(statement, params);
                    return rs != null ? ResultSetLogProxy.newInstance(rs) : null;
                } else {
                    return method.invoke(statement, params);
                }
            } else {
                return PreparedStatementLogProxy.getObject(
                        proxy, method, params, statement);
            }
        } catch (Throwable t) {
            throw ClassInfo.unwrapThrowable(t);
        }
    }

    /**
     * Creates a logging version of a Statement
     *
     * @param stmt - the statement
     * @return - the proxy
     */
    public static Statement newInstance(Statement stmt) {
        InvocationHandler handler = new StatementLogProxy(stmt);
        ClassLoader cl = Statement.class.getClassLoader();
        return (Statement) Proxy.newProxyInstance(cl,
                new Class[]{Statement.class}, handler);
    }

    /**
     * return the wrapped statement
     *
     * @return the statement
     */
    public Statement getStatement() {
        return statement;
    }

}
