package vn.edu.detai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.detai.dto.*;
import vn.edu.detai.entity.*;
import vn.edu.detai.enums.*;
import vn.edu.detai.exception.BusinessException;
import vn.edu.detai.repository.*;
import vn.edu.detai.security.CurrentUser;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final GiangVienRepository giangVienRepository;
    private final SinhVienRepository sinhVienRepository;
    private final BoMonRepository boMonRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUser currentUser;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public List<User> tatCa() { return userRepository.findAllByOrderByRoleAscUsernameAsc(); }

    @Transactional(readOnly = true)
    public User lay(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản."));
    }

    @Transactional(readOnly = true)
    public TaiKhoanForm taoForm(User u) {
        TaiKhoanForm f = new TaiKhoanForm();
        f.setUsername(u.getUsername());
        f.setFullName(u.getFullName());
        f.setEmail(u.getEmail());
        f.setRole(u.getRole());
        giangVienRepository.findByUserId(u.getId()).ifPresent(g -> {
            f.setMaGv(g.getMaGv());
            f.setBoMonId(g.getBoMon().getId());
        });
        sinhVienRepository.findByUserId(u.getId()).ifPresent(s -> {
            f.setMaSv(s.getMaSv());
            f.setLop(s.getLop());
        });
        return f;
    }

    @Transactional
    public User tao(TaiKhoanForm f) {
        if (f.getPassword() == null || f.getPassword().length() < 6) {
            throw new BusinessException("Mật khẩu tối thiểu 6 ký tự.");
        }
        if (userRepository.existsByUsername(f.getUsername())) {
            throw new BusinessException("Tên đăng nhập đã tồn tại.");
        }
        if (userRepository.findByEmail(f.getEmail()).isPresent()) {
            throw new BusinessException("Email đã được sử dụng.");
        }
        User u = new User();
        u.setUsername(f.getUsername().trim());
        u.setPassword(passwordEncoder.encode(f.getPassword()));
        u.setFullName(f.getFullName().trim());
        u.setEmail(f.getEmail().trim());
        u.setRole(f.getRole());
        u.setEnabled(true);
        u = userRepository.save(u);
        capNhatHoSo(u, f);
        audit.ghi("Tạo tài khoản", "User#" + u.getId());
        return u;
    }

    @Transactional
    public User sua(Long id, TaiKhoanForm f) {
        User u = lay(id);
        userRepository.findByEmail(f.getEmail()).filter(x -> !x.getId().equals(id)).ifPresent(x -> {
            throw new BusinessException("Email đã được sử dụng.");
        });
        if (f.getRole() != u.getRole() && u.getId().equals(currentUser.userId())) {
            throw new BusinessException("Không thể tự đổi vai trò của chính mình.");
        }
        u.setFullName(f.getFullName().trim());
        u.setEmail(f.getEmail().trim());
        u.setRole(f.getRole());
        if (f.getPassword() != null && !f.getPassword().isBlank()) {
            if (f.getPassword().length() < 6) throw new BusinessException("Mật khẩu tối thiểu 6 ký tự.");
            u.setPassword(passwordEncoder.encode(f.getPassword()));
        }
        userRepository.save(u);
        capNhatHoSo(u, f);
        audit.ghi("Sửa tài khoản", "User#" + u.getId());
        return u;
    }

    @Transactional
    public void datTrangThai(Long id, boolean enabled) {
        User u = lay(id);
        if (!enabled && u.getId().equals(currentUser.userId())) {
            throw new BusinessException("Không thể tự khóa tài khoản của chính mình.");
        }
        u.setEnabled(enabled);
        userRepository.save(u);
        audit.ghi(enabled ? "Mở khóa tài khoản" : "Khóa tài khoản", "User#" + u.getId());
    }

    /** Tạo hoặc cập nhật hồ sơ GV/SV theo vai trò được gán (FR04). */
    private void capNhatHoSo(User u, TaiKhoanForm f) {
        if (u.getRole() == Role.GIANG_VIEN) {
            if (f.getMaGv() == null || f.getMaGv().isBlank() || f.getBoMonId() == null) {
                throw new BusinessException("Giảng viên cần có mã GV và bộ môn.");
            }
            GiangVien g = giangVienRepository.findByUserId(u.getId()).orElseGet(GiangVien::new);
            giangVienRepository.findByMaGv(f.getMaGv().trim()).filter(x -> !x.getId().equals(g.getId())).ifPresent(x -> {
                throw new BusinessException("Mã giảng viên đã tồn tại.");
            });
            g.setUser(u);
            g.setMaGv(f.getMaGv().trim());
            g.setBoMon(boMonRepository.findById(f.getBoMonId())
                    .orElseThrow(() -> new BusinessException("Bộ môn không tồn tại.")));
            giangVienRepository.save(g);
        } else if (u.getRole() == Role.SINH_VIEN) {
            if (f.getMaSv() == null || f.getMaSv().isBlank() || f.getLop() == null || f.getLop().isBlank()) {
                throw new BusinessException("Sinh viên cần có mã SV và lớp.");
            }
            SinhVien s = sinhVienRepository.findByUserId(u.getId()).orElseGet(SinhVien::new);
            sinhVienRepository.findByMaSv(f.getMaSv().trim()).filter(x -> !x.getId().equals(s.getId())).ifPresent(x -> {
                throw new BusinessException("Mã sinh viên đã tồn tại.");
            });
            s.setUser(u);
            s.setMaSv(f.getMaSv().trim());
            s.setLop(f.getLop().trim());
            sinhVienRepository.save(s);
        }
    }

    /** Hồ sơ cá nhân: người dùng tự sửa họ tên và email; vai trò, mã số và đơn vị do quản trị viên quản lý. */
    @Transactional
    public User capNhatHoSoCaNhan(HoSoForm f) {
        User u = currentUser.user();
        String email = f.getEmail().trim();
        userRepository.findByEmail(email).filter(x -> !x.getId().equals(u.getId())).ifPresent(x -> {
            throw new BusinessException("Email đã được sử dụng.");
        });
        u.setFullName(f.getFullName().trim());
        u.setEmail(email);
        userRepository.save(u);
        audit.ghi("Cập nhật hồ sơ cá nhân", "User#" + u.getId());
        return u;
    }

    /** Các dòng thông tin chỉ đọc của hồ sơ: mã số, lớp hoặc bộ môn. */
    @Transactional(readOnly = true)
    public java.util.Map<String, String> thongTinChucDanh(User u) {
        java.util.Map<String, String> m = new java.util.LinkedHashMap<>();
        giangVienRepository.findByUserId(u.getId()).ifPresent(g -> {
            m.put("Mã giảng viên", g.getMaGv());
            m.put("Bộ môn", g.getBoMon().getTenBoMon());
        });
        sinhVienRepository.findByUserId(u.getId()).ifPresent(s -> {
            m.put("Mã sinh viên", s.getMaSv());
            m.put("Lớp", s.getLop());
        });
        return m;
    }

    /** FR02: đổi mật khẩu của người đang đăng nhập. */
    @Transactional
    public void doiMatKhau(DoiMatKhauForm f) {
        User u = currentUser.user();
        if (!passwordEncoder.matches(f.getMatKhauCu(), u.getPassword())) {
            throw new BusinessException("Mật khẩu hiện tại không đúng.");
        }
        if (!f.getMatKhauMoi().equals(f.getNhapLai())) {
            throw new BusinessException("Mật khẩu nhập lại không khớp.");
        }
        u.setPassword(passwordEncoder.encode(f.getMatKhauMoi()));
        userRepository.save(u);
    }
}
