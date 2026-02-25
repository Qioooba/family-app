package com.family.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.ShoppingItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物清单项Mapper
 */
@Mapper
public interface ShoppingItemMapper extends BaseMapper<ShoppingItem> {
}
