package vn.edu.detai.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import vn.edu.detai.entity.*;
import vn.edu.detai.enums.*;
import java.util.*;

public interface GiangVienRepository extends JpaRepository<GiangVien, Long> {
    Optional<GiangVien> findByUserId(Long userId);
    Optional<GiangVien> findByMaGv(String maGv);
    List<GiangVien> findAllByOrderByMaGvAsc();

    long countByBoMonId(Long boMonId);
}
