package com.library.controller;

import com.library.model.dto.BookListItemDTO;
import com.library.model.dto.PagedResultDTO;
import com.library.model.dto.WishlistItemDTO;
import com.library.model.dto.response.MessageResponse;
import com.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class WishlistController {

    private final UserService userService;

    @Autowired
    public WishlistController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取用户收藏列表（修复类型不匹配问题）
     */
    @GetMapping("/wishlist")
    public ResponseEntity<PagedResultDTO<WishlistItemDTO>> getWishlist(
            @RequestAttribute("currentUser") UserDetails currentUser,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {

        // 调用用户服务获取收藏列表（返回类型为WishlistItemDTO的分页结果）
        @SuppressWarnings("unchecked")
        PagedResultDTO<WishlistItemDTO> wishlist = (PagedResultDTO<WishlistItemDTO>) userService
                .getUserWishlist(Long.valueOf(currentUser.getUsername()), page, limit)
                .get("pagedResult");

        return ResponseEntity.ok(wishlist);
    }

    /**
     * 添加图书到收藏
     */
    @PostMapping("/wishlist/{bookId}")
    public ResponseEntity<MessageResponse> addToWishlist(
            @RequestAttribute("currentUser") UserDetails currentUser,
            @PathVariable Long bookId) {

        userService.addToWishlist(Long.valueOf(currentUser.getUsername()), bookId);
        return ResponseEntity.ok(MessageResponse.success("添加收藏成功"));
    }

    /**
     * 从收藏中移除图书
     */
    @DeleteMapping("/wishlist/{bookId}")
    public ResponseEntity<MessageResponse> removeFromWishlist(
            @RequestAttribute("currentUser") UserDetails currentUser,
            @PathVariable Long bookId) {

        userService.removeFromWishlist(Long.valueOf(currentUser.getUsername()), bookId);
        return ResponseEntity.ok(MessageResponse.success("移除收藏成功"));
    }
}