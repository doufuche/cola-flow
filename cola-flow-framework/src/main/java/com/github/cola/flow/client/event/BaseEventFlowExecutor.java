package com.github.cola.flow.client.event;

import com.github.cola.flow.client.api.ColaFlowServiceI;
import com.github.cola.flow.client.constants.Constants;
import com.github.cola.flow.client.dto.domainmodel.*;
import com.github.cola.flow.client.dto.event.Event;
import com.alibaba.cola.command.CommandBusI;
import com.alibaba.cola.common.ApplicationContextHelper;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.event.EventBus;
import com.alibaba.cola.event.EventHandlerI;
import com.alibaba.cola.event.EventI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** .
 * Project Name: cola-flow
 * Desc: 统一event处理器
 *
 * @author xihadoufuche@aliyun.com
 */
@Slf4j
@Component
public abstract class BaseEventFlowExecutor<R extends Response, E extends FlowFlowBaseEvent> implements EventHandlerI {

    /**
     *
     */
    @Autowired
    private ColaFlowServiceI colaFlowServiceInterface;
    /**
     *
     */
    @Autowired
    private CommandBusI commandBus;
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

        if (StringUtils.isBlank(eventName)) {
            log.error("eventName is null. bizId:{}", bizId);
            throw new RuntimeException("eventName is null. bizId:" + bizId);
        }

        //check the event is completed
        eventStateFinishCheck(eventName, bizId);

        //check bizId state suspend or cancel
        if (checkBizCancelled(response, eventName, bizId)){
            return response;
        }

        log.info("event start,bizId:{},eventName:{}", sourceEvent.getBizId(), eventName);

        response = execute((E) sourceEvent);
        log.info("event end,bizId:{},response:{}", sourceEvent.getBizId(), gson.toJson(response));
            //add traceLog
        traceLogEventHandler(bizId, eventName, TraceLogEvent.EVENT_FINISH);

        if (response != null && response.isSuccess()) {
            //eventstateInsertOrUpdateCmd
            eventSuccessUpdate(eventName, bizId);

            //eventFlowDeleteCmd
            eventFlowDelete(eventName, bizId);
        } else {
            log.warn("this event fail!,bizId:{},eventName:{},response:{}", bizId, eventName, response);
            throw new RuntimeException("this event fail!,bizId:" + bizId + ",eventName:" + eventName + ", response:"+ response);
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
    private boolean checkBizCancelled(final R response, final String eventName, final Long bizId) {
        BizCancelledQry bizCancelledQry = new BizCancelledQry();
        bizCancelledQry.setBizId(bizId.toString());
        R bizCancelQryResponse = (R) commandBus.send(bizCancelledQry);
        if (bizCancelQryResponse != null && bizCancelQryResponse.isSuccess()) {
            log.warn("{} Consumer execute redirect finished. bizId:{}", eventName, bizId);
            bizCanceledHandler(bizId);
            response.setSuccess(false);
            response.setErrMessage("the bizId is cancelled!,bizId:"+bizId);
            return true;
        }
        //suspend check
        checkOrderAndSuspend(bizId, eventName);
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
     * @param eventName event name
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
        EventStateFinishCheckQry eventStateCheckFinishQry = new EventStateFinishCheckQry();
        eventStateCheckFinishQry.setBizId(bizId.toString());
        eventStateCheckFinishQry.setEventName(eventName);
        Response eventStateCheckFinishResponse = commandBus.send(eventStateCheckFinishQry);
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
        EventFlowDeleteCmd eventFlowDeleteCmd = new EventFlowDeleteCmd();
        eventFlowDeleteCmd.setBizId(bizId.toString());
        eventFlowDeleteCmd.setEventName(eventName);
        R eventFlowDeleteResponse = (R) commandBus.send(eventFlowDeleteCmd);

    }

    /**
     *
     * @param eventName
     * @param bizId
     */
    private void eventSuccessUpdate(final String eventName, final Long bizId) {
        EventStateSuccessUpdateCmd eventStateOkInsertUpdateCmd = new EventStateSuccessUpdateCmd();
        eventStateOkInsertUpdateCmd.setBizId(bizId.toString());
        eventStateOkInsertUpdateCmd.setEventName(eventName);
        R eventStateOkResponse = (R) commandBus.send(eventStateOkInsertUpdateCmd);
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
        EventFlowErrorInsertCmd eventFlowErrorInsertCmd = new EventFlowErrorInsertCmd();
        eventFlowErrorInsertCmd.setBizId(bizId.toString());
        String eventsJson = gson.toJson(event);
        eventFlowErrorInsertCmd.setFlowInfo(eventsJson);
        eventFlowErrorInsertCmd.setEventName(eventName);
        eventFlowErrorInsertCmd.setTraceFlowId(event.getTraceFlowId());
        if (isSuspendByEventName(eventName)){
            eventFlowErrorInsertCmd.setSuspendEvent(true);
        }
        R eventFlowErrorInsertResponse = (R) commandBus.send(eventFlowErrorInsertCmd);
        if (eventFlowErrorInsertResponse == null || !eventFlowErrorInsertResponse.isSuccess()){
            log.error("eventFlowErrorInsertCmd error!, bizId:{}, eventName:{}", bizId, eventName);
            throw new RuntimeException("eventFlowErrorInsertCmd error!, bizId:" + bizId + ", eventName:" + eventName);
        }
    }

    /**
     * event execute
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
    private void checkOrderAndSuspend(final Long bizId, final String eventName){
        boolean isSuspendEvent = false;
        //check node is suspendEvent
        if (isSuspendByEventName(eventName)){
            isSuspendEvent = true;
        }

        if (isSuspendEvent) {
            log.warn("checkOrderAndSuspend true!, bizId:{}, eventName:{}", bizId, eventName);
            throw new RuntimeException("checkOrderAndSuspend true!, bizId:" + bizId + ", eventName:" + eventName);
        }
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
