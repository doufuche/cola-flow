package com.github.cola.flow.client.baseevent;

import com.alibaba.cola.exception.BizException;
import com.github.cola.flow.client.api.ColaFlowServiceI;
import com.github.cola.flow.client.constants.Constants;
import com.github.cola.flow.client.dto.domainevent.*;
import com.github.cola.flow.client.dto.domainevent.query.BizCancelledQryEvent;
import com.github.cola.flow.client.dto.domainevent.query.EventStateFinishCheckQryEvent;
import com.github.cola.flow.client.dto.event.Event;
import com.alibaba.cola.event.EventBusI;
import com.alibaba.cola.common.ApplicationContextHelper;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.event.EventBus;
import com.alibaba.cola.event.EventHandlerI;
import com.alibaba.cola.event.EventI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;

/** .
 * Project Name: cola-flow
 * Desc: 统一event处理器
 *
 * @author xihadoufuche@aliyun.com
 */
@Slf4j
@Component
public abstract class BaseEventFlowExecutor<R extends Response, E extends FlowBaseEvent> implements EventHandlerI {

    /**
     *
     */
    @Autowired
    private ColaFlowServiceI colaFlowServiceInterface;
    /**
     *
     */
    @Autowired
    private EventBusI eventBusI;
    /**
     *
     */
    private Gson gson = new GsonBuilder().create();

    @Override
    public R execute(final EventI eventI){
        R response = (R) new Response();
        Event event = null;
        String eventName = null;
        Long bizId = null;

        try {
        E sourceEvent = (E) eventI;

        event = buildEvent(sourceEvent);
        eventName = event.getEventName();
        bizId = event.getBizId();

        //add traceLog
        traceLogEventHandler(bizId, eventName, TraceLogEvent.EVENT_START);

        if (StringUtils.isEmpty(eventName)) {
            log.error("eventName is null. bizId:{}", bizId);
            throw new RuntimeException("eventName is null. bizId:" + bizId);
        }

        //check the baseevent is completed
        eventStateFinishCheck(eventName, bizId);

        //check bizId state suspend or cancel
        if (checkAndSuspend(response, eventName, bizId, event)){
            return response;
        }

        log.info("baseevent start,bizId:{},eventName:{}", sourceEvent.getBizId(), eventName);

        response = execute((E) sourceEvent);
        log.info("baseevent end,bizId:{},response:{}", sourceEvent.getBizId(), gson.toJson(response));
            //add traceLog
        traceLogEventHandler(bizId, eventName, TraceLogEvent.EVENT_FINISH);

        if (response != null && response.isSuccess()) {
            //eventstateInsertOrUpdateCmd
            eventSuccessUpdate(eventName, bizId);

            //eventFlowDeleteCmd
            eventFlowDelete(eventName, bizId);
        } else {
            log.warn("this baseevent fail!,bizId:{},eventName:{},response:{}", bizId, eventName, response);
            throw new RuntimeException("this baseevent fail!,bizId:" + bizId + ",eventName:" + eventName + ", response:"+ response);
        }

        log.info("current eventListener translate ok!, bizId:{}, eventName:{}", sourceEvent.getBizId(), eventName);

        if (event.getEvents().isEmpty()) {
            return response;
        }


            colaFlowServiceInterface.push(event);
        } catch (Exception e) {
            log.error("eventFlowServiceI.push error!!!", e);
            traceLogEventHandler(bizId, eventName, TraceLogEvent.EVENT_ERROR);
            //eventFlowErrorInsertCmd
            eventFlowErrorInsert(event, eventName, bizId);
            response = (R) new Response();
            response.setSuccess(false);
        }
        return response;
    }

    /**
     * 全局檢查命令
     * @param response
     * @param eventName
     * @param bizId
     * @return
     */
    private boolean checkAndSuspend(final R response, final String eventName, final Long bizId, final Event event) {
        BizCancelledQryEvent bizCancelledQryEvent = new BizCancelledQryEvent();
        bizCancelledQryEvent.setBizId(bizId.toString());
        R bizCancelQryResponse = (R) eventBusI.fire(bizCancelledQryEvent);
        if (bizCancelQryResponse != null && bizCancelQryResponse.isSuccess()) {
            log.info("{} Consumer execute redirect finished. bizId:{}", eventName, bizId);
            bizCanceledHandler(bizId);
            response.setSuccess(false);
            response.setErrMessage("the bizId is cancelled!,bizId:"+bizId);
            return true;
        }
        //suspend check
        if(checkOrderAndSuspend(bizId, eventName)){
            EventFlowSuspendUpdateEvent eventFlowSuspendUpdateEvent = new EventFlowSuspendUpdateEvent();
            eventFlowSuspendUpdateEvent.setBizId(bizId.toString());
            eventFlowSuspendUpdateEvent.setEventName(eventName);
            String eventsJson = gson.toJson(event);
            eventFlowSuspendUpdateEvent.setFlowInfo(eventsJson);
            Response suspendUpdateResponse = eventBusI.fire(eventFlowSuspendUpdateEvent);
            log.info("{} suspendUpdateResponse{} bizId:{}", eventName, suspendUpdateResponse, bizId);
            if(Objects.isNull(suspendUpdateResponse) || !suspendUpdateResponse.isSuccess()){
                throw new BizException(bizId+"eventFlowSuspendUpdateEvent is error!");
            }
            response.setSuccess(false);
            response.setErrMessage("this is suspendUpdateResponse!,bizId:"+bizId);
            return true;
        };
        return false;
    }

    /** .
     * 业务取消后处理，待业务方执行逻辑
     * @param  bizId business id
     */
    private void bizCanceledHandler(final Long bizId) {
        BizCanceledHandlerEvent bizCanceledHandlerEvent = new BizCanceledHandlerEvent();
        bizCanceledHandlerEvent.setBizId(bizId.toString());
        EventBus eventBus = ApplicationContextHelper.getBean(EventBus.class);
        eventBus.asyncFire(bizCanceledHandlerEvent);
    }

    /**
     * event节点的执行过程，添加日志和打点
     * @param bizId business id
     * @param eventName baseevent name
     */
    private void traceLogEventHandler(final Long bizId, final String eventName, final String type) {
        TraceLogEvent traceLogEvent = new TraceLogEvent();
        traceLogEvent.setBizId(bizId.toString());
        traceLogEvent.setEventName(eventName);
        traceLogEvent.setType(type);
        EventBus eventBus = ApplicationContextHelper.getBean(EventBus.class);
        eventBus.asyncFire(traceLogEvent);
    }

    /**
     * 判断该节点是否执行完成
     * @param eventName
     * @param bizId
     */
    private void eventStateFinishCheck(final String eventName, final Long bizId) {
        EventStateFinishCheckQryEvent eventStateCheckFinishQry = new EventStateFinishCheckQryEvent();
        eventStateCheckFinishQry.setBizId(bizId.toString());
        eventStateCheckFinishQry.setEventName(eventName);
        Response eventStateCheckFinishResponse = eventBusI.fire(eventStateCheckFinishQry);
        if (eventStateCheckFinishResponse != null && eventStateCheckFinishResponse.isSuccess()){
            //eventFlowDeleteCmd by bizId and node
            eventFlowDelete(eventName, bizId);
        }
    }

    /**
     *
     * @param eventName
     * @param bizId
     */
    private void eventFlowDelete(final String eventName, final Long bizId) {
        EventFlowDeleteEvent eventFlowDeleteEvent = new EventFlowDeleteEvent();
        eventFlowDeleteEvent.setBizId(bizId.toString());
        eventFlowDeleteEvent.setEventName(eventName);
        R eventFlowDeleteResponse = (R) eventBusI.fire(eventFlowDeleteEvent);

    }

    /**
     *
     * @param eventName
     * @param bizId
     */
    private void eventSuccessUpdate(final String eventName, final Long bizId) {
        EventStateSuccessUpdateEvent eventStateOkInsertUpdateCmd = new EventStateSuccessUpdateEvent();
        eventStateOkInsertUpdateCmd.setBizId(bizId.toString());
        eventStateOkInsertUpdateCmd.setEventName(eventName);
        R eventStateOkResponse = (R) eventBusI.fire(eventStateOkInsertUpdateCmd);
        if (eventStateOkResponse==null || !eventStateOkResponse.isSuccess()){
            log.error("eventStateOkInsertUpdateCmd error!, bizId:{}, eventName:{}", bizId, eventName);
            throw new RuntimeException("eventStateOkInsertUpdateCmd error!, bizId:" + bizId + ", eventName:" + eventName);
        }
    }

    /**
     *
     * @param event
     * @param eventName
     * @param bizId
     */
    private void eventFlowErrorInsert(final Event event, final String eventName, final Long bizId) {
        EventFlowErrorInsertEvent eventFlowErrorInsertEvent = new EventFlowErrorInsertEvent();
        eventFlowErrorInsertEvent.setBizId(bizId.toString());
        String eventsJson = gson.toJson(event);
        eventFlowErrorInsertEvent.setFlowInfo(eventsJson);
        eventFlowErrorInsertEvent.setEventName(eventName);
        eventFlowErrorInsertEvent.setTraceFlowId(event.getTraceFlowId());
        if (isSuspendByEventName(eventName)){
            eventFlowErrorInsertEvent.setSuspendEvent(true);
        }
        R eventFlowErrorInsertResponse = (R) eventBusI.fire(eventFlowErrorInsertEvent);
        if (eventFlowErrorInsertResponse == null || !eventFlowErrorInsertResponse.isSuccess()){
            log.error("eventFlowErrorInsertEvent error!, bizId:{}, eventName:{}", bizId, eventName);
            throw new RuntimeException("eventFlowErrorInsertEvent error!, bizId:" + bizId + ", eventName:" + eventName);
        }
    }

    /**
     * baseevent execute
     * @param event 节点
     * @return Response
     */
    public abstract R execute(E event);

    /**
     *
     * @param sourceEvent
     * @return
     */
    private Event buildEvent(final E sourceEvent){
        Event event = new Event();
        event.setBizId(sourceEvent.getBizId());
        String thisClassName = sourceEvent.getClass().getName();
        String eventName = thisClassName.substring(thisClassName.lastIndexOf(".")+1);
        event.setEventName(eventName);
        event.addParameter(eventName, sourceEvent);
        event.setEvents(sourceEvent.getWaitToExecuteEvents());
        event.setEventClassMap(sourceEvent.getFlowEventClassMap());
        event.setEventMap(sourceEvent.getFlowEventMap());
        event.setTraceFlowId(sourceEvent.getTraceFlowId());
        event.setStartEvent(sourceEvent.getStartEvent());
        event.setAsync(sourceEvent.isAsync());
        return event;
    }


    /**
     * 判断订单流程是否是暂停
     * 订单已暂停则等待
     * @param bizId
     * @throws Exception
     */
    private boolean checkOrderAndSuspend(final Long bizId, final String eventName){
        boolean isSuspendEvent = false;
        //check node is suspendEvent
        if (isSuspendByEventName(eventName)){
            log.warn("checkOrderAndSuspend true!, bizId:{}, eventName:{}", bizId, eventName);
            isSuspendEvent = true;
        }
        return isSuspendEvent;
    }

    /**
     *
     * @param eventName
     * @return
     */
    private boolean isSuspendByEventName(final String eventName) {
        return eventName.contains(Constants.SUSPEND_EVENT_SUFFIX);
    }
}
