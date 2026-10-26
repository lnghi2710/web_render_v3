package vn.edu.detai.dto;

import java.util.List;

/** Kết quả nhập tài khoản hàng loạt từ tệp CSV. */
public record KetQuaNhap(int tong, int thanhCong, List<String> loi) implements java.io.Serializable {
}
