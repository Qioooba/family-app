package com.family.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.ShoppingList;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物清单Mapper
 */
@Mapper
public interface ShoppingListMapper extends BaseMapper<ShoppingList> {
}
