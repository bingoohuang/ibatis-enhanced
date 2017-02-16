package com.ibatis.common.jdbc.logging;

import com.github.bingoohuang.ibatis.IbatisTrace;
import com.ibatis.common.beans.ClassInfo;
import com.ibatis.common.logging.Log;
import com.ibatis.common.logging.LogFactory;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;

import java.lang.reflect.InvocationHandler;
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

    @Getter private final PreparedStatement preparedStatement;
    private final String sql;
    private final IbatisTrace ibatisTrace;

    private PreparedStatementLogProxy(
            PreparedStatement stmt, String sql, IbatisTrace ibatisTrace) {
        this.preparedStatement = stmt;
        this.sql = sql;
        this.ibatisTrace = ibatisTrace;
    }

    protected List columnValues = new ArrayList();

    @Override
    protected void setColumn(Object key, Object value) {
        super.setColumn(key, value);

        columnValues.add(value);
    }

    public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
        try {
            if (EXECUTE_METHODS.contains(method.getName())) {
                return processExecuteMethod(method, params);
            }

            if (SET_METHODS.contains(method.getName())) {
                setColumn(params[0], "setNull".equals(method.getName()) ? null : params[1]);
                return method.invoke(preparedStatement, params);
            }

            return getObject(proxy, method, params, preparedStatement, ibatisTrace);
        } catch (Throwable t) {
            throw ClassInfo.unwrapThrowable(t);
        }
    }

    @SneakyThrows
    private Object processExecuteMethod(Method method, Object[] params) {
        String valueString = getValueString();
        ibatisTrace.setParams(valueString);
        String evalSql = createEvalSql(sql, columnValues);
        ibatisTrace.setEval(evalSql);
        if (log.isDebugEnabled() && !"[]".equals(valueString)) {
            log.debug("{pstm-" + id + "} Parameters: " + valueString);
            log.debug("{pstm-" + id + "} Eval: " + evalSql);
        }

        clearColumnInfo();
        Object invoke = method.invoke(preparedStatement, params);
        if (!"executeQuery".equals(method.getName())) {
            return invoke;
        }

        val rs = (ResultSet) invoke;
        return rs != null ? ResultSetLogProxy.newInstance(rs, ibatisTrace) : null;
    }

    @SneakyThrows
    public static Object getObject(Object proxy, Method method, Object[] params,
                                   Statement statement, IbatisTrace ibatisTrace) {
        if ("getResultSet".equals(method.getName())) {
            val rs = (ResultSet) method.invoke(statement, params);
            return rs != null ? ResultSetLogProxy.newInstance(rs, ibatisTrace) : null;
        }

        if ("equals".equals(method.getName())) {
            Object ps = params[0];
            return ps instanceof Proxy && proxy == ps;
        }

        if ("hashCode".equals(method.getName())) {
            return proxy.hashCode();
        }

        Object result = method.invoke(statement, params);
        if ("getUpdateCount".equals(method.getName())) {
            ibatisTrace.setResult(result);
        }
        return result;
    }

    /**
     * Creates a logging version of a PreparedStatement
     *
     * @param stmt        - the statement
     * @param sql         - the sql statement
     * @param ibatisTrace
     * @return - the proxy
     */
    public static PreparedStatement newInstance(
            PreparedStatement stmt, String sql, IbatisTrace ibatisTrace) {
        val handler = new PreparedStatementLogProxy(stmt, sql, ibatisTrace);
        val cl = PreparedStatement.class.getClassLoader();
        Class[] interfaces = {PreparedStatement.class, CallableStatement.class};
        return (PreparedStatement) Proxy.newProxyInstance(cl, interfaces, handler);
    }
}
