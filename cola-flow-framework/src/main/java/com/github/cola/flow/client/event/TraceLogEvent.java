package com.github.cola.flow.client.event;


import com.alibaba.cola.event.DomainEventI;
import lombok.Data;
import lombok.ToString;

/**
 * Project Name: cola-flow
 * Desc: Event执行
 *
 * @author xihadoufuche@aliyun.com
 */
@Data
public class TraceLogEvent implements DomainEventI {
    /**
     * 业务id
     */
    private String bizId;
    /**
     * 当前执行的event名称
     */
    private String eventName;
    /**
     * 日志和打点的执行过程类型，对应下面的常量
     */
    private String type;

    /**
     * 该event执行开始
     */
    @ToString.Exclude
    public static final String EVENT_START = "start";
    /**
     * 该event执行成功完成
     */
    @ToString.Exclude
    public static final String EVENT_FINISH = "finish";
    /**
     * 该event执行过程异常
     */
    @ToString.Exclude
    public static final String EVENT_ERROR = "error";

}
