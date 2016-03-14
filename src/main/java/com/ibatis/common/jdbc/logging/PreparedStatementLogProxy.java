package com.ibatis.common.jdbc.logging;

import com.alibaba.fastjson.JSON;
import com.github.bingoohuang.ibatis.BlackcatUtils;
import com.ibatis.common.beans.ClassInfo;
import com.ibatis.common.logging.Log;
import com.ibatis.common.logging.LogFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static com.github.bingoohuang.ibatis.BlackcatUtils.createEvalSql;


/**
 * PreparedStatement proxy to add logging
 */
public class PreparedStatementLogProxy
        extends BaseLogProxy implements InvocationHandler {

    private static final Log log = LogFactory.getLog(PreparedStatement.class);

    private PreparedStatement statement;
    private String sql;

    private PreparedStatementLogProxy(PreparedStatement stmt, String sql) {
        this.statement = stmt;
        this.sql = sql;
    }

    protected List columnValues = new ArrayList();

    @Override
    protected void setColumn(Object key, Object value) {
        super.setColumn(key, value);

        columnValues.add(value);
    }

    public Object invoke(Object proxy, Method method, Object[] params)
            throws Throwable {
        try {
            if (EXECUTE_METHODS.contains(method.getName())) {
                String valueString = getValueString();
                if (BlackcatUtils.HasBlackcat && !"[]".equals(valueString)) {
                    BlackcatUtils.log("SQL.Parameters", valueString);
                    BlackcatUtils.log("SQL.Eval", createEvalSql(sql, columnValues));
                }

                if (log.isDebugEnabled()) {
//                    log.debug("{pstm-" + id + "} Executing Statement: " + oneLineSql);
                    log.debug("{pstm-" + id + "} Parameters: " + valueString);
                    log.debug("{pstm-" + id + "} Types: " + getTypeString());
                }
                clearColumnInfo();
                if ("executeQuery".equals(method.getName())) {
                    ResultSet rs = (ResultSet) method.invoke(statement, params);
                    return rs != null ? ResultSetLogProxy.newInstance(rs) : null;
                } else {
                    return method.invoke(statement, params);
                }
            }

            if (SET_METHODS.contains(method.getName())) {
                setColumn(params[0], "setNull".equals(method.getName()) ? null : params[1]);
                return method.invoke(statement, params);
            }

            return getObject(proxy, method, params, statement);
        } catch (Throwable t) {
            throw ClassInfo.unwrapThrowable(t);
        }
    }

    public static Object getObject(
            Object proxy, Method method, Object[] params, Statement statement
    ) throws IllegalAccessException, InvocationTargetException {
        if ("getResultSet".equals(method.getName())) {
            ResultSet rs = (ResultSet) method.invoke(statement, params);
            return rs != null ? ResultSetLogProxy.newInstance(rs) : null;
        }

        if ("equals".equals(method.getName())) {
            Object ps = params[0];
            return new Boolean(ps instanceof Proxy && proxy == ps);
        }

        if ("hashCode".equals(method.getName())) {
            return new Integer(proxy.hashCode());
        }

        Object result = method.invoke(statement, params);
        if ("getUpdateCount".equals(method.getName()))
            BlackcatUtils.log("SQL.UpdateCount", JSON.toJSONString(result));

        return result;
    }

    /**
     * Creates a logging version of a PreparedStatement
     *
     * @param stmt - the statement
     * @param sql  - the sql statement
     * @return - the proxy
     */
    public static PreparedStatement newInstance(PreparedStatement stmt, String sql) {
        InvocationHandler handler = new PreparedStatementLogProxy(stmt, sql);
        ClassLoader cl = PreparedStatement.class.getClassLoader();
        return (PreparedStatement) Proxy.newProxyInstance(cl,
                new Class[]{PreparedStatement.class, CallableStatement.class}, handler);
    }

    /**
     * Return the wrapped prepared statement
     *
     * @return the PreparedStatement
     */
    public PreparedStatement getPreparedStatement() {
        return statement;
    }

}
