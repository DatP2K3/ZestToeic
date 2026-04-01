#!/usr/bin/env bash
# ============================================
# Super-Framework Sync Script (macOS/Linux)
# Bash equivalent of sync-super-framework.ps1
# ============================================
#
# Syncs skills, agents, workflows from upstream submodules + evo/ into .agent/
#   - _upstream/antigravity-kit -> .agent/skills/agkit-*, .agent/agents/, .agent/workflows/
#   - _upstream/superpowers     -> .agent/skills/sp-*
#   - evo/GEMINI.md             -> .agent/rules/GEMINI.md
#   - evo/policies/             -> .agent/policies/
#   - evo/agents/               -> .agent/agents/ (overlay)
#   BMAD skills (.agent/skills/bmad-*) are managed by npx bmad-method install and NOT touched.
#
# Usage:
#   ./evo/sync-super-framework.sh                   # Full sync
#   ./evo/sync-super-framework.sh --skip-submodule   # Skip git submodule update
#   ./evo/sync-super-framework.sh --dry-run          # Preview only, no changes

set -euo pipefail

# -----------------------------------------------
# Parse arguments
# -----------------------------------------------
SKIP_SUBMODULE=false
DRY_RUN=false

for arg in "$@"; do
  case "$arg" in
    --skip-submodule) SKIP_SUBMODULE=true ;;
    --dry-run)        DRY_RUN=true ;;
    -h|--help)
      echo "Usage: $0 [--skip-submodule] [--dry-run]"
      echo "  --skip-submodule  Skip git submodule update"
      echo "  --dry-run         Preview changes without modifying files"
      exit 0
      ;;
  esac
done

# -----------------------------------------------
# Colors
# -----------------------------------------------
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
WHITE='\033[1;37m'
GRAY='\033[0;37m'
NC='\033[0m' # No Color

# -----------------------------------------------
# Paths
# -----------------------------------------------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

UPSTREAM_DIR="$PROJECT_ROOT/_upstream"
AGENT_DIR="$PROJECT_ROOT/.agent"
SKILLS_DIR="$AGENT_DIR/skills"
AGENTS_DIR="$AGENT_DIR/agents"
WORKFLOWS_DIR="$AGENT_DIR/workflows"
RULES_DIR="$AGENT_DIR/rules"
POLICIES_DIR="$AGENT_DIR/policies"
EVO_DIR="$PROJECT_ROOT/evo"

AGKIT_DIR="$UPSTREAM_DIR/antigravity-kit"
SUPERPOWERS_DIR="$UPSTREAM_DIR/superpowers"

# Counters
AGKIT_SKILLS=0
AGKIT_AGENTS=0
AGKIT_WORKFLOWS=0
SP_SKILLS=0
EVO_POLICIES_COUNT=0
EVO_AGENTS=0
EVO_WORKFLOWS=0
EVO_SKILLS=0

# -----------------------------------------------
# Header
# -----------------------------------------------
echo ""
echo -e "${MAGENTA}===============================${NC}"
echo -e "${MAGENTA}  Super-Framework Sync v7${NC}"
echo -e "${MAGENTA}  BMAD + Superpowers + AG Kit${NC}"
echo -e "${MAGENTA}===============================${NC}"
echo ""

if [ "$DRY_RUN" = true ]; then
  echo -e "${YELLOW}  [DRY RUN] No changes will be made${NC}"
  echo ""
fi

# -----------------------------------------------
# Step 1: Update git submodules
# -----------------------------------------------
echo -e "${WHITE}[1/13] Git Submodules${NC}"
if [ "$SKIP_SUBMODULE" = true ]; then
  echo -e "  -> ${YELLOW}Skipping submodule update${NC}"
else
  echo -e "  -> ${CYAN}Updating submodules...${NC}"
  if [ "$DRY_RUN" = false ]; then
    cd "$PROJECT_ROOT"
    git submodule update --remote --merge 2>&1 || true
  fi
  echo -e "  -> ${GREEN}Done${NC}"
fi
echo ""

# -----------------------------------------------
# Step 2: Ensure directories exist
# -----------------------------------------------
echo -e "${WHITE}[2/13] Ensuring directories${NC}"
for dir in "$SKILLS_DIR" "$AGENTS_DIR" "$WORKFLOWS_DIR" "$RULES_DIR" "$POLICIES_DIR"; do
  if [ ! -d "$dir" ]; then
    echo -e "  -> ${CYAN}Creating $(basename "$dir")/${NC}"
    if [ "$DRY_RUN" = false ]; then
      mkdir -p "$dir"
    fi
  fi
done
echo -e "  -> ${GREEN}All directories ready${NC}"
echo ""

# -----------------------------------------------
# Step 3: Sync AG Kit Skills (prefix: agkit-)
# -----------------------------------------------
echo -e "${WHITE}[3/13] Syncing AG Kit Skills -> agkit-*${NC}"
AGKIT_SKILLS_SRC="$AGKIT_DIR/.agent/skills"

if [ -d "$AGKIT_SKILLS_SRC" ]; then
  # Remove old agkit-* skills
  if [ "$DRY_RUN" = false ]; then
    find "$SKILLS_DIR" -maxdepth 1 -type d -name "agkit-*" -exec rm -rf {} + 2>/dev/null || true
  fi

  # Copy each skill with agkit- prefix
  for skill_dir in "$AGKIT_SKILLS_SRC"/*/; do
    [ -d "$skill_dir" ] || continue
    skill_name="$(basename "$skill_dir")"
    dest_name="agkit-${skill_name}"
    echo -e "  -> ${CYAN}${skill_name} -> ${dest_name}${NC}"
    if [ "$DRY_RUN" = false ]; then
      cp -r "$skill_dir" "$SKILLS_DIR/$dest_name"
    fi
    AGKIT_SKILLS=$((AGKIT_SKILLS + 1))
  done

  # Copy doc.md if exists
  if [ -f "$AGKIT_SKILLS_SRC/doc.md" ] && [ "$DRY_RUN" = false ]; then
    cp "$AGKIT_SKILLS_SRC/doc.md" "$SKILLS_DIR/agkit-doc.md"
  fi

  echo -e "  -> ${GREEN}${AGKIT_SKILLS} AG Kit skills synced${NC}"
else
  echo -e "  -> ${YELLOW}AG Kit skills source not found${NC}"
fi
echo ""

# -----------------------------------------------
# Step 4: Sync AG Kit Agents
# -----------------------------------------------
echo -e "${WHITE}[4/13] Syncing AG Kit Agents${NC}"
AGKIT_AGENTS_SRC="$AGKIT_DIR/.agent/agents"

if [ -d "$AGKIT_AGENTS_SRC" ]; then
  for agent_file in "$AGKIT_AGENTS_SRC"/*.md; do
    [ -f "$agent_file" ] || continue
    echo -e "  -> ${CYAN}$(basename "$agent_file")${NC}"
    if [ "$DRY_RUN" = false ]; then
      cp "$agent_file" "$AGENTS_DIR/"
    fi
    AGKIT_AGENTS=$((AGKIT_AGENTS + 1))
  done
  echo -e "  -> ${GREEN}${AGKIT_AGENTS} agents synced${NC}"
else
  echo -e "  -> ${YELLOW}AG Kit agents source not found${NC}"
fi
echo ""

# -----------------------------------------------
# Step 5: Sync AG Kit Workflows
# -----------------------------------------------
echo -e "${WHITE}[5/13] Syncing AG Kit Workflows${NC}"
AGKIT_WF_SRC="$AGKIT_DIR/.agent/workflows"

if [ -d "$AGKIT_WF_SRC" ]; then
  for wf_file in "$AGKIT_WF_SRC"/*.md; do
    [ -f "$wf_file" ] || continue
    echo -e "  -> ${CYAN}$(basename "$wf_file")${NC}"
    if [ "$DRY_RUN" = false ]; then
      cp "$wf_file" "$WORKFLOWS_DIR/"
    fi
    AGKIT_WORKFLOWS=$((AGKIT_WORKFLOWS + 1))
  done
  echo -e "  -> ${GREEN}${AGKIT_WORKFLOWS} workflows synced${NC}"
else
  echo -e "  -> ${YELLOW}AG Kit workflows source not found${NC}"
fi
echo ""

# -----------------------------------------------
# Step 6: Sync GEMINI.md from evo/
# -----------------------------------------------
echo -e "${WHITE}[6/13] Syncing GEMINI.md from evo/${NC}"
EVO_GEMINI="$EVO_DIR/GEMINI.md"

if [ -f "$EVO_GEMINI" ]; then
  echo -e "  -> ${CYAN}evo/GEMINI.md -> .agent/rules/GEMINI.md${NC}"
  if [ "$DRY_RUN" = false ]; then
    cp "$EVO_GEMINI" "$RULES_DIR/GEMINI.md"
  fi
  echo -e "  -> ${GREEN}GEMINI.md synced (Constitutional + Toolbox Mastery)${NC}"
else
  # Fallback: try AG Kit base
  AGKIT_GEMINI="$AGKIT_DIR/.agent/rules/GEMINI.md"
  if [ -f "$AGKIT_GEMINI" ]; then
    echo -e "  -> ${YELLOW}evo/GEMINI.md not found, using AG Kit base${NC}"
    if [ "$DRY_RUN" = false ]; then
      cp "$AGKIT_GEMINI" "$RULES_DIR/GEMINI.md"
    fi
  else
    echo -e "  -> ${RED}No GEMINI.md source found${NC}"
  fi
fi
echo ""

# -----------------------------------------------
# Step 7: Sync project-context.md from evo/
# -----------------------------------------------
echo -e "${WHITE}[7/13] Syncing project-context.md from evo/${NC}"
EVO_PC="$EVO_DIR/project-context.md"

if [ -f "$EVO_PC" ]; then
  echo -e "  -> ${CYAN}evo/project-context.md -> .agent/project-context.md${NC}"
  if [ "$DRY_RUN" = false ]; then
    cp "$EVO_PC" "$AGENT_DIR/project-context.md"
  fi
  echo -e "  -> ${GREEN}project-context.md synced${NC}"
else
  echo -e "  -> ${YELLOW}evo/project-context.md not found (optional)${NC}"
fi
echo ""

# -----------------------------------------------
# Step 8: Sync Policies from evo/policies/
# -----------------------------------------------
echo -e "${WHITE}[8/13] Syncing Policies from evo/policies/${NC}"
EVO_POLICIES="$EVO_DIR/policies"

if [ -d "$EVO_POLICIES" ]; then
  for policy_file in "$EVO_POLICIES"/*.md; do
    [ -f "$policy_file" ] || continue
    echo -e "  -> ${CYAN}$(basename "$policy_file")${NC}"
    if [ "$DRY_RUN" = false ]; then
      cp "$policy_file" "$POLICIES_DIR/"
    fi
    EVO_POLICIES_COUNT=$((EVO_POLICIES_COUNT + 1))
  done
  echo -e "  -> ${GREEN}${EVO_POLICIES_COUNT} policies synced${NC}"
else
  echo -e "  -> ${YELLOW}evo/policies/ not found${NC}"
fi
echo ""

# -----------------------------------------------
# Step 9: Sync custom skills from evo/skills/
# -----------------------------------------------
echo -e "${WHITE}[9/13] Syncing custom skills from evo/skills/${NC}"
EVO_SKILLS_DIR="$EVO_DIR/skills"

if [ -d "$EVO_SKILLS_DIR" ]; then
  for skill_dir in "$EVO_SKILLS_DIR"/*/; do
    [ -d "$skill_dir" ] || continue
    skill_name="$(basename "$skill_dir")"
    dest_path="$SKILLS_DIR/$skill_name"
    echo -e "  -> ${CYAN}${skill_name}${NC}"
    if [ "$DRY_RUN" = false ]; then
      rm -rf "$dest_path" 2>/dev/null || true
      cp -r "$skill_dir" "$dest_path"
    fi
    EVO_SKILLS=$((EVO_SKILLS + 1))
  done
  echo -e "  -> ${GREEN}${EVO_SKILLS} custom skills synced${NC}"
else
  echo -e "  -> ${YELLOW}evo/skills/ not found${NC}"
fi
echo ""

# -----------------------------------------------
# Step 10: Sync custom agents from evo/agents/
# -----------------------------------------------
echo -e "${WHITE}[10/13] Syncing custom agents from evo/agents/${NC}"
EVO_AGENTS_DIR="$EVO_DIR/agents"

if [ -d "$EVO_AGENTS_DIR" ]; then
  for agent_file in "$EVO_AGENTS_DIR"/*.md; do
    [ -f "$agent_file" ] || continue
    echo -e "  -> ${CYAN}$(basename "$agent_file")${NC}"
    if [ "$DRY_RUN" = false ]; then
      cp "$agent_file" "$AGENTS_DIR/"
    fi
    EVO_AGENTS=$((EVO_AGENTS + 1))
  done
  echo -e "  -> ${GREEN}${EVO_AGENTS} custom agents synced${NC}"
else
  echo -e "  -> ${YELLOW}evo/agents/ not found${NC}"
fi
echo ""

# -----------------------------------------------
# Step 11: Sync evo workflows (whitelist override)
# -----------------------------------------------
echo -e "${WHITE}[11/13] Syncing evo workflows (smart override)${NC}"
EVO_WF_DIR="$EVO_DIR/workflows"

# Whitelist: evo workflows that can override AG Kit workflows
OVERRIDE_WHITELIST="brainstorm create dev debug test review plan status orchestrate enhance architecture help deploy incident refactor"

if [ -d "$EVO_WF_DIR" ]; then
  for wf_file in "$EVO_WF_DIR"/*.md; do
    [ -f "$wf_file" ] || continue
    wf_name="$(basename "$wf_file")"
    base_name="${wf_name%.md}"
    dest_path="$WORKFLOWS_DIR/$wf_name"

    if [ ! -f "$dest_path" ]; then
      # New evo-only workflow → always add
      echo -e "  -> ${GREEN}${wf_name} [NEW]${NC}"
    elif echo "$OVERRIDE_WHITELIST" | grep -qw "$base_name"; then
      # Whitelisted → override AG Kit version
      echo -e "  -> ${CYAN}${wf_name} [OVERRIDE]${NC}"
    else
      # Not whitelisted → skip
      echo -e "  -> ${YELLOW}${wf_name} [SKIP - not whitelisted]${NC}"
      continue
    fi

    if [ "$DRY_RUN" = false ]; then
      cp "$wf_file" "$dest_path"
    fi
    EVO_WORKFLOWS=$((EVO_WORKFLOWS + 1))
  done
  echo -e "  -> ${GREEN}${EVO_WORKFLOWS} evo workflows synced${NC}"
else
  echo -e "  -> ${YELLOW}evo/workflows/ not found${NC}"
fi
echo ""

# -----------------------------------------------
# Step 12: Sync Superpowers Skills (prefix: sp-)
# -----------------------------------------------
echo -e "${WHITE}[12/13] Syncing Superpowers Skills -> sp-*${NC}"
SP_SKILLS_SRC="$SUPERPOWERS_DIR/skills"

if [ -d "$SP_SKILLS_SRC" ]; then
  # Remove old sp-* skills
  if [ "$DRY_RUN" = false ]; then
    find "$SKILLS_DIR" -maxdepth 1 -type d -name "sp-*" -exec rm -rf {} + 2>/dev/null || true
  fi

  for skill_dir in "$SP_SKILLS_SRC"/*/; do
    [ -d "$skill_dir" ] || continue
    skill_name="$(basename "$skill_dir")"
    dest_name="sp-${skill_name}"
    echo -e "  -> ${CYAN}${skill_name} -> ${dest_name}${NC}"
    if [ "$DRY_RUN" = false ]; then
      cp -r "$skill_dir" "$SKILLS_DIR/$dest_name"
    fi
    SP_SKILLS=$((SP_SKILLS + 1))
  done
  echo -e "  -> ${GREEN}${SP_SKILLS} Superpowers skills synced${NC}"
else
  echo -e "  -> ${YELLOW}Superpowers skills source not found${NC}"
fi
echo ""

# -----------------------------------------------
# Step 13: Sync AG Kit shared resources
# -----------------------------------------------
echo -e "${WHITE}[13/13] Syncing AG Kit Shared Resources${NC}"
AGKIT_SHARED_SRC="$AGKIT_DIR/.agent/.shared"

if [ -d "$AGKIT_SHARED_SRC" ]; then
  SHARED_DEST="$AGENT_DIR/.shared"
  echo -e "  -> ${CYAN}.shared/ -> .agent/.shared/${NC}"
  if [ "$DRY_RUN" = false ]; then
    rm -rf "$SHARED_DEST" 2>/dev/null || true
    cp -r "$AGKIT_SHARED_SRC" "$SHARED_DEST"
  fi
  echo -e "  -> ${GREEN}Shared resources synced${NC}"
else
  echo -e "  -> ${YELLOW}No shared resources found${NC}"
fi
echo ""

# -----------------------------------------------
# Summary
# -----------------------------------------------
BMAD_SKILLS=$(find "$SKILLS_DIR" -maxdepth 1 -type d -name "bmad-*" 2>/dev/null | wc -l | tr -d ' ')
TOTAL_SKILLS=$((AGKIT_SKILLS + SP_SKILLS + BMAD_SKILLS + EVO_SKILLS))

echo -e "${GREEN}===============================${NC}"
echo -e "${GREEN}  Sync Summary${NC}"
echo -e "${GREEN}===============================${NC}"
echo ""
echo -e "  ${CYAN}AG Kit Skills (agkit-*):      ${AGKIT_SKILLS}${NC}"
echo -e "  ${CYAN}AG Kit Agents:                ${AGKIT_AGENTS}${NC}"
echo -e "  ${CYAN}AG Kit Workflows:             ${AGKIT_WORKFLOWS}${NC}"
echo -e "  ${CYAN}Superpowers Skills (sp-*):    ${SP_SKILLS}${NC}"
echo -e "  ${CYAN}Evo Policies:                 ${EVO_POLICIES_COUNT}${NC}"
echo -e "  ${CYAN}Evo Agents:                   ${EVO_AGENTS}${NC}"
echo -e "  ${CYAN}Evo Workflows:                ${EVO_WORKFLOWS}${NC}"
echo -e "  ${CYAN}Evo Skills:                   ${EVO_SKILLS}${NC}"
echo -e "  ${CYAN}GEMINI.md:                    from evo/${NC}"
echo ""
echo -e "  ${GRAY}BMAD Skills (bmad-*):         ${BMAD_SKILLS} (untouched)${NC}"
echo ""
echo -e "  ${WHITE}Total Skills Available:       ${TOTAL_SKILLS}${NC}"
echo ""

if [ "$DRY_RUN" = true ]; then
  echo -e "  ${YELLOW}[DRY RUN] No changes were made${NC}"
else
  echo -e "  ${GREEN}[OK] Sync complete! Your super-framework is ready.${NC}"
fi
echo ""
