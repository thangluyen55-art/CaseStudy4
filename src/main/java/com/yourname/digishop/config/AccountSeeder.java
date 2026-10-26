package com.yourname.digishop.config;

import com.yourname.digishop.entity.Role;
import com.yourname.digishop.entity.User;
import com.yourname.digishop.repository.RoleRepository;
import com.yourname.digishop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountSeeder implements CommandLineRunner {
    private final RoleRepository roles;
    private final UserRepository users;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        Role admin = role("ROLE_ADMIN");
        Role staff = role("ROLE_STAFF");
        Role customer = role("ROLE_CUSTOMER");

        // Tài khoản demo, mật khẩu đều là 123456 (đổi nếu nộp bài thật)
        user("admin@digishop.com", "Quản trị viên", "0900000001", admin);
        user("staff@digishop.com", "Nhân viên", "0900000002", staff);
        user("customer@digishop.com", "Khách hàng mẫu", "0900000003", customer);
    }

    private Role role(String name) {
        return roles.findByName(name).orElseGet(() -> {
            Role r = new Role();
            r.setName(name);
            return roles.save(r);
        });
    }

    private void user(String email, String fullName, String phone, Role role) {
        if (users.existsByEmail(email)) return;
        User u = new User();
        u.setEmail(email);
        u.setFullName(fullName);
        u.setPhone(phone);
        u.setPassword(encoder.encode("123456"));
        u.setRole(role);
        users.save(u);
    }
}