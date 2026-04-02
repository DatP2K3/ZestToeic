# Vocabulary Book Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** Utility / Reference Library

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** 1200px
- **Layout:** Danh sách DataTable kết hợp thư mục
- **Sections:** 1. Filter mạnh (Từ chưa thuộc, Đã thuộc, Mục tiêu), 2. Nút "Luyện tập ngay" (Điều hướng tới list flashcards), 3. Danh sách từ vựng (Từ, Nghĩa, Phiên âm, Audio, Mức độ nhớ).

### Spacing Overrides

- **Table row:** Đủ rộng `--space-lg` padding để click vào Icon âm thanh Audio.

### Typography Overrides

- **Từ vựng (Word):** `headline-sm` weight 600
- **Phiên âm IPA:** `body-sm` monospace màu `--color-text-tertiary` `/kəˈnɛkt/`
- **Nghĩa:** `body-md` italic.

### Color Overrides

- **Trạng thái ghi nhớ:**
  - Cần ôn tập: `--color-error` (Đỏ) báo hiệu đường cong lãng quên (Ebbinghaus).
  - Đang học: `--color-warning` (Cam).
  - Đã master: `--color-success` (Xanh).

### Component Overrides

- **Phát âm Audio Icon:** Thay vì nút bấm, dùng icon Loa (Speaker) nhỏ nhẹ, hover đổi viền.
- **Status Bar:** Sinh ra một thanh progress mini dọc theo mỗi dòng từ, cho thấy từ này đã thuộc sâu tới đâu.

---

## Recommendations

- UX: Trong cột "Ví dụ", giấu bớt ví dụ và bung ra (Accordion nội bộ) khi người dùng click vào từ để bảng không trở nên quá cồng kềnh.
- Action: Thêm nút Kebab (3 chấm) cuối bảng để user xóa/cập nhật từ thủ công.
