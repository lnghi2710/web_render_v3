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

/** FR21 / NFR05: ghi nhật ký người thực hiện, hành động, đối tượng và thời điểm. */
@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository repository;
    private final UserRepository userRepository;

    @Transactional
    public void ghi(String hanhDong, String doiTuong) {
        AuditLog log = new AuditLog();
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof vn.edu.detai.security.CustomUserDetails d) {
            log.setUser(userRepository.getReferenceById(d.getId()));
        }
        log.setHanhDong(hanhDong);
        log.setDoiTuong(doiTuong);
        log.setThoiGian(LocalDateTime.now());
        repository.save(log);
    }

    /** Nhật ký phân trang theo máy chủ, mới nhất trước. */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<AuditLog> trang(String tuKhoa, int trang, int kichThuoc) {
        int t = Math.max(0, trang);
        int k = Math.min(Math.max(10, kichThuoc), 100);
        String q = "%" + (tuKhoa == null ? "" : tuKhoa.trim().toLowerCase()) + "%";
        return repository.tim(q, org.springframework.data.domain.PageRequest.of(t, k));
    }

    @Transactional(readOnly = true)
    public List<AuditLog> moiNhat() {
        return repository.findTop200ByOrderByThoiGianDesc();
    }
}
