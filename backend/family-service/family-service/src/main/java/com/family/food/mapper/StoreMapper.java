package com.family.food.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.Store;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StoreMapper extends BaseMapper<Store> {
    
    @Select("SELECT id, store_name, address, phone, latitude, longitude, category, status, create_time, update_time FROM store WHERE status = 1 AND (latitude BETWEEN #{latMin} AND #{latMax}) AND (longitude BETWEEN #{lngMin} AND #{lngMax})")
    List<Store> selectNearbyStores(@Param("latMin") Double latMin, @Param("latMax") Double latMax, 
                                   @Param("lngMin") Double lngMin, @Param("lngMax") Double lngMax);
}
