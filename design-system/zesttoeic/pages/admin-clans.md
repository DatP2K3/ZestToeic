# Admin Clans Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** Admin / Group Management

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** Full width (1600px max)
- **Layout:** Data table + Side Panel drawer (Giống Admin Users).
- **Sections:** 1. Bảng danh sách Clan (Tên, Trưởng nhóm, Cấp độ, Member Count, Hành động), 2. Khung Quản lý Điểm số / Xóa clan nội dung cấm.

### Spacing Overrides

- **Avatar Clan in row:** `--space-sm` gap to Clan Name.

### Typography Overrides

- **Clan Score (XP):** `body-lg` monospace, `--color-xp`.
- **Warning tag:** Lên danh sách các Clan < 5 người (cảnh báo giải thể).

### Color Overrides

- **Red flags:** Sử dụng màu đỏ cảnh báo nếu tên Clan trùng với từ cấm.
- **Leader tag:** Tô đậm tên clan leader.

### Component Overrides

- Liên kết điều hướng từ bảng admin-clans thẳng sang slide-out danh sách User nằm trong clan đó.

---

## Recommendations

- UX: Bổ sung biểu đồ nhỏ (sparklines) trên cột "Hoạt động gần đây" để Admin nhận biết Clan nào đang "sống" hay đã "chết" (vô dụng).
