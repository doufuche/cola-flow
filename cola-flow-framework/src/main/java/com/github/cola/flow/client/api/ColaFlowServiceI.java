package com.github.cola.flow.client.api;

import com.github.cola.flow.client.dto.event.Event;
import com.github.cola.flow.client.event.FlowFlowBaseEvent;

/**
 * Project Name: cola-flow
 * Desc: event flow service interface
 *
 * @author xihadoufuche@aliyun.com
 */
public interface ColaFlowServiceI<E extends FlowFlowBaseEvent> {

    /**
     * Initialize and start the event flow
     * @param startEvent start and context
     * @param eventFlowInfo The event process chain of the business to be executed,The format is : [{"id":"StartEvent","pid":""},{"id":"EndEvent","pid":"StartEvent"}]
     * @throws Exception anything exception
     */
    void init(E startEvent, String eventFlowInfo) throws Exception;

    /**
     * Push the event to execute in the flow
     * @param event The eventName in the event represents the name of the node that has just been executed
     * @throws Exception anything exception
     */
    void push(Event event) throws Exception ;

    /**
     * Continue the process from the pause node specified by the input parameter
     * @param suspendEvent Event name must have suspendEvent suffix
     * @throws Exception anything exception
     */
    void continueEventFlow(E suspendEvent) throws Exception;

    /**
     * Continue the process from the exception node specified in the input parameter
     * @param errorEvent error event
     * @throws Exception anything exception
     */
    void errorFlowContinue(E errorEvent) throws Exception;
}
