# Exam Simulation (Full 200) Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** Assessment / Real Exam Simulation

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** Full width (100Vw)
- **Layout:** Split-Pane 100Vh (Không scroll toàn trang, cuộn nội bộ từng pane).
- **Sections:** 
  - Trái (30%): Khay điền đáp án 1-200 có dạng lưới cực mỏng + Đồng hồ đếm ngược gắn đỉnh + Nút Nộp bài.
  - Phải (70%): Khu vực dọc đề thi (Hình ảnh / Audio chung của nhóm câu hỏi / Đoạn văn Part 7).

### Spacing Overrides

- **Lưới đáp án (1-200):** Siêu chật / Data dense, `--space-xs` (4px).

### Typography Overrides

- **Đồng hồ đếm ngược:** `display-sm` monospace, đỏ lên khi tự động dưới 10 phút.
- **Sô lượng câu hỏi:** `label-sm` monospace.
- **Đoạn văn đọc hiểu:** Tăng line-height lên 1.8x để chống nhức mắt sau 1 tiếng đọc.

### Color Overrides

- **Nền trang:** Sử dụng `--color-surface-dim` thay vì trắng để giảm ánh sáng chói màn hình trong 2 giờ thi.
- **Câu đã làm:** Đổ nền xám `--color-border` nhạt ở lưới đáp án.
- **Câu đang focus:** Border `--color-primary`.
- **Câu đánh dấu (Flag):** Icon lá cờ `--color-warning`.

### Component Overrides

- **Khay Audio tổng (Part 1-4):** Fix cố định không trôi để user luôn tua được.
- **Bảng đáp án Matrix:** Giao diện lưới ô vuông (Bubble sheet) mô phỏng tờ tô phiếu thi thật.

---

## Recommendations

- Công năng: Hỗ trợ nút Flag để người thi đánh dấu câu "Cần quay lại xem xét".
- Tương tác: Tuyển đối không thiết kế hiệu ứng thừa/hover quá độ. Performance phải là 100% (DOM size có thể rất khủng vì 200 câu).
