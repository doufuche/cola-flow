package com.github.cola.flow.engine.event;

import com.github.cola.flow.client.constants.Constants;
import com.github.cola.flow.client.baseevent.FlowBaseEvent;
import com.alibaba.cola.common.ApplicationContextHelper;
import com.alibaba.cola.event.EventBus;
import lombok.extern.slf4j.Slf4j;
import com.github.cola.flow.client.dto.event.EventEntity;
import com.github.cola.flow.client.dto.event.Event;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * Project Name: cola-flow
 * Desc:
 *
 * @author xihadoufuche@aliyun.com
 */
@Slf4j
public class FlowEventChangeHandler<ET extends EventEntity, E extends FlowBaseEvent> implements FlowEventChangeListener<ET, E> {
    private FlowEventController<ET, E> flowEventController;

    /**
     * @param eteFlowEventController
     */
    public FlowEventChangeHandler(FlowEventController<ET, E> eteFlowEventController) {
        this.flowEventController = eteFlowEventController;
    }


    @Override
    public boolean eventChanged(List<ET> exeEvents) throws Exception {
        EventBus eventBus = ApplicationContextHelper.getBean(EventBus.class);

        boolean b = false;
        for (ET exeEvent : exeEvents) {
            try {
                if (flowEventController.isEmptyListener()){
                    return true;
                }

                String exeEventNodeName = exeEvent.toString();
                Event<ET, E> eteEvent = flowEventController.copyEvent();
                eteEvent.setEventName(exeEventNodeName);

                E thisEvent = getEventInstance(exeEventNodeName, eteEvent);

                thisEvent.setBizId(eteEvent.getBizId());
                thisEvent.setWaitToExecuteEvents(eteEvent.getEvents());
                thisEvent.setFlowEventClassMap(eteEvent.getEventClassMap());
                thisEvent.setStartEvent(this.getStartEvent(eteEvent, exeEventNodeName));
                thisEvent.setFlowEventMap(eteEvent.getEventMap());
                thisEvent.setParameterMap(eteEvent.getParameterMap());
                thisEvent.setTraceFlowId(eteEvent.getTraceFlowId());
                thisEvent.setAsync(eteEvent.isAsync());
                if (Constants.START_EVENT.equals(exeEventNodeName) || eteEvent.isAsync()){
                    eventBus.asyncFire(thisEvent);
                }else {
                    eventBus.fire(thisEvent);
                }
                b = true;
            } catch (Exception ex) {
                log.error("FlowEventChangeHandler.EventChanged error,the parameter is:" + exeEvent, ex);
                throw new Exception(ex);
            }

        }

        flowEventController.removeListener(this);
        return b;
    }

    private E getEventInstance(String exeNodeName, Event<ET, E> etEvent) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        String thisClassName = etEvent.getEventClassMap().get(exeNodeName);
        if (StringUtils.isEmpty(thisClassName)){
            throw new RuntimeException("thisClassName is null!, please check config:eventFlow.classes.config");
        }
        Class aClass = Class.forName(thisClassName);
        Object obj = aClass.newInstance();

        return (E) obj;
    }

    /**
     *
     * @param eteEvent
     * @param exeEventNodeName
     * @return
     */
    public final E getStartEvent(final Event eteEvent, final String exeEventNodeName){
        E startEvent = (E) eteEvent.getParameterMap().get(Constants.START_EVENT);
        if (exeEventNodeName.equals(Constants.START_EVENT)){
            return startEvent;
        } else {
            if (Objects.isNull(startEvent) && Objects.nonNull(eteEvent.getStartEvent())){
                return (E) eteEvent.getStartEvent();
            } else if (Objects.nonNull(startEvent) && Objects.nonNull(startEvent.getStartEvent())){
                return (E) startEvent.getStartEvent();
            } else if (Objects.nonNull(startEvent) && Objects.isNull(startEvent.getStartEvent())){
                return startEvent;
            } else{
                log.error("cannot get startEvent！！，bizId:"+eteEvent.getBizId()+",exeNodeName:"+exeEventNodeName);
                return null;
            }
        }
    }
}