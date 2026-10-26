package vn.edu.detai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.edu.detai.enums.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "de_tai")
@Getter @Setter
public class DeTai {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dot_id", nullable = false)
    private DotDangKy dot;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bo_mon_id", nullable = false)
    private BoMon boMon;

    @Column(name = "ten_de_tai", nullable = false, length = 500)
    private String tenDeTai;

    @Column(name = "mo_ta", nullable = false, length = 4000)
    private String moTa;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false, length = 20)
    private TrangThaiDeTai trangThai = TrangThaiDeTai.CHO_DUYET;

    @Column(name = "ly_do_tu_choi", length = 1000)
    private String lyDoTuChoi;

    /** BR01: từ 1 đến 2 GVHD. */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "de_tai_gvhd",
            joinColumns = @JoinColumn(name = "de_tai_id"),
            inverseJoinColumns = @JoinColumn(name = "giang_vien_id"))
    private List<GiangVien> gvhds = new ArrayList<>();

    /** Nhóm đã đăng ký đề tài này (nếu có). */
    @OneToOne(mappedBy = "deTai", fetch = FetchType.LAZY)
    private Nhom nhom;

    public boolean coGvhd(Long gvId) {
        return gvhds.stream().anyMatch(g -> g.getId().equals(gvId));
    }

    public String getTenGvhds() {
        return String.join(", ", gvhds.stream().map(GiangVien::getHoTen).toList());
    }
}
