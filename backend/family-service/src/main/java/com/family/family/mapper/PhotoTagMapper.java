package com.family.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.family.entity.PhotoTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PhotoTagMapper extends BaseMapper<PhotoTag> {
    
    /**
     * 根据照片ID查询标签
     */
    @Select("SELECT id, photo_id, family_id, tag_name, tag_type, user_id, status, create_time, update_time FROM photo_tag WHERE photo_id = #{photoId} AND status = 1")
    List<PhotoTag> selectByPhotoId(@Param("photoId") Long photoId);
    
    /**
     * 根据家庭ID查询所有标签
     */
    @Select("SELECT DISTINCT tag_name FROM photo_tag WHERE family_id = #{familyId} AND status = 1")
    List<String> selectTagNamesByFamilyId(@Param("familyId") Long familyId);
    
    /**
     * 根据标签类型查询标签
     */
    @Select("SELECT id, photo_id, family_id, tag_name, tag_type, user_id, status, create_time, update_time FROM photo_tag WHERE family_id = #{familyId} AND tag_type = #{tagType} AND status = 1")
    List<PhotoTag> selectByTagType(@Param("familyId") Long familyId, @Param("tagType") Integer tagType);
}
