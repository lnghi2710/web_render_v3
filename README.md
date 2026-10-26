# Hệ thống quản lý đề tài sinh viên

Spring Boot 3.3 · Spring MVC · Spring Data JPA · Spring Security · Thymeleaf · Bootstrap 5 (CDN).
Kiến trúc phân lớp **Controller → Service → Repository → Entity** theo tài liệu thiết kế.

## Chạy
Yêu cầu JDK 17+ và Maven 3.9+.

```bash
mvn spring-boot:run                                   # H2 dạng file (./data), chạy ngay
mvn spring-boot:run -Dspring-boot.run.profiles=mysql  # MySQL (biến DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD)
```
Mở http://localhost:8080. Báo cáo PDF lưu ở `./uploads` (đổi bằng `app.upload-dir`).

## Tài khoản mẫu (nạp lần chạy đầu — hãy đổi mật khẩu)
| Vai trò | Tài khoản | Mật khẩu |
|---|---|---|
| Quản trị viên | admin | admin123 |
| Trưởng khoa | truongkhoa | 123456 |
| Giảng viên | gv01 … gv05 | 123456 |
| Sinh viên | sv01 … sv09 | 123456 |

## Giao diện
- **Khung ứng dụng:** thanh điều hướng bên trái (mực xanh) theo vai trò, thu gọn thành menu trượt trên điện thoại; mỗi trang có đường dẫn, tiêu đề và hành động chính ở đầu trang.
- **Dòng thời gian của đợt:** 5 giai đoạn (GV đăng ký, duyệt đề tài, SV đăng ký, thực hiện và chấm, công bố) hiện ở trang chủ, danh sách đợt, bảng điểm và trang của sinh viên.
- **Trang chủ "Việc cần xử lý":** danh sách việc theo vai trò (đề tài chờ duyệt, nhóm chưa có GVPB, nhóm chờ nhập điểm, chưa nộp báo cáo...) do `DashboardService` tạo.
- **Chữ:** Be Vietnam Pro (giao diện) và Source Serif 4 (tiêu đề, điểm số); biểu tượng Bootstrap Icons. Cả hai tải từ CDN nên cần Internet khi chạy.
- **Mã nguồn:** toàn bộ kiểu dáng ở `static/css/app.css` (biến màu ở đầu tệp), khung trang ở `templates/fragments/shell.html`, thành phần dùng chung ở `templates/fragments/ui.html`.
- Có tìm nhanh trong bảng tài khoản và nhật ký, trạng thái rỗng có hướng dẫn, focus bàn phím rõ ràng và tôn trọng `prefers-reduced-motion`.

## Cấu trúc
```
config/      SecurityConfig, DataInitializer
security/    CustomUserDetails, UserDetailsServiceImpl, CurrentUser
entity/      14 bảng theo ERD (khóa chính kép cho thanh_vien_nhom, thanh_vien_hoi_dong)
repository/  Spring Data JPA (khóa ghi PESSIMISTIC_WRITE cho đăng ký đề tài)
service/     Quy tắc nghiệp vụ BR01–BR17
controller/  Auth, ThongBao, Admin, TruongKhoa, GiangVien, SinhVien
exception/   BusinessException + GlobalExceptionHandler (thông báo lỗi tiếng Việt)
templates/   Thymeleaf theo vai trò
```

## Phân quyền URL
`/admin/**` ADMIN · `/truong-khoa/**` TRUONG_KHOA · `/giang-vien/**` GIANG_VIEN · `/sinh-vien/**` SINH_VIEN ·
`/dang-thong-bao/**` ADMIN + TRUONG_KHOA · còn lại cần đăng nhập. CSRF bật; mật khẩu BCrypt.
Quyền "nhóm trưởng" và "chủ tịch hội đồng" được kiểm tra trong Service.

## Quy tắc nghiệp vụ đã cài đặt
- BR01 1–2 GVHD/đề tài · BR02 nhóm ≤ 3 SV · BR03 mỗi SV một nhóm/đợt (UNIQUE sv+đợt)
- BR04 nhóm đăng ký trước được nhận (khóa ghi + UNIQUE `nhom.de_tai_id`) · BR05 kiểm tra cửa sổ thời gian GV/SV
- BR06–07 mốc theo loại đợt, trạng thái đợt suy ra từ thời gian · BR08 nộp PDF ≤ 20MB, nộp lại thay thế
- BR10 hội đồng 3–5 GV, 1 chủ tịch, 1 thư ký · BR11 GVPB/thành viên hội đồng không được là GVHD của nhóm
- BR12–14 điểm 0–10, mỗi loại một điểm/nhóm, đúng người đúng quyền · BR13 điểm cuối = trung bình thành phần
- BR15 quá hạn GVPB chỉ cảnh báo · BR17 SV chỉ xem điểm sau công bố · FR21 nhật ký thao tác (`audit_log`)

## Ghi chú
- Bộ môn được nạp sẵn 4 mục (tài liệu không có chức năng quản lý bộ môn).
- `ddl-auto=update` phù hợp phát triển; môi trường thật nên dùng Flyway/Liquibase.
