package com.github.bingoohuang.ibatis;

import com.github.bingoohuang.blackcat.instrument.callback.Blackcat;
import com.google.common.base.Splitter;
import com.ibatis.sqlmap.client.SqlMapClient;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

public class IbatisLog {
    static SqlMapClient sqlmap = Ibatis.getSqlMapClient("sqlmap-config.xml");

    @Test @SneakyThrows
    public void test() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("OldResourcesCode", 1);
        Object obj = sqlmap.queryForObject("qrySerialNumberByOldResourcesCode", map);
        assertThat(obj).isEqualTo("123");
    }

    @BeforeClass @SneakyThrows
    public static void beforeClass() {
        @Cleanup val conn = sqlmap.getDataSource().getConnection();
        conn.setAutoCommit(false);
        String initSqls = Ibatis.classpathInputStream("init.sql");
        val sqls = Splitter.on(';').omitEmptyStrings().split(initSqls);
        @Cleanup val stmt = conn.createStatement();
        for (val sql : sqls) {
            stmt.execute(sql);
        }
        conn.commit();
    }

    @Test @SneakyThrows
    public void blackcat() {
        Blackcat.reset("12345566", "0",
                "MAIN.START", "LOG");

        HashMap<String, String> param = new HashMap<String, String>();
        param.put("id", "ABC^TEST");
        Object obj = sqlmap.queryForObject("test1", param);
//        System.out.println(obj);

        List list = sqlmap.queryForList("test2");
//        System.out.println(list);


        val errorCodeMappings = sqlmap.queryForList("getErrorCodeMapping");
//        System.out.println(errorCodeMappings);


        sqlmap.update("updateDescription");
        Map params = new HashMap();
        params.put("id", "ABC^TEST8");
        sqlmap.delete("deleteOne", params);
        sqlmap.insert("insertOne");

        params = new HashMap();

        long base = System.currentTimeMillis();
        params.put("id", "ABC^TEST" + base);
        params.put("groupId", "ABC");
        params.put("dataId", "TEST" + base);
        params.put("content", "XXXYYY");
        params.put("desc", "hello world");
        sqlmap.insert("insertDiamond", params);

        sqlmap.startBatch();

        for (int i = 0; i < 10; ++i) {
            ++base;
            params.put("id", "ABC^TEST" + base);
            params.put("groupId", "ABC");
            params.put("dataId", "TEST" + base);
            params.put("content", "XXXYYY");
            params.put("desc", "hello world");
            sqlmap.insert("insertDiamond", params);
        }
        sqlmap.executeBatch();
    }
}
