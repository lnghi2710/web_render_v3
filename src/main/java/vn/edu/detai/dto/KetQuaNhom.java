package vn.edu.detai.dto;

import vn.edu.detai.entity.Diem;

import java.util.List;

/** Kết quả hiển thị cho sinh viên (BR17): chỉ có nội dung sau khi công bố. */
public record KetQuaNhom(boolean daCongBo, List<Diem> diems, Double diemCuoi) {
}
