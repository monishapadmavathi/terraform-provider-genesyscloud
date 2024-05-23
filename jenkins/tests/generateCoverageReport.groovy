import groovy.json.JsonSlurper

def packages = new JsonSlurper().parse(new File('coverage.json'))

def html = new StringBuilder()
html.append("""
<html>
<head>
    <style>
        body { font-family: Arial, sans-serif; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
        .package { cursor: pointer; }
        .file { display: none; }
        .coverage-details { margin-left: 20px; padding: 10px; border: 1px solid #ccc; background-color: #f9f9f9; }
    </style>
    <script>
        function toggleFiles(packageName) {
            var files = document.querySelectorAll('.' + packageName);
            files.forEach(function(file) {
                file.style.display = file.style.display === 'none' ? 'table-row' : 'none';
            });
        }
        
        function toggleCoverage(fileName) {
            var coverageDetails = document.getElementById(fileName);
            coverageDetails.style.display = coverageDetails.style.display === 'none' ? 'block' : 'none';
        }
    </script>
</head>
<body>
    <h2>Coverage Report</h2>
    <table>
        <tr>
            <th>Package</th>
            <th>Coverage</th>
        </tr>
""")

packages.each { packageName, files ->
    def packageCoverage = files.size() // Calculate package coverage percentage if needed
    html.append("""
    <tr class="package" onclick="toggleFiles('${packageName}')">
        <td>${packageName}</td>
        <td>${packageCoverage}%</td>
    </tr>
    """)
    
    files.each { file ->
        html.append("""
        <tr class="file ${packageName}">
            <td>${file.file}</td>
            <td>${file.coverage}</td>
        </tr>
        <tr class="file ${packageName}">
            <td colspan="2">
                <div id="${file.file}" class="coverage-details" style="display: none;">
                    <!-- Placeholder for coverage details -->
                    Coverage details for ${file.file} will go here.
                </div>
            </td>
        </tr>
        """)
    }
}

html.append("""
    </table>
</body>
</html>
""")

new File('coverage-report.html').write(html.toString())
