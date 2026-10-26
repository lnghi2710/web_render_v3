package vn.edu.detai.dto;

import java.time.LocalDateTime;

/** Một mốc trong lịch của giảng viên; muc: info, warn, danger, ok. */
public record SuKien(LocalDateTime thoiGian, String loai, String noiDung, String url, String muc) {
    public boolean daQua() { return thoiGian != null && thoiGian.isBefore(LocalDateTime.now()); }
}
