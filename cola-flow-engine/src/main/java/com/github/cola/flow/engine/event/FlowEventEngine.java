package com.github.cola.flow.engine.event;

import com.github.cola.flow.client.event.FlowFlowBaseEvent;
import com.github.cola.flow.engine.event.tree.EventTreeConvert;
import com.github.cola.flow.engine.event.tree.FlowEventTreeNode;
import com.github.cola.flow.engine.event.tree.ManyFlowEventTreeNode;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.github.cola.flow.client.constants.Constants;
import com.github.cola.flow.client.dto.event.EventEntity;
import com.github.cola.flow.client.dto.event.Event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Project Name: cola-flow
 * Desc:
 *
 * @author xihadoufuche@aliyun.com
 */
public class FlowEventEngine<ET extends EventEntity, C extends FlowFlowBaseEvent> {
    static GsonBuilder gsonBuilder = new GsonBuilder();

    /**
     *
     * @param event
     * @param current
     * @throws Exception
     */
    public static void init(final Event event, final ManyFlowEventTreeNode current) throws Exception {
        List<ManyFlowEventTreeNode> exeNodes = new ArrayList<>();
        exeNodes.add(current);
        FlowEventController.newInstance(event).handle(exeNodes);
    }

    /**
     * Continue the process from the pause node specified by the input parameter
     * @param event event
     * @throws Exception exception
     */
    public static void push(final Event event) throws Exception {

        String eventNodeName = event.getEventName();

        List<ManyFlowEventTreeNode> exeNodes = new ArrayList<>();

        List<ManyFlowEventTreeNode> unHandleNode = event.getEvents();


        Iterator<ManyFlowEventTreeNode> iterator = unHandleNode.iterator();
        while (iterator.hasNext()) {
            ManyFlowEventTreeNode node = iterator.next();

            String[] parentNames = node.getEventTreeNode().getParentName().split(Constants.STRING_SPLIT);
            List<String> parentNameList = Lists.newArrayList(parentNames);
            //如果父节点包含 eventNodeName则就是待执行节点
            if (parentNameList.contains(eventNodeName)) {
                exeNodes.add(node);
                iterator.remove();
            }
        }

        if (exeNodes.size()>0) {
            FlowEventController.newInstance(event).handle(exeNodes);
        }
    }

    /**
     * 事件流json转为对象
     * @param text
     * @return
     */
    static Event jsonToEvent(String text, Map parameterMap, Map<String,String> eventClassMap, Map eventMap, FlowFlowBaseEvent startEvent) {
        Gson gson = gsonBuilder.create();
        Event event = gson.fromJson(text, Event.class);
        String eventsJson = gson.toJson(event.getEvents());
        List<ManyFlowEventTreeNode> entices =
                gson.fromJson(eventsJson,
                        new TypeToken<List<ManyFlowEventTreeNode>>() {
                        }.getType());
        event.setEvents(entices);

        event.setEventMap(eventMap);
        event.setParameterMap(parameterMap);
        event.setEventClassMap(eventClassMap);
        event.setStartEvent(startEvent);
        return event;
    }

    /**
     * event转换为树
     * @param treeMap
     * @param eventName
     * @return
     * @throws Exception
     */
    public static List<ManyFlowEventTreeNode> convertTree(final Map<String,ManyFlowEventTreeNode> treeMap, String eventName) throws Exception{
        EventTreeConvert tree = new EventTreeConvert(eventName);
        List<ManyFlowEventTreeNode> eventTree = tree.getEventTree(treeMap);
        return eventTree;
    }

    public static Map<String,ManyFlowEventTreeNode> convertTreeMap(final List<FlowEventTreeNode> eventTreeNodes, String eventName) throws InterruptedException {
        EventTreeConvert tree = new EventTreeConvert(eventName);
        EventTreeConvert manyTreeNode = tree.createTree(eventTreeNodes);

        Map<String,ManyFlowEventTreeNode> treeMap = tree.iteratorTreeByBreadthFirst(manyTreeNode.getRoot());
        return treeMap;
    }

    /**
     * 事件流json转为对象
     * @param text
     * @return
     */
    public static Event jsonToEvent(String text) {
        Gson gson = gsonBuilder.create();
        Event event = gson.fromJson(text, Event.class);
        Map parameterMap = event.getParameterMap();
        Map<String,String> eventClassMap = event.getEventClassMap();

        String eventsJson = gson.toJson(event.getEvents());
        String eventMapJson = gson.toJson(event.getEventMap());
        List<ManyFlowEventTreeNode> entices =
                gson.fromJson(eventsJson,
                        new TypeToken<List<ManyFlowEventTreeNode>>() {
                        }.getType());
        event.setEvents(entices);

        Map<String, ManyFlowEventTreeNode> eventMap =
                gson.fromJson(eventMapJson,
                        new TypeToken<Map<String, ManyFlowEventTreeNode>>() {
                        }.getType());
        event.setEventMap(eventMap);
        event.setParameterMap(parameterMap);
        event.setEventClassMap(eventClassMap);
        return event;
    }

}
