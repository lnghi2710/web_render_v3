package vn.edu.detai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.edu.detai.enums.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "thanh_vien_hoi_dong")
@Getter @Setter
public class ThanhVienHoiDong {
    @EmbeddedId
    private ThanhVienHoiDongId id = new ThanhVienHoiDongId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("hoiDongId")
    @JoinColumn(name = "hoi_dong_id")
    private HoiDong hoiDong;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("giangVienId")
    @JoinColumn(name = "giang_vien_id")
    private GiangVien giangVien;

    @Enumerated(EnumType.STRING)
    @Column(name = "vai_tro", nullable = false, length = 20)
    private VaiTroHoiDong vaiTro;
}
