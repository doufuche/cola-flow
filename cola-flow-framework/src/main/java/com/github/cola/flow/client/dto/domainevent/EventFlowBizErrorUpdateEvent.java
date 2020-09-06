package com.github.cola.flow.client.dto.domainevent;

import com.alibaba.cola.event.DomainEventI;
import lombok.Data;

/**
 * Project Name: cola-flow
 * Desc: 查询该业务異常时保存的eventFlow信息，區別于系統異常EventFlowErrorInsertCmd
 *
 * @author xihadoufuche@aliyun.com
 */
@Data
public class EventFlowBizErrorUpdateEvent implements DomainEventI {
    /**
     * 业务id
     */
    private String bizId;
    /**
     * 流程节点名
     */
    private String eventName;
    /**
     * 记录id
     */
    private String traceId;
    /**
     * 流程信息
     */
    private String flowInfo;

}
