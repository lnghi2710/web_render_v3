package vn.edu.detai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.edu.detai.enums.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "sinh_vien")
@Getter @Setter
public class SinhVien {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "ma_sv", nullable = false, unique = true, length = 30)
    private String maSv;

    @Column(nullable = false, length = 50)
    private String lop;

    public String getHoTen() { return user.getFullName(); }

    public String getInitials() { return vn.edu.detai.util.Names.initials(user.getFullName()); }

    public String getNhan() { return maSv + " - " + user.getFullName(); }
}
