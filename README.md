# 🦀 Clawd Pet for Android

Ứng dụng chú cua Clawd Pet dạng **Screen Overlay (Shimeji)** chạy nổi trên màn hình Android, hoạt động đè lên trên tất cả các ứng dụng khác!

---

## ✨ Tính năng

- **Bò tự do trên màn hình**: Chú cua tự đi dạo qua lại ở đáy màn hình điện thoại, tự quay đầu khi chạm mép viền.
- **Tương tác ngón tay (Touch & Drag)**:
  - Chạm và giữ để nhấc chú cua lên bất kỳ đâu trên màn hình.
  - Thả tay: Trọng lực sẽ kéo chú cua rơi tự do xuống sàn.
  - Chạm nhẹ (Tap): Chú cua sẽ nhảy múa vui vẻ hoặc vẫy tay chào!
- **Bong bóng suy nghĩ (Speech Bubble)**: Thỉnh thoảng hiện những câu thoại đáng yêu hoặc trạng thái hoạt động ("thinking...", "all done! 🎉", "writing code...").
- **Cài đặt linh hoạt**: Tùy chỉnh kích thước chú cua (50dp - 130dp) và tốc độ bò (0.5x - 2.5x).
- **Tiết kiệm pin & mượt mà**: Chạy dạng Foreground Service với notification bật/tắt nhanh chóng.

---

## 🚀 Cách tạo file APK để cài vào điện thoại (qua GitHub Actions)

Bạn không cần phải cài Android Studio nặng nề trên máy tính, GitHub sẽ tự động biên dịch file `.apk` miễn phí:

### Bước 1: Tạo kho lưu trữ (Repository) trên GitHub
1. Đăng nhập [GitHub.com](https://github.com).
2. Nhấn nút **New repository**, đặt tên (ví dụ: `clawd-pet-android`), chọn **Public** hoặc **Private**, rồi nhấn **Create repository**.

### Bước 2: Tải thư mục này lên GitHub
- **Cách đơn giản nhất**: Kéo thả toàn bộ các file trong thư mục `D:\downloads\clawd-pet-android\` vào giao diện web của GitHub (nhấn **uploading an existing file**).
- **Hoặc dùng Git**:
  ```bash
  git init
  git add .
  git commit -m "Initial commit for Clawd Pet Android"
  git branch -M main
  git remote add origin https://github.com/TÊN_BẠN/clawd-pet-android.git
  git push -u origin main
  ```

### Bước 3: Tải file APK về điện thoại
1. Vào tab **Actions** trên repo GitHub của bạn.
2. Bạn sẽ thấy quy trình **Build Clawd Pet APK** đang tự động chạy (mất khoảng 1 - 2 phút).
3. Khi biểu tượng chuyển màu xanh lá (Success), nhấn vào phiên bản build đó.
4. Cuộn xuống phần **Artifacts** và nhấn vào **ClawdPet-Debug-APK** để tải file APK về điện thoại.
5. Cài đặt file APK trên điện thoại Android, mở app và cấp quyền **"Hiển thị trên ứng dụng khác"** là chú cua sẽ xuất hiện! 🦀🎉
