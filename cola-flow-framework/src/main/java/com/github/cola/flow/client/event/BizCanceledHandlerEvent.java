package com.github.cola.flow.client.event;


import com.alibaba.cola.event.DomainEventI;
import lombok.Data;

/**
 * Project Name: cola-flow
 * Desc:
 *
 * @author xihadoufuche@aliyun.com
 */
@Data
public class BizCanceledHandlerEvent implements DomainEventI {
    /**
     * 业务id
     */
    private String bizId;

}
