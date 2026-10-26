package vn.edu.detai.enums;

import java.util.List;

public enum LoaiDot {
    MON_HOC("Môn học"), NCKH("Nghiên cứu khoa học"), TLCN("Tiểu luận chuyên ngành"), KLTN("Khóa luận tốt nghiệp");
    private final String nhan;
    LoaiDot(String nhan) { this.nhan = nhan; }
    public String getNhan() { return nhan; }

    /** Đợt có phân công GVPB (BR06): TLCN, KLTN. */
    public boolean coGvpb() { return this == TLCN || this == KLTN; }

    /** Đợt có hội đồng (BR10): chỉ KLTN. */
    public boolean coHoiDong() { return this == KLTN; }

    /** Các điểm thành phần theo loại đợt (BR13). */
    public List<LoaiDiem> cacLoaiDiem() {
        return switch (this) {
            case MON_HOC, NCKH -> List.of(LoaiDiem.GVHD);
            case TLCN -> List.of(LoaiDiem.GVHD, LoaiDiem.GVPB);
            case KLTN -> List.of(LoaiDiem.GVHD, LoaiDiem.GVPB, LoaiDiem.HOI_DONG);
        };
    }
}
