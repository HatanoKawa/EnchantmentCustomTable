# Config Refactor Chain Merge

Created: 2026-05-20
Archived: 2026-05-25

## Scope

This archive records the chain merge for two related changes:

- `97ef927 feat: add incremental enchantment merge mode`
- `3fad02d refactor: normalize boolean config options`

The changes started on `dev` and were propagated through the maintained version branch chain up to `dev_26.1.2`.

## Branch Chain

Completed branch order:

- `dev`
- `dev_1.21.2`
- `dev_1.21.3`
- `dev_1.21.4`
- `dev_1.21.5`
- `dev_1.21.6`
- `dev_1.21.7`
- `dev_1.21.8`
- `dev_1.21.9`
- `dev_1.21.10`
- `dev_1.21.11`
- `dev_26.1`
- `dev_26.1.1`
- `dev_26.1.2`

## Notable Merge Points

`dev_1.21.5` required GameTest registration conflict handling. The target branch's direct registration structure was preserved while adding the new tests to its registration list.

`dev_1.21.9` required menu and GameTest conflict handling around the 1.21.9 transfer-slot implementation. The merge preserved the target branch's `MenuItemStackHandler`, `ResourceHandlerSlot`, and generated-slot behavior while routing placement checks through the new shared rule path.

`dev_1.21.11` had an existing bugfix from another device:

- `084594b fix: repair wrong Class using`

That fix was preserved. The only conflict was an import-level `Config.java` collision between older 1.21.11 migration imports and the new config warning logger.

`dev_26.1` required a version-specific GameTest adaptation:

- `956e757 fix: adapt gametest click input for 26.1`

The 26.x branch uses `ContainerInput` instead of the earlier click type API.

## Validation Summary

Every branch in the chain passed at least:

- `./gradlew compileJava`

Full validation was run on the key branches:

- `dev_1.21.9`: `./gradlew test`, `./gradlew runGameTestServer`, `./gradlew build`
- `dev_1.21.11`: `./gradlew test`, `./gradlew runGameTestServer`, `./gradlew build`
- `dev_26.1.2`: `./gradlew test`, `./gradlew runGameTestServer`, `./gradlew build`

GameTest result on the key branches was 22/22 required tests passing after the config and incremental-merge additions.

## Final Branch Heads From The Merge Run

- `dev`: `3fad02d`
- `dev_1.21.2`: `17a3daa`
- `dev_1.21.3`: `d44563a`
- `dev_1.21.4`: `3ef5e94`
- `dev_1.21.5`: `8e84f58`
- `dev_1.21.6`: `4924ffa`
- `dev_1.21.7`: `7884ba9`
- `dev_1.21.8`: `c17b0c6`
- `dev_1.21.9`: `7ec1cfe`
- `dev_1.21.10`: `bc5fc5c`
- `dev_1.21.11`: `fd66fe0`
- `dev_26.1`: `956e757`
- `dev_26.1.1`: `eed6431`
- `dev_26.1.2`: `52f6f51`

## Environment Note

The 26.x branches require Java 25 through the repository `.java-version`.

During the merge run, OpenJDK 25.0.2 was installed through Homebrew and registered in jEnv as `25` so Gradle could run the 26.x validation tasks.

## Retained Behavior Checklist

- Default custom-table merge behavior still allows over-cap direct level addition.
- `enforceEnchantmentLevelLimit = true` rejects over-cap additions and merges.
- `incrementalSameLevelMerge = true` changes duplicate merges to same-level, plus-one upgrades.
- Incremental split mode generates two same-level `sourceLevel - 1` books.
- Taking an incremental split result reduces the source book by one level.
- Conversion table defaults to highest-level books.
- `convertOnlyLevelOneBook = true` limits conversion output to level-one books.
- Deprecated config options only warn when changed from their old defaults and do not control runtime behavior.
