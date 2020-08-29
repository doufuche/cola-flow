package com.github.cola.flow.client.dto.domainmodel;

import com.alibaba.cola.dto.Command;
import lombok.Data;

/**
 * Project Name: cola-flow
 * Desc: 节点状态插入或更新cmd
 *
 * @author xihadoufuche@aliyun.com
 */
@Data
public class EventStateSuccessUpdateCmd extends Command {
    /**
     * 业务id
     */
    private String bizId;
    /**
     * 流程节点名
     */
    private String eventName;

}
