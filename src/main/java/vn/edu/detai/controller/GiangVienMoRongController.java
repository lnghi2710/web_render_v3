package vn.edu.detai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.detai.dto.NhatKyForm;
import vn.edu.detai.dto.NhiemVuCham;
import vn.edu.detai.entity.Nhom;
import vn.edu.detai.entity.NhatKyHuongDan;
import vn.edu.detai.service.DiemService;
import vn.edu.detai.service.LichService;
import vn.edu.detai.service.NhatKyHuongDanService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Việc cần chấm, lịch cá nhân và nhật ký hướng dẫn của giảng viên. */
@Controller
@RequestMapping("/giang-vien")
@RequiredArgsConstructor
public class GiangVienMoRongController {
    private final DiemService diemService;
    private final LichService lichService;
    private final NhatKyHuongDanService nhatKyService;

    @GetMapping("/viec-can-cham")
    public String vieCanCham(Model model) {
        List<NhiemVuCham> ds = diemService.nhiemVuCuaToi().stream()
                .filter(NhiemVuCham::coQuyenCham)
                .filter(nv -> nv.diem() == null && !nv.nhom().getDot().isDaCongBo() && nv.nhom().getDeTai() != null)
                .toList();
        model.addAttribute("dsNhiemVu", ds);
        return "giangvien/viec-can-cham";
    }

    @GetMapping("/lich")
    public String lich(Model model) {
        model.addAttribute("dsSuKien", lichService.lichGiangVien());
        return "giangvien/lich";
    }

    @GetMapping("/nhat-ky")
    public String dsNhatKy(Model model) {
        List<Nhom> nhoms = nhatKyService.nhomHuongDan();
        Map<Long, Long> soBuoi = new HashMap<>();
        Map<Long, NhatKyHuongDan> moiNhat = new HashMap<>();
        for (Nhom n : nhoms) {
            soBuoi.put(n.getId(), nhatKyService.dem(n));
            NhatKyHuongDan k = nhatKyService.moiNhat(n);
            if (k != null) moiNhat.put(n.getId(), k);
        }
        model.addAttribute("dsNhom", nhoms);
        model.addAttribute("soBuoi", soBuoi);
        model.addAttribute("moiNhat", moiNhat);
        return "giangvien/nhat-ky";
    }

    @GetMapping("/nhom/{id}/nhat-ky")
    public String nhatKyNhom(@PathVariable Long id, Model model) {
        napNhom(model, id);
        model.addAttribute("form", new NhatKyForm());
        return "giangvien/nhat-ky-nhom";
    }

    @PostMapping("/nhom/{id}/nhat-ky")
    public String themNhatKy(@PathVariable Long id, @Valid @ModelAttribute("form") NhatKyForm form, BindingResult br,
                             Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            napNhom(model, id);
            return "giangvien/nhat-ky-nhom";
        }
        nhatKyService.them(id, form);
        ra.addFlashAttribute("success", "Đã ghi buổi hướng dẫn.");
        return "redirect:/giang-vien/nhom/" + id + "/nhat-ky";
    }

    @PostMapping("/nhat-ky/{id}/xoa")
    public String xoaNhatKy(@PathVariable Long id, RedirectAttributes ra) {
        Long nhomId = nhatKyService.xoa(id);
        ra.addFlashAttribute("success", "Đã xóa mục nhật ký.");
        return "redirect:/giang-vien/nhom/" + nhomId + "/nhat-ky";
    }

    private void napNhom(Model model, Long nhomId) {
        Nhom n = nhatKyService.nhomCuaGvhd(nhomId);
        model.addAttribute("nhom", n);
        model.addAttribute("dsNhatKy", nhatKyService.theoNhom(nhomId));
    }
}
