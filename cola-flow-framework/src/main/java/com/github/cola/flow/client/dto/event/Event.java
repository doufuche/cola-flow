package com.github.cola.flow.client.dto.event;

import com.github.cola.flow.client.baseevent.FlowBaseEvent;
import lombok.Data;
import java.util.*;

/**
 * Project Name: cola-flow
 * Desc:
 *
 * @author xihadoufuche@aliyun.com
 */
@Data
public class Event<ET extends EventEntity, E extends FlowBaseEvent> {
    /**
     * 業務id
     */
    private long bizId;
    /**
     * 流程定义表中对应，定义用于区分多流程
     */
    Integer traceFlowId;
    /**
     * event名稱
     */
    private String eventName;
    /**
     * 待执行节点
     */
    List<ET> events=new ArrayList<>();
    /**
     * 流程树形结构节点信息
     */
    Map<String,ET> eventMap = new LinkedHashMap<String, ET>();

    private Map<String,String> eventClassMap = new HashMap<>();
    /**
     * 每个event的参数对象
     */
    private Map<String, E> parameterMap = new HashMap<>();
    /**
     * 开始节点
     */
    private FlowBaseEvent startEvent;
    /**
     * 該event是否異步執行
     */
    private boolean isAsync;

    public Event(){}

    public Event(ET et){
        events.add(et);
    }
    public Event(List<ET> ets){
        events.addAll(ets);
    }
    public void addParameter(String key, E value) {
        this.parameterMap.put(key,value);
    }

    public void addParameters(Map<String, E> parameters) {
        this.parameterMap.putAll(parameters);
    }

    public void clearParameters() {
        this.parameterMap = new HashMap<>(16);
    }
}
