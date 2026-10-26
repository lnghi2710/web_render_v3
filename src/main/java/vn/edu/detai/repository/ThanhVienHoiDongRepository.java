package vn.edu.detai.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import vn.edu.detai.entity.*;
import vn.edu.detai.enums.*;
import java.util.*;

public interface ThanhVienHoiDongRepository extends JpaRepository<ThanhVienHoiDong, ThanhVienHoiDongId> {
    @Query("select t from ThanhVienHoiDong t where t.giangVien.id = :gvId")
    List<ThanhVienHoiDong> findByGiangVienId(@Param("gvId") Long gvId);

    @Query("select t from ThanhVienHoiDong t where t.hoiDong.id = :hdId and t.giangVien.id = :gvId")
    Optional<ThanhVienHoiDong> findByHoiDongAndGiangVien(@Param("hdId") Long hdId, @Param("gvId") Long gvId);

    @Modifying
    @Query("delete from ThanhVienHoiDong t where t.hoiDong.id = :hdId")
    void xoaTheoHoiDong(@Param("hdId") Long hdId);
}
