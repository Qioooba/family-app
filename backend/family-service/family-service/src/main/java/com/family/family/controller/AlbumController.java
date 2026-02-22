
package com.family.family.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;import com.family.common.core.Result;
import com.family.family.entity.AlbumPhoto;
import com.family.family.entity.AlbumShare;
import com.family.family.entity.FamilyAlbum;
import com.family.family.entity.PhotoTag;
import com.family.family.service.AlbumService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 相册控制器
 */
@RestController
@SaCheckLogin
@RequestMapping("/family/album")
public class AlbumController {
    
    private final AlbumService albumService;
    
    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }
    
    // ========== 相册管理 ==========
    
    @PostMapping
    public Result<FamilyAlbum> createAlbum(@RequestBody FamilyAlbum album) {
        Long userId = StpUtil.getLoginIdAsLong();
        album.setCreatorId(userId);
        return Result.success(albumService.createAlbum(album));
    }
    
    @PutMapping("/{id}")
    public Result<FamilyAlbum> updateAlbum(@PathVariable Long id, @RequestBody FamilyAlbum album) {
        album.setId(id);
        return Result.success(albumService.updateAlbum(album));
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteAlbum(@PathVariable Long id) {
        albumService.deleteAlbum(id);
        return Result.success();
    }
    
    @GetMapping("/{id}")
    public Result<FamilyAlbum> getAlbumById(@PathVariable Long id) {
        return Result.success(albumService.getAlbumById(id));
    }
    
    @GetMapping("/family/{familyId}")
    public Result<List<FamilyAlbum>> getFamilyAlbums(@PathVariable Long familyId) {
        return Result.success(albumService.getFamilyAlbums(familyId));
    }
    
    // ========== 时光轴 ==========
    
    @GetMapping("/timeline/{familyId}")
    public Result<List<FamilyAlbum>> getTimelineAlbums(@PathVariable Long familyId) {
        return Result.success(albumService.getTimelineAlbums(familyId));
    }
    
    @GetMapping("/timeline/photos/{familyId}")
    public Result<List<AlbumPhoto>> getTimelinePhotos(
            @PathVariable Long familyId,
            @RequestParam(required = false) String yearMonth) {
        return Result.success(albumService.getTimelinePhotos(familyId, yearMonth));
    }
    
    // ========== 照片管理 ==========
    
    @PostMapping("/photo")
    public Result<AlbumPhoto> addPhoto(@RequestBody AlbumPhoto photo) {
        Long userId = StpUtil.getLoginIdAsLong();
        photo.setUploaderId(userId);
        return Result.success(albumService.addPhoto(photo));
    }
    
    @DeleteMapping("/photo/{id}")
    public Result<Void> deletePhoto(@PathVariable Long id) {
        albumService.deletePhoto(id);
        return Result.success();
    }
    
    @GetMapping("/photo/{id}")
    public Result<AlbumPhoto> getPhotoById(@PathVariable Long id) {
        return Result.success(albumService.getPhotoById(id));
    }
    
    @GetMapping("/{albumId}/photos")
    public Result<List<AlbumPhoto>> getAlbumPhotos(@PathVariable Long albumId) {
        return Result.success(albumService.getAlbumPhotos(albumId));
    }
    
    @PostMapping("/photo/{id}/favorite")
    public Result<Void> favoritePhoto(@PathVariable Long id, @RequestParam Integer isFavorite) {
        albumService.favoritePhoto(id, isFavorite);
        return Result.success();
    }
    
    @PutMapping("/photo/{id}/tags")
    public Result<Void> updatePhotoTags(@PathVariable Long id, @RequestParam String tags) {
        albumService.updatePhotoTags(id, tags);
        return Result.success();
    }
    
    // ========== 智能分类 - 照片标签 ==========
    
    @PostMapping("/photo/tag")
    public Result<PhotoTag> addPhotoTag(@RequestBody PhotoTag tag) {
        Long userId = StpUtil.getLoginIdAsLong();
        tag.setUserId(userId);
        return Result.success(albumService.addPhotoTag(tag));
    }
    
    @DeleteMapping("/photo/tag/{tagId}")
    public Result<Void> deletePhotoTag(@PathVariable Long tagId) {
        albumService.deletePhotoTag(tagId);
        return Result.success();
    }
    
    @GetMapping("/photo/{photoId}/tags")
    public Result<List<PhotoTag>> getPhotoTags(@PathVariable Long photoId) {
        return Result.success(albumService.getPhotoTags(photoId));
    }
    
    @GetMapping("/tags/family/{familyId}")
    public Result<List<String>> getFamilyTagNames(@PathVariable Long familyId) {
        return Result.success(albumService.getFamilyTagNames(familyId));
    }
    
    @GetMapping("/photos/by-tag")
    public Result<List<PhotoTag>> getPhotosByTag(
            @RequestParam Long familyId,
            @RequestParam String tagName) {
        return Result.success(albumService.getPhotosByTag(familyId, tagName));
    }
    
    @GetMapping("/photos/by-tag-type")
    public Result<List<PhotoTag>> getPhotosByTagType(
            @RequestParam Long familyId,
            @RequestParam Integer tagType) {
        return Result.success(albumService.getPhotosByTagType(familyId, tagType));
    }
    
    @PostMapping("/photo/{photoId}/tags/batch")
    public Result<Void> batchAddTags(
            @PathVariable Long photoId,
            @RequestParam List<String> tagNames,
            @RequestParam Integer tagType,
            @RequestParam Long familyId) {
        Long userId = StpUtil.getLoginIdAsLong();
        albumService.batchAddTags(photoId, tagNames, tagType, userId, familyId);
        return Result.success();
    }
    
    // ========== 共享相册 ==========
    
    @PostMapping("/share")
    public Result<AlbumShare> createShare(@RequestBody AlbumShare share) {
        Long userId = StpUtil.getLoginIdAsLong();
        share.setSharedBy(userId);
        return Result.success(albumService.createShare(share));
    }
    
    @DeleteMapping("/share/{id}")
    public Result<Void> cancelShare(@PathVariable Long id) {
        albumService.cancelShare(id);
        return Result.success();
    }
    
    @GetMapping("/share/code/{shareCode}")
    public Result<AlbumShare> getShareByCode(@PathVariable String shareCode) {
        return Result.success(albumService.getShareByCode(shareCode));
    }
    
    @GetMapping("/{albumId}/shares")
    public Result<List<AlbumShare>> getAlbumShares(@PathVariable Long albumId) {
        return Result.success(albumService.getAlbumShares(albumId));
    }
}
