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
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;
    private final BoMonRepository boMonRepository;
    private final AuditService auditService;

    @GetMapping("/tai-khoan")
    public String danhSach(Model model) {
        model.addAttribute("dsTaiKhoan", userService.tatCa());
        return "admin/tai-khoan-list";
    }

    @GetMapping("/tai-khoan/moi")
    public String formMoi(Model model) {
        TaiKhoanForm f = new TaiKhoanForm();
        f.setRole(Role.SINH_VIEN);
        napForm(model, f, "/admin/tai-khoan", true);
        return "admin/tai-khoan-form";
    }

    @PostMapping("/tai-khoan")
    public String tao(@Valid @ModelAttribute("form") TaiKhoanForm form, BindingResult br, Model model,
                      RedirectAttributes ra) {
        if (br.hasErrors()) {
            napForm(model, null, "/admin/tai-khoan", true);
            return "admin/tai-khoan-form";
        }
        userService.tao(form);
        ra.addFlashAttribute("success", "Đã tạo tài khoản " + form.getUsername() + ".");
        return "redirect:/admin/tai-khoan";
    }

    @GetMapping("/tai-khoan/{id}/sua")
    public String formSua(@PathVariable Long id, Model model) {
        napForm(model, userService.taoForm(userService.lay(id)), "/admin/tai-khoan/" + id, false);
        return "admin/tai-khoan-form";
    }

    @PostMapping("/tai-khoan/{id}")
    public String sua(@PathVariable Long id, @Valid @ModelAttribute("form") TaiKhoanForm form, BindingResult br,
                      Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            napForm(model, null, "/admin/tai-khoan/" + id, false);
            return "admin/tai-khoan-form";
        }
        userService.sua(id, form);
        ra.addFlashAttribute("success", "Đã cập nhật tài khoản.");
        return "redirect:/admin/tai-khoan";
    }

    @PostMapping("/tai-khoan/{id}/khoa")
    public String khoa(@PathVariable Long id, RedirectAttributes ra) {
        userService.datTrangThai(id, false);
        ra.addFlashAttribute("success", "Đã khóa tài khoản.");
        return "redirect:/admin/tai-khoan";
    }

    @PostMapping("/tai-khoan/{id}/mo-khoa")
    public String moKhoa(@PathVariable Long id, RedirectAttributes ra) {
        userService.datTrangThai(id, true);
        ra.addFlashAttribute("success", "Đã mở khóa tài khoản.");
        return "redirect:/admin/tai-khoan";
    }

    @GetMapping("/nhat-ky")
    public String nhatKy(@RequestParam(required = false) String q,
                         @RequestParam(defaultValue = "0") int trang,
                         @RequestParam(defaultValue = "25") int kichThuoc, Model model) {
        var p = auditService.trang(q, trang, kichThuoc);
        model.addAttribute("trang", p);
        model.addAttribute("q", q == null ? "" : q.trim());
        model.addAttribute("kichThuoc", p.getSize());
        return "admin/nhat-ky";
    }

    private void napForm(Model model, TaiKhoanForm form, String action, boolean taoMoi) {
        if (form != null) model.addAttribute("form", form);
        model.addAttribute("action", action);
        model.addAttribute("taoMoi", taoMoi);
        model.addAttribute("roles", Role.values());
        model.addAttribute("boMons", boMonRepository.findAll());
    }
}
