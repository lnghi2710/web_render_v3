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
@RequiredArgsConstructor
public class AuthController {
    private final ThongBaoService thongBaoService;
    private final DiemService diemService;
    private final UserService userService;
    private final DashboardService dashboardService;
    private final DotService dotService;

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/")
    public String trangChu(Model model, Authentication auth) {
        // GiaoDienAdvice đã nạp "chuong" (có đệm ngắn); tính trực tiếp nếu chưa có.
        if (model.getAttribute("chuong") == null) {
            Role role = Role.valueOf(auth.getAuthorities().iterator().next().getAuthority().substring("ROLE_".length()));
            model.addAttribute("chuong", dashboardService.tongQuan(role));
        }
        return "home";
    }

    @GetMapping("/doi-mat-khau")
    public String formDoiMatKhau(Model model) {
        model.addAttribute("form", new DoiMatKhauForm());
        return "doi-mat-khau";
    }

    @PostMapping("/doi-mat-khau")
    public String doiMatKhau(@Valid @ModelAttribute("form") DoiMatKhauForm form, BindingResult br,
                             RedirectAttributes ra) {
        if (br.hasErrors()) return "doi-mat-khau";
        userService.doiMatKhau(form);
        ra.addFlashAttribute("success", "Đã đổi mật khẩu thành công.");
        return "redirect:/";
    }
}
