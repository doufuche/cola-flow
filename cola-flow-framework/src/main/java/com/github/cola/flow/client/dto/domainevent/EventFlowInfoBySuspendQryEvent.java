package com.github.cola.flow.client.dto.domainevent;

import com.alibaba.cola.event.DomainEventI;
import lombok.Data;

/**
 * Project Name: cola-flow
 * Desc: 查询该业务暂停时保存的eventFlow信息
 *
 * @author xihadoufuche@aliyun.com
 */
@Data
public class EventFlowInfoBySuspendQryEvent implements DomainEventI {
    /**
     * 业务id
     */
    private String bizId;
    /**
     * 流程节点名
     */
    private String eventName;

}
