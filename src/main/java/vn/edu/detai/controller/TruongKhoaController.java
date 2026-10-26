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
@RequestMapping("/truong-khoa")
@RequiredArgsConstructor
public class TruongKhoaController {
    private final DotService dotService;
    private final DeTaiService deTaiService;
    private final PhanCongService phanCongService;
    private final DiemService diemService;

    // ----- Đợt đăng ký (FR06) -----
    @GetMapping("/dot")
    public String dsDot(Model model) {
        model.addAttribute("dsDot", dotService.tatCa());
        return "truongkhoa/dot-list";
    }

    @GetMapping("/dot/moi")
    public String dotMoi(Model model) {
        napFormDot(model, new DotForm(), "/truong-khoa/dot", "Tạo đợt đăng ký");
        return "truongkhoa/dot-form";
    }

    @PostMapping("/dot")
    public String taoDot(@Valid @ModelAttribute("form") DotForm form, BindingResult br, Model model,
                         RedirectAttributes ra) {
        if (br.hasErrors()) {
            napFormDot(model, null, "/truong-khoa/dot", "Tạo đợt đăng ký");
            return "truongkhoa/dot-form";
        }
        dotService.tao(form);
        ra.addFlashAttribute("success", "Đã tạo đợt đăng ký.");
        return "redirect:/truong-khoa/dot";
    }

    @GetMapping("/dot/{id}/sua")
    public String dotSua(@PathVariable Long id, Model model) {
        napFormDot(model, dotService.taoForm(dotService.lay(id)), "/truong-khoa/dot/" + id, "Sửa đợt đăng ký");
        return "truongkhoa/dot-form";
    }

    @PostMapping("/dot/{id}")
    public String suaDot(@PathVariable Long id, @Valid @ModelAttribute("form") DotForm form, BindingResult br,
                         Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            napFormDot(model, null, "/truong-khoa/dot/" + id, "Sửa đợt đăng ký");
            return "truongkhoa/dot-form";
        }
        dotService.sua(id, form);
        ra.addFlashAttribute("success", "Đã cập nhật đợt đăng ký.");
        return "redirect:/truong-khoa/dot";
    }

    @PostMapping("/dot/{id}/xoa")
    public String xoaDot(@PathVariable Long id, RedirectAttributes ra) {
        dotService.xoa(id);
        ra.addFlashAttribute("success", "Đã xóa đợt đăng ký.");
        return "redirect:/truong-khoa/dot";
    }

    // ----- Duyệt đề tài (FR07) -----
    @GetMapping("/dot/{id}/de-tai")
    public String dsDeTai(@PathVariable Long id, Model model) {
        model.addAttribute("dot", dotService.lay(id));
        model.addAttribute("dsDeTai", deTaiService.theoDot(id));
        return "truongkhoa/de-tai-duyet";
    }

    @PostMapping("/de-tai/{id}/duyet")
    public String duyet(@PathVariable Long id, @RequestParam(required = false) String ve, RedirectAttributes ra) {
        DeTai d = deTaiService.duyet(id);
        ra.addFlashAttribute("success", "Đã duyệt đề tài.");
        if ("hop-duyet".equals(ve)) return "redirect:/truong-khoa/hop-duyet";
        return "redirect:/truong-khoa/dot/" + d.getDot().getId() + "/de-tai";
    }

    @PostMapping("/de-tai/{id}/tu-choi")
    public String tuChoi(@PathVariable Long id, @RequestParam String lyDo,
                         @RequestParam(required = false) String ve, RedirectAttributes ra) {
        DeTai d = deTaiService.tuChoi(id, lyDo);
        ra.addFlashAttribute("success", "Đã từ chối đề tài.");
        if ("hop-duyet".equals(ve)) return "redirect:/truong-khoa/hop-duyet";
        return "redirect:/truong-khoa/dot/" + d.getDot().getId() + "/de-tai";
    }

    // ----- Phân công GVPB (FR08) -----
    @GetMapping("/dot/{id}/phan-cong")
    public String phanCong(@PathVariable Long id, Model model) {
        model.addAttribute("dot", dotService.lay(id));
        model.addAttribute("dsNhom", phanCongService.nhomCoDeTai(id));
        model.addAttribute("dsGv", phanCongService.tatCaGiangVien());
        return "truongkhoa/phan-cong";
    }

    @PostMapping("/nhom/{id}/gvpb")
    public String phanCongGvpb(@PathVariable Long id, @RequestParam(required = false) Long gvId,
                               RedirectAttributes ra) {
        Nhom n = phanCongService.phanCongGvpb(id, gvId);
        ra.addFlashAttribute("success", "Đã phân công GVPB cho nhóm " + n.getTenNhom() + ".");
        return "redirect:/truong-khoa/dot/" + n.getDot().getId() + "/phan-cong";
    }

    // ----- Hội đồng (FR09) -----
    @GetMapping("/dot/{id}/hoi-dong")
    public String dsHoiDong(@PathVariable Long id, Model model) {
        model.addAttribute("dot", dotService.lay(id));
        model.addAttribute("dsHoiDong", phanCongService.dsHoiDong(id));
        model.addAttribute("dsNhom", phanCongService.nhomCoDeTai(id));
        return "truongkhoa/hoi-dong";
    }

    @GetMapping("/dot/{id}/hoi-dong/moi")
    public String hoiDongMoi(@PathVariable Long id, Model model) {
        napFormHoiDong(model, new HoiDongForm(), id);
        return "truongkhoa/hoi-dong-form";
    }

    @PostMapping("/dot/{id}/hoi-dong")
    public String taoHoiDong(@PathVariable Long id, @Valid @ModelAttribute("form") HoiDongForm form,
                             BindingResult br, Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            napFormHoiDong(model, null, id);
            return "truongkhoa/hoi-dong-form";
        }
        phanCongService.taoHoiDong(id, form);
        ra.addFlashAttribute("success", "Đã lập hội đồng.");
        return "redirect:/truong-khoa/dot/" + id + "/hoi-dong";
    }

    @PostMapping("/hoi-dong/{id}/nhom")
    public String ganNhom(@PathVariable Long id, @RequestParam(required = false) List<Long> nhomIds,
                          RedirectAttributes ra) {
        HoiDong hd = phanCongService.ganNhom(id, nhomIds);
        ra.addFlashAttribute("success", "Đã gán nhóm vào hội đồng.");
        return "redirect:/truong-khoa/dot/" + hd.getDot().getId() + "/hoi-dong";
    }

    @PostMapping("/nhom/{id}/go-hoi-dong")
    public String goNhom(@PathVariable Long id, RedirectAttributes ra) {
        Long dotId = phanCongService.goNhomKhoiHoiDong(id);
        ra.addFlashAttribute("success", "Đã gỡ nhóm khỏi hội đồng.");
        return "redirect:/truong-khoa/dot/" + dotId + "/hoi-dong";
    }

    @PostMapping("/hoi-dong/{id}/xoa")
    public String xoaHoiDong(@PathVariable Long id, RedirectAttributes ra) {
        Long dotId = phanCongService.xoaHoiDong(id);
        ra.addFlashAttribute("success", "Đã xóa hội đồng.");
        return "redirect:/truong-khoa/dot/" + dotId + "/hoi-dong";
    }

    // ----- Bảng điểm, công bố, quá hạn (FR10, FR11) -----
    @GetMapping("/dot/{id}/diem")
    public String bangDiem(@PathVariable Long id, Model model) {
        DotDangKy dot = dotService.lay(id);
        model.addAttribute("dot", dot);
        model.addAttribute("dsNhom", phanCongService.nhomCoDeTai(id));
        model.addAttribute("bangDiem", diemService.bangDiem(id));
        model.addAttribute("cacLoai", dot.getLoai().cacLoaiDiem());
        return "truongkhoa/bang-diem";
    }

    @PostMapping("/dot/{id}/cong-bo")
    public String congBo(@PathVariable Long id, RedirectAttributes ra) {
        diemService.congBo(id);
        ra.addFlashAttribute("success", "Đã công bố kết quả và tính điểm cuối cho các nhóm.");
        return "redirect:/truong-khoa/dot/" + id + "/diem";
    }

    @GetMapping("/dot/{id}/qua-han")
    public String quaHan(@PathVariable Long id, Model model) {
        DotDangKy dot = dotService.lay(id);
        model.addAttribute("dot", dot);
        model.addAttribute("dsNhom", diemService.nhomGvpbQuaHan(dot));
        return "truongkhoa/qua-han";
    }

    private void napFormDot(Model model, DotForm form, String action, String tieuDe) {
        if (form != null) model.addAttribute("form", form);
        model.addAttribute("action", action);
        model.addAttribute("tieuDe", tieuDe);
        model.addAttribute("loaiDots", LoaiDot.values());
    }

    private void napFormHoiDong(Model model, HoiDongForm form, Long dotId) {
        if (form != null) model.addAttribute("form", form);
        model.addAttribute("dot", dotService.lay(dotId));
        model.addAttribute("dsGv", phanCongService.tatCaGiangVien());
    }
}
