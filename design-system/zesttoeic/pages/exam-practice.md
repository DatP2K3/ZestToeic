# Exam Practice Page Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** Learning / Mini-Test & Practice

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** 1000px
- **Layout:** Vertical flow or Single Panel (Tập trung tuyệt đối vào 1 câu hỏi tại một thời điểm)
- **Sections:** 1. Header (Tiến độ bài làm 5/10, Nút Thoát), 2. Prompt (Audio / Đoạn văn), 3. Câu hỏi và 4 tùy chọn A/B/C/D, 4. Nút "Chấm điểm ngay" (Immediate Feedback).

### Spacing Overrides

- **Lựa chọn đáp án:** Gap `--space-md` (16px), Padding `--space-lg` (24px) để dễ dùng trên mobile.
- **Khoảng cách Audio/Hình ảnh:** `--space-xl` (32px) so với phần câu hỏi.

### Typography Overrides

- **Đề bài / Câu hỏi:** `headline-sm` weight 600, line-height 1.6
- **Đáp án:** `body-lg` weight 500
- **Giải thích AI (Sau khi chọn):** `body-md` italic trong hộp cảnh báo.

### Color Overrides

- **Đáp án đang chọn:** Border `--color-primary`, Background `--color-primary-light`
- **Đáp án đúng (Kết quả):** Chuyển sang `--color-success` xanh lá với tick icon
- **Đáp án sai (Kết quả):** Chuyển sang `--color-error` đỏ với X icon
- **Nền trang:** Pure `--color-surface` để tập trung.

### Component Overrides

- **Voice/Audio Player:** Trôi nổi ngay trên đầu câu hỏi, kích thước to, dễ bấm play/tua lại.
- **Nút "Tiếp tục / Bỏ qua":** Xuất hiện trượt từ dưới lên sau khi user đã chọn xong 1 câu.

---

## Recommendations

- UX: Cho phép chạm toàn bộ box của câu trả lời, không chỉ radio button.
- Gamification: Hiển thị cộng "XP" bay ra khi trả lời đúng liền 3 câu ngay trong lúc làm bài.
