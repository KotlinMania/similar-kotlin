# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 20/20 (100.0%)
- **Function parity:** 218/249 matched (target 274) — 87.6%
- **Class/type parity:** 31/50 matched (target 58) — 62.0%
- **Combined symbol parity:** 249/299 matched (target 332) — 83.3%
- **Average inline-code cosine:** 0.54 (function body across 17 matched files)
- **Average documentation cosine:** 0.75 (doc text across 17 matched files)
- **Cheat-zeroed Files:** 2
- **Critical Issues:** 16 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. algorithms.capture

- **Target:** `algorithms.Capture`
- **Similarity:** 0.69
- **Dependents:** 2
- **Priority Score:** 2011103.1
- **Functions:** 9/9 matched
- **Missing functions:** _none_
- **Types:** 1/2 matched
- **Missing types:** `Error`
- **Tests:** 1/1 matched

### 2. algorithms.replace

- **Target:** `algorithms.Replace`
- **Similarity:** 0.58
- **Dependents:** 1
- **Priority Score:** 1011504.1
- **Functions:** 13/13 matched
- **Missing functions:** _none_
- **Types:** 1/2 matched
- **Missing types:** `Error`
- **Tests:** 2/2 matched

### 3. algorithms.compact

- **Target:** `algorithms.Compact`
- **Similarity:** 0.87
- **Dependents:** 1
- **Priority Score:** 1011301.2
- **Functions:** 11/11 matched (target 13)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Error`

### 4. algorithms.myers

- **Target:** `algorithms.Myers`
- **Similarity:** 0.40
- **Dependents:** 0
- **Priority Score:** 92206.0
- **Functions:** 12/17 matched
- **Missing functions:** `diff`, `diff_deadline`, `new`, `index`, `index_mut`
- **Types:** 1/5 matched (target 3)
- **Missing types:** `Output`, `SlowIndex`, `HasRunFinish`, `Error`
- **Tests:** 6/6 matched

### 5. algorithms.utils

- **Target:** `algorithms.Utils`
- **Similarity:** 0.51
- **Dependents:** 0
- **Priority Score:** 72404.9
- **Functions:** 14/19 matched
- **Missing functions:** `fmt`, `eq`, `index`, `new`, `hash`
- **Types:** 3/5 matched (target 4)
- **Missing types:** `Output`, `Key`
- **Tests:** 4/4 matched

### 6. text.abstraction

- **Target:** `text.Abstraction`
- **Similarity:** 0.24
- **Dependents:** 0
- **Priority Score:** 52507.6
- **Functions:** 18/22 matched (target 21)
- **Missing functions:** `test_split_lines_bytes`, `test_split_words_bytes`, `test_split_chars_bytes`, `test_split_graphemes_bytes`
- **Types:** 2/3 matched (target 4)
- **Missing types:** `Output`
- **Tests:** 4/8 matched

### 7. algorithms.patience

- **Target:** `algorithms.Patience`
- **Similarity:** 0.47
- **Dependents:** 0
- **Priority Score:** 51005.3
- **Functions:** 5/7 matched (target 9)
- **Missing functions:** `diff`, `diff_deadline`
- **Types:** 0/3 matched
- **Missing types:** `Patience`, `Error`, `HasRunFinish`
- **Tests:** 3/3 matched

### 8. text.mod

- **Target:** `text.Mod [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 44510.0
- **Functions:** 38/42 matched
- **Missing functions:** `test_captured_word_ops`, `test_lifetimes_on_iter`, `test_serde`, `test_serde_ops`
- **Types:** 3/3 matched (target 6)
- **Missing types:** _none_
- **Tests:** 8/12 matched

### 9. text.inline

- **Target:** `text.Inline`
- **Similarity:** 0.44
- **Dependents:** 0
- **Priority Score:** 41905.6
- **Functions:** 13/16 matched
- **Missing functions:** `index`, `fmt`, `test_serde`
- **Types:** 2/3 matched
- **Missing types:** `Output`
- **Tests:** 1/2 matched

### 10. algorithms.lcs

- **Target:** `algorithms.Lcs`
- **Similarity:** 0.43
- **Dependents:** 0
- **Priority Score:** 41305.7
- **Functions:** 9/11 matched (target 14)
- **Missing functions:** `diff`, `diff_deadline`
- **Types:** 0/2 matched
- **Missing types:** `HasRunFinish`, `Error`
- **Tests:** 7/7 matched

### 11. types

- **Target:** `similar.Types`
- **Similarity:** 0.57
- **Dependents:** 0
- **Priority Score:** 23104.3
- **Functions:** 24/26 matched (target 27)
- **Missing functions:** `default`, `fmt`
- **Types:** 5/5 matched (target 10)
- **Missing types:** _none_

### 12. utils

- **Target:** `similar.Utils`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 21702.4
- **Functions:** 13/14 matched (target 15)
- **Missing functions:** `index`
- **Types:** 2/3 matched
- **Missing types:** `Output`
- **Tests:** 1/1 matched

### 13. iter

- **Target:** `similar.Iter`
- **Similarity:** 0.31
- **Dependents:** 0
- **Priority Score:** 20506.9
- **Functions:** 1/2 matched (target 5)
- **Missing functions:** `new`
- **Types:** 2/3 matched (target 2)
- **Missing types:** `Item`

### 14. udiff

- **Target:** `similar.Udiff`
- **Similarity:** 0.50
- **Dependents:** 0
- **Priority Score:** 12205.0
- **Functions:** 16/17 matched (target 26)
- **Missing functions:** `fmt`
- **Types:** 5/5 matched (target 6)
- **Missing types:** _none_
- **Tests:** 3/3 matched

### 15. algorithms.hook

- **Target:** `algorithms.Hook`
- **Similarity:** 0.58
- **Dependents:** 0
- **Priority Score:** 11004.2
- **Functions:** 7/7 matched (target 12)
- **Missing functions:** _none_
- **Types:** 2/3 matched (target 5)
- **Missing types:** `Error`
- **Lint issues:** 6

### 16. text.utils

- **Target:** `text.Utils`
- **Similarity:** 0.52
- **Dependents:** 0
- **Priority Score:** 10404.8
- **Functions:** 2/3 matched (target 2)
- **Missing functions:** `new`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 17. common

- **Target:** `similar.Common`
- **Similarity:** 0.82
- **Dependents:** 0
- **Priority Score:** 701.8
- **Functions:** 7/7 matched (target 8)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 1/1 matched

### 18. algorithms.mod

- **Target:** `algorithms.Mod [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 410.0
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 19. deadline_support

- **Target:** `similar.DeadlineSupport`
- **Similarity:** 0.52
- **Dependents:** 0
- **Priority Score:** 204.8
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 20. lib

- **Target:** `similar.Lib [STUB]`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 0.0
- **Functions:** 0/0 matched
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

## Next Commands

```bash
# Initialize task queue for systematic porting
cd tools/ast_distance
./ast_distance --init-tasks ../../tmp/similar/src rust ../../src/commonMain/kotlin/io/github/kotlinmania/similar kotlin tasks.json ../../AGENTS.md

# Get next high-priority task
./ast_distance --assign tasks.json <agent-id>
```
