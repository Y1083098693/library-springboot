package com.library.repository;

import com.library.model.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

/**
 * 订单项仓库：单独管理OrderItem实体，符合JPA单一职责规范
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * 根据订单ID查询订单项
     */
    @Query(value = "SELECT id, book_id, book_title, book_cover, quantity, unit_price " +
            "FROM order_items WHERE order_id = :orderId",
            nativeQuery = true)
    List<Map<String, Object>> findByOrderId(@Param("orderId") Long orderId);
}