# Flashcards Page Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** Practice / Core Learning

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** 1000px
- **Layout:** Centered focus mode for studying, grid for deck management
- **Sections (Study Mode):** 1. Progress Bar (top), 2. Active Card (center), 3. Response buttons (bottom)
- **Sections (Management Mode):** 1. Deck blocks, 2. Add new card button, 3. Search/Filter deck

### Spacing Overrides

- **Card margins:** Auto (centered in flex layout for study mode)
- **Response button gap:** `--space-md` (16px)

### Typography Overrides

- **Front content (Word):** `display-sm` (2.5rem) weight 700
- **Back content (Definition):** `headline-sm` (1.5rem) weight 500
- **Example sentence:** `body-lg` italic in `--color-text-secondary`

### Color Overrides

- **Card Surface:** Pure white (`#FFFFFF`) to contrast with `--color-background`. 
- **Response "Hard":** `--color-error` (Red)
- **Response "Good":** `--color-warning` (Amber)
- **Response "Easy":** `--color-success` (Green)
- **Card Flip Border:** Subtle glow (`--shadow-glow`) on hover

### Component Overrides

- **Study Card:** Large `card` with fixed aspect ratio (e.g., 4:3), cursor pointer to flip.
- **Response Buttons:** Massive hit area, `btn-secondary` outline initially, color filled on hover.

---

## Page-Specific Components

- **Flip Card 3D:** Uses CSS `perspective` and `transform: rotateY` for 3D flipping animation.
- **Memory Progress Bar:** Segmented bar showing New vs Learning vs Review cards in different colors.
- **Audio Pronunciation Button:** Small floating circular button in top right of card.

---

## Recommendations

- Effects: Performant 3D transform for card flips (hardware accelerated with `transform: translateZ(0)`).
- UX: Support keyboard shortcuts (Space to flip, 1-3 for difficulty rating).
- UX: If user marks multiple "Hard", show subtle encouragement toast.
