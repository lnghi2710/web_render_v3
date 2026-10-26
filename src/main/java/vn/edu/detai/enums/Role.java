package vn.edu.detai.enums;

public enum Role {
    ADMIN("Quản trị viên"), TRUONG_KHOA("Trưởng khoa"), GIANG_VIEN("Giảng viên"), SINH_VIEN("Sinh viên");
    private final String nhan;
    Role(String nhan) { this.nhan = nhan; }
    public String getNhan() { return nhan; }
}
