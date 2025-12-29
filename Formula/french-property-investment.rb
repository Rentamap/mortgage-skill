class FrenchPropertyInvestment < Formula
  desc "Analyze French rental property investments with comprehensive cash flow projections"
  homepage "https://github.com/jordanterry/french-mortgage-cli"
  url "https://github.com/jordanterry/french-mortgage-cli/releases/download/v1.0.0/french-property-investment.jar"
  sha256 "8f87a8f99b1b1f37f7f58a65f74eb94d1effb6d90b9809b82e7dd4c5f43a0986"
  version "1.0.0"
  license "MIT"

  depends_on "openjdk@17"

  def install
    libexec.install "french-property-investment.jar"

    # Create wrapper script
    (bin/"french-property-investment").write <<~EOS
      #!/bin/bash
      exec "#{Formula["openjdk@17"].opt_bin}/java" -jar "#{libexec}/french-property-investment.jar" "$@"
    EOS
  end

  test do
    output = shell_output("#{bin}/french-property-investment --help 2>&1")
    assert_match "French Property Investment", output
  end
end
