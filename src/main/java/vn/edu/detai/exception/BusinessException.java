package vn.edu.detai.exception;

/** Lỗi vi phạm quy tắc nghiệp vụ (BR01–BR17); thông điệp hiển thị trực tiếp cho người dùng (FR20). */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
