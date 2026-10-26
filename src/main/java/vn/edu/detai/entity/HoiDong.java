package vn.edu.detai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.edu.detai.enums.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "hoi_dong")
@Getter @Setter
public class HoiDong {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dot_id", nullable = false)
    private DotDangKy dot;

    @Column(name = "ten_hoi_dong", nullable = false)
    private String tenHoiDong;

    @Column(name = "thoi_gian")
    private LocalDateTime thoiGian;

    @Column(name = "dia_diem")
    private String diaDiem;

    @OneToMany(mappedBy = "hoiDong")
    private List<ThanhVienHoiDong> thanhViens = new ArrayList<>();

    @OneToMany(mappedBy = "hoiDong")
    private List<Nhom> nhoms = new ArrayList<>();

    public Optional<ThanhVienHoiDong> thanhVienTheoVaiTro(VaiTroHoiDong vt) {
        return thanhViens.stream().filter(t -> t.getVaiTro() == vt).findFirst();
    }

    public boolean coGiangVien(Long gvId) {
        return thanhViens.stream().anyMatch(t -> t.getGiangVien().getId().equals(gvId));
    }
}
