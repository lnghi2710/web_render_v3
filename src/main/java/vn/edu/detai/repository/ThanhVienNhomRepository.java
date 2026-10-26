package vn.edu.detai.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import vn.edu.detai.entity.*;
import vn.edu.detai.enums.*;
import java.util.*;

public interface ThanhVienNhomRepository extends JpaRepository<ThanhVienNhom, ThanhVienNhomId> {
    @Query("select tv from ThanhVienNhom tv where tv.nhom.id = :nhomId")
    List<ThanhVienNhom> findByNhomId(@Param("nhomId") Long nhomId);

    @Query("select count(tv) from ThanhVienNhom tv where tv.nhom.id = :nhomId")
    long demTheoNhom(@Param("nhomId") Long nhomId);

    @Query("select count(tv) from ThanhVienNhom tv where tv.sinhVien.id = :svId and tv.dot.id = :dotId")
    long demSvTrongDot(@Param("svId") Long svId, @Param("dotId") Long dotId);

    @Modifying
    @Query("delete from ThanhVienNhom tv where tv.nhom.id = :nhomId")
    void xoaTheoNhom(@Param("nhomId") Long nhomId);

    @Query("select tv.nhom from ThanhVienNhom tv where tv.sinhVien.id = :svId order by tv.nhom.id desc")
    List<Nhom> findNhomCuaSv(@Param("svId") Long svId);
}
