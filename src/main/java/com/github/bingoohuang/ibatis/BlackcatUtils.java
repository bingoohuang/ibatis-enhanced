package com.github.bingoohuang.ibatis;

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

        com.github.bingoohuang.blackcat.javaagent.callback
                .Blackcat.log(msgType, pattern, args);
    }


    static Pattern OneLineTrimPattern = Pattern.compile("[\\s\r\n]+");

    public static String oneLineSql(String original) {
        Matcher matcher = OneLineTrimPattern.matcher(original);
        String oneLine = matcher.replaceAll(" ");

        return oneLine;
    }
}
