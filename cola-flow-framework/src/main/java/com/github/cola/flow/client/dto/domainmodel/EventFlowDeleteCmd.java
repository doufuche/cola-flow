package com.github.cola.flow.client.dto.domainmodel;

import com.alibaba.cola.dto.Command;
import lombok.Data;

/**
 * Project Name: cola-flow
 * Desc: 删除该业务id的流程信息
 *
 * @author xihadoufuche@aliyun.com
 */
@Data
public class EventFlowDeleteCmd extends Command {
    /**
     * 业务id
     */
    private String bizId;
    /**
     * 流程节点名
     */
    private String eventName;
}
