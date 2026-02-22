package com.family.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.message.entity.MessageTemplate;
import org.apache.ibatis.annotations.Select;

/**
 * 消息模板Mapper
 */
public interface MessageTemplateMapper extends BaseMapper<MessageTemplate> {
    
    /**
     * 根据编码查询模板
     */
    @Select("SELECT * FROM message_template WHERE code = #{code} AND is_enabled = 1")
    MessageTemplate selectByCode(String code);
}
