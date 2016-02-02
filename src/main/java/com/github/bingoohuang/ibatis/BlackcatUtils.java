package com.github.bingoohuang.ibatis;

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

    public static void log(String msgType, String msg) {
        if (!HasBlackcat) return;

        com.github.bingoohuang.blackcat.javaagent.callback
                .Blackcat.log(msgType, msg);
    }
}
