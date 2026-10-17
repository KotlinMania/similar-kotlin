# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 17/20 (85.0%)
- **Function parity:** 189/244 matched (target 227) — 77.5%
- **Class/type parity:** 28/47 matched (target 53) — 59.6%
- **Combined symbol parity:** 217/291 matched (target 280) — 74.6%
- **Average inline-code cosine:** 0.16 (function body across 17 matched files)
- **Average documentation cosine:** 0.75 (doc text across 17 matched files)
- **Cheat-zeroed Files:** 1
- **Critical Issues:** 17 files with <0.60 function similarity
- **Needs Review:** 0 files with 0.60-0.84 function similarity
- **Excellent:** 0 files with >=0.85 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. algorithms.capture

- **Target:** `algorithms.Capture`
- **Similarity:** 0.16
- **Dependents:** 2
- **Priority Score:** 2011108.4
- **Functions:** 9/9 matched
- **Missing functions:** _none_
- **Types:** 1/2 matched
- **Missing types:** `Error`
- **Tests:** 1/1 matched

### 2. algorithms.replace

- **Target:** `algorithms.Replace`
- **Similarity:** 0.12
- **Dependents:** 1
- **Priority Score:** 1011508.8
- **Functions:** 13/13 matched
- **Missing functions:** _none_
- **Types:** 1/2 matched
- **Missing types:** `Error`
- **Tests:** 2/2 matched

### 3. algorithms.compact

- **Target:** `algorithms.Compact`
- **Similarity:** 0.30
- **Dependents:** 1
- **Priority Score:** 1011307.1
- **Functions:** 11/11 matched (target 13)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Error`

### 4. text.abstraction

- **Target:** `text.Abstraction [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 194010.0
- **Functions:** 19/37 matched (target 21)
- **Missing functions:** `as_diffable_str`, `as_diffable_str`, `tokenize_lines`, `tokenize_lines_and_newlines`, `tokenize_words`, `tokenize_unicode_words`, `tokenize_graphemes`, `tokenize_chars`, `as_str`, `to_string_lossy`, `ends_with_newline`, `len`, `slice`, `as_bytes`, `test_split_lines_bytes`, `test_split_words_bytes`, `test_split_chars_bytes`, `test_split_graphemes_bytes`
- **Types:** 2/3 matched (target 4)
- **Missing types:** `Output`
- **Tests:** 4/8 matched

### 5. algorithms.myers

- **Target:** `algorithms.Myers`
- **Similarity:** 0.15
- **Dependents:** 0
- **Priority Score:** 102308.5
- **Functions:** 12/18 matched (target 17)
- **Missing functions:** `diff`, `diff_deadline`, `new`, `index`, `index_mut`, `index`
- **Types:** 1/5 matched (target 3)
- **Missing types:** `Output`, `SlowIndex`, `HasRunFinish`, `Error`
- **Tests:** 6/6 matched

### 6. algorithms.utils

- **Target:** `algorithms.Utils`
- **Similarity:** 0.13
- **Dependents:** 0
- **Priority Score:** 82508.7
- **Functions:** 14/20 matched (target 19)
- **Missing functions:** `fmt`, `eq`, `index`, `new`, `hash`, `eq`
- **Types:** 3/5 matched (target 4)
- **Missing types:** `Output`, `Key`
- **Tests:** 4/4 matched

### 7. algorithms.hook

- **Target:** `algorithms.Hook`
- **Similarity:** 0.17
- **Dependents:** 0
- **Priority Score:** 72008.3
- **Functions:** 11/17 matched (target 11)
- **Missing functions:** `new`, `equal`, `delete`, `insert`, `replace`, `finish`
- **Types:** 2/3 matched (target 5)
- **Missing types:** `Error`

### 8. udiff

- **Target:** `similar.Udiff`
- **Similarity:** 0.16
- **Dependents:** 0
- **Priority Score:** 53008.4
- **Functions:** 20/25 matched (target 26)
- **Missing functions:** `fmt`, `fmt`, `fmt`, `fmt`, `fmt`
- **Types:** 5/5 matched (target 6)
- **Missing types:** _none_
- **Tests:** 3/3 matched

### 9. algorithms.patience

- **Target:** `algorithms.Patience`
- **Similarity:** 0.13
- **Dependents:** 0
- **Priority Score:** 51108.7
- **Functions:** 6/8 matched (target 9)
- **Missing functions:** `diff`, `diff_deadline`
- **Types:** 0/3 matched
- **Missing types:** `Patience`, `Error`, `HasRunFinish`
- **Tests:** 3/3 matched

### 10. text.inline

- **Target:** `text.Inline`
- **Similarity:** 0.09
- **Dependents:** 0
- **Priority Score:** 41909.1
- **Functions:** 13/16 matched
- **Missing functions:** `index`, `fmt`, `test_serde`
- **Types:** 2/3 matched (target 4)
- **Missing types:** `Output`
- **Tests:** 1/2 matched

### 11. algorithms.lcs

- **Target:** `algorithms.Lcs`
- **Similarity:** 0.12
- **Dependents:** 0
- **Priority Score:** 41308.8
- **Functions:** 9/11 matched (target 14)
- **Missing functions:** `diff`, `diff_deadline`
- **Types:** 0/2 matched
- **Missing types:** `HasRunFinish`, `Error`
- **Tests:** 7/7 matched

### 12. types

- **Target:** `similar.Types`
- **Similarity:** 0.17
- **Dependents:** 0
- **Priority Score:** 33308.3
- **Functions:** 25/28 matched (target 27)
- **Missing functions:** `default`, `fmt`, `fmt`
- **Types:** 5/5 matched (target 10)
- **Missing types:** _none_

### 13. iter

- **Target:** `similar.Iter`
- **Similarity:** 0.08
- **Dependents:** 0
- **Priority Score:** 30709.2
- **Functions:** 2/4 matched (target 5)
- **Missing functions:** `new`, `new`
- **Types:** 2/3 matched (target 2)
- **Missing types:** `Item`

### 14. utils

- **Target:** `similar.Utils`
- **Similarity:** 0.31
- **Dependents:** 0
- **Priority Score:** 21806.9
- **Functions:** 14/15 matched
- **Missing functions:** `index`
- **Types:** 2/3 matched
- **Missing types:** `Output`
- **Tests:** 1/1 matched

### 15. text.utils

- **Target:** `text.Utils`
- **Similarity:** 0.19
- **Dependents:** 0
- **Priority Score:** 10408.1
- **Functions:** 2/3 matched (target 2)
- **Missing functions:** `new`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 16. common

- **Target:** `similar.Common`
- **Similarity:** 0.36
- **Dependents:** 0
- **Priority Score:** 706.4
- **Functions:** 7/7 matched (target 8)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 1/1 matched

### 17. deadline_support

- **Target:** `similar.DeadlineSupport`
- **Similarity:** 0.13
- **Dependents:** 0
- **Priority Score:** 208.7
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

## Reexport / Wiring Modules

These files match `reexport_modules` patterns in `.ast_distance_config.json`. They are filtered out of
normal priority and missing-file ladders because they are wiring
modules, not direct logic ports. Consult them for call-site routing;
do not treat them as the next implementation target by default.

### Missing

| Source | Expected target | Deps | Source path | Expected path |
|--------|-----------------|------|-------------|---------------|
| `algorithms.mod` | `algorithms.Mod` | 0 | `algorithms/mod.rs` | `algorithms/Mod.kt` |
| `lib` | `Lib` | 0 | `lib.rs` | `Lib.kt` |
| `text.mod` | `text.Mod` | 0 | `text/mod.rs` | `text/Mod.kt` |

