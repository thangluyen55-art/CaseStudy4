package com.yourname.digishop.service;

import com.yourname.digishop.dto.RegisterForm;
import com.yourname.digishop.entity.Role;
import com.yourname.digishop.entity.User;
import com.yourname.digishop.repository.RoleRepository;
import com.yourname.digishop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;

    public boolean emailExists(String email) {
        return email != null && users.existsByEmail(email.trim().toLowerCase());
    }

    @Transactional
    public User registerCustomer(RegisterForm form) {
        Role role = roles.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new IllegalStateException("Chưa có role ROLE_CUSTOMER"));

        User u = new User();
        u.setEmail(form.getEmail().trim().toLowerCase());
        u.setFullName(form.getFullName().trim());
        u.setPhone(form.getPhone().trim());
        u.setPassword(encoder.encode(form.getPassword()));    // mã hóa BCrypt
        u.setRole(role);
        return users.save(u);
    }
}