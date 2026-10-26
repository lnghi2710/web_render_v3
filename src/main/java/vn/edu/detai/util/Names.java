package vn.edu.detai.util;

public final class Names {
    private Names() {}

    /** Chữ cái đầu của tên gọi (từ cuối của họ tên), viết hoa — dùng cho ảnh đại diện. */
    public static String initials(String hoTen) {
        if (hoTen == null || hoTen.isBlank()) return "?";
        String[] tu = hoTen.trim().split("\\s+");
        return tu[tu.length - 1].substring(0, 1).toUpperCase();
    }
}
