package vn.edu.detai.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CauHinhForm {
    @NotNull(message = "Vui lòng nhập số sinh viên tối đa")
    @Min(value = 1, message = "Tối thiểu 1 sinh viên")
    @Max(value = 10, message = "Tối đa 10 sinh viên")
    private Integer svToiDa;

    @NotNull(message = "Vui lòng nhập trọng số")
    @DecimalMin(value = "0.1", message = "Trọng số từ 0,1 đến 10")
    @DecimalMax(value = "10", message = "Trọng số từ 0,1 đến 10")
    private Double trongSoGvhd;

    @NotNull(message = "Vui lòng nhập trọng số")
    @DecimalMin(value = "0.1", message = "Trọng số từ 0,1 đến 10")
    @DecimalMax(value = "10", message = "Trọng số từ 0,1 đến 10")
    private Double trongSoGvpb;

    @NotNull(message = "Vui lòng nhập trọng số")
    @DecimalMin(value = "0.1", message = "Trọng số từ 0,1 đến 10")
    @DecimalMax(value = "10", message = "Trọng số từ 0,1 đến 10")
    private Double trongSoHoiDong;
}
