package vn.edu.detai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.detai.dto.LichBaoVe;
import vn.edu.detai.dto.SuKien;
import vn.edu.detai.entity.*;
import vn.edu.detai.enums.Role;
import vn.edu.detai.repository.*;
import vn.edu.detai.security.CurrentUser;

import java.time.LocalDateTime;
import java.util.*;

/** Lịch báo cáo hội đồng (mọi vai trò) và lịch mốc thời gian của giảng viên. */
@Service
@RequiredArgsConstructor
public class LichService {
    private final HoiDongRepository hoiDongRepository;
    private final ThanhVienHoiDongRepository thanhVienHoiDongRepository;
    private final NhomRepository nhomRepository;
    private final ThanhVienNhomRepository thanhVienRepository;
    private final DotService dotService;
    private final CurrentUser currentUser;

    /** Lịch báo cáo hội đồng theo phạm vi của vai trò: Trưởng khoa và quản trị thấy tất cả. */
    @Transactional(readOnly = true)
    public List<LichBaoVe> lichBaoVe(Role role) {
        List<HoiDong> hoiDongs = new ArrayList<>();
        Set<Long> dotLienQuan = new HashSet<>();
        Map<Long, String> ghiChu = new HashMap<>();
        boolean tatCa = role == Role.ADMIN || role == Role.TRUONG_KHOA;

        if (tatCa) {
            hoiDongs.addAll(hoiDongRepository.findAll());
        } else if (role == Role.GIANG_VIEN) {
            Long gvId = currentUser.giangVien().getId();
            Map<Long, HoiDong> theoId = new LinkedHashMap<>();
            for (ThanhVienHoiDong tv : thanhVienHoiDongRepository.findByGiangVienId(gvId)) {
                theoId.put(tv.getHoiDong().getId(), tv.getHoiDong());
                ghiChu.put(tv.getHoiDong().getId(), "Bạn là " + tv.getVaiTro().getNhan().toLowerCase());
                dotLienQuan.add(tv.getHoiDong().getDot().getId());
            }
            for (Nhom n : nhomRepository.findByGvhdId(gvId)) {
                dotLienQuan.add(n.getDot().getId());
                if (n.getHoiDong() != null) {
                    theoId.putIfAbsent(n.getHoiDong().getId(), n.getHoiDong());
                    ghiChu.putIfAbsent(n.getHoiDong().getId(), "Nhóm bạn hướng dẫn báo cáo tại hội đồng này");
                }
            }
            for (Nhom n : nhomRepository.findByGvpbId(gvId)) dotLienQuan.add(n.getDot().getId());
            hoiDongs.addAll(theoId.values());
        } else {
            Map<Long, HoiDong> theoId = new LinkedHashMap<>();
            for (Nhom n : thanhVienRepository.findNhomCuaSv(currentUser.sinhVien().getId())) {
                dotLienQuan.add(n.getDot().getId());
                if (n.getHoiDong() != null) {
                    theoId.putIfAbsent(n.getHoiDong().getId(), n.getHoiDong());
                    ghiChu.putIfAbsent(n.getHoiDong().getId(), "Nhóm \"" + n.getTenNhom() + "\" của bạn báo cáo tại đây");
                }
            }
            hoiDongs.addAll(theoId.values());
        }

        List<LichBaoVe> kq = new ArrayList<>();
        Set<Long> dotDaCoHoiDong = new HashSet<>();
        for (HoiDong h : hoiDongs) {
            dotDaCoHoiDong.add(h.getDot().getId());
            List<String> thanhVien = h.getThanhViens().stream()
                    .sorted(Comparator.comparingInt((ThanhVienHoiDong t) -> t.getVaiTro().ordinal()))
                    .map(t -> t.getGiangVien().getHoTen() + " (" + t.getVaiTro().getNhan() + ")").toList();
            List<String> nhom = (role == Role.SINH_VIEN) ? List.of()
                    : h.getNhoms().stream().map(Nhom::getTenNhom).toList();
            kq.add(new LichBaoVe(h.getTenHoiDong(), h.getDot().getTenDot(), h.getThoiGian(), h.getDiaDiem(),
                    thanhVien, nhom, ghiChu.get(h.getId())));
        }
        // Ngày báo cáo dự kiến của đợt khi chưa lập hội đồng nào.
        for (DotDangKy d : dotService.tatCa()) {
            if (d.getNgayBaoCaoHoiDong() == null || d.isDaCongBo() || dotDaCoHoiDong.contains(d.getId())) continue;
            if (!tatCa && !dotLienQuan.contains(d.getId())) continue;
            kq.add(new LichBaoVe("Báo cáo hội đồng (dự kiến)", d.getTenDot(), d.getNgayBaoCaoHoiDong(), null,
                    List.of(), List.of(), "Chưa lập hội đồng cho đợt này"));
        }
        kq.sort(Comparator.comparing(LichBaoVe::thoiGian, Comparator.nullsLast(Comparator.naturalOrder())));
        return kq;
    }

    /** Các mốc quan trọng của giảng viên hiện tại, từ 7 ngày trước trở đi, theo thứ tự thời gian. */
    @Transactional(readOnly = true)
    public List<SuKien> lichGiangVien() {
        GiangVien gv = currentUser.giangVien();
        List<Nhom> huongDan = nhomRepository.findByGvhdId(gv.getId());
        List<Nhom> phanBien = nhomRepository.findByGvpbId(gv.getId());
        Set<Long> dotHuongDan = new HashSet<>();
        Set<Long> dotPhanBien = new HashSet<>();
        for (Nhom n : huongDan) dotHuongDan.add(n.getDot().getId());
        for (Nhom n : phanBien) dotPhanBien.add(n.getDot().getId());
        List<ThanhVienHoiDong> hoiDong = thanhVienHoiDongRepository.findByGiangVienId(gv.getId());
        Set<Long> dotHoiDong = new HashSet<>();
        for (ThanhVienHoiDong t : hoiDong) dotHoiDong.add(t.getHoiDong().getDot().getId());

        List<SuKien> ds = new ArrayList<>();
        for (DotDangKy d : dotService.tatCa()) {
            if (d.isDaCongBo()) continue;
            String ten = "Đợt \"" + d.getTenDot() + "\": ";
            ds.add(new SuKien(d.getGvBatDau(), "Đăng ký đề tài", ten + "bắt đầu thời gian giảng viên đăng ký đề tài.",
                    "/giang-vien/de-tai", "info"));
            ds.add(new SuKien(d.getGvKetThuc(), "Đăng ký đề tài", ten + "hạn cuối giảng viên đăng ký đề tài.",
                    "/giang-vien/de-tai", d.trongThoiGianGv() ? "warn" : "info"));
            if (dotHuongDan.contains(d.getId())) {
                ds.add(new SuKien(d.getSvKetThuc(), "Hướng dẫn", ten + "kết thúc đăng ký của sinh viên, nhóm bắt đầu nộp báo cáo.",
                        "/giang-vien/nhom", "info"));
            }
            if (d.getHanNopDiemGvpb() != null && dotPhanBien.contains(d.getId())) {
                ds.add(new SuKien(d.getHanNopDiemGvpb(), "Hạn nộp điểm", ten + "hạn nộp điểm phản biện.",
                        "/giang-vien/viec-can-cham", "warn"));
            }
            if (d.getNgayBaoCaoHoiDong() != null && (dotHoiDong.contains(d.getId()) || dotHuongDan.contains(d.getId()))) {
                ds.add(new SuKien(d.getNgayBaoCaoHoiDong(), "Hội đồng", ten + "ngày báo cáo hội đồng (dự kiến).",
                        "/lich-bao-ve", "info"));
            }
        }
        for (ThanhVienHoiDong t : hoiDong) {
            HoiDong h = t.getHoiDong();
            if (h.getThoiGian() == null || h.getDot().isDaCongBo()) continue;
            ds.add(new SuKien(h.getThoiGian(), "Hội đồng", "Hội đồng \"" + h.getTenHoiDong() + "\" ("
                    + t.getVaiTro().getNhan().toLowerCase() + ")" + (h.getDiaDiem() == null ? "" : " tại " + h.getDiaDiem()) + ".",
                    "/lich-bao-ve", "danger"));
        }
        LocalDateTime tu = LocalDateTime.now().minusDays(7);
        ds.removeIf(s -> s.thoiGian() == null || s.thoiGian().isBefore(tu));
        ds.sort(Comparator.comparing(SuKien::thoiGian));
        return ds;
    }
}
