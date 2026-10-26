package vn.edu.detai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import vn.edu.detai.enums.LoaiDot;

import java.time.LocalDateTime;

@Getter @Setter
public class DotForm {
    @NotBlank(message = "Vui lòng nhập tên đợt")
    private String tenDot;
    @NotNull(message = "Vui lòng chọn loại đợt")
    private LoaiDot loai;
    @NotNull(message = "Vui lòng nhập thời gian bắt đầu của GV")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime gvBatDau;
    @NotNull(message = "Vui lòng nhập thời gian kết thúc của GV")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime gvKetThuc;
    @NotNull(message = "Vui lòng nhập thời gian bắt đầu của SV")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime svBatDau;
    @NotNull(message = "Vui lòng nhập thời gian kết thúc của SV")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime svKetThuc;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime hanNopDiemGvpb;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime ngayBaoCaoHoiDong;
}
