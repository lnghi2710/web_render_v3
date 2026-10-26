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
public class ThongBaoController {
    private final ThongBaoService thongBaoService;

    @GetMapping("/thong-bao")
    public String danhSach(Model model) {
        model.addAttribute("dsThongBao", thongBaoService.tatCa());
        return "thongbao/list";
    }

    @GetMapping("/thong-bao/{id}")
    public String chiTiet(@PathVariable Long id, Model model) {
        model.addAttribute("thongBao", thongBaoService.lay(id));
        return "thongbao/detail";
    }

    @GetMapping("/dang-thong-bao")
    public String formMoi(Model model) {
        model.addAttribute("form", new ThongBaoForm());
        return "thongbao/form";
    }

    @PostMapping("/dang-thong-bao")
    public String dang(@Valid @ModelAttribute("form") ThongBaoForm form, BindingResult br, RedirectAttributes ra) {
        if (br.hasErrors()) return "thongbao/form";
        ThongBao t = thongBaoService.dang(form);
        ra.addFlashAttribute("success", "Đã đăng thông báo.");
        return "redirect:/thong-bao/" + t.getId();
    }
}
