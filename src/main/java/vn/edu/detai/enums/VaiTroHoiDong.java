package vn.edu.detai.enums;

public enum VaiTroHoiDong {
    CHU_TICH("Chủ tịch"), THU_KY("Thư ký"), THANH_VIEN("Thành viên");
    private final String nhan;
    VaiTroHoiDong(String nhan) { this.nhan = nhan; }
    public String getNhan() { return nhan; }
}
