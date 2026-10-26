package vn.edu.detai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.detai.entity.NhatKyHuongDan;

import java.util.List;

public interface NhatKyHuongDanRepository extends JpaRepository<NhatKyHuongDan, Long> {
    @Query("select k from NhatKyHuongDan k where k.nhom.id = :nhomId order by k.thoiGian desc, k.id desc")
    List<NhatKyHuongDan> findTheoNhom(@Param("nhomId") Long nhomId);

    @Query("select count(k) from NhatKyHuongDan k where k.nhom.id = :nhomId")
    long demTheoNhom(@Param("nhomId") Long nhomId);

    @Modifying
    @Query("delete from NhatKyHuongDan k where k.nhom.id = :nhomId")
    void xoaTheoNhom(@Param("nhomId") Long nhomId);
}
