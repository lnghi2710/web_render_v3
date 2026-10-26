package vn.edu.detai.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import vn.edu.detai.entity.*;
import vn.edu.detai.enums.*;
import java.util.*;

public interface NhomRepository extends JpaRepository<Nhom, Long> {
    List<Nhom> findByDotIdOrderByIdAsc(Long dotId);
    long countByDotId(Long dotId);
    Optional<Nhom> findByDeTaiId(Long deTaiId);
    List<Nhom> findByGvpbId(Long gvId);

    @Query("select n from Nhom n join n.deTai d join d.gvhds g where g.id = :gvId order by n.id desc")
    List<Nhom> findByGvhdId(@Param("gvId") Long gvId);

    @Query("select tv.nhom from ThanhVienNhom tv where tv.sinhVien.id = :svId and tv.dot.id = :dotId")
    Optional<Nhom> findByThanhVien(@Param("svId") Long svId, @Param("dotId") Long dotId);
}
