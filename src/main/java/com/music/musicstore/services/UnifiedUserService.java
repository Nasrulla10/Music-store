package com.music.musicstore.services;

import com.music.musicstore.dto.UserDto;
import com.music.musicstore.models.users.*;
import com.music.musicstore.exceptions.ValidationException;
import com.music.musicstore.exceptions.BusinessRuleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class UnifiedUserService {
    private static final Logger logger = LoggerFactory.getLogger(UnifiedUserService.class);

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ArtistService artistService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private StaffService staffService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UnifiedUserService() {
        logger.info("UnifiedUserService initialized successfully");
    }

    public UserDto createUser(String username, String password, String email, String role,
                             String firstName, String lastName, String artistName, String photoUrl) {
        logger.debug("Creating user: {} with role: {}", username, role);

        if (username == null || username.trim().isEmpty()) {
            logger.error("Username is null or empty");
            throw new ValidationException("Username cannot be null or empty");
        }

        if (password == null || password.trim().isEmpty()) {
            logger.error("Password is null or empty");
            throw new ValidationException("Password cannot be null or empty");
        }

        if (role == null || role.trim().isEmpty()) {
            logger.error("Role is null or empty");
            throw new ValidationException("Role cannot be null or empty");
        }

        try {
            String encodedPassword = passwordEncoder.encode(password);

            switch (role.toUpperCase()) {
                case "CUSTOMER":
                    Customer customer = new Customer();
                    customer.setUsername(username);
                    customer.setPassword(encodedPassword);
                    customer.setEmail(email);
                    customer.setFirstName(firstName);
                    customer.setLastName(lastName);
                    customer.setRole("ROLE_CUSTOMER"); // Explicitly set the role
                    Customer savedCustomer = customerService.createCustomer(customer);
                    logger.info("Successfully created customer: {}", username);
                    return convertCustomerToDto(savedCustomer);

                case "ARTIST":
                    Artist artist = new Artist();
                    artist.setUserName(username);
                    artist.setPassword(encodedPassword);
                    artist.setEmail(email);
                    artist.setFirstName(firstName);
                    artist.setLastName(lastName);
                    artist.setArtistName(artistName);
                    artist.setPhotoUrl(photoUrl);
                    Artist savedArtist = artistService.createArtist(artist);
                    logger.info("Successfully created artist: {}", username);
                    return convertArtistToDto(savedArtist);

                case "ADMIN":
                    Admin admin = new Admin();
                    admin.setUsername(username);
                    admin.setPassword(encodedPassword);
                    admin.setEmail(email);
                    admin.setFirstName(firstName);
                    admin.setLastName(lastName);
                    Admin savedAdmin = adminService.createAdmin(admin);
                    logger.info("Successfully created admin: {}", username);
                    return convertAdminToDto(savedAdmin);

                case "STAFF":
                    Staff staff = new Staff();
                    staff.setUsername(username);
                    staff.setPassword(encodedPassword);
                    staff.setEmail(email);
                    staff.setFirstName(firstName);
                    staff.setLastName(lastName);
                    Staff savedStaff = staffService.createStaff(staff);
                    logger.info("Successfully created staff: {}", username);
                    return convertStaffToDto(savedStaff);

                default:
                    logger.error("Invalid role: {}", role);
                    throw new ValidationException("Invalid role: " + role);
            }
        } catch (Exception e) {
            logger.error("Error creating user: {} with role: {}", username, role, e);
            throw e;
        }
    }

    public UserDetails loadUserByUsername(String username, String role) {
        logger.debug("Loading user by username: {} with role: {}", username, role);

        if (username == null || username.trim().isEmpty()) {
            logger.error("Username is null or empty");
            throw new ValidationException("Username cannot be null or empty");
        }

        if (role == null || role.trim().isEmpty()) {
            logger.error("Role is null or empty");
            throw new ValidationException("Role cannot be null or empty");
        }

        try {
            return switch (role.toUpperCase()) {
                case "CUSTOMER" -> customerService.loadUserByUsername(username);
                case "ARTIST" -> artistService.loadUserByUsername(username);
                case "ADMIN" -> adminService.loadUserByUsername(username);
                case "STAFF" -> staffService.loadUserByUsername(username);
                default -> {
                    logger.error("Invalid role for user loading: {}", role);
                    throw new ValidationException("Invalid role: " + role);
                }
            };
        } catch (Exception e) {
            logger.error("Error loading user by username: {} with role: {}", username, role, e);
            throw e;
        }
    }

    public Map<String, Long> getUserCounts() {
        logger.debug("Getting user counts");

        try {
            Map<String, Long> counts = new HashMap<>();
            counts.put("customers", customerService.count());
            counts.put("artists", artistService.count());
            counts.put("admins", adminService.count());
            counts.put("staff", staffService.count());

            long totalUsers = counts.values().stream().mapToLong(Long::longValue).sum();
            counts.put("total", totalUsers);

            logger.info("Successfully retrieved user counts: {}", counts);
            return counts;
        } catch (Exception e) {
            logger.error("Error getting user counts", e);
            throw new RuntimeException("Failed to get user counts", e);
        }
    }

    public UserDto findUserByUsername(String username, String role) {
        logger.debug("Finding user by username: {} with role: {}", username, role);

        if (username == null || username.trim().isEmpty()) {
            logger.error("Username is null or empty");
            throw new ValidationException("Username cannot be null or empty");
        }

        if (role == null || role.trim().isEmpty()) {
            logger.error("Role is null or empty");
            throw new ValidationException("Role cannot be null or empty");
        }

        try {
            return switch (role.toUpperCase()) {
                case "CUSTOMER" -> {
                    Customer customer = customerService.findByUsername(username);
                    yield convertCustomerToDto(customer);
                }
                case "ARTIST" -> {
                    Artist artist = artistService.findByUsername(username);
                    yield convertArtistToDto(artist);
                }
                case "ADMIN" -> {
                    Admin admin = adminService.findByUsername(username);
                    yield convertAdminToDto(admin);
                }
                case "STAFF" -> {
                    Staff staff = staffService.findByUsername(username);
                    yield convertStaffToDto(staff);
                }
                default -> {
                    logger.error("Invalid role for user finding: {}", role);
                    throw new ValidationException("Invalid role: " + role);
                }
            };
        } catch (Exception e) {
            logger.error("Error finding user by username: {} with role: {}", username, role, e);
            throw e;
        }
    }

    // Additional methods for admin functionality
    public long getTotalUsersCount() {
        return customerService.count() + artistService.count() + staffService.count() + adminService.count();
    }

    public UserDto getUserById(Long id) {
        logger.debug("Finding user by ID: {}", id);

        if (id == null) {
            logger.error("User ID is null");
            throw new ValidationException("User ID cannot be null");
        }

        try {
            // Try to find user in all repositories
            try {
                Customer customer = customerService.findById(id);
                if (customer != null) {
                    logger.info("Found customer with ID: {}", id);
                    return convertCustomerToDto(customer);
                }
            } catch (Exception e) {
                logger.debug("Customer not found with ID: {}", id);
            }

            try {
                Artist artist = artistService.findById(id);
                if (artist != null) {
                    logger.info("Found artist with ID: {}", id);
                    return convertArtistToDto(artist);
                }
            } catch (Exception e) {
                logger.debug("Artist not found with ID: {}", id);
            }

            try {
                Staff staff = staffService.findById(id);
                if (staff != null) {
                    logger.info("Found staff with ID: {}", id);
                    return convertStaffToDto(staff);
                }
            } catch (Exception e) {
                logger.debug("Staff not found with ID: {}", id);
            }

            try {
                Admin admin = adminService.findById(id);
                if (admin != null) {
                    logger.info("Found admin with ID: {}", id);
                    return convertAdminToDto(admin);
                }
            } catch (Exception e) {
                logger.debug("Admin not found with ID: {}", id);
            }

            logger.error("User not found with ID: {}", id);
            throw new ValidationException("User not found with id: " + id);
        } catch (Exception e) {
            logger.error("Error finding user by ID: {}", id, e);
            throw e;
        }
    }

    public UserDto updateUser(Long id, Object updateRequest) {
        logger.debug("Updating user with ID: {}", id);

        if (id == null) {
            logger.error("User ID is null");
            throw new ValidationException("User ID cannot be null");
        }

        if (updateRequest == null) {
            logger.error("Update request is null");
            throw new ValidationException("Update request cannot be null");
        }

        try {
            // First find the user to determine their type
            UserDto existingUser = getUserById(id);
            String role = existingUser.getRole();

            logger.debug("Updating user with role: {}", role);

            switch (role.toUpperCase()) {
                case "CUSTOMER":
                    Customer customer = customerService.findById(id);
                    updateCustomerFromRequest(customer, updateRequest);
                    customerService.updateCustomer(customer);
                    logger.info("Successfully updated customer with ID: {}", id);
                    return convertCustomerToDto(customer);

                case "ARTIST":
                    Artist artist = artistService.findById(id);
                    updateArtistFromRequest(artist, updateRequest);
                    artistService.updateArtist(artist);
                    logger.info("Successfully updated artist with ID: {}", id);
                    return convertArtistToDto(artist);

                case "ADMIN":
                    Admin admin = adminService.findById(id);
                    updateAdminFromRequest(admin, updateRequest);
                    adminService.updateAdmin(admin);
                    logger.info("Successfully updated admin with ID: {}", id);
                    return convertAdminToDto(admin);

                case "STAFF":
                    Staff staff = staffService.findById(id);
                    updateStaffFromRequest(staff, updateRequest);
                    staffService.updateStaff(staff);
                    logger.info("Successfully updated staff with ID: {}", id);
                    return convertStaffToDto(staff);

                default:
                    logger.error("Invalid role for user update: {}", role);
                    throw new ValidationException("Invalid role: " + role);
            }
        } catch (Exception e) {
            logger.error("Error updating user with ID: {}", id, e);
            throw e;
        }
    }

    public void deleteUser(Long id) {
        logger.debug("Deleting user with ID: {}", id);

        if (id == null) {
            logger.error("User ID is null");
            throw new ValidationException("User ID cannot be null");
        }

        try {
            // First find the user to determine their type
            UserDto existingUser = getUserById(id);
            String role = existingUser.getRole();

            logger.debug("Deleting user with role: {}", role);

            switch (role.toUpperCase()) {
                case "CUSTOMER":
                    customerService.deleteCustomer(id);
                    logger.info("Successfully deleted customer with ID: {}", id);
                    break;

                case "ARTIST":
                    // Note: Artist deletion might need special handling for their music
                    logger.warn("Artist deletion may affect associated music content");
                    // artistService.deleteArtist(id); // Would need to be implemented
                    throw new BusinessRuleException("Artist deletion not fully implemented - requires handling of associated music");

                case "ADMIN":
                    adminService.deleteAdmin(id);
                    logger.info("Successfully deleted admin with ID: {}", id);
                    break;

                case "STAFF":
                    staffService.deleteStaff(id);
                    logger.info("Successfully deleted staff with ID: {}", id);
                    break;

                default:
                    logger.error("Invalid role for user deletion: {}", role);
                    throw new ValidationException("Invalid role: " + role);
            }
        } catch (Exception e) {
            logger.error("Error deleting user with ID: {}", id, e);
            throw e;
        }
    }

    public void updateUserStatus(Long id, boolean active) {
        logger.debug("Updating user status for ID: {} to active: {}", id, active);

        if (id == null) {
            logger.error("User ID is null");
            throw new ValidationException("User ID cannot be null");
        }

        try {
            // First find the user to determine their type
            UserDto existingUser = getUserById(id);
            String role = existingUser.getRole();

            logger.debug("Updating status for user with role: {}", role);

            switch (role.toUpperCase()) {
                case "CUSTOMER":
                    Customer customer = customerService.findById(id);
                    customer.setEnabled(active);
                    customerService.updateCustomer(customer);
                    logger.info("Successfully updated customer status for ID: {} to {}", id, active);
                    break;

                case "ARTIST":
                    Artist artist = artistService.findById(id);
                    artist.setEnabled(active);
                    artistService.updateArtist(artist);
                    logger.info("Successfully updated artist status for ID: {} to {}", id, active);
                    break;

                case "ADMIN":
                    Admin admin = adminService.findById(id);
                    admin.setEnabled(active);
                    adminService.updateAdmin(admin);
                    logger.info("Successfully updated admin status for ID: {} to {}", id, active);
                    break;

                case "STAFF":
                    Staff staff = staffService.findById(id);
                    staff.setEnabled(active);
                    staffService.updateStaff(staff);
                    logger.info("Successfully updated staff status for ID: {} to {}", id, active);
                    break;

                default:
                    logger.error("Invalid role for status update: {}", role);
                    throw new ValidationException("Invalid role: " + role);
            }
        } catch (Exception e) {
            logger.error("Error updating user status for ID: {}", id, e);
            throw e;
        }
    }

    // NEW: Missing methods for admin analytics
    public long getTodayRegistrationsCount() {
        logger.debug("Getting today's registration count");
        try {
            LocalDate today = LocalDate.now();
            // This would need proper date filtering in the repositories
            // For now, return a placeholder value
            return 0; // Placeholder
        } catch (Exception e) {
            logger.error("Error getting today's registration count", e);
            return 0;
        }
    }

    public Map<String, Object> getUserGrowthAnalytics(LocalDate startDate, LocalDate endDate) {
        logger.debug("Getting user growth analytics from {} to {}", startDate, endDate);
        try {
            Map<String, Object> analytics = new HashMap<>();
            analytics.put("newUsers", 0); // Placeholder
            analytics.put("growthRate", 0.0); // Placeholder
            analytics.put("activeUsers", 0); // Placeholder
            return analytics;
        } catch (Exception e) {
            logger.error("Error getting user growth analytics", e);
            return new HashMap<>();
        }
    }

    public Map<String, Long> getUserCountByRole() {
        logger.debug("Getting user count by role");
        try {
            Map<String, Long> counts = new HashMap<>();
            counts.put("CUSTOMER", customerService.count());
            counts.put("ARTIST", artistService.count());
            counts.put("ADMIN", adminService.count());
            counts.put("STAFF", staffService.count());
            return counts;
        } catch (Exception e) {
            logger.error("Error getting user count by role", e);
            return new HashMap<>();
        }
    }

    public long getActiveUsersCount() {
        logger.debug("Getting active users count");
        try {
            // This would need proper activity tracking
            // For now, return total users as placeholder
            return getTotalUsersCount();
        } catch (Exception e) {
            logger.error("Error getting active users count", e);
            return 0;
        }
    }

    public Page<UserDto> getAllUsers(int page, int size, String role) {
        logger.debug("Getting paginated users: page={}, size={}, role={}", page, size, role);

        if (page < 0) {
            logger.error("Page number cannot be negative: {}", page);
            throw new ValidationException("Page number cannot be negative");
        }

        if (size <= 0) {
            logger.error("Page size must be positive: {}", size);
            throw new ValidationException("Page size must be positive");
        }

        try {
            Pageable pageable = PageRequest.of(page, size);
            final List<UserDto> allUsers = new java.util.ArrayList<>();

            // Filter by role if specified
            if (role != null && !role.trim().isEmpty()) {
                switch (role.toUpperCase()) {
                    case "CUSTOMER":
                        customerService.getAllCustomers().forEach(customer ->
                            allUsers.add(convertCustomerToDto(customer)));
                        break;
                    case "ARTIST":
                        artistService.getAllArtists().forEach(artist ->
                            allUsers.add(convertArtistToDto(artist)));
                        break;
                    case "ADMIN":
                        adminService.getAllAdmins().forEach(admin ->
                            allUsers.add(convertAdminToDto(admin)));
                        break;
                    case "STAFF":
                        staffService.getAllStaff().forEach(staff ->
                            allUsers.add(convertStaffToDto(staff)));
                        break;
                    default:
                        logger.error("Invalid role filter: {}", role);
                        throw new ValidationException("Invalid role: " + role);
                }
            } else {
                // Get all users
                List<UserDto> allUsersList = getAllUsers();
                allUsers.addAll(allUsersList);
            }

            // Manual pagination
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), allUsers.size());
            List<UserDto> pageContent = allUsers.subList(start, end);

            Page<UserDto> result = new org.springframework.data.domain.PageImpl<>(
                pageContent, pageable, allUsers.size());

            logger.info("Successfully retrieved {} users (page {} of total {})",
                       pageContent.size(), page, result.getTotalPages());
            return result;
        } catch (Exception e) {
            logger.error("Error getting paginated users", e);
            throw new RuntimeException("Failed to get paginated users", e);
        }
    }

    // Helper methods for updating user entities from request objects
    private void updateCustomerFromRequest(Customer customer, Object updateRequest) {
        // This would need proper DTO mapping based on your update request structure
        // For now, implementing basic field updates if the request is a map
        if (updateRequest instanceof Map) {
            Map<String, Object> updates = (Map<String, Object>) updateRequest;

            if (updates.containsKey("firstName")) {
                customer.setFirstName((String) updates.get("firstName"));
            }
            if (updates.containsKey("lastName")) {
                customer.setLastName((String) updates.get("lastName"));
            }
            if (updates.containsKey("email")) {
                customer.setEmail((String) updates.get("email"));
            }
            if (updates.containsKey("password") && updates.get("password") != null) {
                customer.setPassword(passwordEncoder.encode((String) updates.get("password")));
            }
        }
    }

    private void updateArtistFromRequest(Artist artist, Object updateRequest) {
        if (updateRequest instanceof Map) {
            Map<String, Object> updates = (Map<String, Object>) updateRequest;

            if (updates.containsKey("firstName")) {
                artist.setFirstName((String) updates.get("firstName"));
            }
            if (updates.containsKey("lastName")) {
                artist.setLastName((String) updates.get("lastName"));
            }
            if (updates.containsKey("email")) {
                artist.setEmail((String) updates.get("email"));
            }
            if (updates.containsKey("artistName")) {
                artist.setArtistName((String) updates.get("artistName"));
            }
            if (updates.containsKey("photoUrl")) {
                artist.setPhotoUrl((String) updates.get("photoUrl"));
            }
            if (updates.containsKey("password") && updates.get("password") != null) {
                artist.setPassword(passwordEncoder.encode((String) updates.get("password")));
            }
        }
    }

    private void updateAdminFromRequest(Admin admin, Object updateRequest) {
        if (updateRequest instanceof Map) {
            Map<String, Object> updates = (Map<String, Object>) updateRequest;

            if (updates.containsKey("firstName")) {
                admin.setFirstName((String) updates.get("firstName"));
            }
            if (updates.containsKey("lastName")) {
                admin.setLastName((String) updates.get("lastName"));
            }
            if (updates.containsKey("email")) {
                admin.setEmail((String) updates.get("email"));
            }
            if (updates.containsKey("password") && updates.get("password") != null) {
                admin.setPassword(passwordEncoder.encode((String) updates.get("password")));
            }
        }
    }

    private void updateStaffFromRequest(Staff staff, Object updateRequest) {
        if (updateRequest instanceof Map) {
            Map<String, Object> updates = (Map<String, Object>) updateRequest;

            if (updates.containsKey("firstName")) {
                staff.setFirstName((String) updates.get("firstName"));
            }
            if (updates.containsKey("lastName")) {
                staff.setLastName((String) updates.get("lastName"));
            }
            if (updates.containsKey("email")) {
                staff.setEmail((String) updates.get("email"));
            }
            if (updates.containsKey("password") && updates.get("password") != null) {
                staff.setPassword(passwordEncoder.encode((String) updates.get("password")));
            }
        }
    }

    // Additional utility methods
    public List<UserDto> searchUsers(String searchTerm) {
        logger.debug("Searching users with term: {}", searchTerm);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            logger.error("Search term is null or empty");
            throw new ValidationException("Search term cannot be null or empty");
        }

        try {
            List<UserDto> results = new java.util.ArrayList<>();
            String lowerSearchTerm = searchTerm.toLowerCase();

            // Search in all user types
            getAllUsers().stream()
                .filter(user ->
                    (user.getUsername() != null && user.getUsername().toLowerCase().contains(lowerSearchTerm)) ||
                    (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerSearchTerm)) ||
                    (user.getFirstName() != null && user.getFirstName().toLowerCase().contains(lowerSearchTerm)) ||
                    (user.getLastName() != null && user.getLastName().toLowerCase().contains(lowerSearchTerm)) ||
                    (user.getArtistName() != null && user.getArtistName().toLowerCase().contains(lowerSearchTerm))
                )
                .forEach(results::add);

            logger.info("Found {} users matching search term: {}", results.size(), searchTerm);
            return results;
        } catch (Exception e) {
            logger.error("Error searching users with term: {}", searchTerm, e);
            throw new RuntimeException("Failed to search users", e);
        }
    }

    public boolean isUsernameAvailable(String username) {
        logger.debug("Checking username availability: {}", username);

        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        try {
            // Check in all user types
            try {
                customerService.findByUsername(username);
                return false; // Username found, not available
            } catch (Exception e) {
                // Username not found in customers, continue checking
            }

            try {
                artistService.findByUsername(username);
                return false; // Username found, not available
            } catch (Exception e) {
                // Username not found in artists, continue checking
            }

            try {
                adminService.findByUsername(username);
                return false; // Username found, not available
            } catch (Exception e) {
                // Username not found in admins, continue checking
            }

            try {
                staffService.findByUsername(username);
                return false; // Username found, not available
            } catch (Exception e) {
                // Username not found in staff, available
            }

            logger.info("Username '{}' is available", username);
            return true; // Username not found anywhere, available
        } catch (Exception e) {
            logger.error("Error checking username availability: {}", username, e);
            return false; // Error occurred, assume not available for safety
        }
    }

    // Helper methods to convert entities to DTOs
    private UserDto convertCustomerToDto(Customer customer) {
        UserDto dto = new UserDto();
        dto.setId(customer.getId());
        dto.setUsername(customer.getUsername());
        dto.setEmail(customer.getEmail());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setRole("CUSTOMER");
        dto.setEnabled(customer.isEnabled());
        dto.setCreatedAt(customer.getCreatedAt());
        return dto;
    }

    private UserDto convertArtistToDto(Artist artist) {
        UserDto dto = new UserDto();
        dto.setId(artist.getId());
        dto.setUsername(artist.getUserName());
        dto.setEmail(artist.getEmail());
        dto.setFirstName(artist.getFirstName());
        dto.setLastName(artist.getLastName());
        dto.setRole("ARTIST");
        dto.setArtistName(artist.getArtistName());
        dto.setEnabled(artist.isEnabled());
        dto.setCreatedAt(artist.getCreatedAt());
        return dto;
    }

    private UserDto convertAdminToDto(Admin admin) {
        UserDto dto = new UserDto();
        dto.setId(admin.getId());
        dto.setUsername(admin.getUsername());
        dto.setEmail(admin.getEmail());
        dto.setFirstName(admin.getFirstName());
        dto.setLastName(admin.getLastName());
        dto.setRole("ADMIN");
        dto.setEnabled(admin.isEnabled());
        dto.setCreatedAt(admin.getCreatedAt());
        return dto;
    }

    private UserDto convertStaffToDto(Staff staff) {
        UserDto dto = new UserDto();
        dto.setId(staff.getId());
        dto.setUsername(staff.getUsername());
        dto.setEmail(staff.getEmail());
        dto.setFirstName(staff.getFirstName());
        dto.setLastName(staff.getLastName());
        dto.setRole("STAFF");
        dto.setEnabled(staff.isEnabled());
        dto.setCreatedAt(staff.getCreatedAt());
        return dto;
    }

    public List<UserDto> getAllUsers() {
        logger.debug("Getting all users");

        try {
            List<UserDto> allUsers = new java.util.ArrayList<>();

            customerService.getAllCustomers().forEach(customer ->
                allUsers.add(convertCustomerToDto(customer)));
            artistService.getAllArtists().forEach(artist ->
                allUsers.add(convertArtistToDto(artist)));
            adminService.getAllAdmins().forEach(admin ->
                allUsers.add(convertAdminToDto(admin)));
            staffService.getAllStaff().forEach(staff ->
                allUsers.add(convertStaffToDto(staff)));

            logger.info("Successfully retrieved {} total users", allUsers.size());
            return allUsers;
        } catch (Exception e) {
            logger.error("Error getting all users", e);
            throw new RuntimeException("Failed to get all users", e);
        }
    }

    public void deleteUser(Long userId, String role) {
        logger.debug("Deleting user ID: {} with role: {}", userId, role);

        if (userId == null) {
            logger.error("User ID is null");
            throw new ValidationException("User ID cannot be null");
        }

        if (role == null || role.trim().isEmpty()) {
            logger.error("Role is null or empty");
            throw new ValidationException("Role cannot be null or empty");
        }

        try {
            switch (role.toUpperCase()) {
                case "CUSTOMER":
                    // Customer deletion would need to be implemented in CustomerService
                    logger.warn("Customer deletion not implemented");
                    throw new BusinessRuleException("Customer deletion not implemented");
                case "ARTIST":
                    // Artist deletion would need to be implemented in ArtistService
                    logger.warn("Artist deletion not implemented");
                    throw new BusinessRuleException("Artist deletion not implemented");
                case "ADMIN":
                    adminService.deleteAdmin(userId);
                    break;
                case "STAFF":
                    staffService.deleteStaff(userId);
                    break;
                default:
                    logger.error("Invalid role for user deletion: {}", role);
                    throw new ValidationException("Invalid role: " + role);
            }
            logger.info("Successfully deleted user ID: {} with role: {}", userId, role);
        } catch (Exception e) {
            logger.error("Error deleting user ID: {} with role: {}", userId, role, e);
            throw e;
        }
    }

    public UserDto getUserInfo(UserDetails userDetails) {
        logger.debug("Getting user info for user: {}", userDetails != null ? userDetails.getUsername() : "null");

        if (userDetails == null) {
            logger.error("UserDetails is null");
            throw new ValidationException("UserDetails cannot be null");
        }

        try {
            String username = userDetails.getUsername();

            // Try to find user in all repositories
            try {
                Customer customer = customerService.findByUsername(username);
                logger.info("Found customer info for: {}", username);
                return convertCustomerToDto(customer);
            } catch (Exception e) {
                logger.debug("Customer not found with username: {}", username);
            }

            try {
                Artist artist = artistService.findByUsername(username);
                logger.info("Found artist info for: {}", username);
                return convertArtistToDto(artist);
            } catch (Exception e) {
                logger.debug("Artist not found with username: {}", username);
            }

            try {
                Staff staff = staffService.findByUsername(username);
                logger.info("Found staff info for: {}", username);
                return convertStaffToDto(staff);
            } catch (Exception e) {
                logger.debug("Staff not found with username: {}", username);
            }

            try {
                Admin admin = adminService.findByUsername(username);
                logger.info("Found admin info for: {}", username);
                return convertAdminToDto(admin);
            } catch (Exception e) {
                logger.debug("Admin not found with username: {}", username);
            }

            logger.error("User not found with username: {}", username);
            throw new ValidationException("User not found with username: " + username);
        } catch (Exception e) {
            logger.error("Error getting user info for: {}", userDetails.getUsername(), e);
            throw e;
        }
    }
}
