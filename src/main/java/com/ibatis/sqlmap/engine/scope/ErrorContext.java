package com.ibatis.sqlmap.engine.scope;

import com.github.bingoohuang.blackcat.javaagent.callback.Blackcat;

import java.util.regex.Pattern;

/**
 * An error context to help us create meaningful error messages
 */
public class ErrorContext {
    private String resource;
    private String activity;
    private String objectId;
    private String moreInfo;
    private Throwable cause;

    public String getResource() {
        return resource;
    }


    static Pattern ExcludedId = Pattern.compile("-(?:SelectKey|AutoResultMap|InlineParameterMap)$");

    public void setResource(String resource) {
        this.resource = resource;

        // 这里依赖setObjectId在setResource之前调用
        if (objectId != null && !ExcludedId.matcher(objectId).find())
            Blackcat.log("SQL.ID", "ID:{}, RESOURCE:{}", objectId, resource);
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getMoreInfo() {
        return moreInfo;
    }

    public void setMoreInfo(String moreInfo) {
        this.moreInfo = moreInfo;
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public String toString() {
        StringBuffer message = new StringBuffer();

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
