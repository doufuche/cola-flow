package com.github.cola.flow.client.constants;

/**
 * Project Name: cola-flow
 * Desc:
 *
 * @author xihadoufuche@aliyun.com
 */
public class Constants {
    /**
     * 开始Event名称
     */
    public static final String START_EVENT = "StartEvent";

    public static final String STRING_SPLIT = ",";

    /**
     * 暂停Event节点名称后缀
     */
    public static final String SUSPEND_EVENT_SUFFIX = "SuspendEvent";

    /**
     * 该node执行状态，0表示成功，1表示失败
     */
    public static final Integer EVENT_STATE_SUCCESS = 0;

    /**
     * 该eventFlow执行状态，0表示成功，1表示失败
     */
    public static final Integer EVENT_FLOW_SUCCESS = 0;
}
