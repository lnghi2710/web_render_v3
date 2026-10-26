package vn.edu.detai.dto;

/** Một thanh trong biểu đồ tiến độ: giaTri trên toiDa. */
public record Cot(String nhan, long giaTri, long toiDa) {
    public int phanTram() {
        if (toiDa <= 0) return 0;
        return (int) Math.min(100, Math.round(giaTri * 100.0 / toiDa));
    }

    /** Màu thanh: ok khi đủ, info từ một nửa, warn khi còn ít, none khi không có mẫu số. */
    public String muc() {
        if (toiDa <= 0) return "none";
        int p = phanTram();
        return p >= 100 ? "ok" : p >= 50 ? "info" : "warn";
    }
}
