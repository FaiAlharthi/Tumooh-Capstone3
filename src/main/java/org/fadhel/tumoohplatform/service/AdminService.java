package org.fadhel.tumoohplatform.service;


import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.Api.ApiException;
import org.fadhel.tumoohplatform.model.Admin;
import org.fadhel.tumoohplatform.repository.AdminRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;

    // Helper method to enforce admin-only authorization
    public void verifyAdmin(Long adminId) {
        if (adminId == null || adminRepository.findAdminById(adminId) == null) {
            throw new ApiException("Access denied: Only registered administrators can perform this action");
        }
    }

    // to get all admins (admin-only)
    public List<Admin> getAllAdmins(Long requestingAdminId) {
        verifyAdmin(requestingAdminId);
        return adminRepository.findAll();
    }

    // to get an admin by id (admin-only)
    public Admin getAdminById(Long id, Long requestingAdminId) {
        verifyAdmin(requestingAdminId);
        Admin admin = adminRepository.findAdminById(id);
        if (admin == null) {
            throw new ApiException("Admin not found");
        }
        return admin;
    }

    // to add an admin (admin-only)
    public void addAdmin(Long requestingAdminId, Admin admin) {
        // If system has zero admins, allow initial admin creation; otherwise require existing admin verification
        if (adminRepository.count() > 0) {
            verifyAdmin(requestingAdminId);
        }
        admin.setRole("ADMIN");
        adminRepository.save(admin);
    }

    // to update an admin (admin-only)
    public void updateAdmin(Long id, Long requestingAdminId, Admin admin) {
        verifyAdmin(requestingAdminId);
        Admin existing = adminRepository.findAdminById(id);
        if (existing == null) {
            throw new ApiException("Admin not found");
        }
        existing.setName(admin.getName());
        existing.setEmail(admin.getEmail());
        existing.setPassword(admin.getPassword());
        existing.setRole("ADMIN");
        adminRepository.save(existing);
    }

    // to delete an admin (admin-only)
    public void deleteAdmin(Long id, Long requestingAdminId) {
        verifyAdmin(requestingAdminId);
        Admin admin = adminRepository.findAdminById(id);
        if (admin == null) {
            throw new ApiException("Admin not found");
        }
        adminRepository.delete(admin);
    }
}
