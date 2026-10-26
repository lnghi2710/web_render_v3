package vn.edu.detai.dto;

import vn.edu.detai.entity.AuditLog;

import java.util.List;

/** Số liệu tổng quan hệ thống cho quản trị viên. */
public record TongQuanHeThong(long tongTaiKhoan, long daKhoa, long admin, long truongKhoa, long giangVien, long sinhVien,
                              long soDot, long dotChuaCongBo, long soDeTai, long soNhom, long thaoTac24h,
                              List<AuditLog> ganDay) {
}
