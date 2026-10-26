package vn.edu.detai.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import vn.edu.detai.enums.LoaiDiem;

@Getter @Setter
public class DiemForm {
    @NotNull
    private LoaiDiem loai;
    @NotNull(message = "Vui lòng nhập điểm")
    @DecimalMin(value = "0", message = "Điểm phải từ 0 đến 10")
    @DecimalMax(value = "10", message = "Điểm phải từ 0 đến 10")
    private Double diem;
    @Size(max = 2000, message = "Nhận xét tối đa 2000 ký tự")
    private String nhanXet;
}
