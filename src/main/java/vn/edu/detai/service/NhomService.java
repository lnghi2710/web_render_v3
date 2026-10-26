package vn.edu.detai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.detai.dto.*;
import vn.edu.detai.entity.*;
import vn.edu.detai.enums.*;
import vn.edu.detai.exception.BusinessException;
import vn.edu.detai.repository.*;
import vn.edu.detai.security.CurrentUser;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class NhomService {
    private static final String NGOAI_GIO_SV = "Ngoài thời gian sinh viên đăng ký của đợt này (BR05).";

    private final NhomRepository nhomRepository;
    private final ThanhVienNhomRepository thanhVienRepository;
    private final DeTaiRepository deTaiRepository;
    private final SinhVienRepository sinhVienRepository;
    private final DotService dotService;
    private final CauHinhService cauHinh;
    private final LoiMoiNhomRepository loiMoiRepository;
    private final NhatKyHuongDanRepository nhatKyRepository;
    private final FileStorageService fileStorage;
    private final CurrentUser currentUser;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public Optional<Nhom> nhomCuaToi(Long dotId) {
        return nhomRepository.findByThanhVien(currentUser.sinhVien().getId(), dotId);
    }

    /** BR02: số sinh viên tối đa mỗi nhóm (cấu hình được). */
    public int toiDa() { return cauHinh.svToiDa(); }

    @Transactional(readOnly = true)
    public Nhom lay(Long id) {
        return nhomRepository.findById(id).orElseThrow(() -> new BusinessException("Không tìm thấy nhóm."));
    }

    /** FR15: SV chưa thuộc nhóm nào trong đợt tạo nhóm và trở thành nhóm trưởng (BR02, BR03). */
    @Transactional
    public Nhom taoNhom(Long dotId, String tenNhom) {
        SinhVien sv = currentUser.sinhVien();
        DotDangKy dot = dotService.lay(dotId);
        if (!dot.trongThoiGianSv()) throw new BusinessException(NGOAI_GIO_SV);
        if (thanhVienRepository.demSvTrongDot(sv.getId(), dotId) > 0) {
            throw new BusinessException("Bạn đã thuộc một nhóm trong đợt này (BR03).");
        }
        Nhom n = new Nhom();
        n.setDot(dot);
        n.setTenNhom(tenNhom == null || tenNhom.isBlank() ? "Nhóm " + sv.getMaSv() : tenNhom.trim());
        n = nhomRepository.save(n);
        themThanhVien(n, sv, dot, true);
        audit.ghi("Tạo nhóm", "Nhom#" + n.getId());
        return n;
    }

    @Transactional
    public Nhom themThanhVien(Long nhomId, String maSv) {
        Nhom n = nhomCuaTruong(nhomId);
        kiemTraGioSv(n);
        if (thanhVienRepository.demTheoNhom(nhomId) >= cauHinh.svToiDa()) {
            throw new BusinessException("Nhóm đã đủ " + cauHinh.svToiDa() + " sinh viên (BR02).");
        }
        SinhVien sv = sinhVienRepository.findByMaSv(maSv == null ? "" : maSv.trim())
                .orElseThrow(() -> new BusinessException("Không tìm thấy sinh viên có mã " + maSv + "."));
        if (thanhVienRepository.demSvTrongDot(sv.getId(), n.getDot().getId()) > 0) {
            throw new BusinessException("Sinh viên " + sv.getMaSv() + " đã thuộc một nhóm trong đợt này (BR03).");
        }
        themThanhVien(n, sv, n.getDot(), false);
        return n;
    }

    @Transactional
    public Nhom xoaThanhVien(Long nhomId, Long svId) {
        Nhom n = nhomCuaTruong(nhomId);
        kiemTraGioSv(n);
        ThanhVienNhom tv = thanhVienRepository.findById(new ThanhVienNhomId(nhomId, svId))
                .orElseThrow(() -> new BusinessException("Sinh viên không thuộc nhóm này."));
        if (tv.isLaNhomTruong()) {
            throw new BusinessException("Không thể xóa nhóm trưởng khỏi nhóm.");
        }
        thanhVienRepository.delete(tv);
        return n;
    }

    @Transactional
    public Long giaiTan(Long nhomId) {
        Nhom n = nhomCuaTruong(nhomId);
        kiemTraGioSv(n);
        if (n.getDeTai() != null) throw new BusinessException("Hãy hủy đăng ký đề tài trước khi giải tán nhóm.");
        Long dotId = n.getDot().getId();
        loiMoiRepository.xoaTheoNhom(nhomId);
        nhatKyRepository.xoaTheoNhom(nhomId);
        thanhVienRepository.xoaTheoNhom(nhomId);
        nhomRepository.delete(n);
        audit.ghi("Giải tán nhóm", "Nhom#" + nhomId);
        return dotId;
    }

    /** FR16 / BR04: nhóm đăng ký trước được nhận; thực hiện trong một giao dịch có khóa ghi trên đề tài. */
    @Transactional
    public Nhom dangKyDeTai(Long nhomId, Long deTaiId) {
        Nhom n = nhomCuaTruong(nhomId);
        kiemTraGioSv(n);
        if (n.getDeTai() != null) {
            throw new BusinessException("Nhóm đã đăng ký một đề tài. Hãy hủy đăng ký trước khi chọn đề tài khác (BR04).");
        }
        DeTai d = deTaiRepository.findByIdForUpdate(deTaiId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đề tài."));
        if (!d.getDot().getId().equals(n.getDot().getId())) {
            throw new BusinessException("Đề tài không thuộc đợt đăng ký của nhóm.");
        }
        if (d.getTrangThai() != TrangThaiDeTai.DA_DUYET) {
            throw new BusinessException("Đề tài chưa nằm trong danh sách đã công bố.");
        }
        if (nhomRepository.findByDeTaiId(d.getId()).isPresent()) {
            throw new BusinessException("Đề tài đã có nhóm khác đăng ký trước (BR04).");
        }
        n.setDeTai(d);
        try {
            nhomRepository.saveAndFlush(n);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Đề tài đã có nhóm khác đăng ký trước (BR04).");
        }
        audit.ghi("Đăng ký đề tài cho nhóm", "Nhom#" + n.getId() + " -> DeTai#" + d.getId());
        return n;
    }

    @Transactional
    public Nhom huyDangKy(Long nhomId) {
        Nhom n = nhomCuaTruong(nhomId);
        kiemTraGioSv(n);
        if (n.getDeTai() == null) throw new BusinessException("Nhóm chưa đăng ký đề tài.");
        if (n.getGvpb() != null || n.getHoiDong() != null) {
            throw new BusinessException("Nhóm đã được phân công GVPB hoặc hội đồng nên không thể hủy đăng ký.");
        }
        Long deTaiId = n.getDeTai().getId();
        n.setDeTai(null);
        nhomRepository.save(n);
        audit.ghi("Hủy đăng ký đề tài", "Nhom#" + n.getId() + " -x DeTai#" + deTaiId);
        return n;
    }

    /** FR17 / BR08: chỉ nhóm trưởng nộp; sau thời gian SV đăng ký và trước khi công bố; nộp lại thì thay thế. */
    @Transactional
    public Nhom nopBaoCao(Long nhomId, MultipartFile file) {
        Nhom n = nhomCuaTruong(nhomId);
        DotDangKy dot = n.getDot();
        if (n.getDeTai() == null) throw new BusinessException("Nhóm chưa đăng ký đề tài nên chưa thể nộp báo cáo.");
        if (!dot.sauThoiGianSv()) {
            throw new BusinessException("Chỉ nộp báo cáo sau khi kết thúc thời gian sinh viên đăng ký (BR08).");
        }
        if (dot.isDaCongBo()) throw new BusinessException("Kết quả đã được công bố, không thể nộp báo cáo (BR08).");
        n.setFileBaoCao(fileStorage.luuBaoCao(file, dot.getId(), n.getId()));
        n.setNgayNopBaoCao(LocalDateTime.now());
        audit.ghi("Nộp báo cáo", "Nhom#" + n.getId());
        return nhomRepository.save(n);
    }

    // ----- hỗ trợ -----
    private void themThanhVien(Nhom n, SinhVien sv, DotDangKy dot, boolean truong) {
        ThanhVienNhom tv = new ThanhVienNhom();
        tv.setNhom(n);
        tv.setSinhVien(sv);
        tv.setDot(dot);
        tv.setLaNhomTruong(truong);
        thanhVienRepository.save(tv);
    }

    private Nhom nhomCuaTruong(Long nhomId) {
        Nhom n = lay(nhomId);
        if (!n.laNhomTruong(currentUser.sinhVien().getId())) {
            throw new BusinessException("Chỉ nhóm trưởng được thực hiện thao tác này.");
        }
        return n;
    }

    private void kiemTraGioSv(Nhom n) {
        if (!n.getDot().trongThoiGianSv()) throw new BusinessException(NGOAI_GIO_SV);
    }
}
