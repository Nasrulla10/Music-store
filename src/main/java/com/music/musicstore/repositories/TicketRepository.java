package com.music.musicstore.repositories;

import com.music.musicstore.models.support.Ticket;
import com.music.musicstore.models.users.Customer;
import com.music.musicstore.models.users.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Find tickets by customer
    List<Ticket> findByCustomer(Customer customer);

    // Find tickets by status
    List<Ticket> findByStatus(String status);

    // Find tickets by assigned staff
    List<Ticket> findByAssignedStaff(Staff staff);

    // Find unassigned tickets
    List<Ticket> findByAssignedStaffIsNull();

    // Count tickets by status
    long countByStatus(String status);

    // Count tickets by multiple statuses
    long countByStatusIn(List<String> statuses);

    // Find tickets by customer and status
    List<Ticket> findByCustomerAndStatus(Customer customer, String status);
    List<Ticket> findByCustomerAndStatusIn(Customer customer, List<String> statuses);

    // Search functionality - search in subject and messages
    @Query("SELECT DISTINCT t FROM Ticket t LEFT JOIN t.messages m WHERE " +
           "LOWER(t.subject) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(m.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Ticket> findBySubjectOrMessageContentContaining(@Param("searchTerm") String searchTerm);

    // Find tickets by customer with search
    @Query("SELECT DISTINCT t FROM Ticket t LEFT JOIN t.messages m WHERE t.customer = :customer AND " +
           "(LOWER(t.subject) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(m.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Ticket> findByCustomerAndSearchTerm(@Param("customer") Customer customer, @Param("searchTerm") String searchTerm);

    // Find tickets by order
    List<Ticket> findByOrder_Id(Long orderId);

    // Find urgent tickets
    @Query("SELECT t FROM Ticket t WHERE t.status = 'URGENT' ORDER BY t.createdAt DESC")
    List<Ticket> findUrgentTickets();

    // Find tickets needing attention (OPEN, URGENT, IN_PROGRESS)
    @Query("SELECT t FROM Ticket t WHERE t.status IN ('OPEN', 'URGENT', 'IN_PROGRESS') " +
           "ORDER BY CASE WHEN t.status = 'URGENT' THEN 1 WHEN t.status = 'IN_PROGRESS' THEN 2 ELSE 3 END, t.createdAt DESC")
    List<Ticket> findTicketsNeedingAttention();

    // Find tickets assigned to staff
    List<Ticket> findByAssignedStaffAndStatusIn(Staff staff, List<String> statuses);

    // Statistics queries
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.customer = :customer")
    long countByCustomer(@Param("customer") Customer customer);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.customer = :customer AND t.status = :status")
    long countByCustomerAndStatus(@Param("customer") Customer customer, @Param("status") String status);

    // Find recent tickets
    @Query("SELECT t FROM Ticket t ORDER BY t.createdAt DESC")
    List<Ticket> findRecentTickets();

    // Custom queries for analytics
    @Query("SELECT t.status, COUNT(t) FROM Ticket t GROUP BY t.status")
    List<Object[]> getStatusDistribution();

    @Query("SELECT DATE(t.createdAt), COUNT(t) FROM Ticket t GROUP BY DATE(t.createdAt) ORDER BY DATE(t.createdAt) DESC")
    List<Object[]> getTicketCountByDate();

    // Find tickets by customer username
    @Query("SELECT t FROM Ticket t WHERE t.customer.username = :username")
    List<Ticket> findByCustomerUsername(@Param("username") String username);

    // Find tickets by customer username and status
    @Query("SELECT t FROM Ticket t WHERE t.customer.username = :username AND t.status = :status")
    List<Ticket> findByCustomerUsernameAndStatus(@Param("username") String username, @Param("status") String status);

    // Find active tickets (not closed)
    @Query("SELECT t FROM Ticket t WHERE t.status != 'CLOSED' ORDER BY t.createdAt DESC")
    List<Ticket> findActiveTickets();

    // Find tickets by status list
    List<Ticket> findByStatusIn(List<String> statuses);

    // Find closed tickets
    List<Ticket> findByStatusOrderByClosedAtDesc(String status);

    // Find tickets assigned to specific staff member
    List<Ticket> findByAssignedStaffOrderByCreatedAtDesc(Staff staff);

    // Count unassigned tickets
    long countByAssignedStaffIsNull();

    // Count tickets assigned to staff
    long countByAssignedStaff(Staff staff);
}
