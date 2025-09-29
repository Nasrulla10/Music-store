# Music Store API Documentation

## Base URL
```
http://localhost:8082
```

## Overview
This API provides endpoints for a comprehensive music store application with support for user authentication, music management, cart operations, order processing, reviews, and administrative functions. The API features an enhanced ticket support system with chat-like messaging capabilities.

## Recent Updates (September 2025)
- **FIXED: Critical Circular Reference Issues**: Resolved JSON serialization infinite loops
  - Fixed Ticket/TicketMessage circular references causing "Document nesting depth exceeds maximum" errors
  - Fixed Customer/Cart circular references
  - Added proper @JsonIgnore annotations and transient fields for safe serialization
- **FIXED: Database Schema Issues**: Resolved BigDecimal column definition problems
- **FIXED: Hibernate Lazy Loading Issues**: Prevented session problems during JSON serialization
- **Enhanced Chat-Based Ticket System**: Complete messaging system with proper DTOs
  - Separate endpoints for ticket creation, message retrieval, and conversation management
  - Proper DTO validation with TicketReplyRequest and TicketStatusUpdateRequest
  - Comprehensive error handling with standardized ErrorResponse
- **Improved Architecture**: Cleaner separation between ticket info and message conversations
- **Better Performance**: Optimized queries with proper transient field population

## API Flow for Ticket System
1. **Create Ticket**: `POST /api/customer/support/ticket` - Returns clean ticket info without messages
2. **Get Ticket Conversation**: `GET /api/customer/support/ticket/{ticketId}/messages` - Returns full chat conversation
3. **Add Message**: `POST /api/customer/support/ticket/{ticketId}/message` - Adds new message to conversation
4. **Staff Management**: Staff can reply, assign, and update ticket status via `/api/staff/tickets/` endpoints

## Authentication
Most endpoints require JWT authentication via the `Authorization` header:
```
Authorization: Bearer <jwt_token>
```

## Role-Based Access Control
- **PUBLIC**: Accessible without authentication
- **CUSTOMER**: Requires CUSTOMER role
- **ARTIST**: Requires ARTIST role  
- **STAFF**: Requires STAFF role
- **ADMIN**: Requires ADMIN role

---

## Authentication Endpoints

### 1. User Login
**Endpoint:** `POST /api/auth/login`

**Access Level:** PUBLIC

**Description:** Authenticate user and receive JWT token

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "string (required, 3-50 characters)",
  "password": "string (required, min 6 characters)"
}
```

**Response (Success - 200 OK):**
```json
{
  "token": "jwt_token_string",
  "type": "Bearer",
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "role": "CUSTOMER"
  }
}
```

---

## Admin Management System

### User Management

#### 1. Create User
**Endpoint:** `POST /api/admin/users/create`

**Access Level:** ADMIN

**Description:** Create a new user with specified role

**Request Body:**
```json
{
  "username": "string (required)",
  "password": "string (required)",
  "email": "string (required)",
  "role": "string (required: CUSTOMER, ARTIST, STAFF, ADMIN)",
  "firstName": "string (optional)",
  "lastName": "string (optional)",
  "artistName": "string (optional, for ARTIST role)",
  "cover": "string (optional, cover image URL)"
}
```

#### 2. Get All Users
**Endpoint:** `GET /api/admin/users`

**Access Level:** ADMIN

**Query Parameters:**
- `page` (int, default: 0): Page number
- `size` (int, default: 10): Page size
- `role` (String, optional): Filter by user role

#### 3. Update User
**Endpoint:** `PUT /api/admin/users/{userId}`

**Access Level:** ADMIN

#### 4. Delete User
**Endpoint:** `DELETE /api/admin/users/{userId}`

**Access Level:** ADMIN

#### 5. Update User Status
**Endpoint:** `PUT /api/admin/users/{userId}/status`

**Access Level:** ADMIN

**Request Body:**
```json
{
  "enabled": "boolean (required)"
}
```

### Music Management

#### 1. Get All Music (Admin)
**Endpoint:** `GET /api/admin/music`

**Access Level:** ADMIN

#### 2. Delete Music
**Endpoint:** `DELETE /api/admin/music/{musicId}`

**Access Level:** ADMIN

#### 3. Update Music Status
**Endpoint:** `PUT /api/admin/music/{musicId}/status`

**Access Level:** ADMIN

**Request Body:**
```json
{
  "status": "string (required)"
}
```

### Order Management

#### 1. Get All Orders (Admin)
**Endpoint:** `GET /api/admin/orders`

**Access Level:** ADMIN

**Query Parameters:**
- `page` (int, default: 0): Page number
- `size` (int, default: 10): Page size
- `status` (String, optional): Filter by order status

#### 2. Refund Order
**Endpoint:** `PUT /api/admin/orders/{orderId}/refund`

**Access Level:** ADMIN

### Analytics and Reports

#### 1. System Overview
**Endpoint:** `GET /api/admin/analytics/overview`

**Access Level:** ADMIN

**Description:** Get comprehensive system statistics

**Response (Success - 200 OK):**
```json
{
  "totalUsers": 1250,
  "totalMusic": 3420,
  "totalOrders": 5670,
  "totalRevenue": 45230.50,
  "activeTickets": 23
}
```

#### 2. Detailed Analytics
**Endpoint:** `GET /api/admin/analytics/detailed`

**Access Level:** ADMIN

**Description:** Get detailed analytics including ticket statistics

**Response (Success - 200 OK):**
```json
{
  "userGrowth": "User growth analytics data",
  "salesAnalytics": "Sales analytics data",
  "musicAnalytics": "Music analytics data",
  "ticketAnalytics": [
    ["OPEN", 15],
    ["IN_PROGRESS", 8],
    ["CLOSED", 120],
    ["URGENT", 3]
  ]
}
```

#### 3. Generate Comprehensive Report
**Endpoint:** `GET /api/admin/reports/comprehensive`

**Access Level:** ADMIN

**Query Parameters:**
- `startDate` (LocalDate, optional): Start date for report
- `endDate` (LocalDate, optional): End date for report
- `format` (String, default: "pdf"): Report format

### System Settings

#### 1. Create System Backup
**Endpoint:** `POST /api/admin/settings/backup`

**Access Level:** ADMIN

**Description:** Initiate system backup

---

## Admin Ticket Management

### 1. Get All Tickets (Admin)
**Endpoint:** `GET /api/admin/tickets`

**Access Level:** ADMIN

**Description:** Retrieve all support tickets with optional status filtering

**Query Parameters:**
- `status` (String, optional): Filter by ticket status (OPEN, IN_PROGRESS, URGENT, CLOSED)

**Response (Success - 200 OK):**
```json
[
  {
    "id": 1,
    "subject": "Payment Issue",
    "status": "OPEN",
    "customer": {
      "id": 1,
      "username": "john_doe"
    },
    "assignedStaff": null,
    "createdAt": "2025-09-21T10:30:00",
    "closedAt": null,
    "messages": [...]
  }
]
```

### 2. Get Ticket Details (Admin)
**Endpoint:** `GET /api/admin/tickets/{ticketId}`

**Access Level:** ADMIN

**Description:** Get detailed information about a specific ticket

### 3. Get Ticket Messages (Admin)
**Endpoint:** `GET /api/admin/tickets/{ticketId}/messages`

**Access Level:** ADMIN

**Description:** Get all messages in a ticket conversation (admin view)

### 4. Reply to Ticket (Admin)
**Endpoint:** `POST /api/admin/tickets/{ticketId}/reply`

**Access Level:** ADMIN

**Description:** Add an admin reply to a ticket conversation

**Request Body:**
```json
{
  "message": "string (required, admin reply content)"
}
```

**Response (Success - 200 OK):**
```json
{
  "id": 3,
  "content": "We've escalated your issue to our technical team.",
  "timestamp": "2025-09-21T11:00:00",
  "isFromStaff": true,
  "customer": null,
  "staff": {
    "id": 1,
    "username": "admin_user"
  }
}
```

### 5. Update Ticket Status (Admin)
**Endpoint:** `PUT /api/admin/tickets/{ticketId}/status`

**Access Level:** ADMIN

**Description:** Update the status of any ticket

**Request Body:**
```json
{
  "status": "string (required: OPEN, IN_PROGRESS, URGENT, CLOSED)"
}
```

### 6. Assign Ticket (Admin)
**Endpoint:** `POST /api/admin/tickets/{ticketId}/assign`

**Access Level:** ADMIN

**Description:** Assign a ticket to the authenticated admin

### 7. Close Ticket (Admin)
**Endpoint:** `POST /api/admin/tickets/{ticketId}/close`

**Access Level:** ADMIN

**Description:** Mark any ticket as closed

### 8. Reopen Ticket (Admin)
**Endpoint:** `POST /api/admin/tickets/{ticketId}/reopen`

**Access Level:** ADMIN

**Description:** Reopen any closed ticket

### 9. Delete Ticket (Admin)
**Endpoint:** `DELETE /api/admin/tickets/{ticketId}`

**Access Level:** ADMIN

**Description:** Permanently delete a ticket (admin only)

### 10. Search Tickets (Admin)
**Endpoint:** `GET /api/admin/tickets/search`

**Access Level:** ADMIN

**Description:** Search all tickets by content in subject or messages

**Query Parameters:**
- `query` (String, required): Search term

### 11. Get Urgent Tickets (Admin)
**Endpoint:** `GET /api/admin/tickets/urgent`

**Access Level:** ADMIN

**Description:** Get all tickets marked as urgent

### 12. Get Unassigned Tickets (Admin)
**Endpoint:** `GET /api/admin/tickets/unassigned`

**Access Level:** ADMIN

**Description:** Get all tickets that haven't been assigned to any staff member

### 13. Get Ticket Statistics (Admin)
**Endpoint:** `GET /api/admin/tickets/stats`

**Access Level:** ADMIN

**Description:** Get comprehensive ticket statistics and status distribution

**Response (Success - 200 OK):**
```json
[
  ["OPEN", 15],
  ["IN_PROGRESS", 8],
  ["CLOSED", 120],
  ["URGENT", 3]
]
```

---

## Customer Support Ticket System (NEW CHAT-BASED)

### API Flow Overview
The new chat-based ticket system follows this optimized flow:

1. **Create Ticket**: `POST /api/customer/support/ticket` - Returns clean ticket info without messages (faster response)
2. **Get Conversation**: `GET /api/customer/support/ticket/{ticketId}/messages` - Returns full chat conversation
3. **Add Message**: `POST /api/customer/support/ticket/{ticketId}/message` - Adds new message to conversation

This separation provides better performance and cleaner API design.

### 1. Create Support Ticket
**Endpoint:** `POST /api/customer/support/ticket`

**Access Level:** CUSTOMER

**Description:** Create a new support ticket with initial message. **Note: Returns ticket info without messages for better performance. Use the messages endpoint to get the full conversation.**

**Request Body:**
```json
{
  "subject": "string (required, max 255 characters)",
  "description": "string (required, initial message)"
}
```

**Response (Success - 200 OK):**
```json
{
  "id": 1,
  "subject": "Payment Issue",
  "status": "OPEN",
  "customer": {
    "id": 1,
    "username": "john_doe",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com"
  },
  "assignedStaff": null,
  "createdAt": "2025-09-21T10:30:00",
  "closedAt": null
}
```

**Note:** The response does NOT include messages to avoid lazy loading issues and improve performance. Use the dedicated messages endpoint to get the conversation.

### 2. Get Customer's Tickets
**Endpoint:** `GET /api/customer/support/tickets`

**Access Level:** CUSTOMER

**Description:** Retrieve all tickets created by the authenticated customer (without messages for performance)

**Response (Success - 200 OK):**
```json
[
  {
    "id": 1,
    "subject": "Payment Issue",
    "status": "IN_PROGRESS",
    "customer": {
      "id": 1,
      "username": "john_doe",
      "firstName": "John",
      "lastName": "Doe"
    },
    "assignedStaff": {
      "id": 1,
      "username": "support_agent"
    },
    "createdAt": "2025-09-21T10:30:00",
    "closedAt": null
  }
]
```

### 3. Add Message to Ticket (Customer Reply)
**Endpoint:** `POST /api/customer/support/ticket/{ticketId}/message`

**Access Level:** CUSTOMER

**Description:** Add a new message to an existing ticket conversation

**Path Parameters:**
- `ticketId` (Long): ID of the ticket

**Request Body:**
```json
{
  "content": "string (required, message content)"
}
```

**Response (Success - 200 OK):**
```json
{
  "id": 5,
  "content": "Thank you for your help!",
  "timestamp": "2025-09-21T11:45:00",
  "isFromStaff": false,
  "customer": {
    "id": 1,
    "username": "john_doe"
  },
  "staff": null
}
```

### 4. Get Ticket Conversation
**Endpoint:** `GET /api/customer/support/ticket/{ticketId}/messages`

**Access Level:** CUSTOMER

**Description:** Retrieve all messages for a specific ticket (complete chat conversation)

**Path Parameters:**
- `ticketId` (Long): ID of the ticket

**Security:** Customers can only access messages for their own tickets.

**Response (Success - 200 OK):**
```json
[
  {
    "id": 1,
    "content": "I'm having trouble with my payment",
    "timestamp": "2025-09-21T10:30:00",
    "isFromStaff": false,
    "customer": {
      "id": 1,
      "username": "john_doe"
    },
    "staff": null
  },
  {
    "id": 2,
    "content": "I can help you with that. What payment method are you using?",
    "timestamp": "2025-09-21T10:35:00",
    "isFromStaff": true,
    "customer": null,
    "staff": {
      "id": 1,
      "username": "support_agent"
    }
  },
  {
    "id": 3,
    "content": "I'm using PayPal but the payment keeps failing",
    "timestamp": "2025-09-21T10:40:00",
    "isFromStaff": false,
    "customer": {
      "id": 1,
      "username": "john_doe"
    },
    "staff": null
  }
]
```

---

## Staff Support Management

### 1. Get All Tickets (Staff)
**Endpoint:** `GET /api/staff/tickets`

**Access Level:** STAFF

**Description:** Retrieve all support tickets with optional status filtering

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

**Query Parameters:**
- `status` (String, optional): Filter by ticket status (OPEN, IN_PROGRESS, URGENT, CLOSED)

**Response (Success - 200 OK):**
```json
[
  {
    "id": 1,
    "subject": "Login Issue",
    "description": "Cannot access my account",
    "status": "OPEN",
    "priority": "MEDIUM",
    "customer": {
      "id": 1,
      "username": "john_doe",
      "email": "john@example.com"
    },
    "staff": null,
    "createdAt": "2025-09-22T10:30:00",
    "lastUpdated": "2025-09-22T10:30:00"
  }
]
```

### 2. Get Specific Ticket Details (Staff)
**Endpoint:** `GET /api/staff/tickets/{ticketId}`

**Access Level:** STAFF

**Description:** Get detailed information about a specific ticket

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

**Path Parameters:**
- `ticketId` (Long, required): ID of the ticket

**Response (Success - 200 OK):**
```json
{
  "id": 1,
  "subject": "Login Issue",
  "description": "Cannot access my account",
  "status": "OPEN",
  "priority": "MEDIUM",
  "customer": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com"
  },
  "staff": null,
  "createdAt": "2025-09-22T10:30:00",
  "lastUpdated": "2025-09-22T10:30:00"
}
```

### 3. Get Ticket Messages/Conversation (Staff)
**Endpoint:** `GET /api/staff/tickets/{ticketId}/messages`

**Access Level:** STAFF

**Description:** Get all messages in a ticket conversation

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

**Path Parameters:**
- `ticketId` (Long, required): ID of the ticket

**Response (Success - 200 OK):**
```json
[
  {
    "id": 1,
    "content": "I cannot log into my account",
    "timestamp": "2025-09-22T10:30:00",
    "isFromStaff": false,
    "customer": {
      "id": 1,
      "username": "john_doe",
      "email": "john@example.com"
    },
    "staff": null
  },
  {
    "id": 2,
    "content": "Let me help you with that. Can you try resetting your password?",
    "timestamp": "2025-09-22T11:00:00",
    "isFromStaff": true,
    "customer": null,
    "staff": {
      "id": 1,
      "username": "staff1",
      "email": "staff1@example.com"
    }
  }
]
```

### 4. Get Urgent Tickets (Staff)
**Endpoint:** `GET /api/staff/tickets/urgent`

**Access Level:** STAFF

**Description:** Get all tickets marked as urgent priority

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response:** Same format as Get All Tickets, filtered for urgent tickets

### 5. Get Tickets Needing Attention (Staff)
**Endpoint:** `GET /api/staff/tickets/needs-attention`

**Access Level:** STAFF

**Description:** Get tickets that require immediate staff attention

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response:** Same format as Get All Tickets

### 6. Get Unassigned Tickets (Staff)
**Endpoint:** `GET /api/staff/tickets/unassigned`

**Access Level:** STAFF

**Description:** Get all tickets that haven't been assigned to any staff member

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response:** Same format as Get All Tickets, filtered for unassigned tickets

### 7. Reply to Ticket (Staff)
**Endpoint:** `POST /api/staff/tickets/{ticketId}/reply`

**Access Level:** STAFF

**Description:** Add a staff reply to a ticket conversation

**Request Headers:**
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Path Parameters:**
- `ticketId` (Long, required): ID of the ticket to reply to

**Request Body:**
```json
{
  "message": "string (required, staff reply content, max 2000 characters)"
}
```

**Response (Success - 200 OK):**
```json
{
  "id": 3,
  "content": "Your reply message here",
  "timestamp": "2025-09-22T11:30:00",
  "isFromStaff": true,
  "customer": null,
  "staff": {
    "id": 1,
    "username": "staff1",
    "email": "staff1@example.com"
  }
}
```

### 8. Assign Ticket to Current Staff (Staff)
**Endpoint:** `POST /api/staff/tickets/{ticketId}/assign`

**Access Level:** STAFF

**Description:** Assign a ticket to the currently authenticated staff member

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

**Path Parameters:**
- `ticketId` (Long, required): ID of the ticket to assign

**Request Body:** None required (staff is determined from JWT token)

**Response (Success - 200 OK):**
```json
{
  "id": 1,
  "subject": "Login Issue",
  "description": "Cannot access my account",
  "status": "IN_PROGRESS",
  "priority": "MEDIUM",
  "customer": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com"
  },
  "staff": {
    "id": 1,
    "username": "staff1",
    "email": "staff1@example.com"
  },
  "createdAt": "2025-09-22T10:30:00",
  "lastUpdated": "2025-09-22T11:45:00"
}
```

### 9. Update Ticket Status (Staff)
**Endpoint:** `PUT /api/staff/tickets/{ticketId}/status`

**Access Level:** STAFF

**Description:** Update the status of a ticket

**Request Headers:**
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Path Parameters:**
- `ticketId` (Long, required): ID of the ticket to update

**Request Body:**
```json
{
  "status": "string (required, valid values: OPEN, IN_PROGRESS, URGENT, CLOSED)"
}
```

**Response (Success - 200 OK):**
```json
{
  "id": 1,
  "subject": "Login Issue",
  "description": "Cannot access my account",
  "status": "IN_PROGRESS",
  "priority": "MEDIUM",
  "customer": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com"
  },
  "staff": {
    "id": 1,
    "username": "staff1",
    "email": "staff1@example.com"
  },
  "createdAt": "2025-09-22T10:30:00",
  "lastUpdated": "2025-09-22T12:00:00"
}
```

### 10. Close Ticket (Staff)
**Endpoint:** `POST /api/staff/tickets/{ticketId}/close`

**Access Level:** STAFF

**Description:** Close a ticket (sets status to CLOSED)

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

**Path Parameters:**
- `ticketId` (Long, required): ID of the ticket to close

**Request Body:** None required

**Response (Success - 200 OK):**
```json
{
  "id": 1,
  "subject": "Login Issue",
  "description": "Cannot access my account",
  "status": "CLOSED",
  "priority": "MEDIUM",
  "customer": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com"
  },
  "staff": {
    "id": 1,
    "username": "staff1",
    "email": "staff1@example.com"
  },
  "createdAt": "2025-09-22T10:30:00",
  "lastUpdated": "2025-09-22T12:15:00"
}
```

### 11. Reopen Ticket (Staff)
**Endpoint:** `POST /api/staff/tickets/{ticketId}/reopen`

**Access Level:** STAFF

**Description:** Reopen a closed ticket (sets status back to OPEN)

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

**Path Parameters:**
- `ticketId` (Long, required): ID of the ticket to reopen

**Request Body:** None required

**Response (Success - 200 OK):**
```json
{
  "id": 1,
  "subject": "Login Issue",
  "description": "Cannot access my account",
  "status": "OPEN",
  "priority": "MEDIUM",
  "customer": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com"
  },
  "staff": {
    "id": 1,
    "username": "staff1",
    "email": "staff1@example.com"
  },
  "createdAt": "2025-09-22T10:30:00",
  "lastUpdated": "2025-09-22T13:00:00"
}
```

### 12. Get Ticket Statistics (Staff)
**Endpoint:** `GET /api/staff/tickets/stats`

**Access Level:** STAFF

**Description:** Get statistical information about tickets (status distribution)

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (Success - 200 OK):**
```json
{
  "OPEN": 5,
  "IN_PROGRESS": 3,
  "URGENT": 2,
  "CLOSED": 15
}
```

---

## Important Note: Staff Analytics and Reports

**Staff users do NOT have access to analytics or sales reports endpoints.** These features are only available to ADMIN users:

- Analytics: Available at `/api/admin/analytics/overview` and `/api/admin/analytics/detailed`
- Reports: Available at `/api/admin/reports/comprehensive`

Staff users should use the ticket statistics endpoint instead:
- **GET `/api/staff/tickets/stats`** - Get ticket status distribution for staff dashboard

---

## Ticket Status Values

The ticket system supports the following status values:

- **OPEN**: New ticket, awaiting staff assignment
- **IN_PROGRESS**: Ticket assigned to staff and being worked on
- **URGENT**: High priority ticket requiring immediate attention
- **CLOSED**: Ticket resolved and closed

## Ticket Message Structure

Each ticket contains multiple messages forming a conversation:

```json
{
  "id": "Long (message ID)",
  "content": "String (message content)",
  "timestamp": "LocalDateTime (when message was sent)",
  "isFromStaff": "Boolean (true if from staff, false if from customer)",
  "customer": "Customer object (if message from customer)",
  "staff": "Staff object (if message from staff)"
}
```

## Key Features

### 1. Multi-Level Management
- **Customer**: Create tickets, add messages, view own tickets
- **Staff**: Manage assigned tickets, reply to customers, update status
- **Admin**: Full control over all tickets, delete tickets, comprehensive analytics

### 2. Chat-Like Experience
- Multiple messages per ticket creating conversation flow
- Clear distinction between customer and staff messages
- Chronological message ordering for easy conversation following

### 3. Comprehensive Analytics
- System overview with key metrics
- Ticket status distribution
- User, music, and order analytics
- Detailed reporting capabilities

### 4. Simplified Architecture
- No complex pagination for university project requirements
- Straightforward REST endpoints
- Clear separation of customer, staff, and admin functionalities

---

## Error Handling

All endpoints return consistent error responses:

**Error Response (400 Bad Request):**
```json
{
  "error": "Detailed error message describing what went wrong"
}
```

**Error Response (401 Unauthorized):**
```json
{
  "error": "Unauthorized access"
}
```

**Error Response (403 Forbidden):**
```json
{
  "error": "Access denied"
}
```

---

## Implementation Notes

### Database Schema Changes
The new system requires these tables:
- `tickets` - Main ticket information with staff assignment
- `ticket_messages` - Individual messages within tickets

### Migration Considerations
- Existing single message/reply tickets need migration to new message format
- Staff assignment functionality requires proper staff user management
- All pagination-based queries replaced with simple list operations

### Performance Considerations
- Messages loaded lazily to improve ticket list performance
- Search functionality works across both subjects and message content
- Simple list operations replace complex paginated queries for better maintainability

# Music Store Admin API Documentation

## Overview
This document provides comprehensive API documentation for the enhanced Music Store admin functionality, including flagged content management, review moderation, advanced analytics, and server management capabilities.

## Admin Features Implemented

### 1. Flagged Content Management
- Music flagging system with customer-initiated flagging
- Admin endpoints to view, manage, and remove flagged content
- Automatic flagging metadata tracking

### 2. Review Management
- Admin can view all reviews with sorting and pagination
- Review deletion capabilities with automatic rating recalculation
- Review analytics and insights

### 3. Enhanced Analytics
- Real-time system metrics and performance data
- User growth analytics and engagement metrics
- Sales and revenue analytics with detailed breakdowns
- Music performance analytics by genre, artist, and popularity

### 4. Server Management
- Graceful server shutdown capabilities
- System status monitoring
- Performance metrics tracking

---

## Authentication
All admin endpoints require authentication with `ROLE_ADMIN` privileges.

**Headers Required:**
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

---

## API Endpoints

### Music Flagging (Customer Endpoints)

#### Flag Music Content
```http
POST /api/music/{musicId}/flag
```

**Description:** Allows customers to flag inappropriate music content

**Request Body:**
```json
{
  "reason": "Inappropriate content"
}
```

**Response:**
```json
{
  "message": "Music flagged successfully",
  "timestamp": "2025-09-22T10:30:00",
  "musicId": 123
}
```

#### Check Flag Status
```http
GET /api/music/{musicId}/flag-status
```

**Response:**
```json
{
  "musicId": 123,
  "isFlagged": true,
  "customerId": 456
}
```

---

### Admin Flagged Content Management

#### Get All Flagged Music
```http
GET /api/admin/music/flagged?page=0&size=10
```

**Description:** Retrieve paginated list of flagged music content

**Query Parameters:**
- `page` (int, default: 0): Page number
- `size` (int, default: 10): Items per page

**Response:**
```json
{
  "content": [
    {
      "id": 123,
      "name": "Song Title",
      "artistUsername": "artist123",
      "flaggedAt": "2025-09-22T09:15:00",
      "flaggedByCustomerId": 456,
      "reason": "Inappropriate content"
    }
  ],
  "totalElements": 25,
  "totalPages": 3,
  "number": 0,
  "size": 10
}
```

#### Unflag Music
```http
POST /api/admin/music/{musicId}/unflag
```

**Description:** Remove flag from music content after review

**Response:**
```json
{
  "message": "Music unflagged successfully",
  "timestamp": "2025-09-22T10:45:00"
}
```

#### Delete Flagged Music
```http
DELETE /api/admin/music/{musicId}/flagged
```

**Description:** Permanently delete flagged music content

**Response:**
```json
{
  "message": "Flagged music deleted successfully",
  "timestamp": "2025-09-22T10:50:00"
}
```

---

### Admin Review Management

#### Get All Reviews
```http
GET /api/admin/reviews?page=0&size=10&sortBy=date
```

**Description:** Retrieve all reviews with sorting and pagination

**Query Parameters:**
- `page` (int, default: 0): Page number
- `size` (int, default: 10): Items per page
- `sortBy` (string): Sort criteria - "date", "rating", or default

**Response:**
```json
{
  "content": [
    {
      "id": 789,
      "musicId": 123,
      "customerUsername": "customer123",
      "rating": 4,
      "comment": "Great song!",
      "createdAt": "2025-09-22T08:30:00",
      "updatedAt": "2025-09-22T08:30:00"
    }
  ],
  "totalElements": 150,
  "totalPages": 15,
  "number": 0,
  "size": 10
}
```

#### Delete Review
```http
DELETE /api/admin/reviews/{reviewId}
```

**Description:** Delete a review and recalculate music ratings

**Response:**
```json
{
  "message": "Review deleted successfully",
  "timestamp": "2025-09-22T11:00:00"
}
```

#### Get Flagged Reviews
```http
GET /api/admin/reviews/flagged?page=0&size=10
```

**Description:** Retrieve flagged reviews (placeholder for future implementation)

---

### Enhanced Analytics

#### System Overview
```http
GET /api/admin/analytics/overview
```

**Description:** Get comprehensive system overview with key metrics

**Response:**
```json
{
  "totalUsers": 1250,
  "totalMusic": 5600,
  "totalOrders": 8900,
  "totalRevenue": 125000.50,
  "activeTickets": 15,
  "totalReviews": 3400,
  "flaggedMusic": 8,
  "averageRating": 4.2,
  "todayRegistrations": 12,
  "todayOrders": 45,
  "todayRevenue": 2100.75
}
```

#### Detailed Analytics
```http
GET /api/admin/analytics/detailed?startDate=2025-09-01&endDate=2025-09-22
```

**Description:** Get detailed analytics for specified date range

**Query Parameters:**
- `startDate` (date, optional): Start date (YYYY-MM-DD)
- `endDate` (date, optional): End date (YYYY-MM-DD)

**Response:**
```json
{
  "userGrowth": {
    "newUsers": 450,
    "growthRate": 12.5,
    "activeUsers": 980
  },
  "usersByRole": {
    "CUSTOMER": 1100,
    "ARTIST": 120,
    "STAFF": 25,
    "ADMIN": 5
  },
  "salesAnalytics": {
    "totalSales": 8900,
    "revenue": 125000.50,
    "averageOrderValue": 14.04
  },
  "revenueByPeriod": [
    {
      "date": "2025-09-20",
      "revenue": 1850.25
    }
  ],
  "topSellingMusic": [
    {
      "id": 123,
      "name": "Popular Song",
      "artist": "artist123",
      "sales": 450,
      "revenue": 6750.00
    }
  ],
  "musicByGenre": {
    "Pop": 1200,
    "Rock": 950,
    "Jazz": 680
  },
  "musicByCategory": {
    "Single": 3200,
    "Album": 2400
  },
  "artistPerformance": [
    {
      "artist": "artist123",
      "totalTracks": 25,
      "avgRating": 4.5,
      "totalReviews": 350
    }
  ],
  "reviewAnalytics": {
    "totalReviews": 3400,
    "averageRating": 4.2,
    "reviewsThisWeek": 125,
    "reviewsThisMonth": 520
  },
  "ratingDistribution": {
    "5 stars": 1200,
    "4 stars": 1100,
    "3 stars": 650,
    "2 stars": 300,
    "1 stars": 150
  },
  "ticketAnalytics": {
    "OPEN": 8,
    "IN_PROGRESS": 5,
    "CLOSED": 125
  },
  "ticketResolutionTime": "2.5 days"
}
```

#### Performance Metrics
```http
GET /api/admin/analytics/performance
```

**Description:** Get system performance metrics

**Response:**
```json
{
  "memoryUsed": 512,
  "memoryTotal": 1024,
  "memoryFree": 512,
  "processors": 4,
  "databaseConnections": "Not implemented",
  "activeUsers": 245,
  "systemUptime": "5 days, 12 hours, 30 minutes"
}
```

#### Comprehensive Report
```http
GET /api/admin/reports/comprehensive?startDate=2025-09-01&endDate=2025-09-22&format=pdf
```

**Description:** Generate comprehensive system report

**Query Parameters:**
- `startDate` (date, optional): Start date
- `endDate` (date, optional): End date
- `format` (string, default: "pdf"): Report format

**Response:**
```json
{
  "generated_at": "2025-09-22T11:15:00",
  "period": {
    "start": "2025-09-01",
    "end": "2025-09-22"
  },
  "format": "pdf",
  "overview": { /* System overview data */ },
  "detailed_analytics": { /* Detailed analytics data */ },
  "performance_metrics": { /* Performance metrics data */ }
}
```

---

### Server Management

#### Server Shutdown
```http
POST /api/admin/system/shutdown?delaySeconds=10&reason=Maintenance
```

**Description:** Initiate graceful server shutdown

**Query Parameters:**
- `delaySeconds` (int, default: 0): Delay before shutdown in seconds
- `reason` (string, optional): Reason for shutdown

**Response:**
```json
{
  "message": "Server shutdown initiated",
  "delay_seconds": 10,
  "reason": "Maintenance",
  "shutdown_time": "2025-09-22T11:25:10"
}
```

#### System Status
```http
GET /api/admin/system/status
```

**Description:** Get current system status

**Response:**
```json
{
  "status": "running",
  "uptime": "5 days, 12 hours, 30 minutes",
  "timestamp": "2025-09-22T11:20:00",
  "version": "1.0.0"
}
```

---

### System Settings

#### Create Backup
```http
POST /api/admin/settings/backup
```

**Description:** Initiate system backup

**Response:**
```json
{
  "initiated_at": "2025-09-22T11:30:00",
  "status": "initiated",
  "message": "System backup initiated successfully"
}
```

---

## Data Models

### Music Entity (with flagging)
```json
{
  "id": 123,
  "name": "Song Title",
  "description": "Song description",
  "price": 15.99,
  "artistUsername": "artist123",
  "genre": "Pop",
  "category": "Single",
  "averageRating": 4.2,
  "totalReviews": 85,
  "isFlagged": false,
  "flaggedAt": null,
  "flaggedByCustomerId": null,
  "createdAt": "2025-09-20T10:00:00",
  "updatedAt": "2025-09-22T09:30:00"
}
```

### Review Entity
```json
{
  "id": 789,
  "musicId": 123,
  "customerId": 456,
  "customerUsername": "customer123",
  "rating": 4,
  "comment": "Great song!",
  "createdAt": "2025-09-22T08:30:00",
  "updatedAt": "2025-09-22T08:30:00"
}
```

---

## Error Responses

### Standard Error Format
```json
{
  "message": "Error description",
  "timestamp": "2025-09-22T11:35:00"
}
```

### Common HTTP Status Codes
- `200 OK`: Success
- `400 Bad Request`: Invalid request parameters
- `401 Unauthorized`: Authentication required
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

---

## Implementation Notes

### Frontend Integration Tips

1. **Pagination**: All list endpoints support pagination with `page` and `size` parameters
2. **Sorting**: Many endpoints support sorting via `sortBy` parameter
3. **Date Ranges**: Use ISO 8601 date format (YYYY-MM-DD) for date parameters
4. **Real-time Updates**: Consider implementing WebSocket connections for real-time admin dashboard updates
5. **Error Handling**: Always handle error responses gracefully with user-friendly messages

### Security Considerations

1. **Role-based Access**: All admin endpoints require `ROLE_ADMIN`
2. **Audit Logging**: All admin actions should be logged for security auditing
3. **Rate Limiting**: Consider implementing rate limiting for admin endpoints
4. **Input Validation**: All inputs are validated server-side

### Performance Recommendations

1. **Caching**: Implement caching for frequently accessed analytics data
2. **Batch Operations**: Use batch operations for bulk data management
3. **Async Processing**: Large reports should be generated asynchronously
4. **Database Indexing**: Ensure proper indexing for flagged content and analytics queries

---

## Example Frontend Implementation

### Admin Dashboard Component
```javascript
// Example React component for admin dashboard
import React, { useState, useEffect } from 'react';

const AdminDashboard = () => {
  const [overview, setOverview] = useState({});
  const [flaggedMusic, setFlaggedMusic] = useState([]);

  useEffect(() => {
    // Fetch system overview
    fetch('/api/admin/analytics/overview', {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    })
    .then(response => response.json())
    .then(data => setOverview(data));

    // Fetch flagged music
    fetch('/api/admin/music/flagged?page=0&size=10', {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    })
    .then(response => response.json())
    .then(data => setFlaggedMusic(data.content));
  }, []);

  const handleUnflagMusic = async (musicId) => {
    try {
      await fetch(`/api/admin/music/${musicId}/unflag`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      // Refresh flagged music list
      // ... refresh logic
    } catch (error) {
      console.error('Error unflagging music:', error);
    }
  };

  return (
    <div className="admin-dashboard">
      <h1>Admin Dashboard</h1>
      
      {/* System Overview */}
      <div className="overview-cards">
        <div className="card">
          <h3>Total Users</h3>
          <p>{overview.totalUsers}</p>
        </div>
        <div className="card">
          <h3>Flagged Content</h3>
          <p>{overview.flaggedMusic}</p>
        </div>
        {/* ... more overview cards */}
      </div>

      {/* Flagged Music Management */}
      <div className="flagged-music">
        <h2>Flagged Music</h2>
        {flaggedMusic.map(music => (
          <div key={music.id} className="flagged-item">
            <h4>{music.name}</h4>
            <p>Artist: {music.artistUsername}</p>
            <p>Flagged: {new Date(music.flaggedAt).toLocaleDateString()}</p>
            <button onClick={() => handleUnflagMusic(music.id)}>
              Unflag
            </button>
          </div>
        ))}
      </div>
    </div>
  );
};

export default AdminDashboard;
```

---

This comprehensive API documentation provides all the necessary information for frontend developers to integrate with the enhanced admin functionality. The system now supports:

✅ **Music flagging system** - Customers can flag inappropriate content
✅ **Admin flagged content management** - View, unflag, and delete flagged music
✅ **Review management** - View all reviews and delete when necessary
✅ **Enhanced analytics** - Comprehensive system metrics and insights
✅ **Server management** - Graceful shutdown and system monitoring
✅ **Performance metrics** - Real-time system performance data

All endpoints are properly secured, documented, and ready for frontend integration.
