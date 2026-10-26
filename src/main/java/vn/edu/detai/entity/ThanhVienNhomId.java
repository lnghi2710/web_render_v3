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
public class ThanhVienNhomId implements java.io.Serializable {
    @Column(name = "nhom_id")
    private Long nhomId;
    @Column(name = "sinh_vien_id")
    private Long sinhVienId;
}
