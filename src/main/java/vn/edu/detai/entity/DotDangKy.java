package vn.edu.detai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.edu.detai.enums.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "dot_dang_ky")
@Getter @Setter
public class DotDangKy {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten_dot", nullable = false)
    private String tenDot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoaiDot loai;

    @Column(name = "gv_bat_dau", nullable = false)
    private LocalDateTime gvBatDau;
    @Column(name = "gv_ket_thuc", nullable = false)
    private LocalDateTime gvKetThuc;
    @Column(name = "sv_bat_dau", nullable = false)
    private LocalDateTime svBatDau;
    @Column(name = "sv_ket_thuc", nullable = false)
    private LocalDateTime svKetThuc;

    /** Chỉ dùng cho TLCN, KLTN (BR06). */
    @Column(name = "han_nop_diem_gvpb")
    private LocalDateTime hanNopDiemGvpb;

    /** Chỉ dùng cho KLTN (BR06). */
    @Column(name = "ngay_bao_cao_hoi_dong")
    private LocalDateTime ngayBaoCaoHoiDong;

    @Column(name = "da_cong_bo", nullable = false)
    private boolean daCongBo = false;

    /** BR07: trạng thái suy ra từ các mốc thời gian và cờ công bố. */
    public TrangThaiDot trangThai(LocalDateTime now) {
        if (daCongBo) return TrangThaiDot.DA_CONG_BO;
        if (now.isBefore(gvBatDau)) return TrangThaiDot.CHUA_MO;
        if (!now.isAfter(gvKetThuc)) return TrangThaiDot.GV_DANG_KY;
        if (now.isBefore(svBatDau)) return TrangThaiDot.CHO_SV;
        if (!now.isAfter(svKetThuc)) return TrangThaiDot.SV_DANG_KY;
        return TrangThaiDot.THUC_HIEN;
    }

    public TrangThaiDot getTrangThaiHienTai() { return trangThai(LocalDateTime.now()); }

    public boolean trongThoiGianGv() {
        LocalDateTime n = LocalDateTime.now();
        return !n.isBefore(gvBatDau) && !n.isAfter(gvKetThuc);
    }

    public boolean trongThoiGianSv() {
        LocalDateTime n = LocalDateTime.now();
        return !n.isBefore(svBatDau) && !n.isAfter(svKetThuc);
    }

    public boolean sauThoiGianSv() { return LocalDateTime.now().isAfter(svKetThuc); }
}
