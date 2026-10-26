package vn.edu.detai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.detai.entity.*;
import vn.edu.detai.enums.TrangThaiLoiMoi;
import vn.edu.detai.security.CurrentUser;
import vn.edu.detai.service.*;

import java.util.*;
import java.util.stream.Collectors;

/** Lối tắt vào nhóm, kho đề tài, tìm bạn ghép nhóm, lời mời nhóm và nhật ký hướng dẫn của sinh viên. */
@Controller
@RequestMapping("/sinh-vien")
@RequiredArgsConstructor
public class SinhVienMoRongController {
    private final DotService dotService;
    private final DeTaiService deTaiService;
    private final NhomService nhomService;
    private final LoiMoiService loiMoiService;
    private final NhatKyHuongDanService nhatKyService;
    private final ThongKeService thongKeService;
    private final CurrentUser currentUser;

    /** Đi thẳng tới nhóm ở đợt hiện tại: đợt chưa công bố đã có nhóm, nếu không thì đợt đang mở đăng ký. */
    @GetMapping("/nhom-cua-toi")
    public String nhomCuaToi() {
        List<DotDangKy> dots = dotService.tatCa();
        for (DotDangKy d : dots) {
            if (!d.isDaCongBo() && nhomService.nhomCuaToi(d.getId()).isPresent()) {
                return "redirect:/sinh-vien/dot/" + d.getId() + "/nhom";
            }
        }
        for (DotDangKy d : dots) {
            if (d.trongThoiGianSv()) return "redirect:/sinh-vien/dot/" + d.getId() + "/nhom";
        }
        return "redirect:/sinh-vien/dot";
    }

    @GetMapping("/kho-de-tai")
    public String khoDeTai(@RequestParam(required = false) Long dotId, Model model) {
        List<DotDangKy> dots = dotService.tatCa();
        DotDangKy dot = chonDot(dotId, dots);
        List<DeTai> ds = dot == null ? List.of() : deTaiService.congBoTheoDot(dot.getId());
        Nhom nhom = dot == null ? null : nhomService.nhomCuaToi(dot.getId()).orElse(null);
        Long svId = currentUser.sinhVien().getId();
        model.addAttribute("dsDot", dots);
        model.addAttribute("dot", dot);
        model.addAttribute("dsDeTai", ds);
        model.addAttribute("boMons", ds.stream().map(d -> d.getBoMon().getTenBoMon())
                .collect(Collectors.toCollection(TreeSet::new)));
        model.addAttribute("giangViens", ds.stream().flatMap(d -> d.getGvhds().stream()).map(GiangVien::getHoTen)
                .collect(Collectors.toCollection(TreeSet::new)));
        model.addAttribute("nhom", nhom);
        model.addAttribute("laTruong", nhom != null && nhom.laNhomTruong(svId));
        return "sinhvien/kho-de-tai";
    }

    @GetMapping("/tim-ban")
    public String timBan(@RequestParam(required = false) Long dotId, Model model) {
        List<DotDangKy> dots = dotService.tatCa();
        DotDangKy dot = chonDot(dotId, dots);
        model.addAttribute("dsDot", dots);
        model.addAttribute("dot", dot);
        if (dot != null) {
            Long svId = currentUser.sinhVien().getId();
            Nhom nhom = nhomService.nhomCuaToi(dot.getId()).orElse(null);
            boolean laTruong = nhom != null && nhom.laNhomTruong(svId);
            Set<Long> daMoi = new HashSet<>();
            if (laTruong) {
                for (LoiMoiNhom l : loiMoiService.daGui()) {
                    if (l.getNhom().getId().equals(nhom.getId()) && l.getTrangThai() == TrangThaiLoiMoi.CHO) {
                        daMoi.add(l.getSinhVien().getId());
                    }
                }
            }
            model.addAttribute("nhom", nhom);
            model.addAttribute("laTruong", laTruong);
            model.addAttribute("conCho", nhom != null && nhom.getThanhViens().size() < nhomService.toiDa());
            model.addAttribute("daMoi", daMoi);
            model.addAttribute("dsSv", thongKeService.svChuaCoNhom(dot.getId()).stream()
                    .filter(s -> !s.getId().equals(svId)).toList());
        }
        return "sinhvien/tim-ban";
    }

    @GetMapping("/loi-moi")
    public String loiMoi(Model model) {
        model.addAttribute("dsNhan", loiMoiService.daNhan());
        model.addAttribute("dsGui", loiMoiService.daGui());
        model.addAttribute("svId", currentUser.sinhVien().getId());
        return "sinhvien/loi-moi";
    }

    @PostMapping("/nhom/{id}/moi")
    public String moi(@PathVariable Long id, @RequestParam Long svId, @RequestParam(required = false) Long dotId,
                      RedirectAttributes ra) {
        LoiMoiNhom l = loiMoiService.moi(id, svId);
        ra.addFlashAttribute("success", "Đã gửi lời mời tới " + l.getSinhVien().getHoTen() + ".");
        return "redirect:/sinh-vien/tim-ban" + (dotId == null ? "" : "?dotId=" + dotId);
    }

    @PostMapping("/loi-moi/{id}/dong-y")
    public String dongY(@PathVariable Long id, RedirectAttributes ra) {
        LoiMoiNhom l = loiMoiService.traLoi(id, true);
        ra.addFlashAttribute("success", "Bạn đã vào nhóm " + l.getNhom().getTenNhom() + ".");
        return "redirect:/sinh-vien/dot/" + l.getNhom().getDot().getId() + "/nhom";
    }

    @PostMapping("/loi-moi/{id}/tu-choi")
    public String tuChoi(@PathVariable Long id, RedirectAttributes ra) {
        loiMoiService.traLoi(id, false);
        ra.addFlashAttribute("success", "Đã từ chối lời mời.");
        return "redirect:/sinh-vien/loi-moi";
    }

    @PostMapping("/loi-moi/{id}/huy")
    public String huy(@PathVariable Long id, RedirectAttributes ra) {
        loiMoiService.huy(id);
        ra.addFlashAttribute("success", "Đã thu hồi lời mời.");
        return "redirect:/sinh-vien/loi-moi";
    }

    @GetMapping("/nhat-ky")
    public String nhatKy(Model model) {
        List<Nhom> nhoms = nhatKyService.nhomCuaSinhVien();
        Map<Long, List<NhatKyHuongDan>> nhatKy = new LinkedHashMap<>();
        for (Nhom n : nhoms) nhatKy.put(n.getId(), nhatKyService.theoNhom(n.getId()));
        model.addAttribute("dsNhom", nhoms);
        model.addAttribute("nhatKy", nhatKy);
        return "sinhvien/nhat-ky";
    }

    /** Đợt được chọn: theo tham số, nếu không thì đợt đang mở cho sinh viên, rồi đợt chưa công bố, rồi đợt mới nhất. */
    private DotDangKy chonDot(Long dotId, List<DotDangKy> dots) {
        if (dotId != null) return dotService.lay(dotId);
        return dots.stream().filter(DotDangKy::trongThoiGianSv).findFirst()
                .or(() -> dots.stream().filter(d -> !d.isDaCongBo()).findFirst())
                .or(() -> dots.stream().findFirst())
                .orElse(null);
    }
}
