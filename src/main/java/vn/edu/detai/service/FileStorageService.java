package vn.edu.detai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.detai.dto.*;
import vn.edu.detai.entity.*;
import vn.edu.detai.enums.*;
import vn.edu.detai.exception.BusinessException;
import vn.edu.detai.repository.*;
import vn.edu.detai.security.CurrentUser;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Lưu báo cáo PDF của nhóm; nộp lại thì thay thế tệp cũ (BR08). */
@Service
public class FileStorageService {
    private static final long TOI_DA = 20L * 1024 * 1024;
    private final Path goc;

    public FileStorageService(@Value("${app.upload-dir:uploads}") String thuMuc) {
        this.goc = Path.of(thuMuc).toAbsolutePath().normalize();
    }

    /** @return đường dẫn tương đối (so với thư mục gốc) để lưu vào nhom.file_bao_cao */
    public String luuBaoCao(MultipartFile file, Long dotId, Long nhomId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Vui lòng chọn tệp báo cáo PDF.");
        }
        if (file.getSize() > TOI_DA) {
            throw new BusinessException("Tệp báo cáo vượt quá dung lượng tối đa 20MB.");
        }
        String ten = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (!ten.endsWith(".pdf") || !"application/pdf".equalsIgnoreCase(file.getContentType()) || !laPdf(file)) {
            throw new BusinessException("Chỉ chấp nhận tệp định dạng PDF.");
        }
        // Tên tệp do hệ thống đặt từ mã số, không dùng tên người dùng gửi lên (chống path traversal).
        String tuongDoi = "dot-" + dotId + "/nhom-" + nhomId + ".pdf";
        Path dich = goc.resolve(tuongDoi).normalize();
        if (!dich.startsWith(goc)) {
            throw new BusinessException("Đường dẫn tệp không hợp lệ.");
        }
        try {
            Files.createDirectories(dich.getParent());
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, dich, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new BusinessException("Không thể lưu tệp báo cáo: " + e.getMessage());
        }
        return tuongDoi;
    }

    public Resource taiBaoCao(String tuongDoi) {
        try {
            Path p = goc.resolve(tuongDoi).normalize();
            if (!p.startsWith(goc) || !Files.exists(p)) {
                throw new BusinessException("Không tìm thấy tệp báo cáo.");
            }
            return new UrlResource(p.toUri());
        } catch (java.net.MalformedURLException e) {
            throw new BusinessException("Không thể đọc tệp báo cáo.");
        }
    }

    /** Kiểm tra loại tệp bằng chữ ký đầu tệp "%PDF-" (NFR01). */
    private boolean laPdf(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            byte[] dau = in.readNBytes(5);
            return dau.length == 5 && new String(dau, StandardCharsets.US_ASCII).equals("%PDF-");
        } catch (IOException e) {
            return false;
        }
    }
}
