package vn.edu.detai.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import vn.edu.detai.entity.*;
import vn.edu.detai.enums.*;
import java.util.*;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop200ByOrderByThoiGianDesc();

    long countByThoiGianAfter(java.time.LocalDateTime moc);

    @Query(value = "select l from AuditLog l left join l.user u where lower(l.hanhDong) like :q "
            + "or lower(coalesce(l.doiTuong, '')) like :q or lower(coalesce(u.username, '')) like :q order by l.thoiGian desc",
            countQuery = "select count(l) from AuditLog l left join l.user u where lower(l.hanhDong) like :q "
            + "or lower(coalesce(l.doiTuong, '')) like :q or lower(coalesce(u.username, '')) like :q")
    org.springframework.data.domain.Page<AuditLog> tim(@Param("q") String q, org.springframework.data.domain.Pageable p);
}
