package com.family.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.health.entity.WeightRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 体重记录Mapper
 */
@Mapper
public interface WeightRecordMapper extends BaseMapper<WeightRecord> {
}
