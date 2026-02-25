package com.family.family.service;

import com.family.family.entity.AlbumPhoto;
import com.family.family.entity.AlbumShare;
import com.family.family.entity.FamilyAlbum;
import com.family.family.entity.PhotoTag;

import java.util.List;

/**
 * 相册服务
 */
public interface AlbumService {
    
    // 相册管理
    FamilyAlbum createAlbum(FamilyAlbum album);
    FamilyAlbum updateAlbum(FamilyAlbum album);
    void deleteAlbum(Long albumId);
    FamilyAlbum getAlbumById(Long albumId);
    List<FamilyAlbum> getFamilyAlbums(Long familyId);
    List<FamilyAlbum> getTimelineAlbums(Long familyId);
    
    // 照片管理
    AlbumPhoto addPhoto(AlbumPhoto photo);
    void deletePhoto(Long photoId);
    AlbumPhoto getPhotoById(Long photoId);
    List<AlbumPhoto> getAlbumPhotos(Long albumId);
    List<AlbumPhoto> getTimelinePhotos(Long familyId, String yearMonth);
    void favoritePhoto(Long photoId, Integer isFavorite);
    void updatePhotoTags(Long photoId, String tags);
    
    // 智能分类 - 照片标签
    PhotoTag addPhotoTag(PhotoTag tag);
    void deletePhotoTag(Long tagId);
    List<PhotoTag> getPhotoTags(Long photoId);
    List<String> getFamilyTagNames(Long familyId);
    List<PhotoTag> getPhotosByTag(Long familyId, String tagName);
    List<PhotoTag> getPhotosByTagType(Long familyId, Integer tagType);
    void batchAddTags(Long photoId, List<String> tagNames, Integer tagType, Long userId, Long familyId);
    
    // 共享相册
    AlbumShare createShare(AlbumShare share);
    void cancelShare(Long shareId);
    AlbumShare getShareByCode(String shareCode);
    List<AlbumShare> getAlbumShares(Long albumId);
    void incrementViewCount(Long shareId);
    void incrementDownloadCount(Long shareId);
}
