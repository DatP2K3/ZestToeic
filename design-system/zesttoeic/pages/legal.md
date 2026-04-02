# Legal Content Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** Operational / Static (Terms of Service, Privacy Policy)

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** 800px (Text optimal line length)
- **Layout:** Document style, sometimes with a sticky left Table of Contents.
- **Sections:** 1. Title + Last Updated, 2. Content blocks, 3. Contact info.

### Spacing Overrides

- **Paragraph margin:** `--space-md` (16px) bottom margin.
- **Section gap:** `--space-xl` (32px) before new headings.

### Typography Overrides

- **Document Title:** `display-sm` (2.5rem)
- **Headings (H2, H3):** Normal Plus Jakarta Sans, but strictly black/dark text `--color-text`.
- **Body Text:** `body-md` (1rem) line-height 1.8 for maximum legibility.

### Color Overrides

- **Background:** High contrast, use solid `--color-surface` (White) to ensure text is unambiguous.
- **Links:** `--color-primary` with underline enabled by default for accessibility.

### Component Overrides

- **Table of Contents:** Sticky sidebar, links use `--color-text-secondary`, turning primary on hover.

---

## Page-Specific Components

- **Summary Box (TL;DR):** A box at the top of long legal documents summarizing the key points in plain English, using `--color-surface-dim` background.

---

## Recommendations

- Accessibility: Avoid small, faded gray text for legalese. Keep contrast high.
- Formatting: Use ordered lists properly formatted natively by CSS `ol`, `li` markers.
- Layout: Do not center-align body text; always left-align.
