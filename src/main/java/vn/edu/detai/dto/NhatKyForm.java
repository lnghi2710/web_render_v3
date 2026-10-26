package vn.edu.detai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter @Setter
public class NhatKyForm {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime thoiGian;

    @NotBlank(message = "Vui lòng nhập nội dung buổi hướng dẫn")
    @Size(max = 2000, message = "Nội dung tối đa 2000 ký tự")
    private String noiDung;

    @Size(max = 1000, message = "Việc tiếp theo tối đa 1000 ký tự")
    private String viecTiepTheo;
}
