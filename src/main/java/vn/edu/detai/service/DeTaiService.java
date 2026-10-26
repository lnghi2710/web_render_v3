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
public class DeTaiService {
    private static final String NGOAI_GIO_GV = "Ngoài thời gian giảng viên đăng ký đề tài của đợt này (BR05).";

    private final DeTaiRepository deTaiRepository;
    private final BoMonRepository boMonRepository;
    private final GiangVienRepository giangVienRepository;
    private final DotService dotService;
    private final CurrentUser currentUser;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public List<DeTai> cuaToi() {
        return deTaiRepository.findByGvhdId(currentUser.giangVien().getId());
    }

    @Transactional(readOnly = true)
    public DeTai lay(Long id) {
        return deTaiRepository.findById(id).orElseThrow(() -> new BusinessException("Không tìm thấy đề tài."));
    }

    @Transactional(readOnly = true)
    public List<DeTai> theoDot(Long dotId) { return deTaiRepository.findByDotIdOrderByIdDesc(dotId); }

    /** Hộp duyệt: mọi đề tài đang chờ duyệt của các đợt chưa công bố, cũ nhất trước. */
    @Transactional(readOnly = true)
    public List<DeTai> choDuyetChuaCongBo() {
        return deTaiRepository.findByTrangThaiOrderByIdAsc(TrangThaiDeTai.CHO_DUYET).stream()
                .filter(d -> !d.getDot().isDaCongBo()).toList();
    }

    /** Danh sách đề tài công bố của đợt: các đề tài đã được duyệt (FR07, FR16). */
    @Transactional(readOnly = true)
    public List<DeTai> congBoTheoDot(Long dotId) {
        return deTaiRepository.findByDotIdAndTrangThaiOrderByIdAsc(dotId, TrangThaiDeTai.DA_DUYET);
    }

    @Transactional(readOnly = true)
    public DeTaiForm taoForm(DeTai d) {
        Long toi = currentUser.giangVien().getId();
        DeTaiForm f = new DeTaiForm();
        f.setDotId(d.getDot().getId());
        f.setTenDeTai(d.getTenDeTai());
        f.setMoTa(d.getMoTa());
        f.setBoMonId(d.getBoMon().getId());
        d.getGvhds().stream().filter(g -> !g.getId().equals(toi)).findFirst()
                .ifPresent(g -> f.setGvhd2Id(g.getId()));
        return f;
    }

    @Transactional
    public DeTai tao(DeTaiForm f) {
        GiangVien gv = currentUser.giangVien();
        DotDangKy dot = dotService.lay(f.getDotId());
        if (!dot.trongThoiGianGv()) throw new BusinessException(NGOAI_GIO_GV);
        DeTai d = new DeTai();
        d.setDot(dot);
        ganNoiDung(d, f, gv);
        d = deTaiRepository.save(d);
        audit.ghi("Đăng ký đề tài", "DeTai#" + d.getId());
        return d;
    }

    @Transactional
    public DeTai sua(Long id, DeTaiForm f) {
        GiangVien gv = currentUser.giangVien();
        DeTai d = layDeTaiCuaGv(id, gv);
        kiemTraSuaDuoc(d);
        ganNoiDung(d, f, gv);
        d.setTrangThai(TrangThaiDeTai.CHO_DUYET); // sửa xong phải được duyệt lại
        d.setLyDoTuChoi(null);
        d = deTaiRepository.save(d);
        audit.ghi("Sửa đề tài", "DeTai#" + d.getId());
        return d;
    }

    @Transactional
    public void xoa(Long id) {
        DeTai d = layDeTaiCuaGv(id, currentUser.giangVien());
        kiemTraSuaDuoc(d);
        deTaiRepository.delete(d);
        audit.ghi("Xóa đề tài", "DeTai#" + id);
    }

    @Transactional
    public DeTai duyet(Long id) {
        DeTai d = layChoDuyet(id);
        d.setTrangThai(TrangThaiDeTai.DA_DUYET);
        d.setLyDoTuChoi(null);
        audit.ghi("Duyệt đề tài", "DeTai#" + id);
        return deTaiRepository.save(d);
    }

    @Transactional
    public DeTai tuChoi(Long id, String lyDo) {
        if (lyDo == null || lyDo.isBlank()) throw new BusinessException("Vui lòng nhập lý do từ chối.");
        DeTai d = layChoDuyet(id);
        d.setTrangThai(TrangThaiDeTai.TU_CHOI);
        d.setLyDoTuChoi(lyDo.trim());
        audit.ghi("Từ chối đề tài", "DeTai#" + id);
        return deTaiRepository.save(d);
    }

    // ----- hỗ trợ -----
    private DeTai layChoDuyet(Long id) {
        DeTai d = lay(id);
        if (d.getTrangThai() != TrangThaiDeTai.CHO_DUYET) {
            throw new BusinessException("Chỉ duyệt hoặc từ chối được đề tài đang ở trạng thái chờ duyệt.");
        }
        return d;
    }

    private DeTai layDeTaiCuaGv(Long id, GiangVien gv) {
        DeTai d = lay(id);
        if (!d.coGvhd(gv.getId())) throw new BusinessException("Bạn không phải giảng viên hướng dẫn của đề tài này.");
        return d;
    }

    private void kiemTraSuaDuoc(DeTai d) {
        if (!d.getDot().trongThoiGianGv()) throw new BusinessException(NGOAI_GIO_GV);
        if (d.getNhom() != null) throw new BusinessException("Đề tài đã có nhóm đăng ký nên không thể sửa hoặc xóa.");
    }

    /** BR01: một bộ môn, nội dung rõ ràng, một đến hai GVHD. */
    private void ganNoiDung(DeTai d, DeTaiForm f, GiangVien chinh) {
        d.setTenDeTai(f.getTenDeTai().trim());
        d.setMoTa(f.getMoTa().trim());
        d.setBoMon(boMonRepository.findById(f.getBoMonId())
                .orElseThrow(() -> new BusinessException("Bộ môn không tồn tại.")));
        List<GiangVien> ds = new ArrayList<>();
        ds.add(chinh);
        if (f.getGvhd2Id() != null) {
            if (f.getGvhd2Id().equals(chinh.getId())) {
                throw new BusinessException("GVHD thứ hai phải khác giảng viên đang đăng ký.");
            }
            ds.add(giangVienRepository.findById(f.getGvhd2Id())
                    .orElseThrow(() -> new BusinessException("Giảng viên hướng dẫn thứ hai không tồn tại.")));
        }
        d.getGvhds().clear();
        d.getGvhds().addAll(ds);
    }
}
