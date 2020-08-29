package com.github.cola.flow.engine.event.tree;

import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Project Name: cola-flow
 * Desc:
 *
 * @author xihadoufuche@aliyun.com
 */
@Slf4j
public class EventTreeNodeConvert {
    static ObjectMapper om = new ObjectMapper();

    /**
     * ruleInfo转换为任务节点
     * @param ruleInfo
     * @return
     */
    public static List<FlowEventTreeNode> convertTreeNode(String ruleInfo) {
        List<FlowEventTreeNode> eventTreeNodes = new ArrayList<>();
        try {
            List<Map<String, Object>> itemList = om.readValue(ruleInfo, List.class);
            for (Map<String, Object> item : itemList) {
                Object id = item.get("id");
                Object pid = item.get("pid");
                Map<String, Object> ext = (Map<String, Object>) item.get("ext");
                eventTreeNodes.add(new FlowEventTreeNode(String.valueOf(id), String.valueOf(pid), ext == null ? null : om.writeValueAsString(ext)));
            }
        } catch (Exception e) {
            log.error("convertTreeNode error. ruleInfo:"+ruleInfo);
        }
        return eventTreeNodes;

    }

}
