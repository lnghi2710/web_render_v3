package vn.edu.detai.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import vn.edu.detai.entity.GiangVien;
import vn.edu.detai.entity.SinhVien;
import vn.edu.detai.entity.User;
import vn.edu.detai.exception.BusinessException;
import vn.edu.detai.repository.GiangVienRepository;
import vn.edu.detai.repository.SinhVienRepository;
import vn.edu.detai.repository.UserRepository;

/** Truy cập người dùng đang đăng nhập và hồ sơ GV/SV tương ứng. */
@Component
@RequiredArgsConstructor
public class CurrentUser {
    private final UserRepository userRepository;
    private final GiangVienRepository giangVienRepository;
    private final SinhVienRepository sinhVienRepository;

    public Long userId() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a != null && a.getPrincipal() instanceof CustomUserDetails d) {
            return d.getId();
        }
        throw new BusinessException("Bạn chưa đăng nhập.");
    }

    public User user() {
        return userRepository.findById(userId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản."));
    }

    public GiangVien giangVien() {
        return giangVienRepository.findByUserId(userId())
                .orElseThrow(() -> new BusinessException("Tài khoản chưa có hồ sơ giảng viên."));
    }

    public SinhVien sinhVien() {
        return sinhVienRepository.findByUserId(userId())
                .orElseThrow(() -> new BusinessException("Tài khoản chưa có hồ sơ sinh viên."));
    }
}
