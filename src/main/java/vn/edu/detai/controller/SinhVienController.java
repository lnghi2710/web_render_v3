package vn.edu.detai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.detai.dto.*;
import vn.edu.detai.entity.*;
import vn.edu.detai.enums.*;
import vn.edu.detai.exception.BusinessException;
import vn.edu.detai.repository.*;
import vn.edu.detai.security.CurrentUser;
import vn.edu.detai.service.*;
import java.util.*;

@Controller
@RequestMapping("/sinh-vien")
@RequiredArgsConstructor
public class SinhVienController {
    private final DotService dotService;
    private final DeTaiService deTaiService;
    private final NhomService nhomService;
    private final DiemService diemService;
    private final CurrentUser currentUser;

    @GetMapping("/dot")
    public String dsDot(Model model) {
        model.addAttribute("dsDot", dotService.tatCa());
        return "sinhvien/dot-list";
    }

    // ----- Tạo nhóm, quản lý thành viên, nộp báo cáo (FR15, FR17) -----
    @GetMapping("/dot/{dotId}/nhom")
    public String nhom(@PathVariable Long dotId, Model model) {
        Long svId = currentUser.sinhVien().getId();
        Nhom n = nhomService.nhomCuaToi(dotId).orElse(null);
        model.addAttribute("dot", dotService.lay(dotId));
        model.addAttribute("nhom", n);
        model.addAttribute("laTruong", n != null && n.laNhomTruong(svId));
        model.addAttribute("svId", svId);
        model.addAttribute("toiDa", nhomService.toiDa());
        return "sinhvien/nhom";
    }

    @PostMapping("/dot/{dotId}/nhom")
    public String taoNhom(@PathVariable Long dotId, @RequestParam(required = false) String tenNhom,
                          RedirectAttributes ra) {
        nhomService.taoNhom(dotId, tenNhom);
        ra.addFlashAttribute("success", "Đã tạo nhóm; bạn là nhóm trưởng.");
        return "redirect:/sinh-vien/dot/" + dotId + "/nhom";
    }

    @PostMapping("/nhom/{id}/thanh-vien")
    public String themThanhVien(@PathVariable Long id, @RequestParam String maSv, RedirectAttributes ra) {
        Nhom n = nhomService.themThanhVien(id, maSv);
        ra.addFlashAttribute("success", "Đã thêm thành viên vào nhóm.");
        return "redirect:/sinh-vien/dot/" + n.getDot().getId() + "/nhom";
    }

    @PostMapping("/nhom/{id}/thanh-vien/{svId}/xoa")
    public String xoaThanhVien(@PathVariable Long id, @PathVariable Long svId, RedirectAttributes ra) {
        Nhom n = nhomService.xoaThanhVien(id, svId);
        ra.addFlashAttribute("success", "Đã xóa thành viên khỏi nhóm.");
        return "redirect:/sinh-vien/dot/" + n.getDot().getId() + "/nhom";
    }

    @PostMapping("/nhom/{id}/giai-tan")
    public String giaiTan(@PathVariable Long id, RedirectAttributes ra) {
        Long dotId = nhomService.giaiTan(id);
        ra.addFlashAttribute("success", "Đã giải tán nhóm.");
        return "redirect:/sinh-vien/dot/" + dotId + "/nhom";
    }

    @PostMapping("/nhom/{id}/bao-cao")
    public String nopBaoCao(@PathVariable Long id, @RequestParam("file") MultipartFile file, RedirectAttributes ra) {
        Nhom n = nhomService.nopBaoCao(id, file);
        ra.addFlashAttribute("success", "Đã nộp báo cáo.");
        return "redirect:/sinh-vien/dot/" + n.getDot().getId() + "/nhom";
    }

    // ----- Xem đề tài công bố, đăng ký / hủy đăng ký (FR16) -----
    @GetMapping("/dot/{dotId}/de-tai")
    public String deTai(@PathVariable Long dotId, Model model) {
        Long svId = currentUser.sinhVien().getId();
        Nhom n = nhomService.nhomCuaToi(dotId).orElse(null);
        model.addAttribute("dot", dotService.lay(dotId));
        model.addAttribute("dsDeTai", deTaiService.congBoTheoDot(dotId));
        model.addAttribute("nhom", n);
        model.addAttribute("laTruong", n != null && n.laNhomTruong(svId));
        return "sinhvien/de-tai";
    }

    @PostMapping("/nhom/{id}/de-tai")
    public String dangKy(@PathVariable Long id, @RequestParam Long deTaiId, RedirectAttributes ra) {
        Nhom n = nhomService.dangKyDeTai(id, deTaiId);
        ra.addFlashAttribute("success", "Đã đăng ký đề tài cho nhóm.");
        return "redirect:/sinh-vien/dot/" + n.getDot().getId() + "/de-tai";
    }

    @PostMapping("/nhom/{id}/huy-de-tai")
    public String huyDangKy(@PathVariable Long id, RedirectAttributes ra) {
        Nhom n = nhomService.huyDangKy(id);
        ra.addFlashAttribute("success", "Đã hủy đăng ký đề tài.");
        return "redirect:/sinh-vien/dot/" + n.getDot().getId() + "/de-tai";
    }

    // ----- Xem điểm và kết quả (FR18) -----
    @GetMapping("/dot/{dotId}/ket-qua")
    public String ketQua(@PathVariable Long dotId, Model model) {
        Nhom n = nhomService.nhomCuaToi(dotId).orElse(null);
        model.addAttribute("dot", dotService.lay(dotId));
        model.addAttribute("nhom", n);
        if (n != null) model.addAttribute("ketQua", diemService.ketQuaCuaNhom(n));
        return "sinhvien/ket-qua";
    }
}
