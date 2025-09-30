package com.music.musicstore.repositories;

import com.music.musicstore.models.users.Customer;
import com.music.musicstore.models.order.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomer(Customer customer);
    List<Order> findByCustomerOrderByOrderDateDesc(Customer customer);
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    Page<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    Page<Order> findByCustomer(Customer customer, Pageable pageable);

    // Missing methods needed by OrderService analytics

    // Find orders by customer username
    Page<Order> findByCustomer_Username(String username, Pageable pageable);
    List<Order> findByCustomer_Username(String username);

    // Count orders by date range
    long countByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Sum total amount methods for revenue calculation
    @Query("SELECT SUM(o.totalAmount) FROM Order o")
    BigDecimal sumTotalAmount();

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalAmountByOrderDateBetween(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    // Count distinct customers
    @Query("SELECT COUNT(DISTINCT o.customer.id) FROM Order o")
    long countDistinctCustomers();

    // Analytics queries for customer insights
    @Query("SELECT COUNT(DISTINCT o.customer) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    long countDistinctCustomersByDateRange(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    // Top customers by revenue
    @Query("SELECT o.customer, SUM(o.totalAmount) as totalSpent FROM Order o " +
           "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY o.customer ORDER BY totalSpent DESC")
    List<Object[]> findTopCustomersByRevenue(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    // Daily sales data
    @Query("SELECT DATE(o.orderDate) as orderDate, COUNT(o) as orderCount, SUM(o.totalAmount) as revenue " +
           "FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(o.orderDate) ORDER BY DATE(o.orderDate)")
    List<Object[]> getDailySalesData(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    // Order status distribution
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> getOrderStatusDistribution();

    // Average order value
    @Query("SELECT AVG(o.totalAmount) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    Double getAverageOrderValue(@Param("startDate") LocalDateTime startDate,
                               @Param("endDate") LocalDateTime endDate);

    // Find orders by status
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);

    // Count orders by status
    long countByStatus(Order.OrderStatus status);
}
