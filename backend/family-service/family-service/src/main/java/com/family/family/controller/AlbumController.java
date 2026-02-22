package com.family.family.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.family.common.core.Result;
import com.family.family.entity.AlbumPhoto;
import com.family.family.entity.AlbumShare;
import com.family.family.entity.FamilyAlbum;
import com.family.family.service.AlbumService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 相册控制器
 */
@RestController
@RequestMapping("/api/album")
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
    
    @GetMapping("/timeline/{familyId}")
    public Result<List<FamilyAlbum>> getTimelineAlbums(@PathVariable Long familyId) {
        return Result.success(albumService.getTimelineAlbums(familyId));
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
    
    @GetMapping("/timeline/photos/{familyId}")
    public Result<List<AlbumPhoto>> getTimelinePhotos(
            @PathVariable Long familyId,
            @RequestParam(required = false) String yearMonth) {
        return Result.success(albumService.getTimelinePhotos(familyId, yearMonth));
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
