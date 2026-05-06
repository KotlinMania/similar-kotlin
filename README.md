# similar-kotlin in Kotlin

[![GitHub link](https://img.shields.io/badge/GitHub-KotlinMania%2Fsimilar--kotlin-blue.svg)](https://github.com/KotlinMania/similar-kotlin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/similar-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/similar-kotlin)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/similar-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/similar-kotlin/actions)

This is a Kotlin Multiplatform line-by-line transliteration port of [`mitsuhiko/similar`](https://github.com/mitsuhiko/similar).

**Original Project:** This port is based on [`mitsuhiko/similar`](https://github.com/mitsuhiko/similar). All design credit and project intent belong to the upstream authors; this repository is a faithful port to Kotlin Multiplatform with no behavioural changes intended.

### Porting status

This is an **in-progress port**. The goal is feature parity with the upstream Rust crate while providing a native Kotlin Multiplatform API. Every Kotlin file carries a `// port-lint: source <path>` header naming its upstream Rust counterpart so the AST-distance tool can track provenance.

---

## Upstream README — `mitsuhiko/similar`

> The text below is reproduced and lightly edited from [`https://github.com/mitsuhiko/similar`](https://github.com/mitsuhiko/similar). It is the upstream project's own description and remains under the upstream authors' authorship; links have been rewritten to absolute upstream URLs so they continue to resolve from this repository.

## Similar: A Diffing Library

[![Crates.io](https://img.shields.io/crates/d/similar.svg)](https://crates.io/crates/similar)
[![License](https://img.shields.io/github/license/mitsuhiko/similar)](https://github.com/mitsuhiko/similar/blob/main/LICENSE)
[![Documentation](https://docs.rs/similar/badge.svg)](https://docs.rs/similar)

Similar is a dependency free crate for Rust that implements different diffing
algorithms and high level interfaces for it.  It was originally inspired by
[pijul](https://pijul.org/)'s diff library but has since changed significantly.
This library was built for the [insta snapshot testing
library](https://insta.rs).

```rust
use similar::{ChangeTag, TextDiff};

fn main() {
    let diff = TextDiff::from_lines(
        "Hello World\nThis is the second line.\nThis is the third.",
        "Hallo Welt\nThis is the second line.\nThis is life.\nMoar and more",
    );

    for change in diff.iter_all_changes() {
        let sign = match change.tag() {
            ChangeTag::Delete => "-",
            ChangeTag::Insert => "+",
            ChangeTag::Equal => " ",
        };
        print!("{}{}", sign, change);
    }
}
```

## Screenshot

![terminal highlighting](https://raw.githubusercontent.com/mitsuhiko/similar/main/assets/terminal-inline.png)

## What's in the box?

* Myers' diff
* Patience diff
* Hunt-style diff
* Histogram diff
* Classic LCS table diff
* Diffing on arbitrary comparable sequences
* Line, word, character and grapheme level diffing
* Text and Byte diffing
* Unified diff generation

## no_std Support

`similar` now enables `std` by default.

For `no_std + alloc` usage:

```toml
[dependencies]
similar = { version = "3", default-features = false }
```

Backend selection in `no_std` mode:

- default (`default-features = false`): `alloc::collections::BTreeMap`
- `default-features = false, features = ["hashbrown"]`: `hashbrown::HashMap`

## Related Projects

* [insta](https://insta.rs) snapshot testing library
* [similar-asserts](https://github.com/mitsuhiko/similar-asserts) assertion library

## License and Links

* [Documentation](https://docs.rs/similar/)
* [Upgrading Guide (2.7 to 3.0)](https://github.com/mitsuhiko/similar/blob/HEAD/UPGRADING.md)
* [Issue Tracker](https://github.com/mitsuhiko/similar/issues)
* [Examples](https://github.com/mitsuhiko/similar/tree/main/examples)
* License: [Apache-2.0](https://github.com/mitsuhiko/similar/blob/main/LICENSE)

---

## About this Kotlin port

### Installation

```kotlin
dependencies {
    implementation("io.github.kotlinmania:similar-kotlin:0.1.0-SNAPSHOT")
}
```

### Building

```bash
./gradlew build
./gradlew test
```

### Targets

- macOS arm64
- Linux x64
- Windows mingw-x64
- iOS arm64 / simulator-arm64 (Swift export + XCFramework)
- JS (browser + Node.js)
- Wasm-JS (browser + Node.js)
- Android (API 24+)

### Porting guidelines

See [AGENTS.md](AGENTS.md) and [CLAUDE.md](CLAUDE.md) for translator discipline, port-lint header convention, and Rust → Kotlin idiom mapping.

### License

This Kotlin port is distributed under the same Apache-2.0 license as the upstream [`mitsuhiko/similar`](https://github.com/mitsuhiko/similar). See [LICENSE](LICENSE) (and any sibling `LICENSE-*` / `NOTICE` files mirrored from upstream) for the full text.

Original work copyrighted by the similar authors.  
Kotlin port: Copyright (c) 2026 Sydney Renee and The Solace Project.

### Acknowledgments

Thanks to the [`mitsuhiko/similar`](https://github.com/mitsuhiko/similar) maintainers and contributors for the original Rust implementation. This port reproduces their work in Kotlin Multiplatform; bug reports about upstream design or behavior should go to the upstream repository.
