package com.library.service.impl;

import com.library.exception.ApiError;
import com.library.model.dto.BookDetailDTO;
import com.library.model.dto.BookListItemDTO;
import com.library.model.dto.PagedResultDTO;
import com.library.model.dto.response.PriceCalculationResult;
import com.library.repository.BookRepository;
import com.library.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private static final int DEFAULT_LIMIT = 10;
    private static final String DEFAULT_SORT = "recommended";
    private static final String DEFAULT_COVER_IMAGE = "/images/default-book.jpg";
    private static final String DEFAULT_DESCRIPTION = "暂无内容简介";

    /**
     * 获取所有图书（支持分页、筛选、排序和搜索）
     */
    @Override
    @Transactional(readOnly = true)
    public PagedResultDTO<BookListItemDTO> getBooks(
            String keyword,
            Long categoryId,
            String sort,
            Integer page,
            Integer limit) {

        validatePageParams(page, limit);
        int offset = calculateOffset(page, limit);
        String sortOrder = Objects.requireNonNullElse(sort, DEFAULT_SORT);

        List<Map<String, Object>> books = bookRepository.findBooks(
                keyword,
                categoryId,
                sortOrder,
                limit,
                offset
        );

        Long total = bookRepository.countBooks(keyword, categoryId);
        List<BookListItemDTO> bookList = convertToBookListItemList(books);

        return new PagedResultDTO<>(
                bookList,
                total,
                page,
                limit
        );
    }

    /**
     * 获取图书详情（基础信息）
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getBookById(Long id) {
        validateId(id);
        return bookRepository.getBookById(id)
                .orElseThrow(() -> new ApiError(404, "图书不存在: ID=" + id));
    }

    /**
     * 获取热门图书
     */
    @Override
    @Transactional(readOnly = true)
    public List<BookListItemDTO> getHotBooks(Integer limit) {
        int queryLimit = Objects.requireNonNullElse(limit, DEFAULT_LIMIT);
        List<Map<String, Object>> books = bookRepository.getHotBooks(queryLimit);
        return convertToBookListItemList(books);
    }

    /**
     * 获取新书上架
     */
    @Override
    @Transactional(readOnly = true)
    public List<BookListItemDTO> getNewBooks(Integer limit) {
        int queryLimit = Objects.requireNonNullElse(limit, DEFAULT_LIMIT);
        List<Map<String, Object>> books = bookRepository.getNewBooks(queryLimit);
        return convertToBookListItemList(books);
    }

    /**
     * 根据分类获取图书
     */
    @Override
    @Transactional(readOnly = true)
    public PagedResultDTO<BookListItemDTO> getBooksByCategory(
            Long categoryId,
            Integer page,
            Integer limit,
            String sort) {

        validateCategoryParams(categoryId, page, limit);
        int offset = calculateOffset(page, limit);
        String querySort = Objects.requireNonNullElse(sort, DEFAULT_SORT);

        List<Map<String, Object>> books = bookRepository.findBooks(
                null,
                categoryId,
                querySort,
                limit,
                offset
        );

        Long total = bookRepository.countBooks(null, categoryId);
        List<BookListItemDTO> bookList = convertToBookListItemList(books);

        return new PagedResultDTO<>(
                bookList,
                total,
                page,
                limit
        );
    }

    /**
     * 获取相关推荐书本
     */
    @Override
    @Transactional(readOnly = true)
    public List<BookListItemDTO> getRelatedBooks(Long bookId) {
        validateId(bookId);

        List<Map<String, Object>> relatedBooks = bookRepository.getRelatedBooks(bookId);

        if (relatedBooks.isEmpty()) {
            Map<String, Object> book = bookRepository.getBookById(bookId)
                    .orElseThrow(() -> new ApiError(404, "图书不存在: ID=" + bookId));

            if (book.get("category_id") != null) {
                Long categoryId = ((Number) book.get("category_id")).longValue();
                relatedBooks = bookRepository.getBooksByCategoryExclude(categoryId, bookId, 4);
            }
        }

        return convertToBookListItemList(relatedBooks);
    }

    /**
     * 获取图书详情（包含价格计算和相关推荐）
     */
    @Override
    @Transactional(readOnly = true)
    public BookDetailDTO getBookDetail(Long id) {
        validateId(id);

        Map<String, Object> book = bookRepository.getBookById(id)
                .orElseThrow(() -> new ApiError(404, "图书不存在: ID=" + id));

        PriceCalculationResult priceResult = calculatePrice(book);
        Long categoryId = ((Number) book.get("category_id")).longValue();
        PagedResultDTO<BookListItemDTO> relatedBooks = getBooksByCategory(categoryId, 1, 4, DEFAULT_SORT);

        return buildBookDetailDTO(book, priceResult, relatedBooks.getItems());
    }

    /**
     * 转换数据库结果列表为图书列表项DTO列表
     */
    private List<BookListItemDTO> convertToBookListItemList(List<Map<String, Object>> bookMaps) {
        return bookMaps.stream()
                .map(this::convertToBookListItem)
                .collect(Collectors.toList());
    }

    /**
     * 转换单个数据库结果为图书列表项DTO
     */
    private BookListItemDTO convertToBookListItem(Map<String, Object> bookMap) {
        BookListItemDTO item = new BookListItemDTO();
        item.setId(((Number) bookMap.get("id")).longValue());
        item.setTitle((String) bookMap.get("title"));
        item.setAuthor((String) bookMap.get("author"));
        item.setCoverImage((String) bookMap.get("cover_image"));
        item.setPrice(parseBigDecimal(bookMap.get("selling_price")));
        item.setOriginalPrice(parseBigDecimal(bookMap.get("original_price")));
        item.setRating(parseBigDecimal(bookMap.get("rating")));
        return item;
    }

    /**
     * 计算图书最终售价、原价及折扣率
     */
    private PriceCalculationResult calculatePrice(Map<String, Object> book) {
        BigDecimal sellingPrice = parseBigDecimal(book.get("selling_price"));
        BigDecimal originalPrice = parseBigDecimal(book.get("original_price"));
        BigDecimal basePrice = parseBigDecimal(book.get("price"));

        BigDecimal price = sellingPrice != null ? sellingPrice :
                (basePrice != null ? basePrice : BigDecimal.ZERO);

        if (originalPrice == null) {
            originalPrice = price;
        }

        int discount = calculateDiscount(price, originalPrice);

        return new PriceCalculationResult(price, originalPrice, discount);
    }

    /**
     * 计算折扣率（售价/原价*100，四舍五入取整）
     */
    private int calculateDiscount(BigDecimal price, BigDecimal originalPrice) {
        if (originalPrice.compareTo(BigDecimal.ZERO) <= 0
                || price.compareTo(BigDecimal.ZERO) <= 0
                || price.compareTo(originalPrice) >= 0) {
            return 0;
        }

        return price.divide(originalPrice, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }

    /**
     * 构建图书详情DTO
     */
    private BookDetailDTO buildBookDetailDTO(
            Map<String, Object> book,
            PriceCalculationResult priceResult,
            List<BookListItemDTO> relatedBooks) {

        BookDetailDTO detail = new BookDetailDTO();
        detail.setId(((Number) book.get("id")).longValue());
        detail.setTitle((String) book.get("title"));
        detail.setAuthor((String) book.get("author"));
        detail.setCoverImage(Objects.requireNonNullElse((String) book.get("cover_image"), DEFAULT_COVER_IMAGE));
        detail.setPrice(priceResult.getPrice());
        detail.setOriginalPrice(priceResult.getOriginalPrice());
        detail.setDiscount(priceResult.getDiscount());
        detail.setRating(Objects.requireNonNullElse(parseBigDecimal(book.get("rating")), BigDecimal.ZERO));
        detail.setReviews(book.get("review_count") != null ? ((Number) book.get("review_count")).intValue() : 0);
        detail.setDescription(Objects.requireNonNullElse((String) book.get("description"), DEFAULT_DESCRIPTION));
        detail.setCategory((String) book.get("category_name"));
        detail.setStock(book.get("stock_quantity") != null ? ((Number) book.get("stock_quantity")).intValue() : 0);
        detail.setIsAvailable(detail.getStock() > 0);
        detail.setRelatedBooks(relatedBooks);

        return detail;
    }

    /**
     * 计算分页偏移量（(页码-1)*每页条数）
     */
    private int calculateOffset(Integer page, Integer limit) {
        return (page - 1) * limit;
    }

    /**
     * 验证图书ID有效性（非null且>0）
     */
    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new ApiError(400, "无效的图书ID: " + id);
        }
    }

    /**
     * 验证分页参数有效性
     */
    private void validatePageParams(Integer page, Integer limit) {
        if (page == null || page < 1) {
            throw new ApiError(400, "页码必须大于0");
        }
        if (limit == null || limit < 1 || limit > 100) {
            throw new ApiError(400, "每页条数必须在1-100之间");
        }
    }

    /**
     * 验证分类查询参数有效性
     */
    private void validateCategoryParams(Long categoryId, Integer page, Integer limit) {
        validateId(categoryId);
        validatePageParams(page, limit);
    }

    /**
     * 安全解析BigDecimal
     */
    private BigDecimal parseBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return new BigDecimal(((Number) value).toString());
        }
        if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }
}