package vn.edu.detai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DeTaiForm {
    @NotNull
    private Long dotId;
    @NotBlank(message = "Vui lòng nhập tên đề tài")
    @Size(max = 500, message = "Tên đề tài tối đa 500 ký tự")
    private String tenDeTai;
    @NotBlank(message = "Vui lòng nhập mô tả đề tài")
    @Size(max = 4000, message = "Mô tả tối đa 4000 ký tự")
    private String moTa;
    @NotNull(message = "Vui lòng chọn bộ môn")
    private Long boMonId;
    /** GVHD thứ hai (tùy chọn) — BR01. */
    private Long gvhd2Id;
}
