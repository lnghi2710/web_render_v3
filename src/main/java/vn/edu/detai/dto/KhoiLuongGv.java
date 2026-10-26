package vn.edu.detai.dto;

import vn.edu.detai.entity.GiangVien;

/** Khối lượng công việc của một giảng viên trong các đợt chưa công bố. */
public record KhoiLuongGv(GiangVien gv, long deTai, long nhomHuongDan, long nhomPhanBien, long hoiDong, long chuaNhapDiem) {
    public long tongNhom() { return nhomHuongDan + nhomPhanBien; }
}
