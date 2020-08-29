package com.github.cola.flow.engine.event;

import com.github.cola.flow.client.dto.event.EventEntity;
import com.github.cola.flow.client.event.FlowFlowBaseEvent;

import java.util.List;

/**
 * Project Name: cola-flow
 * Desc:
 *
 * @author xihadoufuche@aliyun.com
 */
public interface FlowEventChangeListener<ET extends EventEntity, E extends FlowFlowBaseEvent> {

    /**
     * 事件感知
     * @param exeEntitys
     * @return
     * @throws Exception
     */
    boolean eventChanged(List<ET> exeEntitys) throws Exception;
}