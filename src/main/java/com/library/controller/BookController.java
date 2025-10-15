package com.library.controller;

import com.library.model.dto.BookDetailDTO;
import com.library.model.dto.BookListItemDTO;
import com.library.model.dto.PagedResultDTO;
import com.library.model.dto.response.ApiResponse;
import com.library.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "图书管理", description = "图书查询、详情、推荐等相关接口")
public class BookController {

    private final BookService bookService;

    /**
     * 多条件查询图书（支持分页、搜索、分类筛选、排序）
     */
    @GetMapping
    @Operation(summary = "查询图书列表", description = "支持关键词搜索、分类筛选、分页及多种排序方式")
    public ResponseEntity<ApiResponse<PagedResultDTO<BookListItemDTO>>> getBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {

        PagedResultDTO<BookListItemDTO> result = bookService.getBooks(keyword, categoryId, sort, page, limit);
        return ResponseEntity.ok(new ApiResponse<>(true, "图书列表获取成功", result));
    }

    /**
     * 获取图书详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取图书详情", description = "查询指定ID的图书完整信息，包含价格、库存及相关推荐")
    public ResponseEntity<ApiResponse<BookDetailDTO>> getBookDetail(@PathVariable Long id) {
        BookDetailDTO detail = bookService.getBookDetail(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "图书详情获取成功", detail));
    }

    /**
     * 获取热门图书
     */
    @GetMapping("/hot")
    @Operation(summary = "获取热门图书", description = "查询销量/热度排名靠前的图书列表")
    public ResponseEntity<ApiResponse<List<BookListItemDTO>>> getHotBooks(
            @RequestParam(required = false) Integer limit) {

        List<BookListItemDTO> hotBooks = bookService.getHotBooks(limit);
        return ResponseEntity.ok(new ApiResponse<>(true, "热门图书获取成功", hotBooks));
    }

    /**
     * 获取新书上架
     */
    @GetMapping("/new")
    @Operation(summary = "获取新书上架", description = "查询最新上架的图书列表")
    public ResponseEntity<ApiResponse<List<BookListItemDTO>>> getNewBooks(
            @RequestParam(required = false) Integer limit) {

        List<BookListItemDTO> newBooks = bookService.getNewBooks(limit);
        return ResponseEntity.ok(new ApiResponse<>(true, "新书上架获取成功", newBooks));
    }

    /**
     * 根据分类查询图书
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "按分类查询图书", description = "查询指定分类下的图书，支持分页和排序")
    public ResponseEntity<ApiResponse<PagedResultDTO<BookListItemDTO>>> getBooksByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String sort) {

        PagedResultDTO<BookListItemDTO> result = bookService.getBooksByCategory(categoryId, page, limit, sort);
        return ResponseEntity.ok(new ApiResponse<>(true, "分类图书获取成功", result));
    }

    /**
     * 获取相关推荐图书
     */
    @GetMapping("/{id}/related")
    @Operation(summary = "获取相关推荐图书", description = "查询与指定图书相关的推荐图书列表")
    public ResponseEntity<ApiResponse<List<BookListItemDTO>>> getRelatedBooks(@PathVariable Long id) {
        List<BookListItemDTO> relatedBooks = bookService.getRelatedBooks(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "相关图书推荐获取成功", relatedBooks));
    }
}