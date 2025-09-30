package com.music.musicstore.services;

import com.music.musicstore.models.users.Staff;
import com.music.musicstore.repositories.StaffRepository;
import com.music.musicstore.exceptions.ResourceNotFoundException;
import com.music.musicstore.exceptions.ValidationException;
import com.music.musicstore.exceptions.BusinessRuleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class StaffService {
    private static final Logger logger = LoggerFactory.getLogger(StaffService.class);

    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public StaffService(StaffRepository staffRepository, PasswordEncoder passwordEncoder) {
        this.staffRepository = staffRepository;
        this.passwordEncoder = passwordEncoder;
        logger.info("StaffService initialized successfully");
    }


    public UserDetails loadUserByUsername(String username) {
        logger.debug("Loading staff by username: {}", username);

        if (username == null || username.trim().isEmpty()) {
            logger.error("Username is null or empty");
            throw new ValidationException("Username cannot be null or empty");
        }

        Optional<Staff> staffOpt = staffRepository.findByUsername(username);
        if (staffOpt.isEmpty()) {
            // Don't log this as error since CombinedUserDetailsService expects this to fail for non-staff users
            logger.debug("Staff not found with username: {}", username);
            throw new UsernameNotFoundException("Staff not found with username: " + username);
        }

        Staff staff = staffOpt.get();
        logger.info("Successfully loaded staff: {}", username);
        return staff;
    }

    public Staff save(Staff staff) {
        logger.debug("Saving staff: {}", staff != null ? staff.getUsername() : "null");

        if (staff == null) {
            logger.error("Staff object is null");
            throw new ValidationException("Staff cannot be null");
        }

        try {
            Staff savedStaff = staffRepository.save(staff);
            logger.info("Successfully saved staff: {}", savedStaff.getUsername());
            return savedStaff;
        } catch (Exception e) {
            logger.error("Error saving staff: {}", staff.getUsername(), e);
            throw new RuntimeException("Failed to save staff", e);
        }
    }

    public Staff findByUsername(String username) {
        logger.debug("Finding staff by username: {}", username);

        if (username == null || username.trim().isEmpty()) {
            logger.error("Username is null or empty");
            throw new ValidationException("Username cannot be null or empty");
        }

        try {
            Staff staff = staffRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.error("Staff not found with username: {}", username);
                        return new ResourceNotFoundException("Staff", username);
                    });

            logger.info("Successfully found staff: {}", username);
            return staff;
        } catch (Exception e) {
            logger.error("Error finding staff by username: {}", username, e);
            throw e;
        }
    }

    public Staff findById(Long id) {
        logger.debug("Finding staff by ID: {}", id);

        if (id == null) {
            logger.error("Staff ID is null");
            throw new ValidationException("Staff ID cannot be null");
        }

        try {
            Staff staff = staffRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.error("Staff not found with ID: {}", id);
                        return new ResourceNotFoundException("Staff", id.toString());
                    });

            logger.info("Successfully found staff by ID: {}", id);
            return staff;
        } catch (Exception e) {
            logger.error("Error finding staff by ID: {}", id, e);
            throw e;
        }
    }

    public long count() {
        logger.debug("Counting total staff members");

        try {
            long count = staffRepository.count();
            logger.info("Total staff count: {}", count);
            return count;
        } catch (Exception e) {
            logger.error("Error counting staff members", e);
            throw new RuntimeException("Failed to count staff members", e);
        }
    }

    public Staff createStaff(Staff staff) {
        logger.debug("Creating new staff: {}", staff != null ? staff.getUsername() : "null");

        if (staff == null) {
            logger.error("Staff object is null");
            throw new ValidationException("Staff cannot be null");
        }

        if (staff.getUsername() == null || staff.getUsername().trim().isEmpty()) {
            logger.error("Staff username is null or empty");
            throw new ValidationException("Staff username cannot be null or empty");
        }

        if (staff.getPassword() == null || staff.getPassword().trim().isEmpty()) {
            logger.error("Staff password is null or empty");
            throw new ValidationException("Staff password cannot be null or empty");
        }

        try {
            // Check if username already exists
            Optional<Staff> existingStaff = staffRepository.findByUsername(staff.getUsername());
            if (existingStaff.isPresent()) {
                logger.error("Username already exists: {}", staff.getUsername());
                throw new BusinessRuleException("Username already exists: " + staff.getUsername());
            }

            staff.setPassword(passwordEncoder.encode(staff.getPassword()));
            Staff savedStaff = staffRepository.save(staff);

            logger.info("Successfully created staff: {}", savedStaff.getUsername());
            return savedStaff;
        } catch (Exception e) {
            logger.error("Error creating staff: {}", staff.getUsername(), e);
            throw e;
        }
    }

    public List<Staff> getAllStaff() {
        logger.debug("Retrieving all staff members");

        try {
            List<Staff> staffList = staffRepository.findAll();
            logger.info("Successfully retrieved {} staff members", staffList.size());
            return staffList;
        } catch (Exception e) {
            logger.error("Error retrieving all staff members", e);
            throw new RuntimeException("Failed to retrieve staff members", e);
        }
    }

    public void deleteStaff(Long id) {
        logger.debug("Deleting staff with ID: {}", id);

        if (id == null) {
            logger.error("Staff ID is null");
            throw new ValidationException("Staff ID cannot be null");
        }

        try {
            // Check if staff exists before deletion
            Optional<Staff> staff = staffRepository.findById(id);
            if (staff.isEmpty()) {
                logger.error("Staff not found for deletion with ID: {}", id);
                throw new ResourceNotFoundException("Staff", id.toString());
            }

            staffRepository.deleteById(id);
            logger.info("Successfully deleted staff with ID: {}", id);
        } catch (Exception e) {
            logger.error("Error deleting staff with ID: {}", id, e);
            throw new RuntimeException("Failed to delete staff", e);
        }
    }

    public void updateStaff(Staff staff) {
        logger.debug("Updating staff: {}", staff != null ? staff.getUsername() : "null");

        if (staff == null) {
            logger.error("Staff object is null");
            throw new ValidationException("Staff cannot be null");
        }

        if (staff.getId() == null) {
            logger.error("Staff ID is null for update");
            throw new ValidationException("Staff ID cannot be null for update");
        }

        try {
            Staff existingStaff = staffRepository.findById(staff.getId())
                    .orElseThrow(() -> {
                        logger.error("Staff not found for update with ID: {}", staff.getId());
                        return new ResourceNotFoundException("Staff", staff.getId().toString());
                    });

            if (staff.getPassword() != null && !staff.getPassword().isEmpty()) {
                staff.setPassword(passwordEncoder.encode(staff.getPassword()));
            }

            Staff updatedStaff = staffRepository.save(staff);
            logger.info("Successfully updated staff: {} (ID: {})", updatedStaff.getUsername(), updatedStaff.getId());
        } catch (Exception e) {
            logger.error("Error updating staff: {}", staff.getUsername(), e);
            throw e;
        }
    }

    public Optional<Staff> findByIdOptional(Long id) {
        logger.debug("Finding staff by ID (optional): {}", id);

        if (id == null) {
            logger.error("Staff ID is null");
            throw new ValidationException("Staff ID cannot be null");
        }

        try {
            Optional<Staff> staff = staffRepository.findById(id);
            if (staff.isPresent()) {
                logger.info("Successfully found staff by ID: {}", id);
            } else {
                logger.debug("Staff not found by ID: {}", id);
            }
            return staff;
        } catch (Exception e) {
            logger.error("Error finding staff by ID: {}", id, e);
            throw new RuntimeException("Failed to find staff by ID", e);
        }
    }
}
