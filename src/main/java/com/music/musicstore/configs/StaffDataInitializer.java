package com.music.musicstore.configs;

import com.music.musicstore.models.users.Staff;
import com.music.musicstore.services.StaffService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StaffDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(StaffDataInitializer.class);

    private final StaffService staffService;

    @Autowired
    public StaffDataInitializer(StaffService staffService) {
        this.staffService = staffService;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Initializing staff data...");

        // Check if any staff already exist to avoid duplicates
        if (staffService.count() > 0) {
            logger.info("Staff data already exists. Skipping initialization.");
            return;
        }

        // Create staff members with easy-to-remember credentials
        createStaffMember("staff1", "password1", "staff1@musicstore.com", "John", "Smith");
        createStaffMember("staff2", "password2", "staff2@musicstore.com", "Jane", "Doe");
        createStaffMember("manager", "manager123", "manager@musicstore.com", "Mike", "Johnson");
        createStaffMember("support", "support123", "support@musicstore.com", "Sarah", "Wilson");

        logger.info("Staff data initialization completed successfully!");
    }

    private void createStaffMember(String username, String password, String email, String firstName, String lastName) {
        try {
            Staff staff = new Staff(username, password, email, firstName, lastName);
            staffService.createStaff(staff);
            logger.info("Created staff member: {} ({})", username, firstName + " " + lastName);
        } catch (Exception e) {
            logger.error("Failed to create staff member: {}", username, e);
        }
    }
}
