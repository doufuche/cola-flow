package com.github.cola.flow.engine.event;

import com.github.cola.flow.client.event.FlowFlowBaseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.github.cola.flow.client.dto.event.EventEntity;
import com.github.cola.flow.client.dto.event.Event;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Project Name: cola-flow
 * Desc:
 *
 * @author xihadoufuche@aliyun.com
 */
public class FlowEventController<ET extends EventEntity, E extends FlowFlowBaseEvent> {
    Gson newGson = new GsonBuilder().create();
    private Event<ET, E> eteEvent;
    private BlockingQueue<FlowEventChangeListener<ET, E>> flowEventChangeListeners = new LinkedBlockingQueue<FlowEventChangeListener<ET, E>>();


    private FlowEventController(Event<ET, E> eteEvent) {
        this.eteEvent = eteEvent;
    }

    public static <ET extends EventEntity,C extends FlowFlowBaseEvent> FlowEventController newInstance(Event<ET,C> etcEvent) {
        FlowEventController flowEventController = new FlowEventController(etcEvent);
        flowEventController.addListener(new FlowEventChangeHandler<>(flowEventController));
        return flowEventController;
    }


    public void addListener(FlowEventChangeListener<ET, E> eteFlowEventChangeListener) {
        flowEventChangeListeners.add(eteFlowEventChangeListener);
    }


    public boolean removeListener(FlowEventChangeListener flowEventChangeListener) {
        return flowEventChangeListeners.remove(flowEventChangeListener);
    }

    public boolean isEmptyListener(){
        return flowEventChangeListeners.size() == 0;
    }


    public void handle(List<ET> exeEvents) throws Exception {
        for (FlowEventChangeListener<ET, E> eteFlowEventChangeListener : flowEventChangeListeners) {
            eteFlowEventChangeListener.eventChanged(exeEvents);
        }

    }

    public Event<ET, E> getEteEvent() {
        return eteEvent;
    }

    /**
     * 返回event的副本，采用序列化和反序列化实现深层copy
     * @return
     */
    public Event<ET, E> copyEvent() {
        String eventText = newGson.toJson(eteEvent);

        return FlowEventEngine.jsonToEvent(eventText, eteEvent.getParameterMap(), eteEvent.getEventClassMap(), eteEvent.getEventMap(), (E) eteEvent.getStartEvent());
    }

}
