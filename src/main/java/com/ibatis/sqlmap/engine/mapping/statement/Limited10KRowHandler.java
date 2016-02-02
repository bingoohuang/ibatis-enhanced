package com.ibatis.sqlmap.engine.mapping.statement;

import com.alibaba.fastjson.JSON;
import com.ibatis.sqlmap.client.event.RowHandler;
import com.ibatis.sqlmap.engine.scope.StatementScope;

import java.util.ArrayList;
import java.util.List;

public class Limited10KRowHandler implements RowHandler {
    private List list = new ArrayList();
    private Object parameterObject;
    private int rowNum = 0;
    private MappedStatement mappedStatement;
    private StatementScope statementScope;

    public Limited10KRowHandler(
            MappedStatement mappedStatement,
            StatementScope statementScope,
            Object parameterObject) {
        this.statementScope = statementScope;
        this.mappedStatement = mappedStatement;
        this.parameterObject = parameterObject;
    }

    @Override
    public void handleRow(Object valueObject) {
        if (++rowNum <= 100000) {
            list.add(valueObject);
            return;
        }

        String errorMsg = "result rows exceeds 100000!!! id:" + mappedStatement.getId()
                + ", sql:" + mappedStatement.getSql().getSql(statementScope, parameterObject)
                + ", parameter:" + JSON.toJSONString(parameterObject);
        throw new LimitedRowException(errorMsg);
    }

    public List getList() {
        return list;
    }

    public void setList(List list) {
        this.list = list;
    }

}
