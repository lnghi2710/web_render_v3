package vn.edu.detai.enums;

/** Trạng thái đợt được suy ra từ các mốc thời gian (BR07), không lưu trong CSDL. */
public enum TrangThaiDot {
    CHUA_MO("Chưa mở", "secondary"),
    GV_DANG_KY("GV đăng ký đề tài", "primary"),
    CHO_SV("Chờ SV đăng ký", "info"),
    SV_DANG_KY("SV đăng ký đề tài", "success"),
    THUC_HIEN("Đang thực hiện", "warning"),
    DA_CONG_BO("Đã công bố kết quả", "dark");
    private final String nhan;
    private final String mau;
    TrangThaiDot(String nhan, String mau) { this.nhan = nhan; this.mau = mau; }
    public String getNhan() { return nhan; }
    public String getMau() { return mau; }
}
