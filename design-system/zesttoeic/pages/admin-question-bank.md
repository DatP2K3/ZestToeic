# Admin Question Bank Page Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** Admin / Content Management

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** Full width (1600px max)
- **Layout:** Fixed admin sidebar (left) + Question/Test editing workspace (right)
- **Sections:** 1. Filter/Search Header (Part 1-7, difficulty, tags), 2. Question Database Table, 3. Split-screen Editor (Preview left, Edit form right) when modifying, 4. Bulk import tool

### Spacing Overrides

- **Editor form gap:** `--space-md` (16px) for tight data entry
- **Table cell padding:** Compact spacing to fit more fields

### Typography Overrides

- **Question text preview:** `body-md` in Space Grotesk to reflect exact rendering
- **Admin tags:** `label-sm` uppercase, high contrast

### Color Overrides

- **Difficulty Badges:** 
  - Easy: `--color-success`
  - Medium: `--color-warning` 
  - Hard: `--color-error`
- **Draft/Published Status:** Gray for draft, `--color-success` for published.
- **Correct Answer Highlight:** Background glow `--color-success` at 10% opacity in editor

### Component Overrides

- **Rich Text Editor Toolbar:** Neutral styling (`--color-surface-dim`) attached to text area.
- **Tag Input:** Clean pill style with X to remove.
- **Save/Publish Button:** Sticky bottom bar `btn-primary`

---

## Page-Specific Components

- **Drag & Drop Reorder Rule:** Used for reordering questions in a test. Dark border on element grab.
- **Audio Uploader:** Special widget showing waveform and play controls for Listening parts.
- **Image Cropper:** Inline image adjustment for Part 1/Part 7 reading passages.

---

## Recommendations

- UX: Ctrl+S / Cmd+S should trigger auto-save for questions.
- Verification: Highlight missing fields (audio missing for part 1) in `--color-error` before allowing publish.
