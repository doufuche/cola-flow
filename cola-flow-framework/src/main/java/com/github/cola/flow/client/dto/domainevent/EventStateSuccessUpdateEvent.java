package com.github.cola.flow.client.dto.domainevent;

import com.alibaba.cola.event.DomainEventI;
import lombok.Data;

/**
 * Project Name: cola-flow
 * Desc: 节点状态插入或更新cmd
 *
 * @author xihadoufuche@aliyun.com
 */
@Data
public class EventStateSuccessUpdateEvent implements DomainEventI {
    /**
     * 业务id
     */
    private String bizId;
    /**
     * 流程节点名
     */
    private String eventName;

}
