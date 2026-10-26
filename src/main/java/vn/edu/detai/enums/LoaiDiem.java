package vn.edu.detai.enums;

public enum LoaiDiem {
    GVHD("Điểm GVHD"), GVPB("Điểm GVPB"), HOI_DONG("Điểm hội đồng");
    private final String nhan;
    LoaiDiem(String nhan) { this.nhan = nhan; }
    public String getNhan() { return nhan; }
}
