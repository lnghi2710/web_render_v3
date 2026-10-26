package vn.edu.detai.dto;

import vn.edu.detai.entity.DotDangKy;
import vn.edu.detai.entity.ThongBao;

import java.util.List;

/**
 * Dữ liệu tổng quan theo vai trò: dùng cho trang chủ, chuông thông báo và số đếm trên thanh bên.
 * choDuyet (Trưởng khoa), canCham (giảng viên), loiMoi (sinh viên).
 */
public record TongQuan(List<ViecCanLam> viec, List<Kpi> kpi, List<Bieu> bieu, List<DotDangKy> dots,
                       int choDuyet, int canCham, int loiMoi, List<ThongBao> thongBaoMoi) {

    /** Số việc cần chú ý (cảnh báo hoặc quá hạn) hiện trên chuông. */
    public int soCanhBao() {
        int n = 0;
        for (ViecCanLam v : viec) {
            if ("warn".equals(v.muc()) || "danger".equals(v.muc())) n++;
        }
        return n;
    }
}
