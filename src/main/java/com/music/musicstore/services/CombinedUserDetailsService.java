package com.music.musicstore.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CombinedUserDetailsService implements UserDetailsService {
    private final AdminService adminService;
    private final CustomerService customerService;
    private final ArtistService artistService;
    private final StaffService staffService;

    public CombinedUserDetailsService(AdminService adminService, CustomerService customerService,
                                      ArtistService artistService, StaffService staffService) {
        this.adminService = adminService;
        this.customerService = customerService;
        this.artistService = artistService;
        this.staffService = staffService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Try each service in order of privilege
        try {
            return adminService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e1) {
            try {
                return staffService.loadUserByUsername(username);
            } catch (UsernameNotFoundException e2) {
                try {
                    return artistService.loadUserByUsername(username);
                } catch (UsernameNotFoundException e3) {
                    return customerService.loadUserByUsername(username);
                }
            }
        }
    }
}
