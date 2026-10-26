package vn.edu.detai.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;

/** Chuyển lỗi nghiệp vụ thành thông báo rõ ràng và quay lại trang trước (NFR04, FR20). */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public String nghiepVu(BusinessException ex, HttpServletRequest req, RedirectAttributes ra) {
        return quayLai(ex.getMessage(), req, ra);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String tepQuaLon(HttpServletRequest req, RedirectAttributes ra) {
        return quayLai("Tệp tải lên vượt quá dung lượng tối đa 20MB.", req, ra);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String trungDuLieu(HttpServletRequest req, RedirectAttributes ra) {
        return quayLai("Dữ liệu bị trùng hoặc vi phạm ràng buộc toàn vẹn. Vui lòng kiểm tra lại.", req, ra);
    }

    private String quayLai(String thongDiep, HttpServletRequest req, RedirectAttributes ra) {
        ra.addFlashAttribute("error", thongDiep);
        String dich = "/";
        String referer = req.getHeader("Referer");
        if (referer != null) {
            try {
                URI u = URI.create(referer);
                // Chỉ lấy đường dẫn nội bộ để tránh chuyển hướng ra ngoài.
                if (u.getRawPath() != null && u.getRawPath().startsWith("/")) {
                    dich = u.getRawPath() + (u.getRawQuery() != null ? "?" + u.getRawQuery() : "");
                }
            } catch (IllegalArgumentException ignored) {
                // dùng đích mặc định
            }
        }
        return "redirect:" + dich;
    }
}
