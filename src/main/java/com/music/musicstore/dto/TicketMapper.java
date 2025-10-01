package com.music.musicstore.dto;

import com.music.musicstore.models.support.Ticket;
import com.music.musicstore.models.support.TicketMessage;
import com.music.musicstore.models.users.Customer;
import com.music.musicstore.models.users.Staff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TicketMapper {

    @Autowired
    private TicketMessageMapper ticketMessageMapper;

    public TicketDto toDto(Ticket ticket) {
        if (ticket == null) return null;
        TicketDto dto = new TicketDto();
        dto.setId(ticket.getId());
        dto.setSubject(ticket.getSubject());
        dto.setStatus(ticket.getStatus());

        if (ticket.getCreatedAt() != null) {
            ZonedDateTime zdt = ZonedDateTime.of(ticket.getCreatedAt(), ZoneId.systemDefault())
                    .withZoneSameInstant(ZoneId.of("UTC"));
            dto.setCreatedAt(zdt.format(DateTimeFormatter.ISO_INSTANT));
        }

        Customer customer = ticket.getCustomer();
        if (customer != null) {
            UserSummaryDto c = new UserSummaryDto(customer.getId(), customer.getUsername(), "CUSTOMER");
            dto.setCustomer(c);
        }

        Staff staff = ticket.getAssignedStaff();
        if (staff != null) {
            UserSummaryDto s = new UserSummaryDto(staff.getId(), staff.getUsername(), "STAFF");
            dto.setStaff(s);
        }

        // lastMessage: find latest by timestamp
        List<TicketMessage> messages = ticket.getMessages();
        if (messages != null && !messages.isEmpty()) {
            TicketMessage last = messages.stream().max(Comparator.comparing(TicketMessage::getTimestamp)).orElse(null);
            if (last != null) {
                dto.setLastMessage(ticketMessageMapper.toDto(last));
            }
        }

        return dto;
    }

    public List<TicketDto> toDtoList(List<Ticket> tickets) {
        if (tickets == null) return List.of();
        return tickets.stream().map(this::toDto).collect(Collectors.toList());
    }
}

