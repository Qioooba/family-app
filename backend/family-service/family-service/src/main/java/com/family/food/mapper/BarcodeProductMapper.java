package com.family.food.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.food.entity.BarcodeProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BarcodeProductMapper extends BaseMapper<BarcodeProduct> {
    
    /**
     * 增加查询次数
     */
    @Update("UPDATE barcode_product SET query_count = query_count + 1 WHERE id = #{id}")
    void incrementQueryCount(@Param("id") Long id);
}
