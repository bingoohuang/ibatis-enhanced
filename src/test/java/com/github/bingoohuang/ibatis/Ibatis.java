package com.github.bingoohuang.ibatis;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import lombok.SneakyThrows;
import lombok.val;

import java.io.InputStreamReader;
import java.io.Reader;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/21.
 */
public class Ibatis {
    @SneakyThrows
    public static SqlMapClient getSqlMapClient(String resource) {
        Reader reader = Resources.getResourceAsReader(resource);
        return SqlMapClientBuilder.buildSqlMapClient(reader);
    }

    @SneakyThrows
    public static String classpathInputStream(String resourceName) {
        ClassLoader loader = Ibatis.class.getClassLoader();
        val stream = loader.getResourceAsStream(resourceName);
        return CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));
    }

}
