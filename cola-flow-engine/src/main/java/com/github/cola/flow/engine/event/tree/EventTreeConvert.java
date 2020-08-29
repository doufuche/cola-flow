package com.github.cola.flow.engine.event.tree;
import com.github.cola.flow.client.constants.Constants;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Project Name: cola-flow
 * Desc: EventTree转换
 *
 * @author xihadoufuche@aliyun.com
 */
public class EventTreeConvert {

    /** 树根*/
    private ManyFlowEventTreeNode root;

    /**
     * 构造函数
     */
    public EventTreeConvert(String parentName)
    {
        root = new ManyFlowEventTreeNode(new FlowEventTreeNode(parentName));
    }

    /**
     * 生成一颗多叉树，根节点为root
     *
     * @param eventTreeNodes 生成多叉树的节点集合
     * @return EventTreeConvert
     */
    public EventTreeConvert createTree(List<FlowEventTreeNode> eventTreeNodes)
    {
        if (eventTreeNodes == null || eventTreeNodes.size() < 0) {
            return null;
        }

        EventTreeConvert eventTreeConvert =  new EventTreeConvert(root.getEventTreeNode().getName());


        for (FlowEventTreeNode eventTreeNode : eventTreeNodes)
        {
            if (eventTreeNode.getParentName().equals(root.getEventTreeNode().getName()))
            {
                eventTreeConvert.getRoot().getChildListManyTreeNode().add(new ManyFlowEventTreeNode(eventTreeNode));
            }
            else
            {
                addChild(eventTreeConvert.getRoot(), eventTreeNode);
            }
        }

        return eventTreeConvert;
    }

    /**
     * 向指定多叉树节点添加子节点
     *
     * @param manyEventTreeNode 多叉树节点
     * @param child 节点
     */
    private void addChild(ManyFlowEventTreeNode manyEventTreeNode, FlowEventTreeNode child)
    {
        for (ManyFlowEventTreeNode item : manyEventTreeNode.getChildListManyTreeNode())
        {
            if (item.getEventTreeNode().getName().equals(child.getParentName()))
            {

                item.getChildListManyTreeNode().add(new ManyFlowEventTreeNode(child));
                break;
            }
            else
            {
                if (item.getChildListManyTreeNode() != null && item.getChildListManyTreeNode().size() > 0)
                {
                    addChild(item, child);
                }
            }
        }
    }


    /**
     * 遍历多叉树，广度优先
     * @param manyEventTreeNode 多叉树节点
     * @return
     * @throws InterruptedException
     */
    public Map<String,ManyFlowEventTreeNode> iteratorTreeByBreadthFirst(ManyFlowEventTreeNode manyEventTreeNode) throws InterruptedException {
        Map<String,ManyFlowEventTreeNode> result = new LinkedHashMap<>(16,new Float(0.75),true);
        BlockingQueue<ManyFlowEventTreeNode> stack = new LinkedBlockingQueue<>();
        List<ManyFlowEventTreeNode> treeNodeList = manyEventTreeNode.getChildListManyTreeNode();
        for (int i = 0; i<treeNodeList.size(); i++) {
            stack.add(treeNodeList.get(i));
        }

        ManyFlowEventTreeNode item = null;
        while (stack.size()>0) {
            item = stack.take();
            if (result.get(item.getEventTreeNode().getName())!=null){
                ManyFlowEventTreeNode tempManyEventTreeNode = item;
                String parentNames = result.get(item.getEventTreeNode().getName()).getEventTreeNode().getParentName() + Constants.STRING_SPLIT + tempManyEventTreeNode.getEventTreeNode().getParentName();
                FlowEventTreeNode eventTreeNode = tempManyEventTreeNode.getEventTreeNode();
                eventTreeNode.setParentName(parentNames);
                tempManyEventTreeNode.setEventTreeNode(eventTreeNode);
                result.put(item.getEventTreeNode().getName(), tempManyEventTreeNode);
            }else{
                result.put(item.getEventTreeNode().getName(),item);
            }

            if (item.getChildListManyTreeNode()!=null && item.getChildListManyTreeNode().size()>0) {
                stack.addAll(item.getChildListManyTreeNode());
            }
        }
        return result;
    }

    /**
     * 获取event的tree结构数据
     * @param manyEventTreeNode
     * @return
     * @throws InterruptedException
     */
    public List<ManyFlowEventTreeNode> getEventTree(ManyFlowEventTreeNode manyEventTreeNode) throws InterruptedException {
        List<ManyFlowEventTreeNode> result = new ArrayList<>();
        Map<String,ManyFlowEventTreeNode> treeMap = iteratorTreeByBreadthFirst(manyEventTreeNode);

        Iterator<Map.Entry<String,ManyFlowEventTreeNode>> iterator = treeMap.entrySet().iterator();
        while(iterator.hasNext()){
            result.add(iterator.next().getValue());
        }
        return  result;
    }

    /**
     * 获取event的tree结构数据
     * @param treeMap
     * @return
     * @throws InterruptedException
     */
    public List<ManyFlowEventTreeNode> getEventTree(Map<String,ManyFlowEventTreeNode> treeMap) throws InterruptedException {
        List<ManyFlowEventTreeNode> result = new ArrayList<>();

        Iterator<Map.Entry<String,ManyFlowEventTreeNode>> iterator = treeMap.entrySet().iterator();
        while(iterator.hasNext()){
            result.add(iterator.next().getValue());
        }
        return  result;
    }

    public ManyFlowEventTreeNode getRoot() {
        return root;
    }

    public void setRoot(ManyFlowEventTreeNode root) {
        this.root = root;
    }

}
