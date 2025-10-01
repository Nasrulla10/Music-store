package com.music.musicstore.services;

import com.music.musicstore.models.cart.Cart;
import com.music.musicstore.models.cart.CartItem;
import com.music.musicstore.models.order.Order;
import com.music.musicstore.models.order.OrderItem;
import com.music.musicstore.models.users.Customer;
import com.music.musicstore.repositories.CartItemRepository;
import com.music.musicstore.repositories.OrderRepository;
import com.music.musicstore.repositories.CustomerRepository;
import com.music.musicstore.services.CustomerService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final CartItemRepository cartItemRepository;
    private final EmailSender emailSender;
    private final CustomerService customerService;
    private final CustomerRepository customerRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, CartService cartService, CartItemRepository cartItemRepository, EmailSender emailSender, CustomerService customerService, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.cartItemRepository = cartItemRepository;
        this.emailSender = emailSender;
        this.customerService = customerService;
        this.customerRepository = customerRepository;
    }

    public void placeOrder(Customer customer){
        Cart cart = cartService.getOrCreateCart(customer);
        if(cart.getItems().isEmpty()){
            throw new IllegalStateException("Cannot place order with empty cart");
        }

        Order order = new Order(cart);
        order.setOrderDate(LocalDateTime.now());
        order.setCustomer(customer);
        order.setTotalAmount(cart.getTotalAmount());

        orderRepository.save(order);

        // CRITICAL FIX: Add purchased music to customer's purchased music collection
        // Get a fresh customer instance with purchasedMusic loaded to avoid lazy initialization issues
        Customer managedCustomer = customerRepository.findByIdWithPurchasedMusic(customer.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customer.getId()));

        for (CartItem item : cart.getItems()) {
            managedCustomer.getPurchasedMusic().add(item.getMusic());
        }

        customerService.save(managedCustomer);

        // Send receipt email after updating purchased music
        emailSender.sendReceipt(order.getTotalAmount(), cart.getItemList(), customer.getEmail(), order.getId().toString());

        // Clear the cart after successful order
        cartService.clearCart(customer);
    }

    public void cancelOrder(Long orderId, Customer customer){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if(!order.getCustomer().getId().equals(customer.getId())){
            throw new IllegalArgumentException("Order does not belong to the customer");
        }

        if(order.getStatus() == Order.OrderStatus.CANCELLED){
            throw new IllegalStateException("Order is already cancelled");
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    public void updateOrderStatus(Long orderId, Order.OrderStatus status){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        order.setStatus(status);
        orderRepository.save(order);
    }

    public Order checkout(String username) {
        try {
            Customer customer = customerService.findByUsername(username);
            if (customer == null) {
                throw new RuntimeException("Customer not found with username: " + username);
            }
            
            Cart cart = cartService.getOrCreateCart(customer);
            if (cart.getItems().isEmpty()) {
                throw new IllegalStateException("Cannot place order with empty cart");
            }

            // Calculate cart total before creating order
            cart.calculateTotalAmount();

            Order order = new Order(cart);
            order.setOrderDate(LocalDateTime.now());
            order.setCustomer(customer);
            // Use the calculated total instead of the potentially null totalAmount
            order.setTotalAmount(cart.getTotal());
            order.setStatus(Order.OrderStatus.PENDING);

            // Convert cart items to order items
            for (CartItem cartItem : cart.getItems()) {
                OrderItem orderItem = new OrderItem(cartItem);
                orderItem.setOrder(order);
                order.getOrderItems().add(orderItem);
            }

            Order savedOrder = orderRepository.save(order);

            // CRITICAL FIX: Add purchased music to customer's purchased music collection
            // Get a fresh customer instance with purchasedMusic loaded to avoid lazy initialization issues
            Customer managedCustomer = customerRepository.findByIdWithPurchasedMusic(customer.getId())
                    .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customer.getId()));

            for (CartItem item : cart.getItems()) {
                managedCustomer.getPurchasedMusic().add(item.getMusic());
            }

            customerService.save(managedCustomer);

            // Send receipt email
            emailSender.sendReceipt(order.getTotalAmount(), cart.getItemList(), customer.getEmail(), order.getId().toString());

            // Clear the cart after successful order
            cartService.clearCart(customer);

            return savedOrder;
        } catch (Exception e) {
            throw new RuntimeException("Failed to process checkout: " + e.getMessage(), e);
        }
    }

    public Order purchaseMusic(String username, Long musicId) {
        // Implementation for direct music purchase
        // This would need MusicService and CustomerService integration
        throw new RuntimeException("Direct music purchase not yet implemented");
    }

    public Page<Order> getOrdersByUsername(String username, int page, int size) {
        // Implementation to get orders by username with pagination
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findByCustomer_Username(username, pageable);
    }

    public Order getOrderDetails(Long orderId, String username) {
        // Implementation to get order details with username validation
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        if (!order.getCustomer().getUsername().equals(username)) {
            throw new SecurityException("You can only view your own orders");
        }

        return order;
    }

    // Analytics methods needed by StaffApiController

    public long getTotalOrdersCount(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return orderRepository.countByOrderDateBetween(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
            );
        }
        return orderRepository.count();
    }

    public double getTotalRevenue(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            java.math.BigDecimal revenue = orderRepository.sumTotalAmountByOrderDateBetween(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
            );
            return revenue != null ? revenue.doubleValue() : 0.0;
        }

        java.math.BigDecimal totalRevenue = orderRepository.sumTotalAmount();
        return totalRevenue != null ? totalRevenue.doubleValue() : 0.0;
    }

    public Map<String, Object> generateSalesReport(LocalDate startDate, LocalDate endDate, String format) {
        Map<String, Object> report = new HashMap<>();

        long totalOrders = getTotalOrdersCount(startDate, endDate);
        double totalRevenue = getTotalRevenue(startDate, endDate);

        report.put("totalOrders", totalOrders);
        report.put("totalRevenue", totalRevenue);
        report.put("averageOrderValue", totalOrders > 0 ? totalRevenue / totalOrders : 0);
        report.put("period", startDate + " to " + endDate);
        report.put("format", format);
        report.put("generatedAt", LocalDateTime.now());

        // Add more detailed metrics
        report.put("topCustomers", getTopCustomers(startDate, endDate));
        report.put("dailySales", getDailySales(startDate, endDate));

        return report;
    }

    public Map<String, Object> getCustomerInsights(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> insights = new HashMap<>();

        insights.put("totalCustomers", orderRepository.countDistinctCustomers());
        insights.put("newCustomers", getNewCustomersCount(startDate, endDate));
        insights.put("repeatCustomers", getRepeatCustomersCount(startDate, endDate));
        insights.put("customerRetentionRate", calculateRetentionRate(startDate, endDate));
        insights.put("averageOrdersPerCustomer", getAverageOrdersPerCustomer());
        insights.put("topSpendingCustomers", getTopSpendingCustomers(startDate, endDate));

        return insights;
    }

    // Helper methods for analytics

    private List<Map<String, Object>> getTopCustomers(LocalDate startDate, LocalDate endDate) {
        // Implementation for getting top customers by revenue
        return List.of(); // Placeholder
    }

    private List<Map<String, Object>> getDailySales(LocalDate startDate, LocalDate endDate) {
        // Implementation for getting daily sales data
        return List.of(); // Placeholder
    }

    private long getNewCustomersCount(LocalDate startDate, LocalDate endDate) {
        // Implementation for counting new customers in period
        return 0; // Placeholder
    }

    private long getRepeatCustomersCount(LocalDate startDate, LocalDate endDate) {
        // Implementation for counting repeat customers
        return 0; // Placeholder
    }

    private double calculateRetentionRate(LocalDate startDate, LocalDate endDate) {
        // Implementation for calculating customer retention rate
        return 0.0; // Placeholder
    }

    private double getAverageOrdersPerCustomer() {
        long totalOrders = orderRepository.count();
        long totalCustomers = orderRepository.countDistinctCustomers();
        return totalCustomers > 0 ? (double) totalOrders / totalCustomers : 0.0;
    }

    private List<Map<String, Object>> getTopSpendingCustomers(LocalDate startDate, LocalDate endDate) {
        // Implementation for getting top spending customers
        return List.of(); // Placeholder
    }

    // Missing methods needed by AdminApiController

    public long getTotalOrdersCount() {
        return orderRepository.count();
    }

    public double getTotalRevenue() {
        java.math.BigDecimal totalRevenue = orderRepository.sumTotalAmount();
        return totalRevenue != null ? totalRevenue.doubleValue() : 0.0;
    }

    public Page<Order> getAllOrdersForAdmin(int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size);
        if (status != null && !status.trim().isEmpty()) {
            // Would need to implement status filtering in repository
            return orderRepository.findAll(pageable);
        }
        return orderRepository.findAll(pageable);
    }

    public void refundOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        // Set order status to refunded
        order.setStatus(Order.OrderStatus.CANCELLED); // Assuming CANCELLED represents refunded
        orderRepository.save(order);
    }

    // NEW: Missing methods for admin analytics
    public long getTodayOrdersCount() {
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(23, 59, 59);
            return orderRepository.countByOrderDateBetween(startOfDay, endOfDay);
        } catch (Exception e) {
            return 0; // Return 0 if error occurs
        }
    }

    public java.math.BigDecimal getTodayRevenue() {
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(23, 59, 59);
            java.math.BigDecimal revenue = orderRepository.sumTotalAmountByOrderDateBetween(startOfDay, endOfDay);
            return revenue != null ? revenue : java.math.BigDecimal.ZERO;
        } catch (Exception e) {
            return java.math.BigDecimal.ZERO;
        }
    }

    public Map<String, Object> getSalesAnalytics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> analytics = new HashMap<>();
        try {
            if (startDate == null || endDate == null) {
                // Default to last 30 days
                endDate = LocalDate.now();
                startDate = endDate.minusDays(30);
            }

            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

            analytics.put("totalOrders", orderRepository.countByOrderDateBetween(startDateTime, endDateTime));
            analytics.put("totalRevenue", orderRepository.sumTotalAmountByOrderDateBetween(startDateTime, endDateTime));
            analytics.put("averageOrderValue", getAverageOrderValue(startDateTime, endDateTime));
            analytics.put("period", Map.of("start", startDate, "end", endDate));

        } catch (Exception e) {
            // Return empty analytics if error occurs
            analytics.put("totalOrders", 0);
            analytics.put("totalRevenue", java.math.BigDecimal.ZERO);
            analytics.put("averageOrderValue", java.math.BigDecimal.ZERO);
        }
        return analytics;
    }

    private java.math.BigDecimal getAverageOrderValue(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        try {
            long orderCount = orderRepository.countByOrderDateBetween(startDateTime, endDateTime);
            if (orderCount == 0) {
                return java.math.BigDecimal.ZERO;
            }

            java.math.BigDecimal totalRevenue = orderRepository.sumTotalAmountByOrderDateBetween(startDateTime, endDateTime);
            if (totalRevenue == null) {
                return java.math.BigDecimal.ZERO;
            }

            return totalRevenue.divide(java.math.BigDecimal.valueOf(orderCount), 2, java.math.RoundingMode.HALF_UP);
        } catch (Exception e) {
            return java.math.BigDecimal.ZERO;
        }
    }

    public List<Map<String, Object>> getRevenueByPeriod(LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> revenueData = new java.util.ArrayList<>();
        try {
            if (startDate == null || endDate == null) {
                return revenueData; // Return empty list
            }

            // This would need a more sophisticated implementation to group by periods
            // For now, return placeholder data
            Map<String, Object> periodData = new HashMap<>();
            periodData.put("date", startDate);
            periodData.put("revenue", getTodayRevenue());
            revenueData.add(periodData);

        } catch (Exception e) {
            // Return empty list if error occurs
        }
        return revenueData;
    }
}
