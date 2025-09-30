package com.music.musicstore.repositories;

import com.music.musicstore.models.support.TicketMessage;
import com.music.musicstore.models.support.Ticket;
import com.music.musicstore.models.users.Customer;
import com.music.musicstore.models.users.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketMessageRepository extends JpaRepository<TicketMessage, Long> {

    // Find messages by ticket
    List<TicketMessage> findByTicketOrderByTimestampAsc(Ticket ticket);

    // Find messages by ticket ID
    List<TicketMessage> findByTicket_IdOrderByTimestampAsc(Long ticketId);

    // Find messages by customer
    List<TicketMessage> findByCustomerOrderByTimestampDesc(Customer customer);

    // Find messages by staff
    List<TicketMessage> findByStaffOrderByTimestampDesc(Staff staff);

    // Find messages by sender type
    List<TicketMessage> findByIsFromStaffAndTicketOrderByTimestampAsc(boolean isFromStaff, Ticket ticket);

    // Count messages in a ticket
    long countByTicket(Ticket ticket);

    // Count messages from staff in a ticket
    long countByTicketAndIsFromStaff(Ticket ticket, boolean isFromStaff);

    // Find latest message for a ticket
    @Query("SELECT tm FROM TicketMessage tm WHERE tm.ticket = :ticket ORDER BY tm.timestamp DESC")
    List<TicketMessage> findLatestMessageByTicket(@Param("ticket") Ticket ticket);

    // Search messages by content
    List<TicketMessage> findByContentContainingIgnoreCaseOrderByTimestampDesc(String content);

    // Find messages by ticket and sender type
    @Query("SELECT tm FROM TicketMessage tm WHERE tm.ticket = :ticket AND tm.isFromStaff = :isFromStaff ORDER BY tm.timestamp ASC")
    List<TicketMessage> findByTicketAndSenderType(@Param("ticket") Ticket ticket, @Param("isFromStaff") boolean isFromStaff);

    // Delete all messages for a ticket (for cleanup)
    void deleteByTicket(Ticket ticket);
}
