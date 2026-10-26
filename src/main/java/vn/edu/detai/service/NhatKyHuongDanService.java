package vn.edu.detai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.detai.dto.NhatKyForm;
import vn.edu.detai.entity.*;
import vn.edu.detai.enums.LoaiDiem;
import vn.edu.detai.exception.BusinessException;
import vn.edu.detai.repository.*;
import vn.edu.detai.security.CurrentUser;

import java.time.LocalDateTime;
import java.util.List;

/** Nhật ký hướng dẫn: GVHD ghi lại từng buổi làm việc với nhóm; sinh viên của nhóm xem được. */
@Service
@RequiredArgsConstructor
public class NhatKyHuongDanService {
    private final NhatKyHuongDanRepository repository;
    private final NhomRepository nhomRepository;
    private final ThanhVienNhomRepository thanhVienRepository;
    private final DiemService diemService;
    private final CurrentUser currentUser;
    private final AuditService audit;

    /** Các nhóm mà giảng viên hiện tại là GVHD. */
    @Transactional(readOnly = true)
    public List<Nhom> nhomHuongDan() {
        return nhomRepository.findByGvhdId(currentUser.giangVien().getId());
    }

    @Transactional(readOnly = true)
    public long dem(Nhom n) { return repository.demTheoNhom(n.getId()); }

    @Transactional(readOnly = true)
    public NhatKyHuongDan moiNhat(Nhom n) {
        List<NhatKyHuongDan> ds = repository.findTheoNhom(n.getId());
        return ds.isEmpty() ? null : ds.get(0);
    }

    @Transactional(readOnly = true)
    public List<NhatKyHuongDan> theoNhom(Long nhomId) { return repository.findTheoNhom(nhomId); }

    /** GVHD của nhóm xem nhật ký; ném lỗi nếu không phải GVHD. */
    @Transactional(readOnly = true)
    public Nhom nhomCuaGvhd(Long nhomId) {
        Nhom n = diemService.layNhom(nhomId);
        if (!diemService.coQuyenCham(n, currentUser.giangVien(), LoaiDiem.GVHD)) {
            throw new BusinessException("Bạn không phải giảng viên hướng dẫn của nhóm này.");
        }
        return n;
    }

    @Transactional
    public NhatKyHuongDan them(Long nhomId, NhatKyForm f) {
        Nhom n = nhomCuaGvhd(nhomId);
        if (n.getDot().isDaCongBo()) throw new BusinessException("Đợt đã công bố kết quả, không thể thêm nhật ký.");
        LocalDateTime tg = f.getThoiGian() == null ? LocalDateTime.now() : f.getThoiGian();
        if (tg.isAfter(LocalDateTime.now().plusMinutes(5))) {
            throw new BusinessException("Thời điểm buổi hướng dẫn không được ở tương lai.");
        }
        NhatKyHuongDan k = new NhatKyHuongDan();
        k.setNhom(n);
        k.setGiangVien(currentUser.giangVien());
        k.setThoiGian(tg);
        k.setNoiDung(f.getNoiDung().trim());
        k.setViecTiepTheo(f.getViecTiepTheo() == null || f.getViecTiepTheo().isBlank() ? null : f.getViecTiepTheo().trim());
        k = repository.save(k);
        audit.ghi("Thêm nhật ký hướng dẫn", "Nhom#" + nhomId);
        return k;
    }

    /** Chỉ giảng viên đã ghi mục này được xóa, và chỉ khi đợt chưa công bố. */
    @Transactional
    public Long xoa(Long id) {
        NhatKyHuongDan k = repository.findById(id).orElseThrow(() -> new BusinessException("Không tìm thấy mục nhật ký."));
        if (!k.getGiangVien().getId().equals(currentUser.giangVien().getId())) {
            throw new BusinessException("Bạn chỉ xóa được mục do chính bạn ghi.");
        }
        if (k.getNhom().getDot().isDaCongBo()) throw new BusinessException("Đợt đã công bố kết quả, không thể xóa nhật ký.");
        Long nhomId = k.getNhom().getId();
        repository.delete(k);
        audit.ghi("Xóa nhật ký hướng dẫn", "Nhom#" + nhomId);
        return nhomId;
    }

    /** Sinh viên xem nhật ký của các nhóm mình thuộc về (mọi đợt). */
    @Transactional(readOnly = true)
    public List<Nhom> nhomCuaSinhVien() {
        return thanhVienRepository.findNhomCuaSv(currentUser.sinhVien().getId());
    }
}
