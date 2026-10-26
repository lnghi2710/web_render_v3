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
public class DiemService {
    private final DiemRepository diemRepository;
    private final NhomRepository nhomRepository;
    private final ThanhVienHoiDongRepository thanhVienHoiDongRepository;
    private final DotDangKyRepository dotRepository;
    private final DotService dotService;
    private final CauHinhService cauHinh;
    private final CurrentUser currentUser;
    private final AuditService audit;

    // ================= Giảng viên =================

    @Transactional(readOnly = true)
    public Nhom layNhom(Long id) {
        return nhomRepository.findById(id).orElseThrow(() -> new BusinessException("Không tìm thấy nhóm."));
    }

    /** FR13: GV được xem nhóm nếu là GVHD, GVPB hoặc thành viên hội đồng của nhóm. */
    @Transactional(readOnly = true)
    public boolean coLienQuan(Nhom n, GiangVien gv) {
        if (n.getDeTai() != null && n.getDeTai().coGvhd(gv.getId())) return true;
        if (n.getGvpb() != null && n.getGvpb().getId().equals(gv.getId())) return true;
        return n.getHoiDong() != null && n.getHoiDong().coGiangVien(gv.getId());
    }

    /** Danh sách nhóm và vai trò của giảng viên hiện tại (GVHD, GVPB, hội đồng). */
    @Transactional(readOnly = true)
    public List<NhiemVuCham> nhiemVuCuaToi() {
        GiangVien gv = currentUser.giangVien();
        List<NhiemVuCham> ds = new ArrayList<>();
        for (Nhom n : nhomRepository.findByGvhdId(gv.getId())) {
            ds.add(new NhiemVuCham(n, "Giảng viên hướng dẫn", LoaiDiem.GVHD, diem(n, LoaiDiem.GVHD), true));
        }
        for (Nhom n : nhomRepository.findByGvpbId(gv.getId())) {
            if (n.getDeTai() != null) {
                ds.add(new NhiemVuCham(n, "Giảng viên phản biện", LoaiDiem.GVPB, diem(n, LoaiDiem.GVPB), true));
            }
        }
        for (ThanhVienHoiDong tv : thanhVienHoiDongRepository.findByGiangVienId(gv.getId())) {
            boolean chuTich = tv.getVaiTro() == VaiTroHoiDong.CHU_TICH;
            for (Nhom n : tv.getHoiDong().getNhoms()) {
                ds.add(new NhiemVuCham(n, "Hội đồng " + tv.getHoiDong().getTenHoiDong() + " – " + tv.getVaiTro().getNhan(),
                        chuTich ? LoaiDiem.HOI_DONG : null, chuTich ? diem(n, LoaiDiem.HOI_DONG) : null, chuTich));
            }
        }
        return ds;
    }

    @Transactional(readOnly = true)
    public Diem diem(Nhom n, LoaiDiem loai) {
        return diemRepository.findByNhomIdAndLoai(n.getId(), loai).orElse(null);
    }

    /** FR14: nhập điểm và nhận xét; mỗi nhóm một điểm cho mỗi loại (BR12–BR14). */
    @Transactional
    public Diem nhapDiem(Long nhomId, DiemForm f) {
        Nhom n = layNhom(nhomId);
        DotDangKy dot = n.getDot();
        LoaiDiem loai = f.getLoai();
        if (dot.isDaCongBo()) throw new BusinessException("Kết quả đã được công bố, không thể sửa điểm.");
        if (n.getDeTai() == null) throw new BusinessException("Nhóm chưa đăng ký đề tài.");
        if (!dot.getLoai().cacLoaiDiem().contains(loai)) {
            throw new BusinessException("Đợt này không có " + loai.getNhan().toLowerCase() + " (BR13).");
        }
        GiangVien gv = currentUser.giangVien();
        if (!coQuyenCham(n, gv, loai)) {
            throw new BusinessException("Bạn không có quyền nhập " + loai.getNhan().toLowerCase() + " cho nhóm này (BR14).");
        }
        Diem d = diemRepository.findByNhomIdAndLoai(n.getId(), loai).orElseGet(() -> {
            Diem moi = new Diem();
            moi.setNhom(n);
            moi.setLoai(loai);
            return moi;
        });
        d.setGiangVien(gv);
        d.setDiem(lam2ChuSo(f.getDiem()));
        d.setNhanXet(f.getNhanXet() == null || f.getNhanXet().isBlank() ? null : f.getNhanXet().trim());
        d = diemRepository.save(d);
        audit.ghi("Nhập " + loai.getNhan().toLowerCase(), "Nhom#" + n.getId() + " = " + d.getDiem());
        return d;
    }

    @Transactional(readOnly = true)
    public boolean coQuyenCham(Nhom n, GiangVien gv, LoaiDiem loai) {
        return switch (loai) {
            case GVHD -> n.getDeTai() != null && n.getDeTai().coGvhd(gv.getId());
            case GVPB -> n.getGvpb() != null && n.getGvpb().getId().equals(gv.getId());
            case HOI_DONG -> n.getHoiDong() != null
                    && thanhVienHoiDongRepository.findByHoiDongAndGiangVien(n.getHoiDong().getId(), gv.getId())
                    .map(tv -> tv.getVaiTro() == VaiTroHoiDong.CHU_TICH).orElse(false);
        };
    }

    // ================= Trưởng khoa =================

    /** FR19 / BR13: điểm cuối = trung bình có trọng số các điểm thành phần (mặc định bằng nhau); null nếu còn thiếu điểm. */
    @Transactional(readOnly = true)
    public Double tinhDiemCuoi(Nhom n) {
        List<LoaiDiem> yeuCau = n.getDot().getLoai().cacLoaiDiem();
        double tong = 0;
        double tongTrongSo = 0;
        for (LoaiDiem loai : yeuCau) {
            Optional<Diem> d = diemRepository.findByNhomIdAndLoai(n.getId(), loai);
            if (d.isEmpty()) return null;
            double w = cauHinh.trongSo(loai);
            tong += d.get().getDiem() * w;
            tongTrongSo += w;
        }
        return lam2ChuSo(tong / tongTrongSo);
    }

    /** FR10: công bố kết quả; tính và lưu điểm cuối của từng nhóm trong một giao dịch. */
    @Transactional
    public DotDangKy congBo(Long dotId) {
        DotDangKy dot = dotService.lay(dotId);
        if (dot.isDaCongBo()) throw new BusinessException("Đợt này đã được công bố kết quả.");
        if (!dot.sauThoiGianSv()) {
            throw new BusinessException("Chỉ công bố kết quả sau khi kết thúc thời gian sinh viên đăng ký.");
        }
        List<Nhom> nhoms = nhomRepository.findByDotIdOrderByIdAsc(dotId).stream()
                .filter(n -> n.getDeTai() != null).toList();
        if (nhoms.isEmpty()) throw new BusinessException("Đợt chưa có nhóm nào đăng ký đề tài.");
        Map<Nhom, Double> ketQua = new LinkedHashMap<>();
        List<String> thieu = new ArrayList<>();
        for (Nhom n : nhoms) {
            Double dc = tinhDiemCuoi(n);
            if (dc == null) thieu.add(n.getTenNhom()); else ketQua.put(n, dc);
        }
        if (!thieu.isEmpty()) {
            throw new BusinessException("Chưa thể công bố: các nhóm sau còn thiếu điểm thành phần – " + String.join(", ", thieu) + ".");
        }
        ketQua.forEach((n, dc) -> {
            n.setDiemCuoi(dc);
            nhomRepository.save(n);
        });
        dot.setDaCongBo(true);
        dotRepository.save(dot);
        audit.ghi("Công bố kết quả", "DotDangKy#" + dotId);
        return dot;
    }

    /** FR11 / BR15: nhóm có GVPB nhưng chưa có điểm GVPB sau hạn nộp điểm. */
    @Transactional(readOnly = true)
    public List<Nhom> nhomGvpbQuaHan(DotDangKy dot) {
        if (dot.getHanNopDiemGvpb() == null || !LocalDateTime.now().isAfter(dot.getHanNopDiemGvpb())) {
            return List.of();
        }
        return nhomRepository.findByDotIdOrderByIdAsc(dot.getId()).stream()
                .filter(n -> n.getDeTai() != null && n.getGvpb() != null)
                .filter(n -> diemRepository.findByNhomIdAndLoai(n.getId(), LoaiDiem.GVPB).isEmpty())
                .toList();
    }

    /** Tổng số cảnh báo quá hạn trên các đợt chưa công bố (hiển thị cho Trưởng khoa). */
    @Transactional(readOnly = true)
    public int tongCanhBaoQuaHan() {
        int tong = 0;
        for (DotDangKy d : dotService.tatCa()) {
            if (!d.isDaCongBo()) tong += nhomGvpbQuaHan(d).size();
        }
        return tong;
    }

    /** Bảng điểm tổng quan của đợt cho Trưởng khoa: nhóm -> các điểm thành phần. */
    @Transactional(readOnly = true)
    public Map<Long, Map<String, Diem>> bangDiem(Long dotId) {
        Map<Long, Map<String, Diem>> kq = new LinkedHashMap<>();
        for (Nhom n : nhomRepository.findByDotIdOrderByIdAsc(dotId)) {
            Map<String, Diem> m = new HashMap<>();
            for (Diem d : diemRepository.findByNhomId(n.getId())) m.put(d.getLoai().name(), d);
            kq.put(n.getId(), m);
        }
        return kq;
    }

    // ================= Sinh viên =================

    /** FR18 / BR17: sinh viên chỉ xem điểm nhóm mình sau khi Trưởng khoa công bố. */
    @Transactional(readOnly = true)
    public KetQuaNhom ketQuaCuaNhom(Nhom n) {
        if (!n.getDot().isDaCongBo()) return new KetQuaNhom(false, List.of(), null);
        List<Diem> ds = new ArrayList<>();
        for (LoaiDiem loai : n.getDot().getLoai().cacLoaiDiem()) {
            diemRepository.findByNhomIdAndLoai(n.getId(), loai).ifPresent(ds::add);
        }
        return new KetQuaNhom(true, ds, n.getDiemCuoi());
    }

    private static double lam2ChuSo(double x) {
        return Math.round(x * 100.0) / 100.0;
    }
}
