package com.github.cola.flow.client.dto.domainmodel;

import com.alibaba.cola.dto.Query;
import lombok.Data;

/**
 * Project Name: cola-flow
 * Desc: 检查业务是否已取消Query
 *
 * @author xihadoufuche@aliyun.com
 */
@Data
public class BizCancelledQry extends Query {
    /**
     * 业务id
     */
    private String bizId;

}
