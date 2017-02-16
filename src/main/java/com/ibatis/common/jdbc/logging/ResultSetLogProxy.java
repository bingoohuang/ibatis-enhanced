package com.ibatis.common.jdbc.logging;

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
import java.util.ArrayList;
import java.util.List;

/**
 * ResultSet proxy to add logging
 */
public class ResultSetLogProxy extends BaseLogProxy implements InvocationHandler {
    private static final Log log = LogFactory.getLog(ResultSet.class);
    private final IbatisTrace ibatisTrace;

    boolean first = true;
    @Getter private ResultSet rs;

    private ResultSetLogProxy(ResultSet rs, IbatisTrace ibatisTrace) {
        super();
        this.rs = rs;
        this.ibatisTrace = ibatisTrace;
    }

    protected List columnValues = new ArrayList();

    @Override
    protected void setColumn(Object key, Object value) {
        super.setColumn(key, value);
        columnValues.add(value);
    }

    @Override
    protected void clearColumnInfo() {
        super.clearColumnInfo();
        columnValues = new ArrayList();
    }

    public static final int MAX_ROWS = 10;

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
                    if (isNext && (Boolean) result) {
                        ibatisTrace.incrTotalRows();
                    }

                    String s = getValueString();
                    if (!"[]".equals(s)) {
                        if (first) {
                            first = false;
                            String columnString = getColumnString();

                            if (log.isDebugEnabled())
                                log.debug("{rset-" + id + "} Header: " + columnString);
                        }

                        if (ibatisTrace.getRows() < MAX_ROWS) {
                            ibatisTrace.addRow(columnValues);

                            if (log.isDebugEnabled())
                                log.debug("{rset-" + id + "} Result: " + s);
                        }
                    }

                    if (isNext && !(Boolean) result) {
                        if (log.isDebugEnabled() && ibatisTrace.getTotalRows() > MAX_ROWS)
                            log.debug("{rset-" + id + "} Result totalRows "
                                    + (ibatisTrace.getTotalRows() - MAX_ROWS) + " ignored: ");

                        ibatisTrace.setResult(ibatisTrace.getResultSet());
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
     * @param rs          - the ResultSet to proxy
     * @param ibatisTrace - IbatisTrace
     * @return - the ResultSet with logging
     */
    public static ResultSet newInstance(ResultSet rs, IbatisTrace ibatisTrace) {
        val handler = new ResultSetLogProxy(rs, ibatisTrace);
        val cl = ResultSet.class.getClassLoader();
        Class[] interfaces = {ResultSet.class};
        return (ResultSet) Proxy.newProxyInstance(cl, interfaces, handler);
    }
}
