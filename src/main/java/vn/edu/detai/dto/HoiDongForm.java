package vn.edu.detai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class HoiDongForm {
    @NotBlank(message = "Vui lòng nhập tên hội đồng")
    private String tenHoiDong;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime thoiGian;
    private String diaDiem;
    /** Danh sách GV tham gia (3 đến 5) — BR10. */
    private List<Long> thanhVienIds = new ArrayList<>();
    @NotNull(message = "Vui lòng chọn chủ tịch")
    private Long chuTichId;
    @NotNull(message = "Vui lòng chọn thư ký")
    private Long thuKyId;
}
