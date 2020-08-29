package com.github.cola.flow.engine.event.tree;

import com.github.cola.flow.engine.event.FlowEventEntityAdapter;

/**
 * Project Name: cola-flow
 * Desc:
 *
 * @author xihadoufuche@aliyun.com
 */
public class FlowEventTreeNode extends FlowEventEntityAdapter
{
    /** 节点Id*/
    private String name;
    /** 父节点Id*/
    private String parentName;
    /** 扩展属性json串 */
    private String ext;
    /**
     * 构造函数
     *
     * @param name 节点Id
     */
    public FlowEventTreeNode(String name)
    {
        this.name = name;
    }

    /**
     * 构造函数
     *
     * @param name 节点Id
     * @param parentName 父节点Id
     */
    public FlowEventTreeNode(String name, String parentName, String ext)
    {
        this.name = name;
        this.parentName = parentName;
        this.ext = ext;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

}