package com.family.family.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.family.family.entity.AlbumPhoto;
import com.family.family.entity.AlbumShare;
import com.family.family.entity.FamilyAlbum;
import com.family.family.entity.PhotoTag;
import com.family.family.mapper.AlbumPhotoMapper;
import com.family.family.mapper.AlbumShareMapper;
import com.family.family.mapper.FamilyAlbumMapper;
import com.family.family.mapper.PhotoTagMapper;
import com.family.family.service.AlbumService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 相册服务实现
 */
@Service
public class AlbumServiceImpl implements AlbumService {
    
    private final FamilyAlbumMapper familyAlbumMapper;
    private final AlbumPhotoMapper albumPhotoMapper;
    private final AlbumShareMapper albumShareMapper;
    private final PhotoTagMapper photoTagMapper;
    
    public AlbumServiceImpl(FamilyAlbumMapper familyAlbumMapper, 
                           AlbumPhotoMapper albumPhotoMapper,
                           AlbumShareMapper albumShareMapper,
                           PhotoTagMapper photoTagMapper) {
        this.familyAlbumMapper = familyAlbumMapper;
        this.albumPhotoMapper = albumPhotoMapper;
        this.albumShareMapper = albumShareMapper;
        this.photoTagMapper = photoTagMapper;
    }
    
    @Override
    public FamilyAlbum createAlbum(FamilyAlbum album) {
        album.setPhotoCount(0);
        album.setStatus(1);
        album.setCreateTime(LocalDateTime.now());
        album.setUpdateTime(LocalDateTime.now());
        familyAlbumMapper.insert(album);
        return album;
    }
    
    @Override
    public FamilyAlbum updateAlbum(FamilyAlbum album) {
        album.setUpdateTime(LocalDateTime.now());
        familyAlbumMapper.updateById(album);
        return album;
    }
    
    @Override
    public void deleteAlbum(Long albumId) {
        FamilyAlbum album = familyAlbumMapper.selectById(albumId);
        if (album != null) {
            album.setStatus(0);
            album.setUpdateTime(LocalDateTime.now());
            familyAlbumMapper.updateById(album);
        }
    }
    
    @Override
    public FamilyAlbum getAlbumById(Long albumId) {
        return familyAlbumMapper.selectById(albumId);
    }
    
    @Override
    public List<FamilyAlbum> getFamilyAlbums(Long familyId) {
        QueryWrapper<FamilyAlbum> wrapper = new QueryWrapper<>();
        wrapper.eq("family_id", familyId)
               .eq("status", 1)
               .orderByAsc("sort_order");
        return familyAlbumMapper.selectList(wrapper);
    }
    
    @Override
    public List<FamilyAlbum> getTimelineAlbums(Long familyId) {
        QueryWrapper<FamilyAlbum> wrapper = new QueryWrapper<>();
        wrapper.eq("family_id", familyId)
               .eq("type", 2)
               .eq("status", 1);
        return familyAlbumMapper.selectList(wrapper);
    }
    
    @Override
    public AlbumPhoto addPhoto(AlbumPhoto photo) {
        photo.setStatus(1);
        photo.setCreateTime(LocalDateTime.now());
        photo.setUpdateTime(LocalDateTime.now());
        albumPhotoMapper.insert(photo);
        
        // 更新相册照片数
        FamilyAlbum album = familyAlbumMapper.selectById(photo.getAlbumId());
        if (album != null) {
            album.setPhotoCount(album.getPhotoCount() + 1);
            album.setUpdateTime(LocalDateTime.now());
            familyAlbumMapper.updateById(album);
        }
        
        return photo;
    }
    
    @Override
    public void deletePhoto(Long photoId) {
        AlbumPhoto photo = albumPhotoMapper.selectById(photoId);
        if (photo != null) {
            photo.setStatus(0);
            photo.setUpdateTime(LocalDateTime.now());
            albumPhotoMapper.updateById(photo);
            
            // 更新相册照片数
            FamilyAlbum album = familyAlbumMapper.selectById(photo.getAlbumId());
            if (album != null && album.getPhotoCount() > 0) {
                album.setPhotoCount(album.getPhotoCount() - 1);
                album.setUpdateTime(LocalDateTime.now());
                familyAlbumMapper.updateById(album);
            }
        }
    }
    
    @Override
    public AlbumPhoto getPhotoById(Long photoId) {
        return albumPhotoMapper.selectById(photoId);
    }
    
    @Override
    public List<AlbumPhoto> getAlbumPhotos(Long albumId) {
        QueryWrapper<AlbumPhoto> wrapper = new QueryWrapper<>();
        wrapper.eq("album_id", albumId)
               .eq("status", 1)
               .orderByDesc("photo_date");
        return albumPhotoMapper.selectList(wrapper);
    }
    
    @Override
    public List<AlbumPhoto> getTimelinePhotos(Long familyId, String yearMonth) {
        QueryWrapper<AlbumPhoto> wrapper = new QueryWrapper<>();
        wrapper.eq("family_id", familyId)
               .eq("status", 1);
        if (yearMonth != null) {
            wrapper.likeRight("photo_date", yearMonth);
        }
        wrapper.orderByDesc("photo_date");
        return albumPhotoMapper.selectList(wrapper);
    }
    
    @Override
    public void favoritePhoto(Long photoId, Integer isFavorite) {
        AlbumPhoto photo = albumPhotoMapper.selectById(photoId);
        if (photo != null) {
            photo.setIsFavorite(isFavorite);
            photo.setUpdateTime(LocalDateTime.now());
            albumPhotoMapper.updateById(photo);
        }
    }
    
    @Override
    public void updatePhotoTags(Long photoId, String tags) {
        AlbumPhoto photo = albumPhotoMapper.selectById(photoId);
        if (photo != null) {
            photo.setTags(tags);
            photo.setUpdateTime(LocalDateTime.now());
            albumPhotoMapper.updateById(photo);
        }
    }
    
    // ========== 智能分类 - 照片标签 ==========
    
    @Override
    public PhotoTag addPhotoTag(PhotoTag tag) {
        tag.setStatus(1);
        tag.setCreateTime(LocalDateTime.now());
        tag.setUpdateTime(LocalDateTime.now());
        photoTagMapper.insert(tag);
        return tag;
    }
    
    @Override
    public void deletePhotoTag(Long tagId) {
        PhotoTag tag = photoTagMapper.selectById(tagId);
        if (tag != null) {
            tag.setStatus(0);
            tag.setUpdateTime(LocalDateTime.now());
            photoTagMapper.updateById(tag);
        }
    }
    
    @Override
    public List<PhotoTag> getPhotoTags(Long photoId) {
        return photoTagMapper.selectByPhotoId(photoId);
    }
    
    @Override
    public List<String> getFamilyTagNames(Long familyId) {
        return photoTagMapper.selectTagNamesByFamilyId(familyId);
    }
    
    @Override
    public List<PhotoTag> getPhotosByTag(Long familyId, String tagName) {
        QueryWrapper<PhotoTag> wrapper = new QueryWrapper<>();
        wrapper.eq("family_id", familyId)
               .eq("tag_name", tagName)
               .eq("status", 1);
        return photoTagMapper.selectList(wrapper);
    }
    
    @Override
    public List<PhotoTag> getPhotosByTagType(Long familyId, Integer tagType) {
        return photoTagMapper.selectByTagType(familyId, tagType);
    }
    
    @Override
    public void batchAddTags(Long photoId, List<String> tagNames, Integer tagType, Long userId, Long familyId) {
        if (tagNames == null || tagNames.isEmpty()) {
            return;
        }
        for (String tagName : tagNames) {
            PhotoTag tag = new PhotoTag();
            tag.setPhotoId(photoId);
            tag.setFamilyId(familyId);
            tag.setTagName(tagName);
            tag.setTagType(tagType);
            tag.setUserId(userId);
            addPhotoTag(tag);
        }
    }
    
    // ========== 共享相册 ==========
    
    @Override
    public AlbumShare createShare(AlbumShare share) {
        share.setShareCode(UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        share.setViewCount(0);
        share.setDownloadCount(0);
        share.setStatus(1);
        share.setCreateTime(LocalDateTime.now());
        share.setUpdateTime(LocalDateTime.now());
        albumShareMapper.insert(share);
        return share;
    }
    
    @Override
    public void cancelShare(Long shareId) {
        AlbumShare share = albumShareMapper.selectById(shareId);
        if (share != null) {
            share.setStatus(0);
            share.setUpdateTime(LocalDateTime.now());
            albumShareMapper.updateById(share);
        }
    }
    
    @Override
    public AlbumShare getShareByCode(String shareCode) {
        QueryWrapper<AlbumShare> wrapper = new QueryWrapper<>();
        wrapper.eq("share_code", shareCode)
               .eq("status", 1);
        return albumShareMapper.selectOne(wrapper);
    }
    
    @Override
    public List<AlbumShare> getAlbumShares(Long albumId) {
        QueryWrapper<AlbumShare> wrapper = new QueryWrapper<>();
        wrapper.eq("album_id", albumId)
               .eq("status", 1);
        return albumShareMapper.selectList(wrapper);
    }
    
    @Override
    public void incrementViewCount(Long shareId) {
        AlbumShare share = albumShareMapper.selectById(shareId);
        if (share != null) {
            share.setViewCount(share.getViewCount() + 1);
            share.setUpdateTime(LocalDateTime.now());
            albumShareMapper.updateById(share);
        }
    }
    
    @Override
    public void incrementDownloadCount(Long shareId) {
        AlbumShare share = albumShareMapper.selectById(shareId);
        if (share != null) {
            share.setDownloadCount(share.getDownloadCount() + 1);
            share.setUpdateTime(LocalDateTime.now());
            albumShareMapper.updateById(share);
        }
    }
}
