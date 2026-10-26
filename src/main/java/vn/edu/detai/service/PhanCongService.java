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
public class PhanCongService {
    private final NhomRepository nhomRepository;
    private final GiangVienRepository giangVienRepository;
    private final HoiDongRepository hoiDongRepository;
    private final ThanhVienHoiDongRepository thanhVienHoiDongRepository;
    private final DiemRepository diemRepository;
    private final DotService dotService;
    private final AuditService audit;

    /** Các nhóm đã có đề tài của đợt — đối tượng của phân công và chấm điểm. */
    @Transactional(readOnly = true)
    public List<Nhom> nhomCoDeTai(Long dotId) {
        return nhomRepository.findByDotIdOrderByIdAsc(dotId).stream().filter(n -> n.getDeTai() != null).toList();
    }

    @Transactional(readOnly = true)
    public List<GiangVien> tatCaGiangVien() { return giangVienRepository.findAllByOrderByMaGvAsc(); }

    @Transactional(readOnly = true)
    public List<HoiDong> dsHoiDong(Long dotId) { return hoiDongRepository.findByDotIdOrderByIdAsc(dotId); }

    @Transactional(readOnly = true)
    public HoiDong layHoiDong(Long id) {
        return hoiDongRepository.findById(id).orElseThrow(() -> new BusinessException("Không tìm thấy hội đồng."));
    }

    /** FR08: phân công GVPB cho nhóm; BR11: không phân công GV đang hướng dẫn đề tài của nhóm. */
    @Transactional
    public Nhom phanCongGvpb(Long nhomId, Long gvId) {
        Nhom n = nhomRepository.findById(nhomId).orElseThrow(() -> new BusinessException("Không tìm thấy nhóm."));
        DotDangKy dot = n.getDot();
        if (!dot.getLoai().coGvpb()) throw new BusinessException("Đợt này không có giảng viên phản biện.");
        if (dot.isDaCongBo()) throw new BusinessException("Đợt đã công bố kết quả, không thể thay đổi phân công.");
        if (n.getDeTai() == null) throw new BusinessException("Nhóm chưa đăng ký đề tài.");
        if (gvId == null) throw new BusinessException("Vui lòng chọn giảng viên phản biện.");
        GiangVien gv = giangVienRepository.findById(gvId)
                .orElseThrow(() -> new BusinessException("Giảng viên không tồn tại."));
        if (n.getDeTai().coGvhd(gv.getId())) {
            throw new BusinessException("Giảng viên " + gv.getHoTen()
                    + " đang hướng dẫn đề tài của nhóm này nên không thể làm GVPB (BR11).");
        }
        if (n.getGvpb() != null && !n.getGvpb().getId().equals(gv.getId())
                && diemRepository.findByNhomIdAndLoai(n.getId(), LoaiDiem.GVPB).isPresent()) {
            throw new BusinessException("GVPB hiện tại đã nhập điểm nên không thể đổi người khác.");
        }
        n.setGvpb(gv);
        audit.ghi("Phân công GVPB", "Nhom#" + n.getId() + " -> GiangVien#" + gv.getId());
        return nhomRepository.save(n);
    }

    /** FR09 / BR10: hội đồng của đợt KLTN gồm 3–5 GV, một chủ tịch và một thư ký. */
    @Transactional
    public HoiDong taoHoiDong(Long dotId, HoiDongForm f) {
        DotDangKy dot = dotService.lay(dotId);
        if (!dot.getLoai().coHoiDong()) throw new BusinessException("Chỉ đợt khóa luận tốt nghiệp mới có hội đồng (BR10).");
        if (dot.isDaCongBo()) throw new BusinessException("Đợt đã công bố kết quả.");
        Set<Long> ids = new LinkedHashSet<>(f.getThanhVienIds() == null ? List.<Long>of() : f.getThanhVienIds());
        if (ids.size() < 3 || ids.size() > 5) {
            throw new BusinessException("Hội đồng phải có từ 3 đến 5 giảng viên (BR10).");
        }
        if (f.getChuTichId().equals(f.getThuKyId())) {
            throw new BusinessException("Chủ tịch và thư ký phải là hai giảng viên khác nhau (BR10).");
        }
        if (!ids.contains(f.getChuTichId()) || !ids.contains(f.getThuKyId())) {
            throw new BusinessException("Chủ tịch và thư ký phải nằm trong danh sách thành viên hội đồng.");
        }
        List<GiangVien> gvs = giangVienRepository.findAllById(ids);
        if (gvs.size() != ids.size()) throw new BusinessException("Có giảng viên không tồn tại trong danh sách.");

        HoiDong hd = new HoiDong();
        hd.setDot(dot);
        hd.setTenHoiDong(f.getTenHoiDong().trim());
        hd.setThoiGian(f.getThoiGian());
        hd.setDiaDiem(f.getDiaDiem() == null ? null : f.getDiaDiem().trim());
        hd = hoiDongRepository.save(hd);
        for (GiangVien gv : gvs) {
            ThanhVienHoiDong tv = new ThanhVienHoiDong();
            tv.setHoiDong(hd);
            tv.setGiangVien(gv);
            tv.setVaiTro(gv.getId().equals(f.getChuTichId()) ? VaiTroHoiDong.CHU_TICH
                    : gv.getId().equals(f.getThuKyId()) ? VaiTroHoiDong.THU_KY : VaiTroHoiDong.THANH_VIEN);
            thanhVienHoiDongRepository.save(tv);
        }
        audit.ghi("Lập hội đồng", "HoiDong#" + hd.getId());
        return hd;
    }

    /** Gán nhóm vào hội đồng; BR11: không thành viên nào được là GVHD của nhóm. */
    @Transactional
    public HoiDong ganNhom(Long hoiDongId, List<Long> nhomIds) {
        HoiDong hd = layHoiDong(hoiDongId);
        if (hd.getDot().isDaCongBo()) throw new BusinessException("Đợt đã công bố kết quả.");
        if (nhomIds == null || nhomIds.isEmpty()) throw new BusinessException("Vui lòng chọn ít nhất một nhóm.");
        for (Long nhomId : nhomIds) {
            Nhom n = nhomRepository.findById(nhomId).orElseThrow(() -> new BusinessException("Không tìm thấy nhóm."));
            if (!n.getDot().getId().equals(hd.getDot().getId())) {
                throw new BusinessException("Nhóm " + n.getTenNhom() + " không thuộc đợt của hội đồng.");
            }
            if (n.getDeTai() == null) {
                throw new BusinessException("Nhóm " + n.getTenNhom() + " chưa đăng ký đề tài.");
            }
            for (ThanhVienHoiDong tv : hd.getThanhViens()) {
                if (n.getDeTai().coGvhd(tv.getGiangVien().getId())) {
                    throw new BusinessException("Giảng viên " + tv.getGiangVien().getHoTen()
                            + " hướng dẫn đề tài của nhóm " + n.getTenNhom()
                            + " nên không thể là thành viên hội đồng chấm nhóm này (BR11).");
                }
            }
            if (n.getHoiDong() != null && !n.getHoiDong().getId().equals(hd.getId())
                    && diemRepository.findByNhomIdAndLoai(n.getId(), LoaiDiem.HOI_DONG).isPresent()) {
                throw new BusinessException("Nhóm " + n.getTenNhom() + " đã có điểm hội đồng nên không thể chuyển hội đồng.");
            }
            n.setHoiDong(hd);
            nhomRepository.save(n);
        }
        audit.ghi("Gán nhóm vào hội đồng", "HoiDong#" + hd.getId() + " (" + nhomIds.size() + " nhóm)");
        return hd;
    }

    @Transactional
    public Long goNhomKhoiHoiDong(Long nhomId) {
        Nhom n = nhomRepository.findById(nhomId).orElseThrow(() -> new BusinessException("Không tìm thấy nhóm."));
        if (n.getDot().isDaCongBo()) throw new BusinessException("Đợt đã công bố kết quả.");
        if (diemRepository.findByNhomIdAndLoai(n.getId(), LoaiDiem.HOI_DONG).isPresent()) {
            throw new BusinessException("Nhóm đã có điểm hội đồng nên không thể gỡ khỏi hội đồng.");
        }
        n.setHoiDong(null);
        nhomRepository.save(n);
        audit.ghi("Gỡ nhóm khỏi hội đồng", "Nhom#" + nhomId);
        return n.getDot().getId();
    }

    @Transactional
    public Long xoaHoiDong(Long hoiDongId) {
        HoiDong hd = layHoiDong(hoiDongId);
        if (!hd.getNhoms().isEmpty()) throw new BusinessException("Hội đồng đang được gán nhóm nên không thể xóa.");
        Long dotId = hd.getDot().getId();
        thanhVienHoiDongRepository.xoaTheoHoiDong(hoiDongId);
        hoiDongRepository.delete(hd);
        audit.ghi("Xóa hội đồng", "HoiDong#" + hoiDongId);
        return dotId;
    }
}
