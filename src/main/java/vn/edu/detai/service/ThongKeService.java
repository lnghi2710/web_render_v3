package vn.edu.detai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.detai.dto.*;
import vn.edu.detai.entity.*;
import vn.edu.detai.enums.LoaiDiem;
import vn.edu.detai.enums.Role;
import vn.edu.detai.enums.TrangThaiDeTai;
import vn.edu.detai.repository.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/** Số liệu thống kê, tiến độ và xuất dữ liệu cho Trưởng khoa và quản trị viên. */
@Service
@RequiredArgsConstructor
public class ThongKeService {
    private final DotService dotService;
    private final DeTaiRepository deTaiRepository;
    private final NhomRepository nhomRepository;
    private final DiemRepository diemRepository;
    private final GiangVienRepository giangVienRepository;
    private final SinhVienRepository sinhVienRepository;
    private final ThanhVienHoiDongRepository thanhVienHoiDongRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final DiemService diemService;

    // ---------- Tiến độ đợt ----------

    /** Biểu đồ tiến độ của các đợt chưa công bố (kèm đợt đã công bố khi kemDaCongBo = true). */
    @Transactional(readOnly = true)
    public List<Bieu> tienDo(boolean kemDaCongBo) {
        List<Bieu> kq = new ArrayList<>();
        for (DotDangKy d : dotService.tatCa()) {
            if (d.isDaCongBo() && !kemDaCongBo) continue;
            kq.add(tienDoMotDot(d));
        }
        return kq;
    }

    @Transactional(readOnly = true)
    public Bieu tienDoMotDot(DotDangKy d) {
        List<DeTai> deTais = deTaiRepository.findByDotIdOrderByIdDesc(d.getId());
        long tong = deTais.size();
        long daDuyet = deTais.stream().filter(x -> x.getTrangThai() == TrangThaiDeTai.DA_DUYET).count();
        List<Nhom> nhoms = nhomRepository.findByDotIdOrderByIdAsc(d.getId()).stream()
                .filter(n -> n.getDeTai() != null).toList();
        long daNop = nhoms.stream().filter(n -> n.getFileBaoCao() != null).count();
        long duDiem = nhoms.stream().filter(n -> diemService.tinhDiemCuoi(n) != null).count();
        List<Cot> cot = new ArrayList<>();
        cot.add(new Cot("Đề tài đã duyệt", daDuyet, tong));
        cot.add(new Cot("Đề tài đã có nhóm đăng ký", nhoms.size(), daDuyet));
        cot.add(new Cot("Nhóm đã nộp báo cáo", daNop, nhoms.size()));
        cot.add(new Cot("Nhóm đủ điểm thành phần", duDiem, nhoms.size()));
        return new Bieu(d.getTenDot(), d.getLoai().getNhan() + " · " + d.getTrangThaiHienTai().getNhan(), cot,
                "/truong-khoa/dot/" + d.getId() + "/diem");
    }

    // ---------- Khối lượng giảng viên ----------

    @Transactional(readOnly = true)
    public List<KhoiLuongGv> khoiLuongGiangVien() {
        List<KhoiLuongGv> kq = new ArrayList<>();
        for (GiangVien gv : giangVienRepository.findAllByOrderByMaGvAsc()) {
            long deTai = deTaiRepository.findByGvhdId(gv.getId()).size();
            List<Nhom> hd = nhomRepository.findByGvhdId(gv.getId()).stream()
                    .filter(n -> !n.getDot().isDaCongBo()).toList();
            List<Nhom> pb = nhomRepository.findByGvpbId(gv.getId()).stream()
                    .filter(n -> n.getDeTai() != null && !n.getDot().isDaCongBo()).toList();
            long hoiDong = thanhVienHoiDongRepository.findByGiangVienId(gv.getId()).stream()
                    .filter(t -> !t.getHoiDong().getDot().isDaCongBo()).count();
            long chua = hd.stream().filter(n -> diemRepository.findByNhomIdAndLoai(n.getId(), LoaiDiem.GVHD).isEmpty()).count()
                    + pb.stream().filter(n -> diemRepository.findByNhomIdAndLoai(n.getId(), LoaiDiem.GVPB).isEmpty()).count();
            kq.add(new KhoiLuongGv(gv, deTai, hd.size(), pb.size(), hoiDong, chua));
        }
        return kq;
    }

    // ---------- Sinh viên chưa có nhóm ----------

    /** Sinh viên (tài khoản còn hoạt động) chưa thuộc nhóm nào trong đợt. */
    @Transactional(readOnly = true)
    public List<SinhVien> svChuaCoNhom(Long dotId) {
        Set<Long> coNhom = new HashSet<>();
        for (Nhom n : nhomRepository.findByDotIdOrderByIdAsc(dotId)) {
            for (ThanhVienNhom tv : n.getThanhViens()) coNhom.add(tv.getSinhVien().getId());
        }
        return sinhVienRepository.findAllByOrderByMaSvAsc().stream()
                .filter(s -> s.getUser().isEnabled() && !coNhom.contains(s.getId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public long tongSinhVienHoatDong() {
        return sinhVienRepository.findAll().stream().filter(s -> s.getUser().isEnabled()).count();
    }

    // ---------- Tổng quan hệ thống (quản trị viên) ----------

    @Transactional(readOnly = true)
    public TongQuanHeThong heThong() {
        List<DotDangKy> dots = dotService.tatCa();
        long chuaCongBo = dots.stream().filter(d -> !d.isDaCongBo()).count();
        List<AuditLog> ganDay = auditLogRepository.findTop200ByOrderByThoiGianDesc().stream().limit(8).toList();
        return new TongQuanHeThong(userRepository.count(), userRepository.countByEnabled(false),
                userRepository.countByRole(Role.ADMIN), userRepository.countByRole(Role.TRUONG_KHOA),
                userRepository.countByRole(Role.GIANG_VIEN), userRepository.countByRole(Role.SINH_VIEN),
                dots.size(), chuaCongBo, deTaiRepository.count(), nhomRepository.count(),
                auditLogRepository.countByThoiGianAfter(LocalDateTime.now().minusHours(24)), ganDay);
    }

    // ---------- Xuất bảng điểm ----------

    /** Bảng điểm của đợt dạng CSV UTF-8 có BOM để Excel mở đúng tiếng Việt. */
    @Transactional(readOnly = true)
    public byte[] xuatBangDiem(Long dotId) {
        DotDangKy dot = dotService.lay(dotId);
        List<LoaiDiem> loais = dot.getLoai().cacLoaiDiem();
        DateTimeFormatter gio = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        StringBuilder sb = new StringBuilder("\uFEFF");
        List<String> tieuDe = new ArrayList<>(List.of(o("STT"), o("Nhóm"), o("Đề tài"), o("Sinh viên"),
                o("Giảng viên hướng dẫn"), o("Giảng viên phản biện"), o("Hội đồng")));
        for (LoaiDiem l : loais) tieuDe.add(o(l.getNhan()));
        tieuDe.add(o("Điểm cuối"));
        tieuDe.add(o("Trạng thái"));
        dong(sb, tieuDe);
        int stt = 0;
        for (Nhom n : nhomRepository.findByDotIdOrderByIdAsc(dotId)) {
            if (n.getDeTai() == null) continue;
            Map<LoaiDiem, Diem> diem = new EnumMap<>(LoaiDiem.class);
            for (Diem d : diemRepository.findByNhomId(n.getId())) diem.put(d.getLoai(), d);
            List<String> c = new ArrayList<>();
            c.add(String.valueOf(++stt));
            c.add(o(n.getTenNhom()));
            c.add(o(n.getDeTai().getTenDeTai()));
            c.add(o(n.getThanhViens().stream().map(tv -> tv.getSinhVien().getNhan()).collect(Collectors.joining("; "))));
            c.add(o(n.getDeTai().getTenGvhds()));
            c.add(o(n.getGvpb() == null ? "" : n.getGvpb().getHoTen()));
            c.add(o(n.getHoiDong() == null ? "" : n.getHoiDong().getTenHoiDong()
                    + (n.getHoiDong().getThoiGian() == null ? "" : " (" + n.getHoiDong().getThoiGian().format(gio) + ")")));
            for (LoaiDiem l : loais) {
                Diem d = diem.get(l);
                c.add(d == null ? "" : String.valueOf(d.getDiem()));
            }
            Double dc = dot.isDaCongBo() ? n.getDiemCuoi() : diemService.tinhDiemCuoi(n);
            c.add(dc == null ? "" : String.valueOf(dc));
            c.add(o(dot.isDaCongBo() ? "Đã công bố" : dc == null ? "Còn thiếu điểm" : "Tạm tính"));
            dong(sb, c);
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void dong(StringBuilder sb, List<String> cells) {
        sb.append(String.join(",", cells)).append("\r\n");
    }

    /** Đặt trong dấu ngoặc kép; chặn công thức độc hại khi mở bằng Excel (CSV injection). */
    private static String o(String s) {
        String t = s == null ? "" : s;
        if (!t.isEmpty() && "=+-@".indexOf(t.charAt(0)) >= 0) t = "'" + t;
        return "\"" + t.replace("\"", "\"\"") + "\"";
    }
}
