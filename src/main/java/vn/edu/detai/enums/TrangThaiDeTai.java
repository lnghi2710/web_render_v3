package vn.edu.detai.enums;

public enum TrangThaiDeTai {
    CHO_DUYET("Chờ duyệt", "warning"), DA_DUYET("Đã duyệt", "success"), TU_CHOI("Từ chối", "danger");
    private final String nhan;
    private final String mau;
    TrangThaiDeTai(String nhan, String mau) { this.nhan = nhan; this.mau = mau; }
    public String getNhan() { return nhan; }
    public String getMau() { return mau; }
}
