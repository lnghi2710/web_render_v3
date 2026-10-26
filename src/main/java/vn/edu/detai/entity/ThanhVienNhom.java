package vn.edu.detai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.edu.detai.enums.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "thanh_vien_nhom",
        uniqueConstraints = @UniqueConstraint(name = "uk_sv_dot", columnNames = {"sinh_vien_id", "dot_id"}))
@Getter @Setter
public class ThanhVienNhom {
    @EmbeddedId
    private ThanhVienNhomId id = new ThanhVienNhomId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("nhomId")
    @JoinColumn(name = "nhom_id")
    private Nhom nhom;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("sinhVienId")
    @JoinColumn(name = "sinh_vien_id")
    private SinhVien sinhVien;

    /** Dư thừa có chủ đích để khai báo UNIQUE(sinh_vien_id, dot_id) — BR03. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dot_id", nullable = false)
    private DotDangKy dot;

    @Column(name = "la_nhom_truong", nullable = false)
    private boolean laNhomTruong;
}
