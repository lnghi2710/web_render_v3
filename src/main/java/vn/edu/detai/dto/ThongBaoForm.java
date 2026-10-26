package vn.edu.detai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ThongBaoForm {
    @NotBlank(message = "Vui lòng nhập tiêu đề")
    @Size(max = 255, message = "Tiêu đề tối đa 255 ký tự")
    private String tieuDe;
    @NotBlank(message = "Vui lòng nhập nội dung")
    @Size(max = 4000, message = "Nội dung tối đa 4000 ký tự")
    private String noiDung;
}
