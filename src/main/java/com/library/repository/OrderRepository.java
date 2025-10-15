package com.library.repository;

import com.library.model.entity.Order;
import com.library.model.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

// 注意：OrderRepository只管理Order实体，OrderItem需单独创建OrderItemRepository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 查询用户订单列表（分页）
     */
    @Query(value = "SELECT o.id, o.order_no, o.status, o.total_amount, o.final_amount, o.payment_method, " +
            "o.created_at, a.recipient_name AS address_name, a.recipient_phone AS address_phone, " +
            "CONCAT(a.province, a.city, a.district, a.detail_address) AS address_detail " +
            "FROM orders o " +
            "LEFT JOIN user_addresses a ON o.address_id = a.id " +
            "WHERE o.user_id = :userId " +
            "AND (:status != 'all' AND o.status = :status OR :status = 'all') " +
            "ORDER BY o.created_at DESC LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<Map<String, Object>> findUserOrders(
            @Param("userId") Long userId,
            @Param("status") String status,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset
    );

    /**
     * 统计用户订单总数
     */
    @Query(value = "SELECT COUNT(*) FROM orders " +
            "WHERE user_id = :userId " +
            "AND (:status != 'all' AND status = :status OR :status = 'all')",
            nativeQuery = true)
    long countUserOrders(
            @Param("userId") Long userId,
            @Param("status") String status
    );

    /**
     * 查询订单详情（带地址信息）
     */
    @Query(value = "SELECT o.id, o.order_no, o.status, o.total_amount, o.final_amount, o.payment_method, " +
            "o.created_at, a.recipient_name AS address_name, a.recipient_phone AS address_phone, " +
            "CONCAT(a.province, a.city, a.district, a.detail_address) AS address_detail " +
            "FROM orders o " +
            "LEFT JOIN user_addresses a ON o.address_id = a.id " +
            "WHERE o.user_id = :userId AND o.id = :orderId",
            nativeQuery = true)
    Optional<Map<String, Object>> findOrderById(
            @Param("userId") Long userId,
            @Param("orderId") Long orderId
    );

    /**
     * 更新订单状态
     */
    @Transactional
    @Modifying
    @Query(value = "UPDATE orders SET status = :status, updated_at = NOW() " +
            "WHERE id = :orderId",
            nativeQuery = true)
    void updateOrderStatus(
            @Param("orderId") Long orderId,
            @Param("status") String status
    );
}