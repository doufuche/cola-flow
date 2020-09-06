package com.github.cola.flow.client.dto.domainevent;

import com.alibaba.cola.event.DomainEventI;
import lombok.Data;

/**
 * Project Name: cola-flow
 * Desc: 检查业务是否已取消Query
 *
 * @author xihadoufuche@aliyun.com
 */
@Data
public class BizCancelledQryEvent implements DomainEventI {
    /**
     * 业务id
     */
    private String bizId;

}
