#!/bin/bash

# Parse the coverage data and calculate package-wise coverage
declare -A package_lines
declare -A package_covered

while read -r line; do
  if [[ $line == mode:* ]] || [[ -z $line ]]; then
    continue
  fi
  file_path=$(echo $line | awk '{print $1}')
  stats=$(echo $line | awk '{print $2}')
  package=$(dirname $file_path | cut -d/ -f1)

  lines=$(echo $stats | cut -d, -f1 | cut -d: -f3)
  covered=$(echo $stats | cut -d, -f2)

  package_lines[$package]=$(( ${package_lines[$package]:-0} + lines ))
  package_covered[$package]=$(( ${package_covered[$package]:-0} + covered ))
done < merged_coverage.out

# Calculate coverage percentages for each package
declare -A package_coverage
for package in "${!package_lines[@]}"; do
  total_lines=${package_lines[$package]}
  total_covered=${package_covered[$package]}
  package_coverage[$package]=$(awk "BEGIN {print ($total_covered/$total_lines) * 100}")
done

# Modify the HTML file
html_file="coverageAcceptance.html"
tmp_file=$(mktemp)

# Add the new dropdown option and the package-wise coverage div
awk -v pkg_coverage="$(declare -p package_coverage)" '
BEGIN {
  split(pkg_coverage, pkg_arr, " ")
  sub(/\(/, "", pkg_arr[1])
  sub(/\)/, "", pkg_arr[length(pkg_arr)])
}
{
  print $0
  if (/id="coverage-dropdown"/) {
    print "<option value=\"package\">Package Wise Coverage Report</option>"
  }
  if (/<\/body>/) {
    print "<div id=\"package-coverage\" style=\"display:none;\">"
    for (pkg in pkg_arr) {
      split(pkg_arr[pkg], kv, "=")
      print "<p>" kv[1] ": " kv[2] "%</p>"
    }
    print "</div>"
    print "<script>"
    print "document.getElementById('coverage-dropdown').addEventListener('change', function() {"
    print "  var selectedOption = this.value;"
    print "  var packageCoverageDiv = document.getElementById('package-coverage');"
    print "  if (selectedOption === 'package') {"
    print "    packageCoverageDiv.style.display = 'block';"
    print "  } else {"
    print "    packageCoverageDiv.style.display = 'none';"
    print "  }"
    print "});"
    print "</script>"
  }
}' "$html_file" > "$tmp_file"

# Replace the original HTML file with the modified version
mv "$tmp_file" "$html_file"
