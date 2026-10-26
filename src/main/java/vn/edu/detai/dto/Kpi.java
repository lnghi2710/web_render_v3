package vn.edu.detai.dto;

/** Một chỉ số nổi bật ở trang chủ; muc: ok, info, warn, danger. */
public record Kpi(String nhan, String giaTri, String ghiChu, String icon, String muc, String url) {
}
