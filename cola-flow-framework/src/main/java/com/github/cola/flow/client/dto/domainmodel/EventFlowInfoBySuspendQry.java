package com.github.cola.flow.client.dto.domainmodel;

import com.alibaba.cola.dto.Query;
import lombok.Data;

/**
 * Project Name: cola-flow
 * Desc: 查询该业务暂停时保存的eventFlow信息
 *
 * @author xihadoufuche@aliyun.com
 */
@Data
public class EventFlowInfoBySuspendQry extends Query {
    /**
     * 业务id
     */
    private String bizId;
    /**
     * 流程节点名
     */
    private String eventName;

}
