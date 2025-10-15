package com.library.service;

import com.library.model.dto.OrderDTO;
import com.library.model.dto.OrderItemDTO;
import com.library.model.dto.PagedResultDTO;
import com.library.model.dto.request.CreateOrderRequest;
import com.library.model.entity.Order;
import com.library.model.entity.OrderItem;
import com.library.repository.OrderItemRepository;
import com.library.repository.OrderRepository;
import com.library.repository.BookRepository;
import com.library.repository.UserAddressRepository;
import com.library.exception.ResourceNotFoundException;
import com.library.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final BookRepository bookRepository;
    private final UserAddressRepository userAddressRepository;
    private static final int MAX_ORDER_ITEMS = 10; // 订单最大商品数量限制
    private static final int MAX_QUANTITY_PER_ITEM = 5; // 单商品最大购买数量限制

    /**
     * 创建订单（完善校验与业务逻辑）
     */
    @Transactional
    public OrderDTO createOrder(Long userId, CreateOrderRequest request) {
        // 1. 基础参数校验
        validateCreateOrderRequest(request);

        // 2. 验证收货地址归属
        boolean addressExists = userAddressRepository.existsByIdAndUserId(request.getAddressId(), userId);
        if (!addressExists) {
            throw new ResourceNotFoundException("收货地址不存在或不属于当前用户");
        }

        // 3. 验证商品库存并计算总金额
        BigDecimal totalAmount = calculateTotalAmountAndValidateStock(request.getItems());

        // 4. 创建并保存订单
        Order order = buildOrder(userId, request, totalAmount);
        Order savedOrder = orderRepository.save(order);
        Long orderId = savedOrder.getId();

        // 5. 创建并保存订单项
        saveOrderItems(orderId, request.getItems());

        // 6. 扣减库存
        deductBookStock(request.getItems());

        // 7. 返回订单详情
        return getOrderById(userId, orderId);
    }

    /**
     * 获取用户订单列表（分页）
     */
    @Transactional(readOnly = true)
    public PagedResultDTO<OrderDTO> getUserOrders(
            Long userId,
            String status,
            Integer page,
            Integer limit) {

        validatePageParams(page, limit);
        int offset = calculateOffset(page, limit);
        String statusUpper = "all".equalsIgnoreCase(status) ? "all" :
                Objects.requireNonNullElse(status, "all").toUpperCase();

        List<Map<String, Object>> orders = orderRepository.findUserOrders(userId, statusUpper, limit, offset);
        long total = orderRepository.countUserOrders(userId, statusUpper);

        List<OrderDTO> orderDTOs = orders.stream()
                .map(this::convertToOrderDTO)
                .collect(Collectors.toList());

        return new PagedResultDTO<>(orderDTOs, total, page, limit);
    }

    /**
     * 获取订单详情
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long userId, Long orderId) {
        validateId(orderId);

        Map<String, Object> orderMap = orderRepository.findOrderById(userId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("订单不存在或不属于当前用户"));
        OrderDTO orderDTO = convertToOrderDTO(orderMap);

        List<Map<String, Object>> itemMaps = orderItemRepository.findByOrderId(orderId);
        List<OrderItemDTO> itemDTOs = itemMaps.stream()
                .map(this::convertToOrderItemDTO)
                .collect(Collectors.toList());
        orderDTO.setItems(itemDTOs);

        return orderDTO;
    }

    /**
     * 取消订单
     */
    @Transactional
    public void cancelOrder(Long userId, Long orderId) {
        validateId(orderId);

        Map<String, Object> orderMap = orderRepository.findOrderById(userId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("订单不存在或不属于当前用户"));

        String currentStatus = (String) orderMap.get("status");
        if (!Order.OrderStatus.PENDING.name().equals(currentStatus)) {
            throw new BadRequestException("只能取消待支付订单，当前状态：" + currentStatus);
        }

        // 更新订单状态为取消
        orderRepository.updateOrderStatus(orderId, Order.OrderStatus.CANCELLED.name());

        // 恢复库存
        restoreBookStock(orderId);
    }

    /**
     * 确认收货
     */
    @Transactional
    public void confirmReceipt(Long userId, Long orderId) {
        validateId(orderId);

        Map<String, Object> orderMap = orderRepository.findOrderById(userId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("订单不存在或不属于当前用户"));

        String currentStatus = (String) orderMap.get("status");
        if (!Order.OrderStatus.SHIPPED.name().equals(currentStatus)) {
            throw new BadRequestException("只能确认已发货订单，当前状态：" + currentStatus);
        }

        orderRepository.updateOrderStatus(orderId, Order.OrderStatus.COMPLETED.name());
    }

    /**
     * 支付订单（新增方法）
     */
    @Transactional
    public void payOrder(Long userId, Long orderId) {
        validateId(orderId);

        Map<String, Object> orderMap = orderRepository.findOrderById(userId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("订单不存在或不属于当前用户"));

        String currentStatus = (String) orderMap.get("status");
        if (!Order.OrderStatus.PENDING.name().equals(currentStatus)) {
            throw new BadRequestException("只能支付待支付订单，当前状态：" + currentStatus);
        }

        orderRepository.updateOrderStatus(orderId, Order.OrderStatus.PAID.name());
    }

    /**
     * 验证创建订单请求参数
     */
    private void validateCreateOrderRequest(CreateOrderRequest request) {
        if (request.getAddressId() == null || request.getAddressId() < 1) {
            throw new BadRequestException("收货地址ID无效");
        }
        if (request.getPaymentMethod() == null || request.getPaymentMethod().trim().isEmpty()) {
            throw new BadRequestException("支付方式不能为空");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("订单商品不能为空");
        }
        if (request.getItems().size() > MAX_ORDER_ITEMS) {
            throw new BadRequestException("订单商品数量不能超过" + MAX_ORDER_ITEMS + "个");
        }
        request.getItems().forEach(item -> {
            if (item.getBookId() == null || item.getBookId() < 1) {
                throw new BadRequestException("商品ID无效");
            }
            if (item.getQuantity() == null || item.getQuantity() < 1 || item.getQuantity() > MAX_QUANTITY_PER_ITEM) {
                throw new BadRequestException("商品数量必须在1-" + MAX_QUANTITY_PER_ITEM + "之间");
            }
        });
    }

    /**
     * 计算总金额并验证库存
     */
    private BigDecimal calculateTotalAmountAndValidateStock(List<CreateOrderRequest.OrderItemRequest> items) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (var item : items) {
            Map<String, Object> book = bookRepository.getBookStock(item.getBookId())
                    .orElseThrow(() -> new ResourceNotFoundException("图书不存在: ID=" + item.getBookId()));

            int stock = ((Number) book.get("stock_quantity")).intValue();
            if (stock < item.getQuantity()) {
                throw new BadRequestException("图书库存不足: " + book.get("title") + "，当前库存: " + stock);
            }

            BigDecimal price = parseBigDecimal(book.get("selling_price"));
            totalAmount = totalAmount.add(price.multiply(new BigDecimal(item.getQuantity())));
        }
        return totalAmount;
    }

    /**
     * 构建订单实体
     */
    private Order buildOrder(Long userId, CreateOrderRequest request, BigDecimal totalAmount) {
        Order order = new Order();
        order.setUserId(userId);
        order.setAddressId(request.getAddressId());
        order.setTotalAmount(totalAmount);
        order.setFinalAmount(totalAmount); // 暂不考虑优惠
        order.setStatus(Order.OrderStatus.PENDING);
        order.setPaymentMethod(request.getPaymentMethod());
        return order;
    }

    /**
     * 保存订单项
     */
    private void saveOrderItems(Long orderId, List<CreateOrderRequest.OrderItemRequest> items) {
        for (var item : items) {
            Map<String, Object> book = bookRepository.getBookById(item.getBookId())
                    .orElseThrow(() -> new ResourceNotFoundException("图书不存在: ID=" + item.getBookId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(orderId);
            orderItem.setBookId(item.getBookId());
            orderItem.setBookTitle((String) book.get("title"));
            orderItem.setBookCover((String) book.get("cover_image"));
            orderItem.setQuantity(item.getQuantity());
            orderItem.setUnitPrice(parseBigDecimal(book.get("selling_price")));

            orderItemRepository.save(orderItem);
        }
    }

    /**
     * 扣减库存
     */
    private void deductBookStock(List<CreateOrderRequest.OrderItemRequest> items) {
        for (var item : items) {
            int affectedRows = bookRepository.decreaseStock(item.getBookId(), item.getQuantity());
            if (affectedRows == 0) {
                throw new BadRequestException("扣减库存失败，可能库存不足");
            }
        }
    }

    /**
     * 恢复库存（取消订单时）
     */
    private void restoreBookStock(Long orderId) {
        List<Map<String, Object>> itemMaps = orderItemRepository.findByOrderId(orderId);
        for (var itemMap : itemMaps) {
            Long bookId = ((Number) itemMap.get("book_id")).longValue();
            Integer quantity = ((Number) itemMap.get("quantity")).intValue();
            bookRepository.increaseStock(bookId, quantity);
        }
    }

    /**
     * 转换为OrderDTO
     */
    private OrderDTO convertToOrderDTO(Map<String, Object> orderMap) {
        OrderDTO dto = new OrderDTO();
        dto.setId(((Number) orderMap.get("id")).longValue());
        dto.setOrderNo((String) orderMap.get("order_no"));
        dto.setStatus((String) orderMap.get("status"));
        dto.setTotalAmount(parseBigDecimal(orderMap.get("total_amount")));
        dto.setFinalAmount(parseBigDecimal(orderMap.get("final_amount")));
        dto.setPaymentMethod((String) orderMap.get("payment_method"));
        dto.setAddressName((String) orderMap.get("address_name"));
        dto.setAddressPhone((String) orderMap.get("address_phone"));
        dto.setAddressDetail((String) orderMap.get("address_detail"));
        dto.setCreatedAt(((LocalDateTime) orderMap.get("created_at")).toString());
        return dto;
    }

    /**
     * 转换为OrderItemDTO
     */
    private OrderItemDTO convertToOrderItemDTO(Map<String, Object> itemMap) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(((Number) itemMap.get("id")).longValue());
        dto.setBookId(((Number) itemMap.get("book_id")).longValue());
        dto.setBookTitle((String) itemMap.get("book_title"));
        dto.setBookCover((String) itemMap.get("book_cover"));
        dto.setQuantity(((Number) itemMap.get("quantity")).intValue());
        dto.setUnitPrice(parseBigDecimal(itemMap.get("unit_price")));
        return dto;
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

    /**
     * 验证分页参数
     */
    private void validatePageParams(Integer page, Integer limit) {
        if (page == null || page < 1) {
            throw new BadRequestException("页码必须大于等于1");
        }
        if (limit == null || limit < 1 || limit > 50) {
            throw new BadRequestException("每页条数必须在1-50之间");
        }
    }

    /**
     * 计算分页偏移量
     */
    private int calculateOffset(Integer page, Integer limit) {
        return (page - 1) * limit;
    }

    /**
     * 验证ID有效性
     */
    private void validateId(Long id) {
        if (id == null || id < 1) {
            throw new BadRequestException("ID必须大于等于1");
        }
    }
}