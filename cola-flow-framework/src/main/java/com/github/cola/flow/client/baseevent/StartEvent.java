package com.github.cola.flow.client.baseevent;

import lombok.Data;

/**
 * Project Name: cola-flow
 * Desc: 开始event，仅供demo参考, 接入方可自定义
 *
 * @author xihadoufuche@aliyun.com
 */
@Deprecated
@Data
public class StartEvent extends FlowBaseEvent {

    private String startParameter="start";

}
