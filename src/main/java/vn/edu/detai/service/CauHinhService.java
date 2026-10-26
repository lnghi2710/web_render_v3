package vn.edu.detai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.detai.dto.CauHinhForm;
import vn.edu.detai.entity.CauHinh;
import vn.edu.detai.enums.LoaiDiem;
import vn.edu.detai.exception.BusinessException;
import vn.edu.detai.repository.CauHinhRepository;

/** Cấu hình hệ thống do quản trị viên chỉnh: số sinh viên tối đa mỗi nhóm và trọng số các điểm thành phần. */
@Service
@RequiredArgsConstructor
public class CauHinhService {
    public static final int SV_TOI_DA_MAC_DINH = 3;
    private static final String KHOA_SV_TOI_DA = "sv.toi.da";
    private static final String TIEN_TO_TRONG_SO = "trong.so.";

    private final CauHinhRepository repository;
    private final AuditService audit;

    /** BR02: số sinh viên tối đa trong một nhóm. */
    @Transactional(readOnly = true)
    public int svToiDa() {
        return repository.findById(KHOA_SV_TOI_DA)
                .map(c -> docSo(c.getGiaTri(), SV_TOI_DA_MAC_DINH))
                .orElse(SV_TOI_DA_MAC_DINH);
    }

    /** BR13: trọng số của một điểm thành phần; mặc định 1 nên điểm cuối là trung bình cộng. */
    @Transactional(readOnly = true)
    public double trongSo(LoaiDiem loai) {
        return repository.findById(TIEN_TO_TRONG_SO + loai.name())
                .map(c -> docThuc(c.getGiaTri(), 1.0))
                .filter(x -> x > 0)
                .orElse(1.0);
    }

    @Transactional(readOnly = true)
    public CauHinhForm hienTai() {
        CauHinhForm f = new CauHinhForm();
        f.setSvToiDa(svToiDa());
        f.setTrongSoGvhd(trongSo(LoaiDiem.GVHD));
        f.setTrongSoGvpb(trongSo(LoaiDiem.GVPB));
        f.setTrongSoHoiDong(trongSo(LoaiDiem.HOI_DONG));
        return f;
    }

    @Transactional
    public void luu(CauHinhForm f) {
        if (f.getSvToiDa() == null || f.getSvToiDa() < 1 || f.getSvToiDa() > 10) {
            throw new BusinessException("Số sinh viên tối đa mỗi nhóm phải từ 1 đến 10.");
        }
        datGiaTri(KHOA_SV_TOI_DA, String.valueOf(f.getSvToiDa()));
        datGiaTri(TIEN_TO_TRONG_SO + LoaiDiem.GVHD.name(), kiemTraTrongSo(f.getTrongSoGvhd()));
        datGiaTri(TIEN_TO_TRONG_SO + LoaiDiem.GVPB.name(), kiemTraTrongSo(f.getTrongSoGvpb()));
        datGiaTri(TIEN_TO_TRONG_SO + LoaiDiem.HOI_DONG.name(), kiemTraTrongSo(f.getTrongSoHoiDong()));
        audit.ghi("Cập nhật cấu hình hệ thống", "CauHinh");
    }

    private String kiemTraTrongSo(Double x) {
        if (x == null || x < 0.1 || x > 10) throw new BusinessException("Trọng số phải từ 0,1 đến 10.");
        return String.valueOf(x);
    }

    private void datGiaTri(String khoa, String giaTri) {
        CauHinh c = repository.findById(khoa).orElseGet(() -> {
            CauHinh moi = new CauHinh();
            moi.setKhoa(khoa);
            return moi;
        });
        c.setGiaTri(giaTri);
        repository.save(c);
    }

    private static int docSo(String s, int macDinh) {
        try {
            return Integer.parseInt(s.trim());
        } catch (RuntimeException e) {
            return macDinh;
        }
    }

    private static double docThuc(String s, double macDinh) {
        try {
            return Double.parseDouble(s.trim());
        } catch (RuntimeException e) {
            return macDinh;
        }
    }
}
