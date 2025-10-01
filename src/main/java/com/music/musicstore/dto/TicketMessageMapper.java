package com.music.musicstore.dto;

import com.music.musicstore.models.support.TicketMessage;
import com.music.musicstore.models.users.Customer;
import com.music.musicstore.models.users.Staff;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
public class TicketMessageMapper {

    public TicketMessageDto toDto(TicketMessage message) {
        if (message == null) return null;

        TicketMessageDto dto = new TicketMessageDto();
        dto.setId(message.getId());
        dto.setContent(message.getContent());

        // Convert LocalDateTime to UTC ISO 8601 string
        if (message.getTimestamp() != null) {
            ZonedDateTime zdt = ZonedDateTime.of(message.getTimestamp(), ZoneId.systemDefault())
                    .withZoneSameInstant(ZoneId.of("UTC"));
            dto.setTimestamp(zdt.format(DateTimeFormatter.ISO_INSTANT));
        }

        // Determine sender: prefer staff, then customer
        Staff staff = message.getStaff();
        Customer customer = message.getCustomer();

        if (staff != null) {
            UserSummaryDto sender = new UserSummaryDto(staff.getId(), staff.getUsername(), "STAFF");
            dto.setSender(sender);
            dto.setIsFromStaff(true);
            // legacy
            dto.setStaff(sender);
            dto.setCustomer(null);
        } else if (customer != null) {
            UserSummaryDto sender = new UserSummaryDto(customer.getId(), customer.getUsername(), "CUSTOMER");
            dto.setSender(sender);
            dto.setIsFromStaff(false);
            // legacy
            dto.setCustomer(sender);
            dto.setStaff(null);
        } else {
            // system or unknown author: use transient names if available
            String name = Optional.ofNullable(message.getStaffName())
                    .orElse(Optional.ofNullable(message.getCustomerName()).orElse("system"));
            UserSummaryDto sender = new UserSummaryDto(0L, name, "ADMIN");
            dto.setSender(sender);
            dto.setIsFromStaff(false);
            dto.setCustomer(null);
            dto.setStaff(null);
        }

        return dto;
    }
}

