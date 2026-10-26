package vn.edu.detai.dto;

import vn.edu.detai.entity.Diem;
import vn.edu.detai.entity.Nhom;
import vn.edu.detai.enums.LoaiDiem;

/** Một dòng công việc của giảng viên: nhóm liên quan, vai trò và điểm đã nhập (nếu có). */
public record NhiemVuCham(Nhom nhom, String vaiTro, LoaiDiem loai, Diem diem, boolean coQuyenCham) {
}
