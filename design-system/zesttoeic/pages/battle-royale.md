# Battle Royale Page Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** Real-time Competition / Gaming Mode

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** Full viewport width
- **Layout:** Immersive full-screen, no sidebar
- **Sections:** 1. Match lobby (waiting room + player list), 2. Live match (question + players + timer), 3. Elimination feed, 4. Results / Victory screen

### Spacing Overrides

- **Content Density:** High — game HUD-style compact layout
- **Player card gaps:** `--space-sm` (8px)

### Typography Overrides

- **Timer:** `display-lg` using Space Grotesk, centered
- **Player name:** `label-md` weight 600
- **Elimination text:** `title-sm` italic

### Color Overrides — ⚠️ THIS PAGE USES DARK MODE

- **Background:** `--dark-bg` (#0F172A) — Immersive dark arena
- **Surface cards:** `--dark-surface` (#1E293B)
- **Elevated cards:** `--dark-surface-elevated` (#334155)
- **Text:** `--dark-text` (#F1F5F9)
- **Text secondary:** `--dark-text-secondary` (#94A3B8)
- **Accent glow (correct):** `--dark-accent` (#14B8A6) with 15% glow
- **Accent glow (player focus):** `--dark-accent-blue` (#38BDF8)
- **Timer normal:** `--dark-text` (#F1F5F9)
- **Timer warning:** `--color-warning` (#F59E0B) with pulse
- **Timer critical:** `--color-error` (#EF4444) with rapid pulse
- **Elimination:** `--color-error` (#EF4444) with fade-out
- **Victory:** Gold gradient `#F59E0B` → `#D97706` with glow
- **Player alive:** `--color-success` (#22C55E) indicator dot
- **Player eliminated:** `--color-error` (#EF4444) with line-through

### Component Overrides

- Buttons use dark variant: `rgba(255,255,255,0.1)` background + white text
- Answer options: Dark surface with glow border on hover

---

## Page-Specific Components

- **Match Timer:** Large centered countdown with conic-gradient ring animation
- **Player Grid:** Avatar circles with alive/eliminated state indicators
- **Elimination Feed:** Toast notifications sliding from right (player X eliminated)
- **Victory Screen:** Confetti + crown animation + score breakdown

---

## Recommendations

- Effects: Real-time player elimination animations, timer pulse, answer feedback glow
- Audio: Sound effects for correct/wrong/elimination (respect mute setting)
- Performance: WebSocket for real-time updates, optimistic UI
- Immersion: Full-screen mode encouraged, minimal chrome
