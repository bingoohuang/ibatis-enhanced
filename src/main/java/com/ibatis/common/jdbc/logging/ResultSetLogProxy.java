package com.ibatis.common.jdbc.logging;

import com.ailk.ecs.esf.conf.EsfProperties;
import com.github.bingoohuang.ibatis.BlackcatUtils;
import com.ibatis.common.beans.ClassInfo;
import com.ibatis.common.logging.Log;
import com.ibatis.common.logging.LogFactory;

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

    static int maxRows = EsfProperties.getInt("Blackcat.SQLResult.MaxRows", 5);

    public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
        try {
            Object o = method.invoke(rs, params);
            if (GET_METHODS.contains(method.getName())) {
                if (params[0] instanceof String) {
                    setColumn(params[0], rs.wasNull() ? null : o);
                }

            } else if ("next".equals(method.getName())
                    || "close".equals(method.getName())) {
                if ("next".equals(method.getName()) && (Boolean) o) ++rows;

                String s = getValueString();
                if (!"[]".equals(s)) {
                    if (first) {
                        first = false;
                        String columnString = getColumnString();
                        BlackcatUtils.log("SQL.Header", columnString);

                        if (log.isDebugEnabled())
                            log.debug("{rset-" + id + "} Header: " + columnString);
                    }

                    if (maxRows <= 0 || rows <= maxRows + 1)
                        BlackcatUtils.log("SQL.Result", s);

                    if (log.isDebugEnabled())
                        log.debug("{rset-" + id + "} Result: " + s);
                }
                if (maxRows > 0 && rows - maxRows > 0 && "close".equals(method.getName()))
                    BlackcatUtils.log("SQL.Result", "[And more " + (rows - maxRows) + " rows ignored]");

                clearColumnInfo();
            }
            return o;
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
        InvocationHandler handler = new ResultSetLogProxy(rs);
        ClassLoader cl = ResultSet.class.getClassLoader();
        return (ResultSet) Proxy.newProxyInstance(
                cl, new Class[]{ResultSet.class}, handler);
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
