package vn.edu.detai.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Một buổi báo cáo của hội đồng. */
public record LichBaoVe(String hoiDong, String dot, LocalDateTime thoiGian, String diaDiem,
                        List<String> thanhVien, List<String> nhom, String ghiChu) {
    public boolean daQua() { return thoiGian != null && thoiGian.isBefore(LocalDateTime.now()); }
}
