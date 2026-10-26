package vn.edu.detai.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.detai.dto.HoSoForm;
import vn.edu.detai.entity.User;
import vn.edu.detai.enums.Role;
import vn.edu.detai.security.CurrentUser;
import vn.edu.detai.security.CustomUserDetails;
import vn.edu.detai.service.LichService;
import vn.edu.detai.service.UserService;

/** Các trang dùng chung cho mọi vai trò: hồ sơ cá nhân và lịch báo cáo hội đồng. */
@Controller
@RequiredArgsConstructor
public class ChungController {
    private final UserService userService;
    private final LichService lichService;
    private final CurrentUser currentUser;

    @GetMapping("/ho-so")
    public String hoSo(Model model) {
        User u = currentUser.user();
        HoSoForm f = new HoSoForm();
        f.setFullName(u.getFullName());
        f.setEmail(u.getEmail());
        model.addAttribute("form", f);
        napThongTin(model, u);
        return "ho-so";
    }

    @PostMapping("/ho-so")
    public String luuHoSo(@Valid @ModelAttribute("form") HoSoForm form, BindingResult br, Model model,
                          RedirectAttributes ra, HttpServletRequest req, HttpServletResponse res) {
        if (br.hasErrors()) {
            napThongTin(model, currentUser.user());
            return "ho-so";
        }
        User u = userService.capNhatHoSoCaNhan(form);
        // Làm mới người dùng trong phiên để tên hiển thị ở thanh bên đổi ngay.
        CustomUserDetails moi = new CustomUserDetails(u);
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken(moi, null, moi.getAuthorities()));
        SecurityContextHolder.setContext(ctx);
        new HttpSessionSecurityContextRepository().saveContext(ctx, req, res);
        ra.addFlashAttribute("success", "Đã cập nhật hồ sơ cá nhân.");
        return "redirect:/ho-so";
    }

    @GetMapping("/lich-bao-ve")
    public String lichBaoVe(Authentication auth, Model model) {
        Role role = Role.valueOf(auth.getAuthorities().iterator().next().getAuthority().substring("ROLE_".length()));
        model.addAttribute("dsLich", lichService.lichBaoVe(role));
        model.addAttribute("vaiTro", role.name());
        return "lich-bao-ve";
    }

    private void napThongTin(Model model, User u) {
        model.addAttribute("user", u);
        model.addAttribute("chiTiet", userService.thongTinChucDanh(u));
    }
}
