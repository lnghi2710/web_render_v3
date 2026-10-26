package vn.edu.detai.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import vn.edu.detai.dto.TongQuan;
import vn.edu.detai.security.CustomUserDetails;
import vn.edu.detai.service.DashboardService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Nạp "chuong" (việc cần xử lý, số đếm trên thanh bên, thông báo mới) cho mọi trang đã đăng nhập.
 * Kết quả được đệm vài giây theo từng người dùng để không truy vấn lại ở mỗi lần chuyển trang;
 * mọi thao tác ghi (POST) xóa đệm của chính người đó nên số đếm luôn mới sau khi xử lý xong.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GiaoDienAdvice {
    private static final long HIEU_LUC_MS = 15_000;

    private record Dem(TongQuan tongQuan, long luc) {}

    private final DashboardService dashboardService;
    private final Map<Long, Dem> dem = new ConcurrentHashMap<>();

    @ModelAttribute("chuong")
    public TongQuan chuong(Authentication auth, HttpServletRequest req) {
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails u)) return null;
        if (!"GET".equalsIgnoreCase(req.getMethod())) {
            dem.remove(u.getId());
            return null;
        }
        long now = System.currentTimeMillis();
        Dem cu = dem.get(u.getId());
        if (cu != null && now - cu.luc() < HIEU_LUC_MS) return cu.tongQuan();
        try {
            TongQuan t = dashboardService.tongQuan(u.getRole());
            dem.put(u.getId(), new Dem(t, now));
            return t;
        } catch (RuntimeException e) {
            // Thanh bên vẫn dùng được khi không tính được số đếm (ví dụ tài khoản chưa có hồ sơ GV/SV).
            return null;
        }
    }
}
