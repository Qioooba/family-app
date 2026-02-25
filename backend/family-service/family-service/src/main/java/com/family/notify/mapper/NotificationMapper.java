package com.family.notify.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 通知Mapper
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
    
    /**
     * 获取用户未读通知数
     */
    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = 0 AND deleted = 0")
    Long selectUnreadCount(@Param("userId") Long userId);
    
    /**
     * 获取用户各类型未读通知数
     */
    @Select("SELECT type, COUNT(*) as count FROM notification WHERE user_id = #{userId} AND is_read = 0 AND deleted = 0 GROUP BY type")
    List<TypeCount> selectUnreadCountByType(@Param("userId") Long userId);
    
    /**
     * 标记所有通知为已读
     */
    @Update("UPDATE notification SET is_read = 1, read_time = NOW() WHERE user_id = #{userId} AND is_read = 0")
    int markAllAsRead(@Param("userId") Long userId);
    
    /**
     * 类型统计内部类
     */
    class TypeCount {
        private Integer type;
        private Long count;
        
        public Integer getType() { return type; }
        public void setType(Integer type) { this.type = type; }
        public Long getCount() { return count; }
        public void setCount(Long count) { this.count = count; }
    }
}
