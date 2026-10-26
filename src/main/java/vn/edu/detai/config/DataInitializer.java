package vn.edu.detai.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.detai.entity.*;
import vn.edu.detai.enums.Role;
import vn.edu.detai.repository.*;

import java.util.List;

/**
 * Nạp dữ liệu mẫu lần chạy đầu tiên (hệ thống không có chức năng tự đăng ký - BR16).
 * Mật khẩu mặc định: admin -> admin123; các tài khoản còn lại -> 123456. HÃY ĐỔI SAU KHI ĐĂNG NHẬP.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final BoMonRepository boMonRepository;
    private final GiangVienRepository giangVienRepository;
    private final SinhVienRepository sinhVienRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        List<BoMon> boMons = List.of("Công nghệ phần mềm", "Hệ thống thông tin", "Khoa học máy tính",
                        "Mạng máy tính và truyền thông").stream()
                .map(ten -> { BoMon b = new BoMon(); b.setTenBoMon(ten); return boMonRepository.save(b); })
                .toList();

        taoUser("admin", "admin123", "Quản trị hệ thống", "admin@khoa.edu.vn", Role.ADMIN);
        taoUser("truongkhoa", "123456", "Trưởng khoa CNTT", "truongkhoa@khoa.edu.vn", Role.TRUONG_KHOA);

        String[] tenGv = {"Nguyễn Văn An", "Trần Thị Bình", "Lê Minh Châu", "Phạm Quốc Dũng", "Hoàng Thu Hà"};
        for (int i = 0; i < tenGv.length; i++) {
            User u = taoUser("gv0" + (i + 1), "123456", tenGv[i], "gv0" + (i + 1) + "@khoa.edu.vn", Role.GIANG_VIEN);
            GiangVien g = new GiangVien();
            g.setUser(u);
            g.setMaGv("GV00" + (i + 1));
            g.setBoMon(boMons.get(i % boMons.size()));
            giangVienRepository.save(g);
        }
        for (int i = 1; i <= 9; i++) {
            User u = taoUser("sv0" + i, "123456", "Sinh viên " + i, "sv0" + i + "@sv.edu.vn", Role.SINH_VIEN);
            SinhVien s = new SinhVien();
            s.setUser(u);
            s.setMaSv("SV00" + i);
            s.setLop("DH22CNTT");
            sinhVienRepository.save(s);
        }
    }

    private User taoUser(String username, String matKhau, String hoTen, String email, Role role) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(matKhau));
        u.setFullName(hoTen);
        u.setEmail(email);
        u.setRole(role);
        u.setEnabled(true);
        return userRepository.save(u);
    }
}
