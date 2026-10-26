package vn.edu.detai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Một buổi hướng dẫn do GVHD ghi lại cho nhóm (nội dung đã trao đổi, việc cần làm tiếp). */
@Entity
@Table(name = "nhat_ky_huong_dan")
@Getter @Setter
public class NhatKyHuongDan {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nhom_id", nullable = false)
    private Nhom nhom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "giang_vien_id", nullable = false)
    private GiangVien giangVien;

    @Column(name = "thoi_gian", nullable = false)
    private LocalDateTime thoiGian;

    @Column(name = "noi_dung", nullable = false, length = 2000)
    private String noiDung;

    @Column(name = "viec_tiep_theo", length = 1000)
    private String viecTiepTheo;
}
