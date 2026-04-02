# Quests Page Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** Progression / Gamification

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** 800px (Centered card layout)
- **Layout:** Vertical list of active and completed quests
- **Sections:** 1. Daily Streak header (Fire icon + current days), 2. Daily Quests checklist, 3. Bonus Quests / Milestones, 4. XP Summary

### Spacing Overrides

- **Quest Card gap:** `--space-md` (16px)
- **Header margin:** `--space-xl` (32px)

### Typography Overrides

- **Streak Number:** `display-md` (3rem) weight 700, italics
- **Quest Title:** `headline-sm` weight 600
- **Quest Reward XP:** `label-md` weight 700, colored using `--color-xp`

### Color Overrides

- **Streak Flame:** Gradient from `#F59E0B` to `#EA580C`
- **Quest Background:** Standard `--color-surface` but transitions to `--color-surface-dim` + lower opacity when completed.
- **Progress Bar Fill:** `--color-success` (Green)
- **Claim Button:** `--color-xp` (Violet background) for high gamification feel.

### Component Overrides

- **Quest Card:** Combines title, description, progress bar, and "Claim Reward" state.
- **Progress Bar:** Thicker than standard bar, 12px height for visual emphasis.

---

## Page-Specific Components

- **Flame Icon Animation:** CSS keyframe pulse/flicker animation for the active streak.
- **Reward Claim Animation:** Upon clicking "Claim", XP numbers float upwards and fade out.
- **Treasure Chest Icon:** Used for weekly milestone rewards, shakes on hover if unlockable.

---

## Recommendations

- Effects: Trigger confetti effect (Canvas integration) if user completes perfectly (Perfect Streak).
- Auditory: Add subtle "ding" sound on claiming XP (optional config).
- Experience: Sort unclaimed quests to top, incomplete next, claimed at bottom.
