package com.github.cola.flow.engine.service;

import com.github.cola.flow.client.api.ColaFlowServiceI;
import com.github.cola.flow.client.constants.Constants;
import com.github.cola.flow.client.dto.domainmodel.EventFlowInfoBySuspendQry;
import com.github.cola.flow.client.dto.event.Event;
import com.github.cola.flow.client.event.FlowFlowBaseEvent;
import com.github.cola.flow.engine.event.FlowEventEngine;
import com.github.cola.flow.engine.event.tree.EventTreeConvert;
import com.github.cola.flow.engine.event.tree.ManyFlowEventTreeNode;
import com.github.cola.flow.engine.event.tree.FlowEventTreeNode;
import com.github.cola.flow.engine.event.tree.EventTreeNodeConvert;
import com.alibaba.cola.command.CommandBusI;
import com.alibaba.cola.dto.SingleResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Project Name: cola-flow
 * Desc:
 *
 * @author xihadoufuche@aliyun.com
 */
@Slf4j
@Service
public class ColaFlowServiceImpl<E extends FlowFlowBaseEvent> implements ColaFlowServiceI {
    /**
     *
     */
    @Autowired
    private CommandBusI commandBus;

    /**
     * 流程节点名称对应类，demo如下：
     * {"StartEvent":"com.github.cola.flow.client.event.StartEvent","EndEvent":"com.github.cola.flow.client.event.EndEvent"}
     */
    @Value("${eventFlow.classes.config}")
    private String eventFlowClasses;
    /**
     *
     */
    private static ObjectMapper om = new ObjectMapper();
    /**
     *
     */
    private Gson gson = new GsonBuilder().create();

    @Override
    public void init(final FlowFlowBaseEvent startEvent, final String eventFlowInfo) throws Exception {

        E thisEvent = (E) startEvent;
        Event<ManyFlowEventTreeNode, E> event = buildEventByInterrupt(thisEvent);

        //JSON convert to Map
        Map eventClassMap = om.readValue(eventFlowClasses, Map.class);
        event.setEventClassMap(eventClassMap);

        List<FlowEventTreeNode> eventTreeNodes = EventTreeNodeConvert.convertTreeNode(eventFlowInfo);

        EventTreeConvert tree = new EventTreeConvert(event.getEventName());
        EventTreeConvert manyTreeNode = tree.createTree(eventTreeNodes);
        List<ManyFlowEventTreeNode> eventTree = tree.getEventTree(manyTreeNode.getRoot());
        event.setEvents(eventTree);

        Map<String, ManyFlowEventTreeNode> treeMap = tree.iteratorTreeByBreadthFirst(manyTreeNode.getRoot());
        event.setEventMap(treeMap);

        FlowEventEngine.init(event, manyTreeNode.getRoot());
    }

    /**
     *
     * @param sourceEvent
     * @return
     */
    private Event<ManyFlowEventTreeNode, E> buildEventByInterrupt(E sourceEvent){
        Event<ManyFlowEventTreeNode, E> event = new Event<>();
        event.setBizId(sourceEvent.getBizId());
        String thisClassName = sourceEvent.getClass().getName();
        String eventName = thisClassName.substring(thisClassName.lastIndexOf(".")+1);
        event.setEventName(eventName);
        event.addParameter(eventName, sourceEvent);
        event.setTraceFlowId(sourceEvent.getTraceFlowId());
        event.setStartEvent(sourceEvent);
        event.setAsync(sourceEvent.isAsync());
        return event;
    }

    /**
     * Continue the process from the pause node specified by the input parameter
     * @param event The eventName in the event represents the name of the node that has just been executed
     * @throws Exception
     */
    @Override
    public void push(final Event event) throws Exception {
        FlowEventEngine.push(event);
    }

    @Override
    public void continueEventFlow(final FlowFlowBaseEvent suspendEvent) throws Exception {
        String thisClassName = suspendEvent.getClass().getName();
        String suspendEventName = thisClassName.substring(thisClassName.lastIndexOf(".")+1);
        String bizId = suspendEvent.getBizId().toString();

        Event queryEvent = getEventBySuspendQry(suspendEventName, bizId);
        if (Objects.isNull(queryEvent)) {
            log.error("continueEventFlow fail!, because eventFlow not at suspend, bizId:{}, suspendEventName:{}", bizId, suspendEventName);
            throw new RuntimeException("continueEventFlow fail!, because eventFlow not at suspend, bizId:" + bizId + ",suspendEventName:" + suspendEventName);
        }
        List<ManyFlowEventTreeNode> tmpNodes = getTreeNodesBySuspendFlowInfo(suspendEventName, bizId, queryEvent.getEvents());
        log.info("continueEventFlow log,suspendEvent:{} ,tmpNodes:{}", suspendEvent, gson.toJson(tmpNodes));

        E thisEvent = (E) suspendEvent;
        Event event = buildEventByInterrupt(thisEvent, tmpNodes);

        event.setEventMap(queryEvent.getEventMap());
        event.setEventName(suspendEventName);
        Map eventClassMap = om.readValue(eventFlowClasses, Map.class);
        event.setEventClassMap(eventClassMap);

        setStartEventToParameterMap(queryEvent, event);

        FlowEventEngine.push(event);
    }

    @Override
    public final void errorFlowContinue(final FlowFlowBaseEvent errorEvent) throws Exception {
        String thisClassName = errorEvent.getClass().getName();
        String eventName = thisClassName.substring(thisClassName.lastIndexOf(".")+1);
        String bizId = errorEvent.getBizId().toString();

        List<ManyFlowEventTreeNode> tmpNodes = getTreeNodesBySuspendFlowInfo(eventName, bizId, errorEvent.getWaitToExecuteEvents());
        log.info("errorFlowContinue log,errorEvent:{} ", errorEvent);

        E thisEvent = (E) errorEvent;
        Event event = buildEventByInterrupt(thisEvent, tmpNodes);

        event.setEventMap(thisEvent.getFlowEventMap());
        event.setEventName(eventName);

        Map<String, String> eventClassMap = om.readValue(eventFlowClasses, Map.class);
        event.setEventClassMap(eventClassMap);

        setStartEventWhenError(thisEvent, event);

        List events = event.getEvents();
        ManyFlowEventTreeNode current = (ManyFlowEventTreeNode) event.getEventMap().get(event.getEventName());
        //将重试节点放到待执行列表第一个
        events.add(0, current);
        event.setEventName(current.getEventTreeNode().getParentName());
        event.setEvents(events);

        FlowEventEngine.push(event);
    }

    /**
     * queryEvent中取startEvent属性到一个新的实例，然后设置到event.parameterMap.StartEvent中
     * @param queryEvent query
     * @param event set
     * @throws ClassNotFoundException class not found
     * @throws InstantiationException exception
     * @throws IllegalAccessException exception
     * @throws InvocationTargetException exception
     * @throws NoSuchMethodException exception
     */
    private void setStartEventToParameterMap(final Event queryEvent, final Event event) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Class catClass = Class.forName(queryEvent.getEventClassMap().get(Constants.START_EVENT).toString());
        if (catClass != null) {
            Object newInstance = catClass.newInstance();
            E startEventInstance = (E) newInstance;

            LinkedTreeMap startEventPropertiesMap = (LinkedTreeMap) ( (LinkedTreeMap) queryEvent.getParameterMap().get(queryEvent.getEventName())).get("startEvent");

            setStartEventProperties(event, startEventInstance, startEventPropertiesMap);

        }
    }

    /**
     * sourceEvent中取startEvent属性到一个新的实例，然后设置到targetEvent.parameterMap.StartEvent中
     * @param sourceEvent source
     * @param targetEvent target
     * @throws ClassNotFoundException class not found
     * @throws InstantiationException exception
     * @throws IllegalAccessException exception
     * @throws InvocationTargetException exception
     * @throws NoSuchMethodException exception
     */
    private void setStartEventWhenError(final FlowFlowBaseEvent sourceEvent, final Event targetEvent) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Class catClass = Class.forName(sourceEvent.getFlowEventClassMap().get(Constants.START_EVENT).toString());
        if (catClass!=null) {
            Object obj = catClass.newInstance();
            E startEventInstance = (E) obj;

            LinkedTreeMap startEventPropertiesMap = (LinkedTreeMap) ( (LinkedTreeMap) sourceEvent.getParameterMap().get(targetEvent.getEventName())).get("startEvent");

            setStartEventProperties(targetEvent, startEventInstance, startEventPropertiesMap);

        }
    }

    /**
     * set properties to StartEvent
     * @param targetEvent target event
     * @param startEventInstance instance
     * @param startEventPropertiesMap
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws InstantiationException
     */
    private void setStartEventProperties(final Event targetEvent, final E startEventInstance, final LinkedTreeMap startEventPropertiesMap) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
        Field[] fields = startEventInstance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (Objects.isNull(startEventPropertiesMap.get(field.getName()))) {
                log.warn("startEvent get field is null！，bizId:{},fieldName:{}", targetEvent.getBizId(), field.getName());
                continue;
            }
            if (field.getType() == String.class) {
                Class getObjectType = PropertyUtils.getPropertyType(startEventInstance, field.getName());
                Object getObjectInstance = getObjectType.newInstance();
                PropertyUtils.copyProperties(getObjectInstance, startEventPropertiesMap.get(field.getName()));

                BeanUtils.setProperty(startEventInstance, field.getName(), getObjectInstance);
            } else if (field.getType().equals(Boolean.class) || field.getType() == boolean.class) {
                BeanUtils.setProperty(startEventInstance, field.getName(), Boolean.valueOf(startEventPropertiesMap.get(field.getName()).toString()));
            } else if (field.getType().equals(Short.class) || field.getType() == short.class) {
                BeanUtils.setProperty(startEventInstance, field.getName(), Short.valueOf(startEventPropertiesMap.get(field.getName()).toString()));
            } else if (field.getType().equals(Long.class) || field.getType().equals(long.class)) {
                BeanUtils.setProperty(startEventInstance, field.getName(), Long.valueOf(startEventPropertiesMap.get(field.getName()).toString()));
            } else if (field.getType().equals(Float.class) || field.getType() == float.class) {
                BeanUtils.setProperty(startEventInstance, field.getName(), Float.valueOf(startEventPropertiesMap.get(field.getName()).toString()));
            } else if (field.getType().equals(Integer.class) || field.getType() == int.class) {
                BeanUtils.setProperty(startEventInstance, field.getName(), Integer.valueOf(startEventPropertiesMap.get(field.getName()).toString()));
            } else if (field.getType().equals(Byte.class) || field.getType() == byte.class) {
                BeanUtils.setProperty(startEventInstance, field.getName(), Byte.valueOf(startEventPropertiesMap.get(field.getName()).toString()));
            } else if (field.getType().equals(Character.class) || field.getType() == char.class) {
                throw new RuntimeException("the StartEvent parameter type must be String!, error fieldName:"+field.getName());
            } else if (field.getType().equals(Double.class) || field.getType() == double.class) {
                BeanUtils.setProperty(startEventInstance, field.getName(), Double.valueOf(startEventPropertiesMap.get(field.getName()).toString()));
            } else if (field.getType().isEnum()) {
                BeanUtils.setProperty(startEventInstance, field.getName(), field.getType().getEnumConstants()[0]);
            } else if (Object.class.isAssignableFrom(field.getType())) {
                //map里面嵌套对象的赋值
                LinkedTreeMap getObjectMap = (LinkedTreeMap) startEventPropertiesMap.get(field.getName());
                Class getObjectType = PropertyUtils.getPropertyType(startEventInstance, field.getName());
                Object getObjectInstance = getObjectType.newInstance();
                BeanUtils.populate(getObjectInstance, getObjectMap);

                BeanUtils.setProperty(startEventInstance, field.getName(), getObjectInstance);
                targetEvent.addParameter(Constants.START_EVENT, startEventInstance);
            }

        }
    }

    /**
     *
     * @param eventName Interrupt event name
     * @param bizId business id
     * @param events continue events
     * @return
     */
    private List<ManyFlowEventTreeNode> getTreeNodesBySuspendFlowInfo(final String eventName, final String bizId, final List events) {
        List<ManyFlowEventTreeNode> treeNodes = new ArrayList<>();

        //treeNodes is selectEventFlowDB.eventFlow.jsonToBean(Event)
        if (!Objects.isNull(events)) {
            treeNodes.addAll(events);
        }

        boolean isContinue = false;
        List<ManyFlowEventTreeNode> tmpNodes = new ArrayList<>();
        for (ManyFlowEventTreeNode treeNode : treeNodes){
            if (eventName.equals(treeNode.getEventTreeNode().getParentName()) ){
                isContinue = true;
            }
            if (isContinue){
                tmpNodes.add(treeNode);
            }
        }
        return tmpNodes;
    }

    private Event getEventBySuspendQry(String suspendEventName, String bizId) {
        EventFlowInfoBySuspendQry eventFlowInfoBySuspendQry = new EventFlowInfoBySuspendQry();
        eventFlowInfoBySuspendQry.setBizId(bizId);
        eventFlowInfoBySuspendQry.setEventName(suspendEventName);
        SingleResponse eventFlowInfoResponse = (SingleResponse) commandBus.send(eventFlowInfoBySuspendQry);

        Event event = null;
        if (eventFlowInfoResponse!=null && eventFlowInfoResponse.isSuccess()) {
            String eventFlowInfo = eventFlowInfoResponse.getData().toString();
            event = FlowEventEngine.jsonToEvent(eventFlowInfo);
        }
        return event;
    }

    /**
     * Event construction of interrupt scenarios
     * @param sourceEvent
     * @param continueEvents
     * @return
     */
    private Event buildEventByInterrupt(E sourceEvent, List<ManyFlowEventTreeNode> continueEvents){
        Event event = new Event(continueEvents);
        event.setBizId(sourceEvent.getBizId());
        String thisClassName = sourceEvent.getClass().getName();
        String eventName = thisClassName.substring(thisClassName.lastIndexOf(".")+1);
        event.setEventName(eventName);
        event.addParameter(eventName, sourceEvent);
        event.setTraceFlowId(sourceEvent.getTraceFlowId());
        event.setAsync(sourceEvent.isAsync());
        return event;
    }
}
