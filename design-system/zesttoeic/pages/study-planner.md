# Study Planner Page Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** Productivity / Planning

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** 1200px
- **Layout:** Calendar/Timeline view
- **Sections:** 1. Weekly Overview (Goal progress), 2. Week timeline (Mon-Sun columns), 3. Task backlog / Recommendations side panel

### Spacing Overrides

- **Day column gap:** `--space-sm` (8px)
- **Task block padding:** `--space-sm` (8px)

### Typography Overrides

- **Day Headers:** `label-md` uppercase, weight 700
- **Task Name:** `body-sm` weight 600
- **Time/Duration:** `label-sm` monospace

### Color Overrides

- **Today Column:** Highlighted with `--color-primary-light` background at 30% opacity.
- **Task Blocks:**
  - **Vocab task:** `--color-xp` background, dark text
  - **Grammar task:** `--color-tertiary` background, dark text
  - **Test task:** `--color-warning` background, dark text
- **Completed Task:** Greyscale with `--color-success` checkmark overlay.

### Component Overrides

- **Draggable Task Block:** Shadow deepens during drag (`--shadow-lg`), scale up slightly `transform: scale(1.02)`.
- **Add Task Button:** `btn-secondary` full width inside columns.

---

## Page-Specific Components

- **Weekly Progress Ring:** Small circular charts showing completion vs target (e.g. 5hr / 10hr)
- **AI Recommendation Block:** Highlighted box with `--color-primary` border and "sparkles" icon, containing suggested schedule based on intelligence hub.
- **Drag-and-Drop Area:** Visible dashed border outline on empty columns to drop tasks.

---

## Recommendations

- UX: Smooth drag and drop animations. Snap to grid.
- Accessibility: Allow keyboard navigation for assigning tasks to days.
- State: Ensure immediate UI updates upon drag release before server sync.
