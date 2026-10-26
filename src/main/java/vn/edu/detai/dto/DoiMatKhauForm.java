package vn.edu.detai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DoiMatKhauForm {
    @NotBlank(message = "Vui lòng nhập mật khẩu hiện tại")
    private String matKhauCu;
    @NotBlank(message = "Vui lòng nhập mật khẩu mới")
    @Size(min = 6, message = "Mật khẩu mới tối thiểu 6 ký tự")
    private String matKhauMoi;
    @NotBlank(message = "Vui lòng nhập lại mật khẩu mới")
    private String nhapLai;
}
