package com.ibatis.common.jdbc.logging;

import com.github.bingoohuang.ibatis.BlackcatUtils;
import com.ibatis.common.beans.ClassInfo;
import com.ibatis.common.logging.Log;
import com.ibatis.common.logging.LogFactory;
import lombok.val;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;

/**
 * ResultSet proxy to add logging
 */
public class ResultSetLogProxy extends BaseLogProxy implements InvocationHandler {
    private static final Log log = LogFactory.getLog(ResultSet.class);

    int rows = 0;
    boolean first = true;
    private ResultSet rs;

    private ResultSetLogProxy(ResultSet rs) {
        super();
        this.rs = rs;
        if (log.isDebugEnabled()) {
            log.debug("{rset-" + id + "} ResultSet");
        }
    }

    static int maxRows = 5;

    public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
        try {
            Object result = method.invoke(rs, params);
            String methodName = method.getName();
            if (GET_METHODS.contains(methodName)) {
                if (params[0] instanceof String) {
                    setColumn(params[0], rs.wasNull() ? null : result);
                }
            } else {
                boolean isNext = "next".equals(methodName);
                boolean isClose = "close".equals(methodName);
                if (isNext || isClose) {
                    if (isNext && (Boolean) result) ++rows;

                    String s = getValueString();
                    if (!"[]".equals(s)) {
                        if (first) {
                            first = false;
                            String columnString = getColumnString();
                            BlackcatUtils.log("SQL.Header", columnString);

                            if (log.isDebugEnabled())
                                log.debug("{rset-" + id + "} Header: " + columnString);
                        }

                        if (rows <= maxRows + 1)
                            BlackcatUtils.log("SQL.Result", s);

                        if (log.isDebugEnabled() && rows <= maxRows + 1)
                            log.debug("{rset-" + id + "} Result: " + s);
                    }

                    if (isNext && !(Boolean) result) {
                        if (log.isDebugEnabled() && rows > maxRows + 1)
                            log.debug("{rset-" + id + "} Result rows "
                                    + (rows - maxRows) + " ignored: ");
                    }

                    int ignoredRows = rows - maxRows - 1;
                    if (ignoredRows > 0 && isClose) {
                        BlackcatUtils.log("SQL.Result",
                                "[And more " + ignoredRows
                                        + " rows ignored]");
                    }

                    clearColumnInfo();
                }
            }
            return result;
        } catch (Throwable t) {
            throw ClassInfo.unwrapThrowable(t);
        }
    }

    /**
     * Creates a logging version of a ResultSet
     *
     * @param rs - the ResultSet to proxy
     * @return - the ResultSet with logging
     */
    public static ResultSet newInstance(ResultSet rs) {
        val handler = new ResultSetLogProxy(rs);
        val cl = ResultSet.class.getClassLoader();
        return (ResultSet) Proxy.newProxyInstance(cl,
                new Class[]{ResultSet.class}, handler);
    }

    /**
     * Get the wrapped result set
     *
     * @return the resultSet
     */
    public ResultSet getRs() {
        return rs;
    }

}
