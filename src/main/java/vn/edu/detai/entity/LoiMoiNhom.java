package vn.edu.detai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.edu.detai.enums.TrangThaiLoiMoi;

import java.time.LocalDateTime;

/** Lời mời của nhóm trưởng gửi một sinh viên chưa có nhóm. */
@Entity
@Table(name = "loi_moi_nhom")
@Getter @Setter
public class LoiMoiNhom {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nhom_id", nullable = false)
    private Nhom nhom;

    /** Sinh viên được mời. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sinh_vien_id", nullable = false)
    private SinhVien sinhVien;

    /** Nhóm trưởng gửi lời mời. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nguoi_moi_id", nullable = false)
    private SinhVien nguoiMoi;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false, length = 20)
    private TrangThaiLoiMoi trangThai = TrangThaiLoiMoi.CHO;

    @Column(name = "ngay_tao", nullable = false)
    private LocalDateTime ngayTao;

    @Column(name = "ngay_tra_loi")
    private LocalDateTime ngayTraLoi;
}
