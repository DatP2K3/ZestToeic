# Public Profile Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** Social Identity

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** 800px (Centered card look)
- **Layout:** Xếp chồng Cover banner -> Avatar -> Info -> Tabs
- **Sections:** 1. Ảnh bìa (Cover Image), 2. Ảnh đại diện chồng lên cover, 3. Tên + ID + Thẻ Fan/Rank, 4. Theo dõi/Kết bạn CTA, 5. Tab menu (Thành tựu, Lịch sử Squads, Thống kê học).

### Spacing Overrides

- **Avatar margin-top:** Âm margin (VD: `-60px`) để avatar nổi lên giữa đường cắt ảnh cover.
- **Tab padding:** `--space-md`

### Typography Overrides

- **Tên hiển thị:** `headline-lg` weight 700
- **Username / Clan Name:** `body-md` `--color-text-secondary`
- **Chỉ số (Số follower/Số streak):** `title-lg` weight 600 Space Grotesk.

### Color Overrides

- **Avatar Ring:** Viền bọc avatar hiển thị Level (Vàng, Bạc, Đồng, Kim cương).
- **Online Dot:** Điểm xanh sáng (`--color-success`) nằm sát góc phải avatar.
- **Nền Cover mặc định:** Pattern hạt mưa mờ hoặc chuyển sắc xanh Gradient của riêng nền tảng.

### Component Overrides

- **Thẻ Thành tựu (Badge Hexagon):** Icon hình lục giác, sử dụng vàng/đỏ/tím đặc trưng cho các milestones (VD: 30 days streak, 900+ score).
- **Nút Add Friend:** `btn-secondary` có icon User-Plus.

---

## Recommendations

- Privacy: Hiển thị thêm dòng "Private profile" nếu người dùng đó giấu lịch sử học, chỉ hiện name card cơ bản.
- UI: Dùng phong cách thẻ bài game (Card layout) bo tròn toàn diện `--radius-xl` cho khung profile.
