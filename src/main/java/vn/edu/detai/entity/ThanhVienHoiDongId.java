package vn.edu.detai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.edu.detai.enums.*;
import java.time.LocalDateTime;
import java.util.*;

@Embeddable
@Getter @Setter
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@lombok.EqualsAndHashCode
public class ThanhVienHoiDongId implements java.io.Serializable {
    @Column(name = "hoi_dong_id")
    private Long hoiDongId;
    @Column(name = "giang_vien_id")
    private Long giangVienId;
}
