package vn.edu.detai.dto;

/** Một việc cần xử lý hiển thị ở trang chủ; muc: info, warn, danger hoặc ok. */
public record ViecCanLam(String noiDung, String url, String muc) {
}
