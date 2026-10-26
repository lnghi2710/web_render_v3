package vn.edu.detai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.edu.detai.enums.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "diem",
        uniqueConstraints = @UniqueConstraint(name = "uk_nhom_loai", columnNames = {"nhom_id", "loai"}))
@Getter @Setter
public class Diem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nhom_id", nullable = false)
    private Nhom nhom;

    /** Giảng viên nhập điểm. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "giang_vien_id", nullable = false)
    private GiangVien giangVien;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoaiDiem loai;

    @Column(name = "diem", nullable = false)
    private Double diem;

    @Column(name = "nhan_xet", length = 2000)
    private String nhanXet;
}
