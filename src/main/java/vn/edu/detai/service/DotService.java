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
public class DotService {
    private final DotDangKyRepository dotRepository;
    private final DeTaiRepository deTaiRepository;
    private final NhomRepository nhomRepository;
    private final HoiDongRepository hoiDongRepository;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public List<DotDangKy> tatCa() { return dotRepository.findAllByOrderByIdDesc(); }

    @Transactional(readOnly = true)
    public DotDangKy lay(Long id) {
        return dotRepository.findById(id).orElseThrow(() -> new BusinessException("Không tìm thấy đợt đăng ký."));
    }

    @Transactional(readOnly = true)
    public DotForm taoForm(DotDangKy d) {
        DotForm f = new DotForm();
        f.setTenDot(d.getTenDot());
        f.setLoai(d.getLoai());
        f.setGvBatDau(d.getGvBatDau());
        f.setGvKetThuc(d.getGvKetThuc());
        f.setSvBatDau(d.getSvBatDau());
        f.setSvKetThuc(d.getSvKetThuc());
        f.setHanNopDiemGvpb(d.getHanNopDiemGvpb());
        f.setNgayBaoCaoHoiDong(d.getNgayBaoCaoHoiDong());
        return f;
    }

    @Transactional
    public DotDangKy tao(DotForm f) {
        DotDangKy d = new DotDangKy();
        ganGiaTri(d, f);
        d = dotRepository.save(d);
        audit.ghi("Tạo đợt đăng ký", "DotDangKy#" + d.getId());
        return d;
    }

    @Transactional
    public DotDangKy sua(Long id, DotForm f) {
        DotDangKy d = lay(id);
        if (d.isDaCongBo()) {
            throw new BusinessException("Đợt đã công bố kết quả, không thể chỉnh sửa.");
        }
        ganGiaTri(d, f);
        d = dotRepository.save(d);
        audit.ghi("Sửa đợt đăng ký", "DotDangKy#" + d.getId());
        return d;
    }

    @Transactional
    public void xoa(Long id) {
        DotDangKy d = lay(id);
        if (deTaiRepository.countByDotId(id) > 0 || nhomRepository.countByDotId(id) > 0
                || hoiDongRepository.countByDotId(id) > 0) {
            throw new BusinessException("Đợt đã có đề tài, nhóm hoặc hội đồng nên không thể xóa.");
        }
        dotRepository.delete(d);
        audit.ghi("Xóa đợt đăng ký", "DotDangKy#" + id);
    }

    /** FR06: kiểm tra mốc thời gian; BR06: chỉ lưu các mốc phù hợp loại đợt. */
    private void ganGiaTri(DotDangKy d, DotForm f) {
        if (!f.getGvKetThuc().isAfter(f.getGvBatDau())) {
            throw new BusinessException("Thời gian kết thúc GV đăng ký phải sau thời gian bắt đầu.");
        }
        if (!f.getSvKetThuc().isAfter(f.getSvBatDau())) {
            throw new BusinessException("Thời gian kết thúc SV đăng ký phải sau thời gian bắt đầu.");
        }
        if (!f.getSvBatDau().isAfter(f.getGvKetThuc())) {
            throw new BusinessException("Thời gian SV đăng ký phải bắt đầu sau khi GV đăng ký kết thúc.");
        }
        d.setTenDot(f.getTenDot().trim());
        d.setLoai(f.getLoai());
        d.setGvBatDau(f.getGvBatDau());
        d.setGvKetThuc(f.getGvKetThuc());
        d.setSvBatDau(f.getSvBatDau());
        d.setSvKetThuc(f.getSvKetThuc());
        d.setHanNopDiemGvpb(f.getLoai().coGvpb() ? f.getHanNopDiemGvpb() : null);
        d.setNgayBaoCaoHoiDong(f.getLoai().coHoiDong() ? f.getNgayBaoCaoHoiDong() : null);
    }
}
