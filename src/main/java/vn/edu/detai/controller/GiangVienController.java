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
@RequestMapping("/giang-vien")
@RequiredArgsConstructor
public class GiangVienController {
    private final DeTaiService deTaiService;
    private final DotService dotService;
    private final DiemService diemService;
    private final BoMonRepository boMonRepository;
    private final GiangVienRepository giangVienRepository;
    private final FileStorageService fileStorage;
    private final CurrentUser currentUser;

    // ----- Quản lý đề tài (FR12) -----
    @GetMapping("/de-tai")
    public String dsDeTai(Model model) {
        model.addAttribute("dsDeTai", deTaiService.cuaToi());
        model.addAttribute("dsDotMo", dotService.tatCa().stream().filter(DotDangKy::trongThoiGianGv).toList());
        return "giangvien/de-tai-list";
    }

    @GetMapping("/de-tai/moi")
    public String deTaiMoi(@RequestParam Long dotId, Model model) {
        DeTaiForm f = new DeTaiForm();
        f.setDotId(dotId);
        napForm(model, f, dotId, "/giang-vien/de-tai", "Đăng ký đề tài mới");
        return "giangvien/de-tai-form";
    }

    @PostMapping("/de-tai")
    public String tao(@Valid @ModelAttribute("form") DeTaiForm form, BindingResult br, Model model,
                      RedirectAttributes ra) {
        if (br.hasErrors()) {
            napForm(model, null, form.getDotId(), "/giang-vien/de-tai", "Đăng ký đề tài mới");
            return "giangvien/de-tai-form";
        }
        deTaiService.tao(form);
        ra.addFlashAttribute("success", "Đã đăng ký đề tài, đang chờ Trưởng khoa duyệt.");
        return "redirect:/giang-vien/de-tai";
    }

    @GetMapping("/de-tai/{id}/sua")
    public String formSua(@PathVariable Long id, Model model) {
        DeTai d = deTaiService.lay(id);
        DeTaiForm f = deTaiService.taoForm(d);
        napForm(model, f, f.getDotId(), "/giang-vien/de-tai/" + id, "Sửa đề tài");
        return "giangvien/de-tai-form";
    }

    @PostMapping("/de-tai/{id}")
    public String sua(@PathVariable Long id, @Valid @ModelAttribute("form") DeTaiForm form, BindingResult br,
                      Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            napForm(model, null, form.getDotId(), "/giang-vien/de-tai/" + id, "Sửa đề tài");
            return "giangvien/de-tai-form";
        }
        deTaiService.sua(id, form);
        ra.addFlashAttribute("success", "Đã cập nhật đề tài; đề tài cần được duyệt lại.");
        return "redirect:/giang-vien/de-tai";
    }

    @PostMapping("/de-tai/{id}/xoa")
    public String xoa(@PathVariable Long id, RedirectAttributes ra) {
        deTaiService.xoa(id);
        ra.addFlashAttribute("success", "Đã xóa đề tài.");
        return "redirect:/giang-vien/de-tai";
    }

    // ----- Xem nhóm, tải báo cáo (FR13) -----
    @GetMapping("/nhom")
    public String dsNhom(Model model) {
        model.addAttribute("dsNhiemVu", diemService.nhiemVuCuaToi());
        return "giangvien/nhom-list";
    }

    @GetMapping("/nhom/{id}/bao-cao")
    public ResponseEntity<Resource> taiBaoCao(@PathVariable Long id) {
        Nhom n = diemService.layNhom(id);
        if (!diemService.coLienQuan(n, currentUser.giangVien())) {
            throw new BusinessException("Bạn không có quyền xem báo cáo của nhóm này.");
        }
        if (n.getFileBaoCao() == null) throw new BusinessException("Nhóm chưa nộp báo cáo.");
        Resource r = fileStorage.taiBaoCao(n.getFileBaoCao());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("bao-cao-nhom-" + n.getId() + ".pdf").build().toString())
                .body(r);
    }

    // ----- Nhập điểm và nhận xét (FR14) -----
    @GetMapping("/nhom/{id}/diem")
    public String formDiem(@PathVariable Long id, @RequestParam LoaiDiem loai, Model model) {
        Nhom n = diemService.layNhom(id);
        if (!diemService.coQuyenCham(n, currentUser.giangVien(), loai)) {
            throw new BusinessException("Bạn không có quyền nhập " + loai.getNhan().toLowerCase() + " cho nhóm này.");
        }
        DiemForm f = new DiemForm();
        f.setLoai(loai);
        Diem cu = diemService.diem(n, loai);
        if (cu != null) {
            f.setDiem(cu.getDiem());
            f.setNhanXet(cu.getNhanXet());
        }
        model.addAttribute("form", f);
        model.addAttribute("nhom", n);
        model.addAttribute("loai", loai);
        return "giangvien/diem-form";
    }

    @PostMapping("/nhom/{id}/diem")
    public String luuDiem(@PathVariable Long id, @Valid @ModelAttribute("form") DiemForm form, BindingResult br,
                          Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            model.addAttribute("nhom", diemService.layNhom(id));
            model.addAttribute("loai", form.getLoai());
            return "giangvien/diem-form";
        }
        diemService.nhapDiem(id, form);
        ra.addFlashAttribute("success", "Đã lưu " + form.getLoai().getNhan().toLowerCase() + ".");
        return "redirect:/giang-vien/nhom";
    }

    private void napForm(Model model, DeTaiForm form, Long dotId, String action, String tieuDe) {
        if (form != null) model.addAttribute("form", form);
        Long toi = currentUser.giangVien().getId();
        model.addAttribute("dot", dotService.lay(dotId));
        model.addAttribute("boMons", boMonRepository.findAll());
        model.addAttribute("dsGv", giangVienRepository.findAllByOrderByMaGvAsc().stream()
                .filter(g -> !g.getId().equals(toi)).toList());
        model.addAttribute("action", action);
        model.addAttribute("tieuDe", tieuDe);
    }
}
