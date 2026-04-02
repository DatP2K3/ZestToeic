# Test Results Range Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** Feedback / Score Report

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** 1200px
- **Layout:** Dashbaord Tổng kết (Top) -> Lưới danh sách 200 câu chi tiết (Bottom).
- **Sections:** 1. Vòng tròn điểm số TOEIC 10-990 (Listening + Reading), 2. Thống kê tốc độ trung bình, 3. Biểu đồ điểm số Part 1-7 (Radar chart), 4. Nút xem "Gợi ý của AI Coach", 5. Phân tích chi tiết 200 câu.

### Spacing Overrides

- **Margin báo cáo:** `--space-2xl`
- **Khoảng cách hàng câu hỏi (Detail grid):** Bố cục hẹp.

### Typography Overrides

- **Điểm số Tổng:** `display-lg` (4rem) weight 800, italic, Space Grotesk.
- **Tags đánh giá (Kém, Tốt):** `label-md` uppercase.

### Color Overrides

- **Điểm cao (800+):** Gradient `--color-xp` -> Vàng.
- **Điểm Khá (600+):** Biên độ `--color-primary`.
- **Cấu trúc Lưới kết quả câu hỏi:**
  - Ô Đúng: Nền nhạt `--color-success` 
  - Ô Sai: Nền nhạt `--color-error`
  - Ô Chưa tick: Nền xám nhạt

### Component Overrides

- **Nút "Giải trí chi tiết AI":** Nút có icon tia sét (✨) màu tím.
- **Thanh Progress Bar Listening/Reading:** Dạng vạch đo 2 chiều song song (bar chart).

---

## Recommendations

- Animation: Khi vừa nộp bài, điểm số nên chạy lốc xoay đếm (Odometer effect) từ 10 lên đến điểm thật. Nó là khoảnh khắc hồi hộp nhất của app.
- Tương tác: Trong phần lưới câu hỏi, click vào bất kỳ câu nào sẽ bung Modal (thay vì chuyển trang) hiển thị Đề bài + Đáp án user + Đáp án đúng + Giải thích tại chỗ.
