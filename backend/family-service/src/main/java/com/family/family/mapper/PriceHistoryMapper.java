package com.family.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.PriceHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 价格历史Mapper
 */
@Mapper
public interface PriceHistoryMapper extends BaseMapper<PriceHistory> {
    
    /**
     * 查询商品最近的价格记录
     */
    @Select("SELECT id, item_name, barcode, price, store_name, purchase_date, family_id, create_time FROM price_history WHERE item_name = #{itemName} OR barcode = #{barcode} ORDER BY purchase_date DESC LIMIT 10")
    List<PriceHistory> selectRecentPrices(@Param("itemName") String itemName, @Param("barcode") String barcode);
}
