package vn.edu.detai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.edu.detai.enums.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "bo_mon")
@Getter @Setter
public class BoMon {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten_bo_mon", nullable = false, unique = true)
    private String tenBoMon;
}
