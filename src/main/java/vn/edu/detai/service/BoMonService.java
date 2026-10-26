package vn.edu.detai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.detai.dto.BoMonThongKe;
import vn.edu.detai.entity.BoMon;
import vn.edu.detai.exception.BusinessException;
import vn.edu.detai.repository.BoMonRepository;
import vn.edu.detai.repository.DeTaiRepository;
import vn.edu.detai.repository.GiangVienRepository;

import java.util.Comparator;
import java.util.List;

/** Quản lý danh mục bộ môn (quản trị viên). */
@Service
@RequiredArgsConstructor
public class BoMonService {
    private final BoMonRepository boMonRepository;
    private final GiangVienRepository giangVienRepository;
    private final DeTaiRepository deTaiRepository;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public List<BoMonThongKe> tatCa() {
        return boMonRepository.findAll().stream()
                .sorted(Comparator.comparing(BoMon::getTenBoMon, String.CASE_INSENSITIVE_ORDER))
                .map(b -> new BoMonThongKe(b, giangVienRepository.countByBoMonId(b.getId()),
                        deTaiRepository.countByBoMonId(b.getId())))
                .toList();
    }

    @Transactional
    public BoMon tao(String ten) {
        String t = chuanHoa(ten);
        if (boMonRepository.findByTenBoMon(t).isPresent()) throw new BusinessException("Bộ môn \"" + t + "\" đã tồn tại.");
        BoMon b = new BoMon();
        b.setTenBoMon(t);
        b = boMonRepository.save(b);
        audit.ghi("Thêm bộ môn", "BoMon#" + b.getId());
        return b;
    }

    @Transactional
    public BoMon doiTen(Long id, String ten) {
        BoMon b = boMonRepository.findById(id).orElseThrow(() -> new BusinessException("Không tìm thấy bộ môn."));
        String t = chuanHoa(ten);
        boMonRepository.findByTenBoMon(t).filter(x -> !x.getId().equals(id)).ifPresent(x -> {
            throw new BusinessException("Bộ môn \"" + t + "\" đã tồn tại.");
        });
        b.setTenBoMon(t);
        audit.ghi("Đổi tên bộ môn", "BoMon#" + id);
        return boMonRepository.save(b);
    }

    @Transactional
    public void xoa(Long id) {
        BoMon b = boMonRepository.findById(id).orElseThrow(() -> new BusinessException("Không tìm thấy bộ môn."));
        if (giangVienRepository.countByBoMonId(id) > 0 || deTaiRepository.countByBoMonId(id) > 0) {
            throw new BusinessException("Bộ môn đã có giảng viên hoặc đề tài nên không thể xóa.");
        }
        boMonRepository.delete(b);
        audit.ghi("Xóa bộ môn", "BoMon#" + id);
    }

    private String chuanHoa(String ten) {
        if (ten == null || ten.isBlank()) throw new BusinessException("Vui lòng nhập tên bộ môn.");
        String t = ten.trim().replaceAll("\\s+", " ");
        if (t.length() > 255) throw new BusinessException("Tên bộ môn tối đa 255 ký tự.");
        return t;
    }
}
