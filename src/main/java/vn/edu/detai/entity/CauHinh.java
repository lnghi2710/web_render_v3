package vn.edu.detai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Cặp khóa - giá trị cấu hình hệ thống (số SV tối đa mỗi nhóm, trọng số điểm...). */
@Entity
@Table(name = "cau_hinh")
@Getter @Setter
public class CauHinh {
    @Id
    @Column(name = "khoa", length = 60)
    private String khoa;

    @Column(name = "gia_tri", nullable = false)
    private String giaTri;
}
