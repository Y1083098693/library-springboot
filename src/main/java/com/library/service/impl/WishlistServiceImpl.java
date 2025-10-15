package com.library.service.impl;

import com.library.model.dto.PagedResultDTO;
import com.library.model.dto.WishlistItemDTO;
import com.library.model.entity.Wishlist;
import com.library.repository.BookRepository;
import com.library.repository.WishlistRepository;
import com.library.service.WishlistService;
import com.library.exception.BadRequestException;
import com.library.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final BookRepository bookRepository; // 用于验证图书是否存在

    @Override
    @Transactional
    public WishlistItemDTO addToWishlist(Long userId, Long bookId) {
        // 1. 验证图书是否存在
        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException("图书不存在: ID=" + bookId);
        }

        // 2. 检查是否已收藏
        if (wishlistRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new BadRequestException("该图书已在收藏列表中");
        }

        // 3. 创建收藏记录
        Wishlist wishlist = new Wishlist();
        wishlist.setUserId(userId);
        wishlist.setBookId(bookId);
        Wishlist saved = wishlistRepository.save(wishlist);

        // 4. 构建返回DTO（补充图书信息）
        return buildWishlistItemDTO(saved);
    }

    @Override
    @Transactional
    public void removeFromWishlist(Long userId, Long bookId) {
        // 1. 验证收藏是否存在
        if (!wishlistRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new ResourceNotFoundException("收藏记录不存在");
        }

        // 2. 删除收藏
        int affectedRows = wishlistRepository.deleteByUserIdAndBookId(userId, bookId);
        if (affectedRows == 0) {
            throw new BadRequestException("删除收藏失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResultDTO<WishlistItemDTO> getUserWishlist(Long userId, int page, int limit) {
        // 1. 计算分页偏移量
        int offset = (page - 1) * limit;

        // 2. 查询收藏列表和总数
        List<Object[]> wishlistItems = wishlistRepository.findUserWishlist(userId, limit, offset);
        long total = wishlistRepository.countByUserId(userId);

        // 3. 转换为DTO列表
        List<WishlistItemDTO> items = wishlistItems.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // 4. 构建分页结果
        return new PagedResultDTO<>(items, total, page, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBookInWishlist(Long userId, Long bookId) {
        return wishlistRepository.existsByUserIdAndBookId(userId, bookId);
    }

    // ------------------------------ 私有工具方法 ------------------------------

    /**
     * 将数据库查询结果转换为DTO
     */
    private WishlistItemDTO convertToDTO(Object[] item) {
        WishlistItemDTO dto = new WishlistItemDTO();
        dto.setId(((Number) item[0]).longValue()); // 图书ID
        dto.setTitle((String) item[1]); // 图书标题
        dto.setAuthor((String) item[2]); // 作者
        dto.setCoverImage((String) item[3]); // 封面图
        dto.setPrice(parseBigDecimal(item[4])); // 价格
        dto.setAddedAt(((LocalDateTime) item[5]).toString()); // 收藏时间
        return dto;
    }

    /**
     * 构建收藏项DTO（用于新增收藏时）
     */
    private WishlistItemDTO buildWishlistItemDTO(Wishlist wishlist) {
        // 查询图书信息补充DTO
        return bookRepository.getBookById(wishlist.getBookId())
                .map(bookMap -> {
                    WishlistItemDTO dto = new WishlistItemDTO();
                    dto.setId(wishlist.getBookId());
                    dto.setTitle((String) bookMap.get("title"));
                    dto.setAuthor((String) bookMap.get("author"));
                    dto.setCoverImage((String) bookMap.get("cover_image"));
                    dto.setPrice(parseBigDecimal(bookMap.get("selling_price")));
                    dto.setAddedAt(wishlist.getCreatedAt().toString());
                    return dto;
                })
                .orElseThrow(() -> new ResourceNotFoundException("图书信息不存在"));
    }

    /**
     * 安全解析BigDecimal
     */
    private BigDecimal parseBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof Number) {
            return new BigDecimal(value.toString());
        }
        return BigDecimal.ZERO;
    }
}