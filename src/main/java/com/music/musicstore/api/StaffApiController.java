package com.music.musicstore.api;

import com.music.musicstore.models.support.TicketMessage;
import com.music.musicstore.services.TicketService;
import com.music.musicstore.services.StaffService;
import com.music.musicstore.models.users.Staff;
import com.music.musicstore.dto.ErrorResponse;
import com.music.musicstore.dto.TicketReplyRequest;
import com.music.musicstore.dto.TicketStatusUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin(origins = "http://localhost:5173")
public class StaffApiController {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(StaffApiController.class);

    @Autowired
    private TicketService ticketService;

    @Autowired
    private StaffService staffService;

    @Autowired
    private com.music.musicstore.dto.TicketMessageMapper ticketMessageMapper;

    // Enhanced ticket management for staff
    @GetMapping("/tickets")
    public ResponseEntity<?> getAllTickets(@RequestParam(required = false) String status) {
        try {
            if (status != null) {
                return ResponseEntity.ok(ticketService.getTicketsByStatus(status));
            } else {
                return ResponseEntity.ok(ticketService.getAllTickets());
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch tickets: " + e.getMessage()));
        }
    }

    @GetMapping("/tickets/urgent")
    public ResponseEntity<?> getUrgentTickets() {
        try {
            return ResponseEntity.ok(ticketService.getUrgentTickets());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch urgent tickets: " + e.getMessage()));
        }
    }

    @GetMapping("/tickets/needs-attention")
    public ResponseEntity<?> getTicketsNeedingAttention() {
        try {
            return ResponseEntity.ok(ticketService.getTicketsNeedingAttention());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch tickets needing attention: " + e.getMessage()));
        }
    }

    @GetMapping("/tickets/unassigned")
    public ResponseEntity<?> getUnassignedTickets() {
        try {
            return ResponseEntity.ok(ticketService.getUnassignedTickets());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch unassigned tickets: " + e.getMessage()));
        }
    }

    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<?> getTicketDetails(@PathVariable Long ticketId) {
        try {
            return ResponseEntity.ok(ticketService.getTicketById(ticketId));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch ticket: " + e.getMessage()));
        }
    }

    @GetMapping("/tickets/{ticketId}/messages")
    public ResponseEntity<?> getTicketMessages(@PathVariable Long ticketId) {
        try {
            List<TicketMessage> messages = ticketService.getTicketMessages(ticketId);
            List<com.music.musicstore.dto.TicketMessageDto> dtos = messages.stream()
                    .map(ticketMessageMapper::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch ticket messages: " + e.getMessage()));
        }
    }

    @PostMapping("/tickets/{ticketId}/reply")
    public ResponseEntity<?> replyToTicket(
            @PathVariable Long ticketId,
            @RequestBody TicketReplyRequest request,
            @AuthenticationPrincipal Staff staff) {
        try {
            logger.info("Staff {} is replying to ticket {}", staff.getUsername(), ticketId);
            // Get the existing staff entity from database using StaffService
            Staff staff1 = staffService.findByUsername(staff.getUsername());

            TicketMessage message = ticketService.addStaffReply(ticketId, request.getMessage(), staff1);
            logger.info("Staff {} replied to ticket {}", staff.getUsername(), ticketId);
            com.music.musicstore.dto.TicketMessageDto dto = ticketMessageMapper.toDto(message);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to reply to ticket: " + e.getMessage()));
        }
    }

    @PostMapping("/tickets/{ticketId}/assign")
    public ResponseEntity<?> assignTicket(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Get the existing staff entity from database using StaffService
            Staff staff = staffService.findByUsername(userDetails.getUsername());

            var ticket = ticketService.assignTicket(ticketId, staff);
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to assign ticket: " + e.getMessage()));
        }
    }

    @PutMapping("/tickets/{ticketId}/status")
    public ResponseEntity<?> updateTicketStatus(
            @PathVariable Long ticketId,
            @RequestBody TicketStatusUpdateRequest request) {
        try {
            var ticket = ticketService.updateTicketStatus(ticketId, request.getStatus());
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to update ticket status: " + e.getMessage()));
        }
    }

    @PostMapping("/tickets/{ticketId}/close")
    public ResponseEntity<?> closeTicket(@PathVariable Long ticketId) {
        try {
            var ticket = ticketService.closeTicket(ticketId);
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to close ticket: " + e.getMessage()));
        }
    }

    @PostMapping("/tickets/{ticketId}/reopen")
    public ResponseEntity<?> reopenTicket(@PathVariable Long ticketId) {
        try {
            var ticket = ticketService.reopenTicket(ticketId);
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to reopen ticket: " + e.getMessage()));
        }
    }

    @GetMapping("/tickets/stats")
    public ResponseEntity<?> getTicketStatistics() {
        try {
            var stats = ticketService.getStatusDistribution();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch ticket statistics: " + e.getMessage()));
        }
    }

    @GetMapping("/tickets/search")
    public ResponseEntity<?> searchTickets(@RequestParam String query) {
        try {
            return ResponseEntity.ok(ticketService.searchTickets(query));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to search tickets: " + e.getMessage()));
        }
    }
}
