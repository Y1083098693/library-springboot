package com.library.service;

import com.library.model.dto.BookDetailDTO;
import com.library.model.dto.BookListItemDTO;
import com.library.model.dto.PagedResultDTO;
import com.library.exception.ApiError;
import com.library.repository.BookRepository;
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
public class BookService {

    private final BookRepository bookRepository;
    private static final int DEFAULT_LIMIT = 10;
    private static final String DEFAULT_SORT = "recommended";
    private static final String DEFAULT_COVER_IMAGE = "/images/default-book.jpg";
    private static final String DEFAULT_DESCRIPTION = "暂无内容简介";

    /**
     * 获取所有图书（支持分页、筛选、排序和搜索）
     */
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

        // 调用Repository的findBooks方法获取图书列表
        List<Map<String, Object>> books = bookRepository.findBooks(
                keyword,
                categoryId,
                sortOrder,
                limit,
                offset
        );

        // 调用countBooks方法获取总数
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
    @Transactional(readOnly = true)
    public Map<String, Object> getBookById(Long id) {
        validateId(id);
        // 处理Optional：存在则返回Map，不存在则抛404异常
        return bookRepository.getBookById(id)
                .orElseThrow(() -> new ApiError(404, "图书不存在: ID=" + id));
    }

    /**
     * 获取热门图书
     */
    @Transactional(readOnly = true)
    public List<BookListItemDTO> getHotBooks(Integer limit) {
        int queryLimit = Objects.requireNonNullElse(limit, DEFAULT_LIMIT);
        List<Map<String, Object>> books = bookRepository.getHotBooks(queryLimit);
        return convertToBookListItemList(books);
    }

    /**
     * 获取新书上架
     */
    @Transactional(readOnly = true)
    public List<BookListItemDTO> getNewBooks(Integer limit) {
        int queryLimit = Objects.requireNonNullElse(limit, DEFAULT_LIMIT);
        List<Map<String, Object>> books = bookRepository.getNewBooks(queryLimit);
        return convertToBookListItemList(books);
    }

    /**
     * 根据分类获取图书
     */
    @Transactional(readOnly = true)
    public PagedResultDTO<BookListItemDTO> getBooksByCategory(
            Long categoryId,
            Integer page,
            Integer limit,
            String sort) {

        validateCategoryParams(categoryId, page, limit);
        int offset = calculateOffset(page, limit);
        String querySort = Objects.requireNonNullElse(sort, DEFAULT_SORT);

        // 使用findBooks方法实现分类查询（关键字为null）
        List<Map<String, Object>> books = bookRepository.findBooks(
                null,
                categoryId,
                querySort,
                limit,
                offset
        );

        // 计算分类下图书总数
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
    @Transactional(readOnly = true)
    public List<BookListItemDTO> getRelatedBooks(Long bookId) {
        validateId(bookId);

        // 优先使用关联表查询相关图书
        List<Map<String, Object>> relatedBooks = bookRepository.getRelatedBooks(bookId);

        // 关联表无数据时，按分类推荐（排除当前图书）
        if (relatedBooks.isEmpty()) {
            // 处理Optional：确保获取到当前图书信息
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
    @Transactional(readOnly = true)
    public BookDetailDTO getBookDetail(Long id) {
        validateId(id);

        // 处理Optional：获取图书详情，不存在则抛404
        Map<String, Object> book = bookRepository.getBookById(id)
                .orElseThrow(() -> new ApiError(404, "图书不存在: ID=" + id));

        // 计算价格与折扣
        PriceCalculationResult priceResult = calculatePrice(book);

        // 获取同分类相关推荐（取第1页，4条数据）
        Long categoryId = ((Number) book.get("category_id")).longValue();
        PagedResultDTO<BookListItemDTO> relatedBooks = getBooksByCategory(categoryId, 1, 4, DEFAULT_SORT);

        // 构建并返回图书详情DTO
        return buildBookDetailDTO(book, priceResult, relatedBooks.getItems());
    }

    // ------------------------------ 私有工具方法 ------------------------------

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

        // 确定最终售价：优先用促销价，无促销价则用基础价，均无则为0
        BigDecimal price = sellingPrice != null ? sellingPrice :
                (basePrice != null ? basePrice : BigDecimal.ZERO);

        // 确定原价：无原价则与售价一致
        if (originalPrice == null) {
            originalPrice = price;
        }

        // 计算折扣率（整数，如85表示85折）
        int discount = calculateDiscount(price, originalPrice);

        return new PriceCalculationResult(price, originalPrice, discount);
    }

    /**
     * 计算折扣率（售价/原价*100，四舍五入取整）
     */
    private int calculateDiscount(BigDecimal price, BigDecimal originalPrice) {
        // 异常情况（原价为0、售价为0、售价≥原价）返回0（无折扣）
        if (originalPrice.compareTo(BigDecimal.ZERO) <= 0
                || price.compareTo(BigDecimal.ZERO) <= 0
                || price.compareTo(originalPrice) >= 0) {
            return 0;
        }

        // 计算折扣率并四舍五入取整
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
        // 封面图为空时用默认图
        detail.setCoverImage(Objects.requireNonNullElse((String) book.get("cover_image"), DEFAULT_COVER_IMAGE));
        detail.setPrice(priceResult.getPrice());
        detail.setOriginalPrice(priceResult.getOriginalPrice());
        detail.setDiscount(priceResult.getDiscount());
        // 评分为空时默认为0
        detail.setRating(Objects.requireNonNullElse(parseBigDecimal(book.get("rating")), BigDecimal.ZERO));
        // 评论数为空时默认为0
        detail.setReviews(book.get("review_count") != null ? ((Number) book.get("review_count")).intValue() : 0);
        // 描述为空时用默认描述
        detail.setDescription(Objects.requireNonNullElse((String) book.get("description"), DEFAULT_DESCRIPTION));
        detail.setCategory((String) book.get("category_name"));
        // 库存为空时默认为0
        detail.setStock(book.get("stock_quantity") != null ? ((Number) book.get("stock_quantity")).intValue() : 0);
        // 库存>0则为可购买状态
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
            throw new IllegalArgumentException("无效的图书ID：必须为正整数");
        }
    }

    /**
     * 验证分页参数有效性（页码和每页条数均为非null且>0）
     */
    private void validatePageParams(Integer page, Integer limit) {
        if (page == null || page <= 0 || limit == null || limit <= 0) {
            throw new IllegalArgumentException("无效的分页参数：页码和每页条数必须为正整数");
        }
    }

    /**
     * 验证分类查询参数有效性（分类ID非null且>0，分页参数有效）
     */
    private void validateCategoryParams(Long categoryId, Integer page, Integer limit) {
        if (categoryId == null || categoryId <= 0) {
            throw new IllegalArgumentException("无效的分类ID：必须为正整数");
        }
        validatePageParams(page, limit);
    }

    /**
     * 安全解析Object为BigDecimal（处理null和不同类型值）
     */
    private BigDecimal parseBigDecimal(Object value) {
        if (value == null) {
            return null;
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
                return null;
            }
        }
        return null;
    }

    /**
     * 内部类：封装价格计算结果（售价、原价、折扣率）
     */
    private static class PriceCalculationResult {
        private final BigDecimal price;
        private final BigDecimal originalPrice;
        private final int discount;

        public PriceCalculationResult(BigDecimal price, BigDecimal originalPrice, int discount) {
            this.price = price;
            this.originalPrice = originalPrice;
            this.discount = discount;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public BigDecimal getOriginalPrice() {
            return originalPrice;
        }

        public int getDiscount() {
            return discount;
        }
    }
}