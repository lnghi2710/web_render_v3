package vn.edu.detai.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import vn.edu.detai.entity.*;
import vn.edu.detai.enums.*;
import java.util.*;

public interface SinhVienRepository extends JpaRepository<SinhVien, Long> {
    Optional<SinhVien> findByUserId(Long userId);
    Optional<SinhVien> findByMaSv(String maSv);

    java.util.List<SinhVien> findAllByOrderByMaSvAsc();
}
