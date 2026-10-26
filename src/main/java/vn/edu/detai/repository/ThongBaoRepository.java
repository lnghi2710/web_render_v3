package vn.edu.detai.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import vn.edu.detai.entity.*;
import vn.edu.detai.enums.*;
import java.util.*;

public interface ThongBaoRepository extends JpaRepository<ThongBao, Long> {
    List<ThongBao> findAllByOrderByNgayDangDesc();
    List<ThongBao> findTop5ByOrderByNgayDangDesc();
}
