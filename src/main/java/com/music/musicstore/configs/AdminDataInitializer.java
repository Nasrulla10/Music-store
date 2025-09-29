package com.music.musicstore.configs;

import com.music.musicstore.models.users.Admin;
import com.music.musicstore.services.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminDataInitializer.class);

    private final AdminService adminService;

    @Autowired
    public AdminDataInitializer(AdminService adminService) {
        this.adminService = adminService;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Initializing admin data...");

        // Check if any admins already exist to avoid duplicates
        if (adminService.count() > 0) {
            logger.info("Admin data already exists. Skipping initialization.");
            return;
        }

        // Create admin users with easy-to-remember credentials
        createAdmin("admin", "admin123", "admin@musicstore.com", "Alice", "Administrator");
        createAdmin("superadmin", "super123", "superadmin@musicstore.com", "Bob", "SuperAdmin");
        createAdmin("admin1", "password1", "admin1@musicstore.com", "Charlie", "Admin");
        createAdmin("testadmin", "test123", "testadmin@musicstore.com", "Diana", "TestAdmin");

        logger.info("Admin data initialization completed successfully!");
    }

    private void createAdmin(String username, String password, String email, String firstName, String lastName) {
        try {
            Admin admin = new Admin();
            admin.setUsername(username);
            admin.setPassword(password);
            admin.setEmail(email);
            admin.setFirstName(firstName);
            admin.setLastName(lastName);
            admin.setEnabled(true);

            adminService.createAdmin(admin);
            logger.info("Created admin user: {} ({})", username, firstName + " " + lastName);
        } catch (Exception e) {
            logger.error("Failed to create admin user: {}", username, e);
        }
    }
}
