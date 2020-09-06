package com.github.cola.flow.client.baseevent;

import lombok.Data;

/**
 * Project Name: cola-flow
 * Desc: 结束节点event，仅供demo参考, 接入方可自定义
 *
 * @author xihadoufuche@aliyun.com
 */
@Deprecated
@Data
public class EndEvent extends FlowBaseEvent {

    private String endParameter="end";

}
