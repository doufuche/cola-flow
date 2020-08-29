package com.github.cola.flow.client.dto.domainmodel;

import com.alibaba.cola.dto.Query;
import lombok.Data;

/**
 * Project Name: cola-flow
 * Desc: 判断节点是否执行完成
 *
 * @author xihadoufuche@aliyun.com
 */
@Data
public class EventStateFinishCheckQry extends Query {
    /**
     * 业务id
     */
    private String bizId;
    /**
     * 流程节点名
     */
    private String eventName;

}
