---
name: kotlin-migration
description: Convert only user-specified Java source files to Kotlin during a Kotlin migration. Use when Codex must rewrite named Java files as idiomatic Kotlin while keeping them in their existing Java package directories, coexisting with remaining Java sources, preserving behavior and public contracts, and skipping compilation unless explicitly requested.
---

# Kotlin Migration

## Scope

- Treat the user-provided file paths as the complete edit scope. Do not migrate adjacent files or make unrelated cleanup changes.
- Inspect directly referenced types only as needed to preserve contracts; do not edit them unless the user includes them in the scope.
- Report a missing or ambiguous path before editing.

## Migration

- Place each `.kt` file in the same package directory as its original `.java` file. Never create or use a separate `kotlin` package or source tree solely for the migration.
- Replace the requested Java source file with its Kotlin equivalent unless the user explicitly requests otherwise.
- Preserve the package declaration, externally visible class and member names, signatures, annotations, nullability intent, framework conventions, and runtime behavior.
- Use idiomatic Kotlin where it does not alter behavior: properties where appropriate, primary constructors, Kotlin collection operations, string templates, `when`, null-safe operators, and concise expression bodies.
- Keep Java interoperability in mind. Use `@JvmStatic`, `@JvmField`, `@JvmOverloads`, or other JVM annotations only when the existing Java-facing contract requires them.
- Preserve imports and use fully qualified types only when name collisions require it.

## Verification and handoff

- Do not run compilation, tests, formatting, or build commands unless the user explicitly asks.
- Review the diff for unintended scope expansion and obvious syntax or semantic regressions.
- Respond in Korean throughout the migration, including progress updates, questions, and the final handoff.
- Summarize converted paths and any assumptions or compatibility risks in Korean so the user can perform the final review and commit.
