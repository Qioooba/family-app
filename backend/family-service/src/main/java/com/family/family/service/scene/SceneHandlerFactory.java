package com.family.family.service.scene;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 场景处理器工厂
 */
@Component
public class SceneHandlerFactory {
    
    @Autowired
    private List<SceneReminderHandler> handlers;
    
    private Map<String, SceneReminderHandler> handlerMap = new HashMap<>();
    
    @PostConstruct
    public void init() {
        for (SceneReminderHandler handler : handlers) {
            handlerMap.put(handler.getSceneType(), handler);
        }
    }
    
    /**
     * 获取场景处理器
     * @param sceneType 场景类型
     * @return 处理器，不存在返回null
     */
    public SceneReminderHandler getHandler(String sceneType) {
        return handlerMap.get(sceneType);
    }
    
    /**
     * 获取所有处理器
     * @return 处理器列表
     */
    public List<SceneReminderHandler> getAllHandlers() {
        return handlers;
    }
    
    /**
     * 判断是否存在该场景处理器
     * @param sceneType 场景类型
     * @return true-存在
     */
    public boolean hasHandler(String sceneType) {
        return handlerMap.containsKey(sceneType);
    }
}
