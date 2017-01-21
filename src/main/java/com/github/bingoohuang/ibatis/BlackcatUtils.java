package com.github.bingoohuang.ibatis;

import com.github.bingoohuang.blackcat.javaagent.callback.Blackcat;
import org.joda.time.DateTime;

import java.sql.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlackcatUtils {
    public static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (Throwable e) { // including ClassNotFoundException
            return false;
        }
    }

    public static boolean HasBlackcat = classExists(
            "com.github.bingoohuang.blackcat.javaagent.callback.Blackcat");

    public static void log(String msgType, String pattern, Object... args) {
        if (!HasBlackcat) return;

        Blackcat.log(msgType, pattern, args);
    }


    static Pattern OneLineTrimPattern = Pattern.compile("[\\s\r\n]+");

    public static String oneLineSql(String original) {
        Matcher matcher = OneLineTrimPattern.matcher(original);
        String oneLine = matcher.replaceAll(" ");

        return oneLine.trim();
    }

    public static String createEvalSql(String evalSqlTemplate, List boundParams) {
        int size = boundParams.size();
        if (size == 0) return BlackcatUtils.oneLineSql(evalSqlTemplate);

        StringBuilder eval = new StringBuilder();
        int startPos = 0;
        int index = -1;
        int evalSqlLength = evalSqlTemplate.length();

        String placeholder = "?";

        while (startPos < evalSqlLength) {
            int pos = evalSqlTemplate.indexOf(placeholder, startPos);
            if (pos < 0) break;

            ++index;
            eval.append(evalSqlTemplate.substring(startPos, pos));

            if (index < size) {
                Object boundParam = boundParams.get(index);
                eval.append(createEvalBoundParam(boundParam));
            } else {
                eval.append('?');
            }

            startPos = pos + placeholder.length();
        }

        eval.append(evalSqlTemplate.substring(startPos));

        String evalSql = eval.toString();
        return BlackcatUtils.oneLineSql(evalSql);
    }

    private static String createEvalBoundParam(Object boundParam) {
        if (boundParam == null) return "NULL";
        if (boundParam instanceof Boolean)
            return (Boolean) boundParam ? "1" : "0";
        if (boundParam instanceof Number) return boundParam.toString();
        if (boundParam instanceof Date)
            return '\'' + new DateTime(boundParam).toString("yyyy-MM-DD HH:mm:ss") + '\'';
        if (boundParam instanceof byte[])
            return '\'' + Hex.encode((byte[]) boundParam) + '\'';

        String strParam = boundParam.toString();
        return '\'' + strParam.replaceAll("'", "''") + '\'';
    }

}
