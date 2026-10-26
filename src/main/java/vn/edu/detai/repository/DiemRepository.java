package vn.edu.detai.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import vn.edu.detai.entity.*;
import vn.edu.detai.enums.*;
import java.util.*;

public interface DiemRepository extends JpaRepository<Diem, Long> {
    @Query("select d from Diem d where d.nhom.id = :nhomId")
    List<Diem> findByNhomId(@Param("nhomId") Long nhomId);

    @Query("select d from Diem d where d.nhom.id = :nhomId and d.loai = :loai")
    Optional<Diem> findByNhomIdAndLoai(@Param("nhomId") Long nhomId, @Param("loai") LoaiDiem loai);
}
