package com.github.cola.flow.client.dto.domainevent;

import com.alibaba.cola.event.DomainEventI;
import lombok.Data;

/**
 * Desc:流程暂停时更新流程信息
 *
 * @author doufuche
 * @date 2020/11/16
 */
@Data
public class EventFlowSuspendUpdateEvent implements DomainEventI {
    /**
     * 业务id
     */
    private String bizId;
    /**
     * 流程节点名
     */
    private String eventName;
    /**
     * 流程信息
     */
    private String flowInfo;
}
