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

@Service
@RequiredArgsConstructor
public class ThongBaoService {
    private final ThongBaoRepository repository;
    private final CurrentUser currentUser;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public List<ThongBao> tatCa() { return repository.findAllByOrderByNgayDangDesc(); }

    @Transactional(readOnly = true)
    public List<ThongBao> moiNhat() { return repository.findTop5ByOrderByNgayDangDesc(); }

    @Transactional(readOnly = true)
    public ThongBao lay(Long id) {
        return repository.findById(id).orElseThrow(() -> new BusinessException("Không tìm thấy thông báo."));
    }

    @Transactional
    public ThongBao dang(ThongBaoForm f) {
        ThongBao t = new ThongBao();
        t.setNguoiDang(currentUser.user());
        t.setTieuDe(f.getTieuDe().trim());
        t.setNoiDung(f.getNoiDung().trim());
        t.setNgayDang(LocalDateTime.now());
        t = repository.save(t);
        audit.ghi("Đăng thông báo", "ThongBao#" + t.getId());
        return t;
    }
}
