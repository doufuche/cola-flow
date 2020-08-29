package com.github.cola.flow.engine.event.tree;

import com.github.cola.flow.engine.event.FlowEventEntityAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Project Name: cola-flow
 * Desc:
 *
 * @author xihadoufuche@aliyun.com
 */
public class ManyFlowEventTreeNode extends FlowEventEntityAdapter
{
    /** 树节点*/
    private FlowEventTreeNode eventTreeNode;
    /** 子树集合*/
    private List<ManyFlowEventTreeNode> childListManyTreeNode;

    /**
     * 构造函数
     *
     * @param eventTreeNode 树节点
     */
    public ManyFlowEventTreeNode(FlowEventTreeNode eventTreeNode)
    {
        this.eventTreeNode = eventTreeNode;
        this.childListManyTreeNode = new ArrayList<ManyFlowEventTreeNode>();
    }

    /**
     * 构造函数
     *
     * @param eventTreeNode 树节点
     * @param childListManyTreeNode 子树集合
     */
    public ManyFlowEventTreeNode(FlowEventTreeNode eventTreeNode, List<ManyFlowEventTreeNode> childListManyTreeNode)
    {
        this.eventTreeNode = eventTreeNode;
        this.childListManyTreeNode = childListManyTreeNode;
    }

    public FlowEventTreeNode getEventTreeNode() {
        return eventTreeNode;
    }

    public void setEventTreeNode(FlowEventTreeNode eventTreeNode) {
        this.eventTreeNode = eventTreeNode;
    }

    public List<ManyFlowEventTreeNode> getChildListManyTreeNode() {
        return childListManyTreeNode;
    }

    public void setChildListManyTreeNode(List<ManyFlowEventTreeNode> childListManyTreeNode) {
        this.childListManyTreeNode = childListManyTreeNode;
    }

    @Override
    public String toString() {
        return eventTreeNode.getName();
    }
}
