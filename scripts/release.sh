#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_DIR"

VERSION_FILE="version.properties"
FORMULA_FILE="Formula/french-property-investment.rb"
JAR_PATH="build/libs/french-property-investment.jar"

usage() {
    cat <<EOF
Usage: $0 <new-version>

Automated release script for french-property-investment

Arguments:
  new-version    Semantic version (e.g., 1.0.1, 1.1.0, 2.0.0)

Steps performed:
  1. Update version in version.properties
  2. Build and test the project
  3. Calculate SHA256 of JAR
  4. Update Homebrew formula
  5. Commit changes
  6. Create and push git tag
  7. Create GitHub release with JAR
  8. Update Homebrew tap repository

Example:
  $0 1.0.1

EOF
    exit 1
}

if [ $# -ne 1 ]; then
    usage
fi

NEW_VERSION="$1"

if ! [[ "$NEW_VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "Error: Version must be in semver format (e.g., 1.0.1)"
    exit 1
fi

echo "=========================================="
echo "Release Process for v${NEW_VERSION}"
echo "=========================================="
echo ""

echo "Step 1: Checking working directory is clean..."
if ! git diff-index --quiet HEAD --; then
    echo "Error: Working directory has uncommitted changes. Please commit or stash them first."
    exit 1
fi
echo "✓ Working directory is clean"
echo ""

echo "Step 2: Updating version in ${VERSION_FILE}..."
echo "version=${NEW_VERSION}" > "$VERSION_FILE"
echo "✓ Version updated to ${NEW_VERSION}"
echo ""

echo "Step 3: Building and testing..."
JAVA_HOME=/opt/homebrew/opt/openjdk ./gradlew clean test shadowJar
echo "✓ Build and tests passed"
echo ""

echo "Step 4: Calculating SHA256..."
if [ ! -f "$JAR_PATH" ]; then
    echo "Error: JAR not found at $JAR_PATH"
    exit 1
fi
SHA256=$(shasum -a 256 "$JAR_PATH" | awk '{print $1}')
echo "✓ SHA256: $SHA256"
echo ""

echo "Step 5: Updating Homebrew formula..."
sed -i '' "s/version \".*\"/version \"${NEW_VERSION}\"/" "$FORMULA_FILE"
sed -i '' "s|releases/download/v.*/french-property-investment.jar|releases/download/v${NEW_VERSION}/french-property-investment.jar|" "$FORMULA_FILE"
sed -i '' "s/sha256 \".*\"/sha256 \"${SHA256}\"/" "$FORMULA_FILE"
echo "✓ Formula updated"
echo ""

echo "Step 6: Committing changes..."
git add "$VERSION_FILE" "$FORMULA_FILE"
git commit -m "Release v${NEW_VERSION}

- Update version to ${NEW_VERSION}
- Update Homebrew formula with new SHA256

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
echo "✓ Changes committed"
echo ""

echo "Step 7: Creating git tag..."
git tag -a "v${NEW_VERSION}" -m "Release v${NEW_VERSION}

See release notes at https://github.com/jordanterry/french-mortgage-cli/releases/tag/v${NEW_VERSION}

🤖 Generated with [Claude Code](https://claude.com/claude-code)"
echo "✓ Tag created: v${NEW_VERSION}"
echo ""

echo "Step 8: Pushing to GitHub..."
git push origin main
git push origin "v${NEW_VERSION}"
echo "✓ Pushed to GitHub"
echo ""

echo "Step 9: Creating GitHub release..."
gh release create "v${NEW_VERSION}" \
    "$JAR_PATH" \
    --title "v${NEW_VERSION}" \
    --notes "## Release v${NEW_VERSION}

See the [CHANGELOG](https://github.com/jordanterry/french-mortgage-cli/blob/main/CHANGELOG.md) for details.

### Installation

**Homebrew:**
\`\`\`bash
brew tap jordanterry/tap
brew install french-property-investment
\`\`\`

Or upgrade:
\`\`\`bash
brew update
brew upgrade french-property-investment
\`\`\`

**Manual:**
\`\`\`bash
wget https://github.com/jordanterry/french-mortgage-cli/releases/download/v${NEW_VERSION}/french-property-investment.jar
java -jar french-property-investment.jar --help
\`\`\`

🤖 Generated with [Claude Code](https://claude.com/claude-code)"
echo "✓ GitHub release created"
echo ""

echo "Step 10: Updating Homebrew tap..."
TAP_DIR="../homebrew-tap"
if [ ! -d "$TAP_DIR" ]; then
    echo "Warning: Homebrew tap directory not found at $TAP_DIR"
    echo "Please update manually:"
    echo "  cd ../homebrew-tap"
    echo "  cp ../french-mortgage-cli/Formula/french-property-investment.rb Formula/"
    echo "  git add Formula/"
    echo "  git commit -m 'Update french-property-investment to v${NEW_VERSION}'"
    echo "  git push"
else
    cd "$TAP_DIR"
    cp "$PROJECT_DIR/$FORMULA_FILE" Formula/
    git add Formula/
    git commit -m "Update french-property-investment to v${NEW_VERSION}

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
    git push
    cd "$PROJECT_DIR"
    echo "✓ Homebrew tap updated"
fi
echo ""

echo "=========================================="
echo "Release v${NEW_VERSION} Complete! 🎉"
echo "=========================================="
echo ""
echo "Release URL: https://github.com/jordanterry/french-mortgage-cli/releases/tag/v${NEW_VERSION}"
echo ""
echo "Users can now install with:"
echo "  brew update && brew upgrade french-property-investment"
echo ""
