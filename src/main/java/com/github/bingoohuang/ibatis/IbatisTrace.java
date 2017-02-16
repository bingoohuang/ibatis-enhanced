package com.github.bingoohuang.ibatis;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/2/15.
 */
@Data
public class IbatisTrace {
    private String prepared;
    private Throwable throwable;
    private String params;
    private String eval;
    private Object result;
    private int totalRows;
    private List<List> resultSet = Lists.newArrayList();
    private static ThreadLocal<String> sqlId = new ThreadLocal<String>();

    public int incrTotalRows() {
        return ++totalRows;
    }

    public void addRow(List row) {
        resultSet.add(row);
    }

    public static void setSqlId(String objectId) {
        sqlId.set(objectId);
    }

    public void setResult(Object result) {
        this.result = result;

        BlackcatUtils.trace(this);
        clear();
    }

    private void clear() {
        prepared = null;
        throwable = null;
        params = null;
        eval = null;
        result = null;
        totalRows = 0;
        resultSet.clear();
        ;
        sqlId.remove();
    }

    public static String getSqlId() {
        return sqlId.get();
    }

    public int getRows() {
        return resultSet.size();
    }
}
