# Release Process

This document describes how to create a new release of the French Property Investment Analyzer.

## Prerequisites

- Git repository is clean (no uncommitted changes)
- All tests pass
- You have push access to the GitHub repository
- GitHub CLI (`gh`) is installed and authenticated
- Homebrew tap repository is cloned at `../homebrew-tap`

## Automated Release

The entire release process is automated via a single script:

```bash
./scripts/release.sh <version>
```

### Example

```bash
./scripts/release.sh 1.0.1
```

### What the Script Does

The release script performs these steps automatically:

1. **Validates working directory** - Ensures no uncommitted changes
2. **Updates version** - Modifies `version.properties` with new version
3. **Builds and tests** - Runs `./gradlew clean test shadowJar`
4. **Calculates SHA256** - Computes hash of the JAR file
5. **Updates Homebrew formula** - Updates version and SHA256 in `Formula/french-property-investment.rb`
6. **Commits changes** - Commits version and formula updates
7. **Creates git tag** - Tags the release as `v<version>`
8. **Pushes to GitHub** - Pushes commits and tag
9. **Creates GitHub release** - Creates release with JAR attachment
10. **Updates Homebrew tap** - Updates the tap repository with new formula

### Version Format

Versions must follow semantic versioning:

- **MAJOR.MINOR.PATCH** (e.g., `1.0.1`, `1.1.0`, `2.0.0`)

Examples:
- `1.0.1` - Patch release (bug fixes)
- `1.1.0` - Minor release (new features, backwards compatible)
- `2.0.0` - Major release (breaking changes)

## Manual Release (Not Recommended)

If you need to release manually, follow these steps:

### 1. Update Version

Edit `version.properties`:
```properties
version=1.0.1
```

### 2. Build and Test

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk ./gradlew clean test shadowJar
```

### 3. Calculate SHA256

```bash
shasum -a 256 build/libs/french-property-investment.jar
```

### 4. Update Formula

Edit `Formula/french-property-investment.rb`:
- Update `version` line
- Update `url` with new version
- Update `sha256` with calculated hash

### 5. Commit and Tag

```bash
git add version.properties Formula/french-property-investment.rb
git commit -m "Release v1.0.1"
git tag -a v1.0.1 -m "Release v1.0.1"
git push origin main
git push origin v1.0.1
```

### 6. Create GitHub Release

```bash
gh release create v1.0.1 \
  build/libs/french-property-investment.jar \
  --title "v1.0.1" \
  --notes "Release notes here"
```

### 7. Update Homebrew Tap

```bash
cd ../homebrew-tap
cp ../french-mortgage-cli/Formula/french-property-investment.rb Formula/
git add Formula/
git commit -m "Update french-property-investment to v1.0.1"
git push
```

## Post-Release

After a release:

1. **Verify installation** - Test that users can install via Homebrew:
   ```bash
   brew update
   brew upgrade french-property-investment
   french-property-investment --help
   ```

2. **Update CHANGELOG** - Document changes in `CHANGELOG.md` (if exists)

3. **Announce** - Share the release on relevant channels

## Troubleshooting

### Build Fails

Ensure Java 17 is being used:
```bash
JAVA_HOME=/opt/homebrew/opt/openjdk ./gradlew clean test shadowJar
```

### Formula SHA256 Mismatch

Recalculate and update:
```bash
shasum -a 256 build/libs/french-property-investment.jar
# Update Formula/french-property-investment.rb with new hash
```

### GitHub Release Failed

Create manually:
```bash
gh release create v<version> build/libs/french-property-investment.jar
```

### Homebrew Tap Not Found

The script expects the tap repository at `../homebrew-tap`. Either:
- Clone it: `cd .. && git clone https://github.com/jordanterry/homebrew-tap.git`
- Update manually as shown in the script's warning message

## Version Storage

The project version is stored in `version.properties`:

```properties
version=1.0.0
```

This single source of truth is read by:
- `build.gradle.kts` - Sets the project version
- `scripts/release.sh` - Updates during release

The Homebrew formula (`Formula/french-property-investment.rb`) is automatically updated by the release script.
