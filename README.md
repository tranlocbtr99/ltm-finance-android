# LTM Finance — Hệ thống Quản trị Tài chính Cá nhân Tích hợp AI 💎

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Platform-Android_8.0+_(API_26+)-green.svg)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose_Material3-blue.svg)](https://developer.android.com/jetpack/compose)
[![Style](https://img.shields.io/badge/Theme-iOS_Liquid_Glass-cyan.svg)](#giao-diện-ios-liquid-glass)

> **LTM Finance** là ứng dụng Android native được xây dựng theo chuẩn **Product Requirements Document (PRD v1.0)**, kết hợp ba thành phần độc lập nhưng liên kết chặt chẽ:
> 1. **Finance Engine:** Tính toán số dư, thu nhập, chi tiêu, nợ, ngân sách và dòng tiền với độ chính xác tuyệt đối (`BigDecimal`).
> 2. **AI Finance Assistant (Hybrid):** Trợ lý tài chính hỗ trợ On-demand (hỏi đáp theo kịch bản sinh viên) và Proactive (cảnh báo rủi ro chủ động).
> 3. **Research Analytics:** Theo dõi hành vi sử dụng, mức độ tiếp nhận và tuân thủ ngân sách phục vụ nghiên cứu quan sát theo chiều dọc (*Longitudinal Observational Study*).

---

## 📱 Giao diện iOS Liquid Glass (Kính lỏng hiện đại)

Ứng dụng mang phong cách thiết kế **iOS Liquid Glass / Frosted Glass**:
- **Ambient Fluid Mesh:** Nền không gian sâu với các quầng sáng hữu cơ chuyển động mượt mà.
- **Translucent Glass Cards:** Khối kính mờ bo góc tròn lớn (`24-26dp`), viền highlight phản xạ ánh sáng cao cấp (`GlassBorderTopHighlight`).
- **Floating Pill Bottom Navigation:** Thanh điều hướng nổi dạng viên thuốc kính mờ ở cạnh đáy với đèn chỉ báo tab phát sáng.
- **Liquid Segmented Controls:** Bộ chuyển tab trượt mượt mà theo phong cách iOS.
- **Liquid Progress Bars:** Thanh tiến độ tài chính chất lỏng đổi màu theo ngưỡng cảnh báo.

---

## 🚀 Các tính năng chính

### 1. Màn hình Tổng quan (Overview) — Trả lời 5 câu hỏi cốt lõi
- **Tôi đang có bao nhiêu tiền và tài sản?** ➔ Phân biệt *Tổng tài sản*, *Tài sản ròng* và *Tiền thanh khoản khả dụng ngay* (loại trừ tiền gửi có kỳ hạn bị khóa).
- **Tôi đang nợ bao nhiêu và phải trả khi nào?** ➔ Theo dõi tổng dư nợ và lịch thanh toán sắp đến hạn.
- **Tôi đang chi tiêu có đúng ngân sách không?** ➔ Theo dõi tiến độ chi tiêu tháng và danh mục vượt hạn mức.
- **Nếu tiếp tục như hiện tại, tài chính sẽ ra sao?** ➔ Snapshot dự báo dòng tiền 30 ngày an toàn hay nguy cơ thiếu hụt.
- **Nếu thực hiện quyết định mới, tương lai thay đổi thế nào?** ➔ Lối tắt kích hoạt bộ mô phỏng quyết định mua sắm.

### 2. Sổ giao dịch & Chuyển tiền nội bộ (FR-003, FR-004, FR-005, FR-006)
- Ghi chép Thu nhập, Chi tiêu và **Chuyển tiền giữa các tài khoản** (tự động khấu trừ tài khoản nguồn, cộng tài khoản đích, không tính vào tổng thu/chi).
- Bộ lọc thông minh theo danh mục, ngày tháng và loại giao dịch.

### 3. Quản lý ngân sách & Truy vết điều chỉnh (FR-014, FR-015, FR-016)
- Phân bổ ngân sách theo danh mục (Ăn uống, Nhà ở, Đi lại, Học tập, Giải trí...).
- Ngưỡng cảnh báo thông minh: 80% (Warning), 100% (Alert) và >100% (Overspent).
- **Audit Log:** Lưu vết toàn bộ lịch sử điều chỉnh (Ngân sách gốc, Đề xuất AI, Người dùng điều chỉnh kèm lý do và mốc thời gian).

### 4. Tài sản & Nghĩa vụ nợ (FR-008, FR-009, FR-011, FR-012, FR-013)
- Tài khoản: Tiền mặt, Ngân hàng (Vietcombank), Ví điện tử (MoMo), Tiết kiệm có kỳ hạn (BIDV kèm ngày đáo hạn và lãi suất).
- Khoản nợ: Trả góp thiết bị, vay cá nhân, thẻ tín dụng (hạn mức tín dụng không bị tính vào tài sản người dùng).
- Lịch trả nợ định kỳ và trạng thái thanh toán.

### 5. AI Finance Assistant (Hybrid On-demand + Proactive) (PRD Section 5)
- **On-demand AI Chat:** Hỏi đáp dựa trên dữ liệu tính toán từ Finance Engine (không bịa số) với các gợi ý nhanh cho sinh viên (*"Có nên mua laptop 20tr không?"*, *"Tháng này chi nhiều vào đâu?"*...).
- **Proactive Alerts:** Chủ động quét và cảnh báo nguy cơ vượt hạn mức ngân sách, dòng tiền âm hoặc nợ sắp đến hạn.
- **AI Recommendation Log (FR-024):** State Machine hoàn chỉnh (`Created` ➔ `Viewed` ➔ `Accepted`/`Rejected` ➔ `Monitoring` ➔ `Implemented`), tích hợp quy trình **Double-check** đối chiếu giao dịch thực tế và đánh giá chất lượng phản hồi.

### 6. Dự báo dòng tiền & Mô phỏng quyết định (FR-019, FR-020, FR-021)
- **Dự báo 30 ngày, 90 ngày, 12 tháng:** Tính toán dòng tiền theo từng mốc thời gian thực phát sinh nghĩa vụ tài chính; phát hiện nguy cơ thiếu tiền (Shortage Alert) và đề xuất giải pháp.
- **Mô phỏng mua sắm:** So sánh 4 kịch bản (*Không mua*, *Mua ngay*, *Trả góp*, *Tích lũy trước rồi mua*) kèm phân tích tác động số dư sau quyết định.

### 7. Trung tâm Nghiên cứu Khoa học & Quyền riêng tư (Section 6 & 10)
- Quản lý mã nghiên cứu ẩn danh riêng biệt (`PARTICIPANT-XXXX`).
- Theo dõi các chỉ số khoa học: Tỷ lệ tuân thủ ngân sách, Tần suất tương tác AI, Tỷ lệ chấp nhận & thực hiện lời khuyên.
- **Phân tách sự đồng ý (Consent Management):** 3 công tắc độc lập tuân thủ Luật Bảo vệ dữ liệu cá nhân số 91/2025/QH15.
- Xuất dữ liệu nghiên cứu chuẩn CSV phục vụ phân tích SPSS, R và Python.

---

## 🛠️ Công nghệ & Kiến trúc

- **Ngôn ngữ:** Kotlin 2.0.21
- **UI Framework:** Jetpack Compose + Material 3 + Custom iOS Liquid Glass Shaders
- **Kiến trúc:** Clean Architecture / MVVM (Model-View-ViewModel) + Reactive Flow / StateFlow
- **Độ chính xác tiền tệ:** `java.math.BigDecimal` (0 decimal VND)
- **Build Tool:** Gradle 8.14.3 (Android Gradle Plugin 8.9.2), Target/Compile SDK 36, Min SDK 26

---

## 📦 Hướng dẫn cài đặt & Chạy ứng dụng

### 1. Clone repository
```bash
git clone https://github.com/tranlocbtr99/ltm-finance-android.git
cd ltm-finance-android
```

### 2. Build APK
```bash
./gradlew assembleDebug
```
File APK đầu ra nằm tại: `app/build/outputs/apk/debug/app-debug.apk`

### 3. Cài đặt trực tiếp lên thiết bị Android
```bash
./gradlew installDebug
```

---

## 👤 Tác giả
- GitHub: [@tranlocbtr99](https://github.com/tranlocbtr99)
- Bản quyền thuộc về dự án nghiên cứu LTM Finance 2026.
