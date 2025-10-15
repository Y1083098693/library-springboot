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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    // 注入所有需要的Repository（包括新增的OrderItemRepository）
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final BookRepository bookRepository;
    private final UserAddressRepository userAddressRepository;

    /**
     * 创建订单（修复：逻辑顺序、变量定义、订单项保存）
     */
    @Transactional
    public OrderDTO createOrder(Long userId, CreateOrderRequest request) {
        // 1. 验证收货地址
        boolean addressExists = userAddressRepository.existsByIdAndUserId(request.getAddressId(), userId);
        if (!addressExists) {
            throw new ResourceNotFoundException("收货地址不存在");
        }

        // 2. 验证商品库存并计算总金额（修复：新增totalAmount定义，避免未定义报错）
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (var item : request.getItems()) {
            // 修复：用Optional接收book，避免Null
            Map<String, Object> book = bookRepository.getBookStock(item.getBookId())
                    .orElseThrow(() -> new ResourceNotFoundException("图书不存在: ID=" + item.getBookId()));

            // 验证库存
            int stock = ((Number) book.get("stock_quantity")).intValue();
            if (stock < item.getQuantity()) {
                throw new BadRequestException("图书库存不足: " + book.get("title"));
            }

            // 累加总金额
            BigDecimal price = parseBigDecimal(book.get("selling_price"));
            totalAmount = totalAmount.add(price.multiply(new BigDecimal(item.getQuantity())));
        }

        // 3. 创建并保存订单（修复：先计算金额再创建订单）
        Order order = new Order();
        order.setUserId(userId);
        order.setAddressId(request.getAddressId());
        order.setTotalAmount(totalAmount); // 已定义的totalAmount
        order.setFinalAmount(totalAmount); // 暂不考虑优惠
        order.setStatus(Order.OrderStatus.PENDING); // 枚举状态
        order.setPaymentMethod(request.getPaymentMethod()); // 支付方式
        Order savedOrder = orderRepository.save(order); // JPA原生save，自动获取ID
        Long orderId = savedOrder.getId();

        // 4. 创建并保存订单项（修复：用OrderItemRepository保存，符合规范）
        for (var item : request.getItems()) {
            Map<String, Object> book = bookRepository.getBookById(item.getBookId())
                    .orElseThrow(() -> new ResourceNotFoundException("图书不存在: ID=" + item.getBookId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(orderId);
            orderItem.setBookId(item.getBookId());
            orderItem.setBookTitle((String) book.get("title"));
            orderItem.setBookCover((String) book.get("cover_image"));
            orderItem.setQuantity(item.getQuantity());
            orderItem.setUnitPrice(parseBigDecimal(book.get("selling_price"))); // 原生BigDecimal

            orderItemRepository.save(orderItem); // 用订单项专属Repository保存
        }

        // 5. 扣减库存（修复：移到订单项创建后，逻辑更合理）
        for (var item : request.getItems()) {
            int affectedRows = bookRepository.decreaseStock(item.getBookId(), item.getQuantity());
            if (affectedRows == 0) {
                throw new BadRequestException("扣减库存失败，可能库存不足");
            }
        }

        // 6. 返回订单详情
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

        int offset = (page - 1) * limit;
        // 状态转大写（匹配数据库枚举存储：PENDING/CANCELLED等）
        String statusUpper = "all".equals(status) ? "all" : status.toUpperCase();

        // 查询订单列表和总数
        List<Map<String, Object>> orders = orderRepository.findUserOrders(userId, statusUpper, limit, offset);
        long total = orderRepository.countUserOrders(userId, statusUpper);

        // 转换为DTO
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
        // 查询订单基本信息
        Map<String, Object> orderMap = orderRepository.findOrderById(userId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("订单不存在或不属于当前用户"));
        OrderDTO orderDTO = convertToOrderDTO(orderMap);

        // 查询关联的订单项
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
        // 验证订单存在且归属当前用户
        Map<String, Object> orderMap = orderRepository.findOrderById(userId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("订单不存在或不属于当前用户"));

        // 验证订单状态（只能取消待支付订单）
        String currentStatus = (String) orderMap.get("status");
        if (!Order.OrderStatus.PENDING.name().equals(currentStatus)) {
            throw new BadRequestException("只能取消待支付订单，当前状态：" + currentStatus);
        }

        // 更新订单状态为取消
        orderRepository.updateOrderStatus(orderId, Order.OrderStatus.CANCELLED.name());

        // 恢复库存
        List<Map<String, Object>> itemMaps = orderItemRepository.findByOrderId(orderId);
        for (var itemMap : itemMaps) {
            Long bookId = ((Number) itemMap.get("book_id")).longValue();
            Integer quantity = ((Number) itemMap.get("quantity")).intValue();
            bookRepository.increaseStock(bookId, quantity);
        }
    }

    /**
     * 确认收货
     */
    @Transactional
    public void confirmReceipt(Long userId, Long orderId) {
        // 验证订单存在且归属当前用户
        Map<String, Object> orderMap = orderRepository.findOrderById(userId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("订单不存在或不属于当前用户"));

        // 验证订单状态（只能确认已发货订单）
        String currentStatus = (String) orderMap.get("status");
        if (!Order.OrderStatus.SHIPPED.name().equals(currentStatus)) {
            throw new BadRequestException("只能确认已发货订单，当前状态：" + currentStatus);
        }

        // 更新订单状态为已完成
        orderRepository.updateOrderStatus(orderId, Order.OrderStatus.COMPLETED.name());
    }

    /**
     * 订单实体转DTO
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
     * 订单项实体转DTO
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
     * 安全解析BigDecimal（修复：删除重复方法，保留一个）
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