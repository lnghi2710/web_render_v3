package vn.edu.detai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.edu.detai.enums.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "audit_log")
@Getter @Setter
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "hanh_dong", nullable = false)
    private String hanhDong;

    @Column(name = "doi_tuong")
    private String doiTuong;

    @Column(name = "thoi_gian", nullable = false)
    private LocalDateTime thoiGian;
}
