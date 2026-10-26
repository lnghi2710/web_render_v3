package vn.edu.detai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import vn.edu.detai.enums.Role;

@Getter @Setter
public class TaiKhoanForm {
    @NotBlank(message = "Vui lòng nhập tên đăng nhập")
    @Size(min = 3, max = 50, message = "Tên đăng nhập từ 3 đến 50 ký tự")
    private String username;
    /** Bắt buộc khi tạo mới; để trống khi sửa nghĩa là giữ nguyên mật khẩu. */
    private String password;
    @NotBlank(message = "Vui lòng nhập họ tên")
    private String fullName;
    @NotBlank(message = "Vui lòng nhập email")
    @Email(message = "Email không hợp lệ")
    private String email;
    @NotNull(message = "Vui lòng chọn vai trò")
    private Role role;
    // Hồ sơ giảng viên
    private String maGv;
    private Long boMonId;
    // Hồ sơ sinh viên
    private String maSv;
    private String lop;
}
