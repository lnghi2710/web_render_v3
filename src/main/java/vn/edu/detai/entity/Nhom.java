package vn.edu.detai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.edu.detai.enums.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "nhom")
@Getter @Setter
public class Nhom {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dot_id", nullable = false)
    private DotDangKy dot;

    @Column(name = "ten_nhom", nullable = false)
    private String tenNhom;

    /** UNIQUE: mỗi đề tài chỉ có một nhóm chọn (BR04). Rỗng đến khi nhóm đăng ký. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "de_tai_id", unique = true)
    private DeTai deTai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gvpb_id")
    private GiangVien gvpb;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hoi_dong_id")
    private HoiDong hoiDong;

    /** Đường dẫn tương đối của tệp báo cáo PDF (BR08). */
    @Column(name = "file_bao_cao")
    private String fileBaoCao;

    @Column(name = "ngay_nop_bao_cao")
    private LocalDateTime ngayNopBaoCao;

    /** Điểm cuối, được tính và lưu khi công bố kết quả (FR10, FR19). */
    @Column(name = "diem_cuoi")
    private Double diemCuoi;

    @OneToMany(mappedBy = "nhom")
    private List<ThanhVienNhom> thanhViens = new ArrayList<>();

    public Optional<ThanhVienNhom> truong() {
        return thanhViens.stream().filter(ThanhVienNhom::isLaNhomTruong).findFirst();
    }

    public boolean laNhomTruong(Long svId) {
        return thanhViens.stream().anyMatch(t -> t.isLaNhomTruong() && t.getSinhVien().getId().equals(svId));
    }

    public boolean coThanhVien(Long svId) {
        return thanhViens.stream().anyMatch(t -> t.getSinhVien().getId().equals(svId));
    }
}
