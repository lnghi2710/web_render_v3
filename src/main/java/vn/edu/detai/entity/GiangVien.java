package vn.edu.detai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.edu.detai.enums.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "giang_vien")
@Getter @Setter
public class GiangVien {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bo_mon_id", nullable = false)
    private BoMon boMon;

    @Column(name = "ma_gv", nullable = false, unique = true, length = 30)
    private String maGv;

    public String getHoTen() { return user.getFullName(); }

    public String getInitials() { return vn.edu.detai.util.Names.initials(user.getFullName()); }

    public String getNhan() { return maGv + " - " + user.getFullName(); }
}
