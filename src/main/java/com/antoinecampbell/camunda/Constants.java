package com.antoinecampbell.camunda;

public final class Constants {

    public static final String ATTRIBUTE_NAME_TYPE = "type";
    public static final String ATTRIBUTE_ROUTING_KEY = "routing-key";
    public static final String ATTRIBUTE_TASK_ID = "task-id";
    public static final String ATTRIBUTE_REPLY_TO = "reply-to";
    public static final String ATTRIBUTE_ROUTING_SUCCESS = "success";

    public static final String WORKER_NAME = "camunda";

    public static final long TASK_TIMEOUT_MILLIS = 30_000L;
    public static final long TASK_POLL_MILLIS = 5_000L;
    public static final int TASK_LOCK_COUNT = 50;

    public static final int EXTERNAL_TASK_RETRY_COUNT = 2;
    public static final int EXTERNAL_TASK_RETRY_TIMEOUT_MILLIS = 3000;

    private Constants() {
    }
}
