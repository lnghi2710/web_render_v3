package vn.edu.detai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.detai.entity.DotDangKy;
import vn.edu.detai.service.AuditService;
import vn.edu.detai.service.DeTaiService;
import vn.edu.detai.service.DotService;
import vn.edu.detai.service.ThongKeService;

import java.nio.charset.StandardCharsets;
import java.util.List;

/** Các màn hình tổng hợp của Trưởng khoa: hộp duyệt, tiến độ, khối lượng, sinh viên chưa có nhóm, xuất bảng điểm. */
@Controller
@RequestMapping("/truong-khoa")
@RequiredArgsConstructor
public class TruongKhoaMoRongController {
    private final DotService dotService;
    private final DeTaiService deTaiService;
    private final ThongKeService thongKeService;
    private final AuditService audit;

    @GetMapping("/hop-duyet")
    public String hopDuyet(Model model) {
        model.addAttribute("dsDeTai", deTaiService.choDuyetChuaCongBo());
        return "truongkhoa/hop-duyet";
    }

    @GetMapping("/tien-do")
    public String tienDo(@RequestParam(defaultValue = "false") boolean tatCa, Model model) {
        model.addAttribute("dsBieu", thongKeService.tienDo(tatCa));
        model.addAttribute("tatCa", tatCa);
        return "truongkhoa/tien-do";
    }

    @GetMapping("/khoi-luong")
    public String khoiLuong(Model model) {
        var ds = thongKeService.khoiLuongGiangVien();
        model.addAttribute("dsKhoiLuong", ds);
        model.addAttribute("nhomNhieuNhat", ds.stream().mapToLong(k -> k.tongNhom()).max().orElse(0));
        return "truongkhoa/khoi-luong";
    }

    @GetMapping("/sv-chua-nhom")
    public String svChuaNhom(@RequestParam(required = false) Long dotId, Model model) {
        List<DotDangKy> dots = dotService.tatCa();
        DotDangKy dot = dotId != null ? dotService.lay(dotId)
                : dots.stream().filter(d -> !d.isDaCongBo()).findFirst().or(() -> dots.stream().findFirst()).orElse(null);
        model.addAttribute("dsDot", dots);
        model.addAttribute("dot", dot);
        if (dot != null) {
            var ds = thongKeService.svChuaCoNhom(dot.getId());
            long tong = thongKeService.tongSinhVienHoatDong();
            model.addAttribute("dsSv", ds);
            model.addAttribute("tongSv", tong);
            model.addAttribute("daCoNhom", Math.max(0, tong - ds.size()));
        }
        return "truongkhoa/sv-chua-nhom";
    }

    @GetMapping("/xuat-diem")
    public String xuatDiem(Model model) {
        model.addAttribute("dsDot", dotService.tatCa());
        return "truongkhoa/xuat-diem";
    }

    @GetMapping("/dot/{id}/xuat-diem")
    public ResponseEntity<byte[]> taiBangDiem(@PathVariable Long id) {
        byte[] noiDung = thongKeService.xuatBangDiem(id);
        audit.ghi("Xuất bảng điểm", "DotDangKy#" + id);
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("bang-diem-dot-" + id + ".csv", StandardCharsets.UTF_8).build().toString())
                .body(noiDung);
    }
}
