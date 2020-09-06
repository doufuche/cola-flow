package com.github.cola.flow.client.dto.domainevent;

import com.alibaba.cola.event.DomainEventI;
import lombok.Data;

/**
 * Project Name: cola-flow
 * Desc: 删除该业务id的流程信息
 *
 * @author xihadoufuche@aliyun.com
 */
@Data
public class EventFlowDeleteEvent implements DomainEventI {
    /**
     * 业务id
     */
    private String bizId;
    /**
     * 流程节点名
     */
    private String eventName;
}
