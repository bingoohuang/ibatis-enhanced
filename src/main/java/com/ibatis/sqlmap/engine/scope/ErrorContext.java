package com.ibatis.sqlmap.engine.scope;

import com.github.bingoohuang.ibatis.IbatisTrace;
import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

/**
 * An error context to help us create meaningful error messages
 */
public class ErrorContext {
    @Getter private String resource;
    @Getter @Setter private String activity;
    @Getter @Setter private String objectId;
    @Getter @Setter private String moreInfo;
    @Getter @Setter private Throwable cause;

    private static final Pattern EXCLUDED_OBJECTID_PATTERN
            = Pattern.compile("-(?:SelectKey|AutoResultMap|InlineParameterMap)$");

    public void setResource(String resource) {
        this.resource = resource;

        // 这里依赖setActivity, setObjectId在setResource之前调用
        if (objectId != null
                && !"applying a result map".equals(activity) // 结果映射的排除
                && !EXCLUDED_OBJECTID_PATTERN.matcher(objectId).find()) {// 辅助的排除
            IbatisTrace.setSqlId(objectId);
        }
    }

    public String toString() {
        StringBuilder message = new StringBuilder();

        // resource
        if (resource != null) {
            message.append("  \n--- The error occurred in ");
            message.append(resource);
            message.append(".");
        }

        // activity
        if (activity != null) {
            message.append("  \n--- The error occurred while ");
            message.append(activity);
            message.append(".");
        }

        // object
        if (objectId != null) {
            message.append("  \n--- Check the ");
            message.append(objectId);
            message.append(".");
        }

        // more info
        if (moreInfo != null) {
            message.append("  \n--- ");
            message.append(moreInfo);
        }

        // cause
        if (cause != null) {
            message.append("  \n--- Cause: ");
            message.append(cause.toString());
        }

        return message.toString();
    }

    /**
     * Clear the error context
     */
    public void reset() {
        resource = null;
        activity = null;
        objectId = null;
        moreInfo = null;
        cause = null;
    }
}
