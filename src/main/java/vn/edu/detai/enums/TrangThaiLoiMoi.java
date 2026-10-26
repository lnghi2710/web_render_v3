package vn.edu.detai.enums;

/** Trạng thái lời mời vào nhóm. */
public enum TrangThaiLoiMoi {
    CHO("Đang chờ", "info"), DONG_Y("Đã đồng ý", "success"), TU_CHOI("Đã từ chối", "danger"), HUY("Đã hủy", "secondary");
    private final String nhan;
    private final String mau;
    TrangThaiLoiMoi(String nhan, String mau) { this.nhan = nhan; this.mau = mau; }
    public String getNhan() { return nhan; }
    public String getMau() { return mau; }
}
