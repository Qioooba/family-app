package com.family.family.service.scene;

import com.family.family.entity.Reminder;
import com.family.family.entity.User;

/**
 * 场景化提醒处理器接口
 */
public interface SceneReminderHandler {
    
    /**
     * 获取场景类型
     * @return 场景类型标识
     */
    String getSceneType();
    
    /**
     * 获取场景名称
     * @return 场景名称
     */
    String getSceneName();
    
    /**
     * 验证配置是否有效
     * @param businessData 业务数据JSON
     * @throws IllegalArgumentException 配置无效时抛出
     */
    void validateConfig(String businessData);
    
    /**
     * 检查当前是否应该触发提醒
     * @param reminder 提醒配置
     * @return true-应该触发
     */
    boolean shouldTrigger(Reminder reminder);
    
    /**
     * 渲染消息标题
     * @param reminder 提醒配置
     * @param user 目标用户
     * @return 渲染后的标题
     */
    String renderTitle(Reminder reminder, User user);
    
    /**
     * 渲染消息内容
     * @param reminder 提醒配置
     * @param user 目标用户
     * @return 渲染后的内容
     */
    String renderContent(Reminder reminder, User user);
    
    /**
     * 获取默认配置模板
     * @return 场景模板
     */
    SceneTemplate getDefaultTemplate();
    
    /**
     * 获取场景图标
     * @return 图标emoji或URL
     */
    default String getIcon() {
        return "🔔";
    }
    
    /**
     * 获取场景背景色
     * @return CSS渐变或颜色值
     */
    default String getBgColor() {
        return "linear-gradient(135deg, #667eea 0%, #764ba2 100%)";
    }
}
