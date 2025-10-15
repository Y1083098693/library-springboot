package com.library.service;

import com.library.model.dto.WishlistItemDTO;
import com.library.model.dto.PagedResultDTO;

/**
 * 收藏服务接口
 */
public interface WishlistService {

    /**
     * 添加图书到收藏
     */
    WishlistItemDTO addToWishlist(Long userId, Long bookId);

    /**
     * 从收藏中移除图书
     */
    void removeFromWishlist(Long userId, Long bookId);

    /**
     * 分页查询用户的收藏列表
     */
    PagedResultDTO<WishlistItemDTO> getUserWishlist(Long userId, int page, int limit);

    /**
     * 检查图书是否已收藏
     */
    boolean isBookInWishlist(Long userId, Long bookId);
}