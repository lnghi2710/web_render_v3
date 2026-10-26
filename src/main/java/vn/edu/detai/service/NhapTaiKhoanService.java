package vn.edu.detai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.detai.dto.KetQuaNhap;
import vn.edu.detai.dto.TaiKhoanForm;
import vn.edu.detai.entity.BoMon;
import vn.edu.detai.enums.Role;
import vn.edu.detai.exception.BusinessException;
import vn.edu.detai.repository.BoMonRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Nhập tài khoản hàng loạt từ tệp CSV (UTF-8; dấu phân cách là dấu phẩy hoặc chấm phẩy, Excel xuất được cả hai).
 * Cột: username, mat_khau, ho_ten, email, vai_tro, ma_so, lop_hoac_bo_mon.
 * Bean này cố ý KHÔNG đánh dấu @Transactional: mỗi dòng được tạo trong giao dịch riêng của UserService,
 * nên một dòng lỗi không làm hỏng các dòng còn lại.
 */
@Service
@RequiredArgsConstructor
public class NhapTaiKhoanService {
    public static final int TOI_DA_DONG = 1000;
    public static final String MAU_CSV = "\uFEFFusername,mat_khau,ho_ten,email,vai_tro,ma_so,lop_hoac_bo_mon\r\n"
            + "sv100,123456,Nguyễn Văn A,sv100@sv.edu.vn,SINH_VIEN,22110100,DH22CNTT\r\n"
            + "gv100,123456,Trần Thị B,gv100@khoa.edu.vn,GIANG_VIEN,GV100,Công nghệ phần mềm\r\n";

    private final UserService userService;
    private final BoMonRepository boMonRepository;

    public KetQuaNhap nhap(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BusinessException("Vui lòng chọn tệp CSV.");
        String ten = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        if (!ten.endsWith(".csv")) {
            throw new BusinessException("Chỉ nhận tệp .csv. Với Excel, hãy chọn Lưu thành CSV UTF-8.");
        }
        String noiDung;
        try {
            noiDung = new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BusinessException("Không đọc được tệp tải lên.");
        }
        if (noiDung.startsWith("\uFEFF")) noiDung = noiDung.substring(1);

        List<List<String>> dong = docCsv(noiDung);
        if (dong.size() < 2) throw new BusinessException("Tệp không có dòng dữ liệu nào.");
        if (dong.size() - 1 > TOI_DA_DONG) throw new BusinessException("Mỗi lần nhập tối đa " + TOI_DA_DONG + " dòng.");

        List<String> tieuDe = dong.get(0).stream().map(s -> s.trim().toLowerCase(Locale.ROOT)).toList();
        int cUser = tieuDe.indexOf("username"), cMk = tieuDe.indexOf("mat_khau"), cTen = tieuDe.indexOf("ho_ten"),
                cMail = tieuDe.indexOf("email"), cRole = tieuDe.indexOf("vai_tro"), cMa = tieuDe.indexOf("ma_so"),
                cPhu = tieuDe.indexOf("lop_hoac_bo_mon");
        if (cUser < 0 || cMk < 0 || cTen < 0 || cMail < 0 || cRole < 0) {
            throw new BusinessException("Dòng tiêu đề phải có các cột: username, mat_khau, ho_ten, email, vai_tro (và ma_so, lop_hoac_bo_mon). Hãy tải tệp mẫu.");
        }

        List<String> loi = new ArrayList<>();
        int thanhCong = 0;
        for (int i = 1; i < dong.size(); i++) {
            List<String> o = dong.get(i);
            int soDong = i + 1;
            try {
                TaiKhoanForm f = new TaiKhoanForm();
                f.setUsername(lay(o, cUser));
                f.setPassword(lay(o, cMk));
                f.setFullName(lay(o, cTen));
                f.setEmail(lay(o, cMail));
                if (f.getUsername().length() < 3) throw new BusinessException("Tên đăng nhập tối thiểu 3 ký tự.");
                if (f.getFullName().isEmpty()) throw new BusinessException("Thiếu họ tên.");
                if (f.getEmail().isEmpty() || !f.getEmail().contains("@")) throw new BusinessException("Email không hợp lệ.");
                try {
                    f.setRole(Role.valueOf(lay(o, cRole).toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException e) {
                    throw new BusinessException("Vai trò phải là ADMIN, TRUONG_KHOA, GIANG_VIEN hoặc SINH_VIEN.");
                }
                String ma = lay(o, cMa);
                String phu = lay(o, cPhu);
                if (f.getRole() == Role.SINH_VIEN) {
                    f.setMaSv(ma);
                    f.setLop(phu);
                } else if (f.getRole() == Role.GIANG_VIEN) {
                    f.setMaGv(ma);
                    BoMon b = boMonRepository.findByTenBoMon(phu).orElseThrow(
                            () -> new BusinessException("Không có bộ môn \"" + phu + "\" (tạo trước ở mục Bộ môn)."));
                    f.setBoMonId(b.getId());
                }
                userService.tao(f);
                thanhCong++;
            } catch (BusinessException e) {
                loi.add("Dòng " + soDong + ": " + e.getMessage());
            } catch (RuntimeException e) {
                loi.add("Dòng " + soDong + ": dữ liệu bị trùng hoặc không hợp lệ.");
            }
        }
        return new KetQuaNhap(dong.size() - 1, thanhCong, loi);
    }

    private static String lay(List<String> o, int c) {
        return c < 0 || c >= o.size() ? "" : o.get(c).trim();
    }

    /** Đọc CSV có hỗ trợ dấu ngoặc kép; tự nhận dấu phẩy hoặc chấm phẩy từ dòng tiêu đề. */
    static List<List<String>> docCsv(String s) {
        int hetDong = s.indexOf('\n');
        String dauTien = hetDong < 0 ? s : s.substring(0, hetDong);
        char ngan = dauTien.chars().filter(c -> c == ';').count() > dauTien.chars().filter(c -> c == ',').count() ? ';' : ',';

        List<List<String>> kq = new ArrayList<>();
        List<String> dong = new ArrayList<>();
        StringBuilder o = new StringBuilder();
        boolean trongNgoac = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (trongNgoac) {
                if (c == '"') {
                    if (i + 1 < s.length() && s.charAt(i + 1) == '"') { o.append('"'); i++; }
                    else trongNgoac = false;
                } else {
                    o.append(c);
                }
            } else if (c == '"') {
                trongNgoac = true;
            } else if (c == ngan) {
                dong.add(o.toString());
                o.setLength(0);
            } else if (c == '\n' || c == '\r') {
                if (c == '\r' && i + 1 < s.length() && s.charAt(i + 1) == '\n') i++;
                dong.add(o.toString());
                o.setLength(0);
                if (!(dong.size() == 1 && dong.get(0).isBlank())) kq.add(dong);
                dong = new ArrayList<>();
            } else {
                o.append(c);
            }
        }
        if (o.length() > 0 || !dong.isEmpty()) {
            dong.add(o.toString());
            kq.add(dong);
        }
        return kq;
    }
}
