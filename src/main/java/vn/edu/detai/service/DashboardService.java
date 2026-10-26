package vn.edu.detai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.detai.dto.*;
import vn.edu.detai.entity.*;
import vn.edu.detai.enums.Role;
import vn.edu.detai.enums.TrangThaiDeTai;
import vn.edu.detai.enums.TrangThaiLoiMoi;
import vn.edu.detai.repository.LoiMoiNhomRepository;
import vn.edu.detai.repository.NhomRepository;
import vn.edu.detai.security.CurrentUser;

import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Tổng hợp trang chủ theo vai trò: việc cần xử lý, chỉ số nổi bật, biểu đồ tiến độ và các đợt liên quan.
 * Chỉ hiển thị đợt liên quan tới người dùng: quản trị viên không thấy đợt; giảng viên và sinh viên
 * chỉ thấy đợt mình tham gia hoặc đang mở cho mình.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {
    private static final DateTimeFormatter NGAY_GIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int TOI_DA_DOT = 3;

    private final DotService dotService;
    private final DeTaiService deTaiService;
    private final DiemService diemService;
    private final PhanCongService phanCongService;
    private final ThongKeService thongKeService;
    private final ThongBaoService thongBaoService;
    private final NhomRepository nhomRepository;
    private final LoiMoiNhomRepository loiMoiRepository;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public TongQuan tongQuan(Role role) {
        return switch (role) {
            case ADMIN -> admin();
            case TRUONG_KHOA -> truongKhoa();
            case GIANG_VIEN -> giangVien();
            case SINH_VIEN -> sinhVien();
        };
    }

    private TongQuan admin() {
        List<ViecCanLam> ds = new ArrayList<>();
        TongQuanHeThong t = thongKeService.heThong();
        if (t.daKhoa() > 0) ds.add(new ViecCanLam(t.daKhoa() + " tài khoản đang bị khóa.", "/admin/tai-khoan", "warn"));
        ds.add(new ViecCanLam("Hệ thống có " + t.tongTaiKhoan() + " tài khoản.", "/admin/tai-khoan", "info"));
        List<Kpi> kpi = List.of(
                new Kpi("Tài khoản", String.valueOf(t.tongTaiKhoan()), t.daKhoa() + " đang bị khóa", "bi-people", t.daKhoa() > 0 ? "warn" : "info", "/admin/tai-khoan"),
                new Kpi("Sinh viên", String.valueOf(t.sinhVien()), "đã có tài khoản", "bi-mortarboard", "info", "/admin/tai-khoan"),
                new Kpi("Đợt chưa công bố", String.valueOf(t.dotChuaCongBo()), "trên tổng " + t.soDot() + " đợt", "bi-calendar2-week", "info", "/admin/tong-quan"),
                new Kpi("Thao tác 24 giờ qua", String.valueOf(t.thaoTac24h()), "ghi trong nhật ký", "bi-clock-history", "info", "/admin/nhat-ky"));
        List<Cot> cot = List.of(
                new Cot("Quản trị viên", t.admin(), t.tongTaiKhoan()),
                new Cot("Trưởng khoa", t.truongKhoa(), t.tongTaiKhoan()),
                new Cot("Giảng viên", t.giangVien(), t.tongTaiKhoan()),
                new Cot("Sinh viên", t.sinhVien(), t.tongTaiKhoan()));
        List<Bieu> bieu = List.of(new Bieu("Tài khoản theo vai trò", null, cot, "/admin/tai-khoan"));
        return new TongQuan(ds, kpi, bieu, List.of(), 0, 0, 0, thongBaoService.moiNhat());
    }

    private TongQuan truongKhoa() {
        List<ViecCanLam> ds = new ArrayList<>();
        List<DotDangKy> chuaCongBo = new ArrayList<>();
        long tongChoDuyet = 0, tongChuaGvpb = 0, tongChuaHd = 0, tongQuaHan = 0;
        for (DotDangKy d : dotService.tatCa()) {
            if (d.isDaCongBo()) continue;
            chuaCongBo.add(d);
            String ten = "Đợt \"" + d.getTenDot() + "\": ";
            long choDuyet = deTaiService.theoDot(d.getId()).stream()
                    .filter(x -> x.getTrangThai() == TrangThaiDeTai.CHO_DUYET).count();
            tongChoDuyet += choDuyet;
            if (choDuyet > 0) {
                ds.add(new ViecCanLam(ten + choDuyet + " đề tài đang chờ duyệt.", "/truong-khoa/dot/" + d.getId() + "/de-tai", "warn"));
            }
            List<Nhom> nhoms = phanCongService.nhomCoDeTai(d.getId());
            if (d.getLoai().coGvpb()) {
                long chuaGvpb = nhoms.stream().filter(n -> n.getGvpb() == null).count();
                tongChuaGvpb += chuaGvpb;
                if (chuaGvpb > 0) {
                    ds.add(new ViecCanLam(ten + chuaGvpb + " nhóm chưa có giảng viên phản biện.", "/truong-khoa/dot/" + d.getId() + "/phan-cong", "warn"));
                }
                int quaHan = diemService.nhomGvpbQuaHan(d).size();
                tongQuaHan += quaHan;
                if (quaHan > 0) {
                    ds.add(new ViecCanLam(ten + quaHan + " nhóm quá hạn nộp điểm phản biện.", "/truong-khoa/dot/" + d.getId() + "/qua-han", "danger"));
                }
            }
            if (d.getLoai().coHoiDong()) {
                long chuaHd = nhoms.stream().filter(n -> n.getHoiDong() == null).count();
                tongChuaHd += chuaHd;
                if (chuaHd > 0) {
                    ds.add(new ViecCanLam(ten + chuaHd + " nhóm chưa được gán vào hội đồng.", "/truong-khoa/dot/" + d.getId() + "/hoi-dong", "warn"));
                }
            }
            if (d.sauThoiGianSv()) {
                ds.add(new ViecCanLam(ten + "đã kết thúc thời gian đăng ký, kiểm tra bảng điểm để công bố.", "/truong-khoa/dot/" + d.getId() + "/diem", "info"));
            }
        }
        List<Kpi> kpi = List.of(
                new Kpi("Đề tài chờ duyệt", String.valueOf(tongChoDuyet), "ở các đợt chưa công bố", "bi-inbox", tongChoDuyet > 0 ? "warn" : "ok", "/truong-khoa/hop-duyet"),
                new Kpi("Nhóm chưa có GVPB", String.valueOf(tongChuaGvpb), "cần phân công phản biện", "bi-person-check", tongChuaGvpb > 0 ? "warn" : "ok", "/truong-khoa/dot"),
                new Kpi("Nhóm chưa vào hội đồng", String.valueOf(tongChuaHd), "đợt khóa luận", "bi-people", tongChuaHd > 0 ? "warn" : "ok", "/truong-khoa/dot"),
                new Kpi("Phản biện quá hạn", String.valueOf(tongQuaHan), "nhóm chưa có điểm GVPB", "bi-alarm", tongQuaHan > 0 ? "danger" : "ok", "/truong-khoa/dot"));
        List<Bieu> bieu = new ArrayList<>();
        for (DotDangKy d : chuaCongBo) {
            if (bieu.size() >= TOI_DA_DOT) break;
            bieu.add(thongKeService.tienDoMotDot(d));
        }
        return new TongQuan(ds, kpi, bieu, gioiHan(chuaCongBo), (int) tongChoDuyet, 0, 0, thongBaoService.moiNhat());
    }

    private TongQuan giangVien() {
        List<ViecCanLam> ds = new ArrayList<>();
        List<DeTai> deTais = deTaiService.cuaToi();
        long tuChoi = deTais.stream().filter(x -> x.getTrangThai() == TrangThaiDeTai.TU_CHOI).count();
        long choDuyet = deTais.stream().filter(x -> x.getTrangThai() == TrangThaiDeTai.CHO_DUYET).count();
        long daDuyet = deTais.stream().filter(x -> x.getTrangThai() == TrangThaiDeTai.DA_DUYET).count();
        long coNhom = deTais.stream().filter(x -> x.getNhom() != null).count();
        if (tuChoi > 0) ds.add(new ViecCanLam(tuChoi + " đề tài bị từ chối, cần chỉnh sửa lại.", "/giang-vien/de-tai", "danger"));
        if (choDuyet > 0) ds.add(new ViecCanLam(choDuyet + " đề tài đang chờ Trưởng khoa duyệt.", "/giang-vien/de-tai", "info"));
        for (DotDangKy d : dotService.tatCa()) {
            if (d.trongThoiGianGv()) {
                ds.add(new ViecCanLam("Đợt \"" + d.getTenDot() + "\" đang mở cho giảng viên đăng ký đề tài đến "
                        + d.getGvKetThuc().format(NGAY_GIO) + ".", "/giang-vien/de-tai", "info"));
            }
        }
        List<NhiemVuCham> nhiemVu = diemService.nhiemVuCuaToi();
        List<NhiemVuCham> coQuyen = nhiemVu.stream()
                .filter(NhiemVuCham::coQuyenCham).filter(nv -> !nv.nhom().getDot().isDaCongBo()).toList();
        long canCham = coQuyen.stream().filter(nv -> nv.diem() == null).count();
        if (canCham > 0) ds.add(new ViecCanLam(canCham + " nhóm đang chờ bạn nhập điểm.", "/giang-vien/viec-can-cham", "warn"));

        List<Kpi> kpi = List.of(
                new Kpi("Đề tài của tôi", String.valueOf(deTais.size()), daDuyet + " đã duyệt, " + coNhom + " đã có nhóm", "bi-journal-text", "info", "/giang-vien/de-tai"),
                new Kpi("Đề tài chờ duyệt", String.valueOf(choDuyet), "Trưởng khoa chưa xử lý", "bi-hourglass-split", "info", "/giang-vien/de-tai"),
                new Kpi("Đề tài bị từ chối", String.valueOf(tuChoi), "cần sửa và gửi lại", "bi-x-octagon", tuChoi > 0 ? "danger" : "ok", "/giang-vien/de-tai"),
                new Kpi("Nhóm chờ chấm", String.valueOf(canCham), "bạn chưa nhập điểm", "bi-clipboard-check", canCham > 0 ? "warn" : "ok", "/giang-vien/viec-can-cham"));
        List<Bieu> bieu = new ArrayList<>();
        if (!deTais.isEmpty()) {
            long tong = deTais.size();
            bieu.add(new Bieu("Đề tài của tôi", null, List.of(
                    new Cot("Đã được duyệt", daDuyet, tong),
                    new Cot("Đã có nhóm đăng ký", coNhom, tong),
                    new Cot("Đang chờ duyệt", choDuyet, tong),
                    new Cot("Bị từ chối", tuChoi, tong)), "/giang-vien/de-tai"));
        }
        if (!coQuyen.isEmpty()) {
            long daCham = coQuyen.size() - canCham;
            bieu.add(new Bieu("Tiến độ chấm điểm", "Các đợt chưa công bố", List.of(
                    new Cot("Đã nhập điểm", daCham, coQuyen.size())), "/giang-vien/viec-can-cham"));
        }

        Set<Long> dotCuaToi = new HashSet<>();
        for (DeTai d : deTais) dotCuaToi.add(d.getDot().getId());
        for (NhiemVuCham nv : nhiemVu) dotCuaToi.add(nv.nhom().getDot().getId());
        List<DotDangKy> lienQuan = new ArrayList<>();
        for (DotDangKy d : dotService.tatCa()) {
            if (!d.isDaCongBo() && (d.trongThoiGianGv() || dotCuaToi.contains(d.getId()))) lienQuan.add(d);
        }
        return new TongQuan(ds, kpi, bieu, gioiHan(lienQuan), 0, (int) canCham, 0, thongBaoService.moiNhat());
    }

    private TongQuan sinhVien() {
        List<ViecCanLam> ds = new ArrayList<>();
        Long svId = currentUser.sinhVien().getId();
        List<DotDangKy> lienQuan = new ArrayList<>();
        long dotMo = 0, soNhom = 0;
        Double diemGanNhat = null;
        for (DotDangKy d : dotService.tatCa()) {
            Nhom n = nhomRepository.findByThanhVien(svId, d.getId()).orElse(null);
            String ten = "Đợt \"" + d.getTenDot() + "\": ";
            String goc = "/sinh-vien/dot/" + d.getId();
            if (n != null) soNhom++;
            if (d.trongThoiGianSv()) {
                dotMo++;
                if (n == null) {
                    ds.add(new ViecCanLam(ten + "bạn chưa thuộc nhóm nào. Tạo nhóm hoặc nhờ nhóm trưởng thêm bạn vào nhóm (hạn " + d.getSvKetThuc().format(NGAY_GIO) + ").", goc + "/nhom", "warn"));
                } else if (n.getDeTai() == null) {
                    ds.add(new ViecCanLam(ten + "nhóm của bạn chưa đăng ký đề tài.", goc + "/de-tai", "warn"));
                }
            } else if (d.sauThoiGianSv() && !d.isDaCongBo() && n != null && n.getDeTai() != null && n.getFileBaoCao() == null) {
                ds.add(new ViecCanLam(ten + "nhóm chưa nộp báo cáo.", goc + "/nhom", "warn"));
            }
            if (d.isDaCongBo() && n != null) {
                ds.add(new ViecCanLam(ten + "kết quả đã được công bố.", goc + "/ket-qua", "ok"));
                if (diemGanNhat == null && n.getDiemCuoi() != null) diemGanNhat = n.getDiemCuoi();
            }
            if (!d.isDaCongBo() && (n != null || d.trongThoiGianSv())) lienQuan.add(d);
        }
        long loiMoi = loiMoiRepository.demDaNhan(svId, TrangThaiLoiMoi.CHO);
        if (loiMoi > 0) ds.add(new ViecCanLam("Bạn có " + loiMoi + " lời mời vào nhóm đang chờ trả lời.", "/sinh-vien/loi-moi", "warn"));
        List<Kpi> kpi = List.of(
                new Kpi("Đợt đang mở đăng ký", String.valueOf(dotMo), "sinh viên đăng ký nhóm và đề tài", "bi-calendar2-check", dotMo > 0 ? "info" : "ok", "/sinh-vien/dot"),
                new Kpi("Nhóm của tôi", String.valueOf(soNhom), "trên các đợt đã tham gia", "bi-people", "info", "/sinh-vien/nhom-cua-toi"),
                new Kpi("Lời mời chờ trả lời", String.valueOf(loiMoi), "từ các nhóm khác", "bi-envelope-paper", loiMoi > 0 ? "warn" : "ok", "/sinh-vien/loi-moi"),
                new Kpi("Điểm cuối gần nhất", diemGanNhat == null ? "Chưa có" : String.valueOf(diemGanNhat), "hiện sau khi công bố", "bi-award", diemGanNhat == null ? "info" : "ok", "/sinh-vien/dot"));
        return new TongQuan(ds, kpi, List.of(), gioiHan(lienQuan), 0, 0, (int) loiMoi, thongBaoService.moiNhat());
    }

    private static List<DotDangKy> gioiHan(List<DotDangKy> ds) {
        return ds.size() > TOI_DA_DOT ? new ArrayList<>(ds.subList(0, TOI_DA_DOT)) : ds;
    }
}
