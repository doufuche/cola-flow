package com.github.cola.flow.client.event;

import com.alibaba.cola.event.DomainEventI;
import com.github.cola.flow.client.constants.Constants;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Project Name: cola-flow
 * Desc:
 *
 * @author xihadoufuche@aliyun.com
 */
public class FlowFlowBaseEvent<E extends FlowFlowBaseEvent> implements DomainEventI {
    /**
     * 业务ID
     */
    private Long bizId;
    /**
     * 流程定义编号
     */
    Integer traceFlowId;
    /**
     * 是否跳过该event执行，true表示跳过
     */
    private Boolean isExecuted=Boolean.FALSE;
    /**
     * 等待执行event集合
     */
    List waitToExecuteEvents;
    /**
     * 流程event对应class类
     */
    Map<String,String> flowEventClassMap;
    /**
     * 流程开始节点
     */
    E startEvent;

    /**
     * 流程树形结构节点信息
     */
    Map<String, E> flowEventMap;
    /**
     * 每个event的参数对象
     */
    private Map<String, E> parameterMap = new HashMap<>();
    /**
     * 該event是否異步執行
     */
    private boolean isAsync;


    public Long getBizId() {
        return bizId;
    }

    public void setBizId(Long bizId) {
        this.bizId = bizId;
    }

    public Boolean getExecuted() {
        return isExecuted;
    }

    public void setExecuted(Boolean executed) {
        isExecuted = executed;
    }

    public List getWaitToExecuteEvents() {
        return waitToExecuteEvents;
    }

    public void setWaitToExecuteEvents(List waitToExecuteEvents) {
        this.waitToExecuteEvents = waitToExecuteEvents;
    }

    public Map getFlowEventClassMap() {
        return flowEventClassMap;
    }

    public void setFlowEventClassMap(Map flowEventClassMap) {
        this.flowEventClassMap = flowEventClassMap;
    }

    public E getStartEvent() {
        return startEvent;
    }

    public void setStartEvent(E startEvent) {
        this.startEvent = startEvent;
    }

    public Map<String, E> getFlowEventMap() {
        return flowEventMap;
    }

    public void setFlowEventMap(Map<String, E> flowEventMap) {
        this.flowEventMap = flowEventMap;
    }

    public Integer getTraceFlowId() {
        return traceFlowId;
    }

    public void setTraceFlowId(Integer traceFlowId) {
        this.traceFlowId = traceFlowId;
    }

    public Map<String, E> getParameterMap() {
        return parameterMap;
    }

    public void setParameterMap(Map<String, E> parameterMap) {
        this.parameterMap = parameterMap;
    }

    public boolean isAsync() {
        return isAsync;
    }

    public void setAsync(boolean async) {
        isAsync = async;
    }

    public E getStartEventByParameter(String exeNodeName){
        E startEvent = (E) this.getParameterMap().get(Constants.START_EVENT);
        if (exeNodeName.contains(Constants.START_EVENT)){
            return startEvent;
        }else{
            return (E) startEvent.getStartEvent();
        }
    }
}
