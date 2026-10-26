package vn.edu.detai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.detai.entity.LoiMoiNhom;
import vn.edu.detai.enums.TrangThaiLoiMoi;

import java.util.List;

public interface LoiMoiNhomRepository extends JpaRepository<LoiMoiNhom, Long> {
    @Query("select l from LoiMoiNhom l where l.sinhVien.id = :svId order by l.id desc")
    List<LoiMoiNhom> findDaNhan(@Param("svId") Long svId);

    @Query("select l from LoiMoiNhom l where l.nguoiMoi.id = :svId order by l.id desc")
    List<LoiMoiNhom> findDaGui(@Param("svId") Long svId);

    @Query("select count(l) from LoiMoiNhom l where l.sinhVien.id = :svId and l.trangThai = :tt")
    long demDaNhan(@Param("svId") Long svId, @Param("tt") TrangThaiLoiMoi tt);

    @Query("select count(l) from LoiMoiNhom l where l.nhom.id = :nhomId and l.sinhVien.id = :svId and l.trangThai = :tt")
    long demTheoNhomVaSv(@Param("nhomId") Long nhomId, @Param("svId") Long svId, @Param("tt") TrangThaiLoiMoi tt);

    /** Các lời mời đang chờ của một sinh viên trong cùng một đợt. */
    @Query("select l from LoiMoiNhom l where l.sinhVien.id = :svId and l.nhom.dot.id = :dotId and l.trangThai = :tt")
    List<LoiMoiNhom> findTheoSvVaDot(@Param("svId") Long svId, @Param("dotId") Long dotId, @Param("tt") TrangThaiLoiMoi tt);

    @Modifying
    @Query("delete from LoiMoiNhom l where l.nhom.id = :nhomId")
    void xoaTheoNhom(@Param("nhomId") Long nhomId);
}
