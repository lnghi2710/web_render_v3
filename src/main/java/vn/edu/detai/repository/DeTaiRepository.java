package vn.edu.detai.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import vn.edu.detai.entity.*;
import vn.edu.detai.enums.*;
import java.util.*;

public interface DeTaiRepository extends JpaRepository<DeTai, Long> {
    List<DeTai> findByDotIdOrderByIdDesc(Long dotId);
    List<DeTai> findByDotIdAndTrangThaiOrderByIdAsc(Long dotId, TrangThaiDeTai trangThai);
    long countByDotId(Long dotId);

    @Query("select distinct d from DeTai d join d.gvhds g where g.id = :gvId order by d.id desc")
    List<DeTai> findByGvhdId(@Param("gvId") Long gvId);

    /** Khóa ghi để hai nhóm không cùng nhận một đề tài (BR04, NFR02). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DeTai d where d.id = :id")
    Optional<DeTai> findByIdForUpdate(@Param("id") Long id);

    long countByBoMonId(Long boMonId);

    List<DeTai> findByTrangThaiOrderByIdAsc(TrangThaiDeTai trangThai);
}
