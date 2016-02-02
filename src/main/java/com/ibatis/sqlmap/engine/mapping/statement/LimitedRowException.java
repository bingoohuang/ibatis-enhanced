package com.ibatis.sqlmap.engine.mapping.statement;


public class LimitedRowException extends RuntimeException {

    public LimitedRowException(String message) {
        super(message);
    }

}
