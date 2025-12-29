#!/bin/bash
# Installation script for French Property Investment Analyzer

set -e

VERSION="${1:-1.0.0}"
INSTALL_DIR="${HOME}/.local/bin"
JAR_URL="https://github.com/jordanterry/french-mortgage-cli/releases/download/v${VERSION}/french-property-investment.jar"

echo "Installing French Property Investment Analyzer v${VERSION}..."

# Create install directory if it doesn't exist
mkdir -p "${INSTALL_DIR}"

# Download JAR
echo "Downloading JAR..."
curl -L -o "${INSTALL_DIR}/french-property-investment.jar" "${JAR_URL}"

# Create wrapper script
echo "Creating wrapper script..."
cat > "${INSTALL_DIR}/french-property-investment" <<'EOF'
#!/bin/bash
exec java -jar "$HOME/.local/bin/french-property-investment.jar" "$@"
EOF

chmod +x "${INSTALL_DIR}/french-property-investment"

echo ""
echo "✅ Installation complete!"
echo ""
echo "Make sure ${INSTALL_DIR} is in your PATH:"
echo "  export PATH=\"\$HOME/.local/bin:\$PATH\""
echo ""
echo "Test the installation:"
echo "  french-property-investment --help"
echo ""
