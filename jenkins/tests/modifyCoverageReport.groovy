import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

// Load the generated HTML file
def htmlFile = new File('coverageAcceptance.html')
def document = Jsoup.parse(htmlFile, 'UTF-8')

// Find the dropdown menu
def dropdownMenu = document.select('select#coverage-dropdown').first()

// Add a new option for package-wise coverage report
dropdownMenu.append('<option value="package">Package Wise Coverage Report</option>')

// Calculate package-wise coverage percentages
def coverageData = parseCoverageData('merged_coverage.out')
def packageWiseCoverage = calculatePackageWiseCoverage(coverageData)

// Function to parse coverage data
def parseCoverageData(coverageFile) {
    def coverageData = [:] // Map to store coverage data
    new File(coverageFile).eachLine { line ->
        if (line.startsWith('mode:') || line.trim().isEmpty()) {
            return
        }
        def parts = line.split(' ')
        def fileName = parts[0]
        def coverageInfo = parts[1..-1]
        coverageData[fileName] = coverageInfo
    }
    return coverageData
}

// Function to calculate package-wise coverage
def calculatePackageWiseCoverage(coverageData) {
    def packageCoverage = [:]
    coverageData.each { file, coverage ->
        def packageName = file.split('/')[0]
        if (!packageCoverage.containsKey(packageName)) {
            packageCoverage[packageName] = [lines: 0, covered: 0]
        }
        coverage.each { covInfo ->
            def (start, end, statements, covered) = covInfo.split(':')
            packageCoverage[packageName].lines += statements.toInteger()
            packageCoverage[packageName].covered += covered.toInteger()
        }
    }
    def packageWiseCoverage = packageCoverage.collectEntries { pkg, stats ->
        [(pkg): (stats.covered / stats.lines) * 100]
    }
    return packageWiseCoverage
}

// Add a div to display package-wise coverage
def packageCoverageDiv = document.body().appendElement('div')
packageCoverageDiv.attr('id', 'package-coverage')
packageCoverageDiv.attr('style', 'display:none;')

packageWiseCoverage.each { pkg, percentage ->
    packageCoverageDiv.appendElement('p').text("${pkg}: ${percentage.round(2)}%")
}

// Add JavaScript to handle dropdown change event
def script = """
<script>
    document.getElementById('coverage-dropdown').addEventListener('change', function() {
        var selectedOption = this.value;
        var packageCoverageDiv = document.getElementById('package-coverage');
        if (selectedOption === 'package') {
            packageCoverageDiv.style.display = 'block';
        } else {
            packageCoverageDiv.style.display = 'none';
        }
    });
</script>
"""
document.body().append(script)

// Save the modified HTML file
new File('modified_coverageAcceptance.html').text = document.html()
