# Episodic Memory Fix Report

**Date:** 13 June 2026  
**Fix:** TASK 1 — Duplicate storage bug

## Problem

`EpisodicMemoryEngine` had two separate storage paths:

1. `remember()` → stored to `EpisodeStore`
2. `store()` → stored to local `List<Episode> episodes`
3. `all()` → returned local `episodes` list (NOT EpisodeStore)

**Impact:** `EpisodicRecallEngine.recallRelevant()` calls `memory.all()` which returned the local empty list. Episodes stored via `remember()` were invisible to recall. Memory recall failed.

## Fix

**File modified:** `src/main/java/com/darshan/agent/memory/EpisodicMemoryEngine.java`

| Before | After |
|--------|-------|
| `private final List<Episode> episodes = new ArrayList<>()` | Removed local list entirely |
| `store(Episode e)` added to `episodes` list | `store()` delegates to `EpisodeStore.add()` |
| `all()` returned `episodes` local list | `all()` returns `store.all()` |
| Dual storage paths | Single source of truth: `EpisodeStore` |

**Result:** Episodes stored via `remember()` or `store()` both go to the same `EpisodeStore`. `all()` returns all stored episodes. `recallRelevant()` now works correctly.
