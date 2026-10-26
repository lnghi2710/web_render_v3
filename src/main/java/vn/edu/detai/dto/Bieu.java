package vn.edu.detai.dto;

import java.util.List;

/** Một biểu đồ thanh ngang gồm nhiều thanh; url có thể rỗng. */
public record Bieu(String tieuDe, String phu, List<Cot> cot, String url) {
}
