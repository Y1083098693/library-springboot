package com.library.service;

import com.library.model.dto.BookDetailDTO;
import com.library.model.dto.BookListItemDTO;
import com.library.model.dto.PagedResultDTO;
import com.library.model.dto.response.PriceCalculationResult;

import java.util.List;
import java.util.Map;

public interface BookService {

    /**
     * 获取所有图书（支持分页、筛选、排序和搜索）
     */
    PagedResultDTO<BookListItemDTO> getBooks(
            String keyword,
            Long categoryId,
            String sort,
            Integer page,
            Integer limit);

    /**
     * 获取图书详情（基础信息）
     */
    Map<String, Object> getBookById(Long id);

    /**
     * 获取热门图书
     */
    List<BookListItemDTO> getHotBooks(Integer limit);

    /**
     * 获取新书上架
     */
    List<BookListItemDTO> getNewBooks(Integer limit);

    /**
     * 根据分类获取图书
     */
    PagedResultDTO<BookListItemDTO> getBooksByCategory(
            Long categoryId,
            Integer page,
            Integer limit,
            String sort);

    /**
     * 获取相关推荐书本
     */
    List<BookListItemDTO> getRelatedBooks(Long bookId);

    /**
     * 获取图书详情（包含价格计算和相关推荐）
     */
    BookDetailDTO getBookDetail(Long id);
}