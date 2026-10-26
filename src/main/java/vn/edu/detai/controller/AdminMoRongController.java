package vn.edu.detai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.detai.dto.CauHinhForm;
import vn.edu.detai.dto.KetQuaNhap;
import vn.edu.detai.service.*;

import java.nio.charset.StandardCharsets;

/** Tổng quan hệ thống, nhập tài khoản hàng loạt, danh mục bộ môn và cấu hình (chỉ quản trị viên). */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminMoRongController {
    private final ThongKeService thongKeService;
    private final NhapTaiKhoanService nhapTaiKhoanService;
    private final BoMonService boMonService;
    private final CauHinhService cauHinhService;

    @GetMapping("/tong-quan")
    public String tongQuan(Model model) {
        model.addAttribute("tq", thongKeService.heThong());
        return "admin/tong-quan";
    }

    // ----- Nhập tài khoản hàng loạt -----
    @GetMapping("/nhap-tai-khoan")
    public String formNhap(Model model) {
        model.addAttribute("toiDa", NhapTaiKhoanService.TOI_DA_DONG);
        return "admin/nhap-tai-khoan";
    }

    @PostMapping("/nhap-tai-khoan")
    public String nhap(@RequestParam("file") MultipartFile file, RedirectAttributes ra) {
        KetQuaNhap kq = nhapTaiKhoanService.nhap(file);
        ra.addFlashAttribute("ketQua", kq);
        if (kq.thanhCong() > 0) {
            ra.addFlashAttribute("success", "Đã tạo " + kq.thanhCong() + "/" + kq.tong() + " tài khoản.");
        }
        return "redirect:/admin/nhap-tai-khoan";
    }

    @GetMapping("/nhap-tai-khoan/mau")
    public ResponseEntity<byte[]> mauCsv() {
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("mau-nhap-tai-khoan.csv", StandardCharsets.UTF_8).build().toString())
                .body(NhapTaiKhoanService.MAU_CSV.getBytes(StandardCharsets.UTF_8));
    }

    // ----- Bộ môn -----
    @GetMapping("/bo-mon")
    public String boMon(Model model) {
        model.addAttribute("dsBoMon", boMonService.tatCa());
        return "admin/bo-mon";
    }

    @PostMapping("/bo-mon")
    public String themBoMon(@RequestParam String ten, RedirectAttributes ra) {
        boMonService.tao(ten);
        ra.addFlashAttribute("success", "Đã thêm bộ môn.");
        return "redirect:/admin/bo-mon";
    }

    @PostMapping("/bo-mon/{id}")
    public String doiTenBoMon(@PathVariable Long id, @RequestParam String ten, RedirectAttributes ra) {
        boMonService.doiTen(id, ten);
        ra.addFlashAttribute("success", "Đã đổi tên bộ môn.");
        return "redirect:/admin/bo-mon";
    }

    @PostMapping("/bo-mon/{id}/xoa")
    public String xoaBoMon(@PathVariable Long id, RedirectAttributes ra) {
        boMonService.xoa(id);
        ra.addFlashAttribute("success", "Đã xóa bộ môn.");
        return "redirect:/admin/bo-mon";
    }

    // ----- Cấu hình -----
    @GetMapping("/cau-hinh")
    public String cauHinh(Model model) {
        model.addAttribute("form", cauHinhService.hienTai());
        return "admin/cau-hinh";
    }

    @PostMapping("/cau-hinh")
    public String luuCauHinh(@Valid @ModelAttribute("form") CauHinhForm form, BindingResult br, RedirectAttributes ra) {
        if (br.hasErrors()) return "admin/cau-hinh";
        cauHinhService.luu(form);
        ra.addFlashAttribute("success", "Đã lưu cấu hình. Thay đổi áp dụng cho các thao tác từ bây giờ.");
        return "redirect:/admin/cau-hinh";
    }
}
