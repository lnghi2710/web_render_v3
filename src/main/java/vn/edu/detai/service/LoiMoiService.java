package vn.edu.detai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.detai.entity.*;
import vn.edu.detai.enums.TrangThaiLoiMoi;
import vn.edu.detai.exception.BusinessException;
import vn.edu.detai.repository.*;
import vn.edu.detai.security.CurrentUser;

import java.time.LocalDateTime;
import java.util.List;

/** Lời mời vào nhóm: nhóm trưởng mời sinh viên chưa có nhóm, sinh viên đồng ý hoặc từ chối. */
@Service
@RequiredArgsConstructor
public class LoiMoiService {
    private static final String NGOAI_GIO_SV = "Ngoài thời gian sinh viên đăng ký của đợt này (BR05).";

    private final LoiMoiNhomRepository loiMoiRepository;
    private final NhomRepository nhomRepository;
    private final ThanhVienNhomRepository thanhVienRepository;
    private final SinhVienRepository sinhVienRepository;
    private final CauHinhService cauHinh;
    private final CurrentUser currentUser;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public List<LoiMoiNhom> daNhan() { return loiMoiRepository.findDaNhan(currentUser.sinhVien().getId()); }

    @Transactional(readOnly = true)
    public List<LoiMoiNhom> daGui() { return loiMoiRepository.findDaGui(currentUser.sinhVien().getId()); }

    @Transactional(readOnly = true)
    public long soChoTraLoi() {
        return loiMoiRepository.demDaNhan(currentUser.sinhVien().getId(), TrangThaiLoiMoi.CHO);
    }

    /** Nhóm trưởng mời một sinh viên chưa có nhóm trong đợt. */
    @Transactional
    public LoiMoiNhom moi(Long nhomId, Long svId) {
        SinhVien toi = currentUser.sinhVien();
        Nhom n = nhomRepository.findById(nhomId).orElseThrow(() -> new BusinessException("Không tìm thấy nhóm."));
        if (!n.laNhomTruong(toi.getId())) throw new BusinessException("Chỉ nhóm trưởng được gửi lời mời.");
        if (!n.getDot().trongThoiGianSv()) throw new BusinessException(NGOAI_GIO_SV);
        if (thanhVienRepository.demTheoNhom(nhomId) >= cauHinh.svToiDa()) {
            throw new BusinessException("Nhóm đã đủ " + cauHinh.svToiDa() + " sinh viên (BR02).");
        }
        SinhVien sv = sinhVienRepository.findById(svId).orElseThrow(() -> new BusinessException("Không tìm thấy sinh viên."));
        if (sv.getId().equals(toi.getId())) throw new BusinessException("Bạn không thể tự mời chính mình.");
        if (thanhVienRepository.demSvTrongDot(sv.getId(), n.getDot().getId()) > 0) {
            throw new BusinessException("Sinh viên " + sv.getMaSv() + " đã thuộc một nhóm trong đợt này (BR03).");
        }
        if (loiMoiRepository.demTheoNhomVaSv(nhomId, sv.getId(), TrangThaiLoiMoi.CHO) > 0) {
            throw new BusinessException("Bạn đã mời sinh viên " + sv.getMaSv() + " và đang chờ trả lời.");
        }
        LoiMoiNhom l = new LoiMoiNhom();
        l.setNhom(n);
        l.setSinhVien(sv);
        l.setNguoiMoi(toi);
        l.setTrangThai(TrangThaiLoiMoi.CHO);
        l.setNgayTao(LocalDateTime.now());
        l = loiMoiRepository.save(l);
        audit.ghi("Gửi lời mời vào nhóm", "Nhom#" + nhomId + " -> SinhVien#" + sv.getId());
        return l;
    }

    /** Sinh viên được mời trả lời; đồng ý thì vào nhóm nếu vẫn còn chỗ và chưa có nhóm khác. */
    @Transactional
    public LoiMoiNhom traLoi(Long id, boolean dongY) {
        SinhVien toi = currentUser.sinhVien();
        LoiMoiNhom l = loiMoiRepository.findById(id).orElseThrow(() -> new BusinessException("Không tìm thấy lời mời."));
        if (!l.getSinhVien().getId().equals(toi.getId())) throw new BusinessException("Lời mời này không dành cho bạn.");
        if (l.getTrangThai() != TrangThaiLoiMoi.CHO) throw new BusinessException("Lời mời này đã được xử lý.");
        Nhom n = l.getNhom();
        DotDangKy dot = n.getDot();
        if (!dongY) {
            dongBang(l, TrangThaiLoiMoi.TU_CHOI);
            audit.ghi("Từ chối lời mời vào nhóm", "LoiMoi#" + id);
            return l;
        }
        if (!dot.trongThoiGianSv()) throw new BusinessException(NGOAI_GIO_SV);
        if (thanhVienRepository.demSvTrongDot(toi.getId(), dot.getId()) > 0) {
            dongBang(l, TrangThaiLoiMoi.HUY);
            throw new BusinessException("Bạn đã thuộc một nhóm trong đợt này (BR03).");
        }
        if (thanhVienRepository.demTheoNhom(n.getId()) >= cauHinh.svToiDa()) {
            dongBang(l, TrangThaiLoiMoi.HUY);
            throw new BusinessException("Nhóm đã đủ sinh viên nên không thể tham gia (BR02).");
        }
        ThanhVienNhom tv = new ThanhVienNhom();
        tv.setNhom(n);
        tv.setSinhVien(toi);
        tv.setDot(dot);
        tv.setLaNhomTruong(false);
        thanhVienRepository.save(tv);
        dongBang(l, TrangThaiLoiMoi.DONG_Y);
        // Các lời mời khác của cùng đợt không còn hiệu lực.
        for (LoiMoiNhom khac : loiMoiRepository.findTheoSvVaDot(toi.getId(), dot.getId(), TrangThaiLoiMoi.CHO)) {
            dongBang(khac, TrangThaiLoiMoi.HUY);
        }
        audit.ghi("Đồng ý lời mời vào nhóm", "LoiMoi#" + id + " Nhom#" + n.getId());
        return l;
    }

    /** Nhóm trưởng thu hồi lời mời đang chờ. */
    @Transactional
    public LoiMoiNhom huy(Long id) {
        SinhVien toi = currentUser.sinhVien();
        LoiMoiNhom l = loiMoiRepository.findById(id).orElseThrow(() -> new BusinessException("Không tìm thấy lời mời."));
        if (!l.getNhom().laNhomTruong(toi.getId())) throw new BusinessException("Chỉ nhóm trưởng được thu hồi lời mời.");
        if (l.getTrangThai() != TrangThaiLoiMoi.CHO) throw new BusinessException("Lời mời này đã được xử lý.");
        dongBang(l, TrangThaiLoiMoi.HUY);
        audit.ghi("Thu hồi lời mời vào nhóm", "LoiMoi#" + id);
        return l;
    }

    private void dongBang(LoiMoiNhom l, TrangThaiLoiMoi tt) {
        l.setTrangThai(tt);
        l.setNgayTraLoi(LocalDateTime.now());
        loiMoiRepository.save(l);
    }
}
