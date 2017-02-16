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
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Statement proxy to add logging
 */
public class StatementLogProxy extends BaseLogProxy implements InvocationHandler {
    private static final Log log = LogFactory.getLog(Statement.class);
    private final IbatisTrace ibatisTrace;
    @Getter private Statement statement;

    private StatementLogProxy(Statement stmt, IbatisTrace ibatisTrace) {
        super();
        this.statement = stmt;
        this.ibatisTrace = ibatisTrace;
    }

    public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
        try {
            if (!EXECUTE_METHODS.contains(method.getName())) {
                return PreparedStatementLogProxy.getObject(
                        proxy, method, params, statement, ibatisTrace);
            }

            String oneLineSql = BlackcatUtils.oneLineSql((String) params[0]);
            ibatisTrace.setPrepared(oneLineSql);

            if (log.isDebugEnabled()) {
                log.debug("{stmt-" + id + "} Statement: " + oneLineSql);
            }
            Object invoke = method.invoke(statement, params);
            if (invoke == null) {
                return null;
            }

            if (!"executeQuery".equals(method.getName())) {
                return invoke;
            }

            ResultSet rs = (ResultSet) invoke;
            return ResultSetLogProxy.newInstance(rs, ibatisTrace);
        } catch (Throwable t) {
            throw ClassInfo.unwrapThrowable(t);
        }
    }

    /**
     * Creates a logging version of a Statement
     *
     * @param stmt        - the statement
     * @param ibatisTrace
     * @return - the proxy
     */
    public static Statement newInstance(Statement stmt, IbatisTrace ibatisTrace) {
        val handler = new StatementLogProxy(stmt, ibatisTrace);
        val cl = Statement.class.getClassLoader();
        Class[] interfaces = {Statement.class};
        return (Statement) Proxy.newProxyInstance(cl, interfaces, handler);
    }
}
