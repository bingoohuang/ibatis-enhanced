package com.ibatis.sqlmap.engine.mapping.statement;

import java.sql.SQLException;
import java.util.List;

import com.ibatis.common.jdbc.exception.NestedSQLException;
import com.ibatis.sqlmap.engine.scope.StatementScope;
import com.ibatis.sqlmap.engine.transaction.Transaction;
import com.ibatis.sqlmap.engine.transaction.TransactionException;

public class SelectStatement extends MappedStatement {

    @Override
    public StatementType getStatementType() {
        return StatementType.SELECT;
    }

    @Override
    public int executeUpdate(
            StatementScope statementScope, Transaction trans,
            Object parameterObject)
            throws SQLException {
        throw new SQLException(
                "Select statements cannot be executed as an update.");
    }

    @Override
    public List executeQueryForList(
            StatementScope statementScope, Transaction trans,
            Object parameterObject,
            int skipResults, int maxResults) throws SQLException {
        try {
            Limited10KRowHandler rowHandler = new Limited10KRowHandler(
                    this, statementScope, parameterObject);
            executeQueryWithCallback(statementScope, trans.getConnection(),
                    parameterObject, null, rowHandler,
                    skipResults, maxResults);
            return rowHandler.getList();
        } catch (TransactionException e) {
            throw new NestedSQLException(
                    "Error getting Connection from Transaction.", e);
        }
    }
}
