package com.music.musicstore.services;

import com.music.musicstore.models.users.Admin;
import com.music.musicstore.repositories.AdminRepository;
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
public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminService(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        logger.info("AdminService initialized successfully");
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Loading admin by username: {}", username);

        if (username == null || username.trim().isEmpty()) {
            logger.error("Username is null or empty");
            throw new ValidationException("Username cannot be null or empty");
        }

        Optional<Admin> adminOpt = adminRepository.findByUsername(username);
        if (adminOpt.isEmpty()) {
            // Don't log this as error since CombinedUserDetailsService expects this to fail for non-admin users
            logger.debug("Admin not found with username: {}", username);
            throw new UsernameNotFoundException("Admin not found with username: " + username);
        }

        Admin admin = adminOpt.get();
        logger.info("Successfully loaded admin: {}", username);
        return admin;
    }

    public Admin createAdmin(Admin admin) {
        logger.debug("Creating new admin: {}", admin != null ? admin.getUsername() : "null");

        if (admin == null) {
            logger.error("Admin object is null");
            throw new ValidationException("Admin cannot be null");
        }

        if (admin.getUsername() == null || admin.getUsername().trim().isEmpty()) {
            logger.error("Admin username is null or empty");
            throw new ValidationException("Admin username cannot be null or empty");
        }

        if (admin.getEmail() == null || admin.getEmail().trim().isEmpty()) {
            logger.error("Admin email is null or empty");
            throw new ValidationException("Admin email cannot be null or empty");
        }

        if (admin.getPassword() == null || admin.getPassword().trim().isEmpty()) {
            logger.error("Admin password is null or empty");
            throw new ValidationException("Admin password cannot be null or empty");
        }

        try {
            // Check if username or email already exists
            if (adminRepository.existsByUsername(admin.getUsername())) {
                logger.error("Username already exists: {}", admin.getUsername());
                throw new BusinessRuleException("Username already exists: " + admin.getUsername());
            }

            if (adminRepository.existsByEmail(admin.getEmail())) {
                logger.error("Email already exists: {}", admin.getEmail());
                throw new BusinessRuleException("Email already exists: " + admin.getEmail());
            }

            // Encode password
            admin.setPassword(passwordEncoder.encode(admin.getPassword()));
            Admin savedAdmin = adminRepository.save(admin);

            logger.info("Successfully created admin: {} (ID: {})", savedAdmin.getUsername(), savedAdmin.getId());
            return savedAdmin;
        } catch (Exception e) {
            logger.error("Error creating admin: {}", admin.getUsername(), e);
            throw e;
        }
    }

    public List<Admin> getAllAdmins() {
        logger.debug("Retrieving all admins");

        try {
            List<Admin> admins = adminRepository.findAll();
            logger.info("Successfully retrieved {} admins", admins.size());
            return admins;
        } catch (Exception e) {
            logger.error("Error retrieving all admins", e);
            throw new RuntimeException("Failed to retrieve admins", e);
        }
    }

    public Admin findByUsername(String username) {
        logger.debug("Finding admin by username: {}", username);

        if (username == null || username.trim().isEmpty()) {
            logger.error("Username is null or empty");
            throw new ValidationException("Username cannot be null or empty");
        }

        try {
            Admin admin = adminRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.error("Admin not found with username: {}", username);
                        return new ResourceNotFoundException("Admin", username);
                    });

            logger.info("Successfully found admin: {}", username);
            return admin;
        } catch (Exception e) {
            logger.error("Error finding admin by username: {}", username, e);
            throw e;
        }
    }

    public Admin findById(Long id) {
        logger.debug("Finding admin by ID: {}", id);

        if (id == null) {
            logger.error("Admin ID is null");
            throw new ValidationException("Admin ID cannot be null");
        }

        try {
            Admin admin = adminRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.error("Admin not found with ID: {}", id);
                        return new ResourceNotFoundException("Admin", id.toString());
                    });

            logger.info("Successfully found admin by ID: {}", id);
            return admin;
        } catch (Exception e) {
            logger.error("Error finding admin by ID: {}", id, e);
            throw e;
        }
    }

    public Admin save(Admin admin) {
        logger.debug("Saving admin: {}", admin != null ? admin.getUsername() : "null");

        if (admin == null) {
            logger.error("Admin object is null");
            throw new ValidationException("Admin cannot be null");
        }

        try {
            Admin savedAdmin = adminRepository.save(admin);
            logger.info("Successfully saved admin: {}", savedAdmin.getUsername());
            return savedAdmin;
        } catch (Exception e) {
            logger.error("Error saving admin: {}", admin.getUsername(), e);
            throw new RuntimeException("Failed to save admin", e);
        }
    }

    public void deleteAdmin(Long id) {
        logger.debug("Deleting admin with ID: {}", id);

        if (id == null) {
            logger.error("Admin ID is null");
            throw new ValidationException("Admin ID cannot be null");
        }

        try {
            // Check if admin exists before deletion
            Optional<Admin> admin = adminRepository.findById(id);
            if (admin.isEmpty()) {
                logger.error("Admin not found for deletion with ID: {}", id);
                throw new ResourceNotFoundException("Admin", id.toString());
            }

            adminRepository.deleteById(id);
            logger.info("Successfully deleted admin with ID: {}", id);
        } catch (Exception e) {
            logger.error("Error deleting admin with ID: {}", id, e);
            throw new RuntimeException("Failed to delete admin", e);
        }
    }

    public long count() {
        logger.debug("Counting total admins");

        try {
            long count = adminRepository.count();
            logger.info("Total admin count: {}", count);
            return count;
        } catch (Exception e) {
            logger.error("Error counting admins", e);
            throw new RuntimeException("Failed to count admins", e);
        }
    }

    public Optional<Admin> findByIdOptional(Long id) {
        logger.debug("Finding admin by ID (optional): {}", id);

        if (id == null) {
            logger.error("Admin ID is null");
            throw new ValidationException("Admin ID cannot be null");
        }

        try {
            Optional<Admin> admin = adminRepository.findById(id);
            if (admin.isPresent()) {
                logger.info("Successfully found admin by ID: {}", id);
            } else {
                logger.debug("Admin not found by ID: {}", id);
            }
            return admin;
        } catch (Exception e) {
            logger.error("Error finding admin by ID: {}", id, e);
            throw new RuntimeException("Failed to find admin by ID", e);
        }
    }

    public void updateAdmin(Admin admin) {
        logger.debug("Updating admin: {}", admin != null ? admin.getUsername() : "null");

        if (admin == null) {
            logger.error("Admin object is null");
            throw new ValidationException("Admin cannot be null");
        }

        if (admin.getId() == null) {
            logger.error("Admin ID is null for update");
            throw new ValidationException("Admin ID cannot be null for update");
        }

        try {
            Admin existingAdmin = adminRepository.findById(admin.getId())
                    .orElseThrow(() -> {
                        logger.error("Admin not found for update with ID: {}", admin.getId());
                        return new ResourceNotFoundException("Admin", admin.getId().toString());
                    });

            if (admin.getPassword() != null && !admin.getPassword().isEmpty()) {
                admin.setPassword(passwordEncoder.encode(admin.getPassword()));
            }

            Admin updatedAdmin = adminRepository.save(admin);
            logger.info("Successfully updated admin: {} (ID: {})", updatedAdmin.getUsername(), updatedAdmin.getId());
        } catch (Exception e) {
            logger.error("Error updating admin: {}", admin.getUsername(), e);
            throw e;
        }
    }
}