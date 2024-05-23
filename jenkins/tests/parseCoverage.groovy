import groovy.json.JsonOutput

def parseCoverageData(String filePath) {
    def coverageData = [:]
    def currentPackage = ""
    def currentPackageFiles = []
    def totalStatements = 0
    def totalCovered = 0

    new File(filePath).eachLine { line ->
        if (line.startsWith("mode:")) {
            // Skip the mode line
        } else if (line.contains(".go:")) {
            def parts = line.split("\\s+")
            def packageFile = parts[0].substring(0, parts[0].lastIndexOf(":"))
            def coverageInfo = parts[1]
            def coverageParts = coverageInfo.split("/")
            def statements = Integer.parseInt(coverageParts[1])
            def covered = Integer.parseInt(coverageParts[0])

            def packageName = packageFile.substring(0, packageFile.lastIndexOf("/"))
            def fileName = packageFile.substring(packageFile.lastIndexOf("/") + 1)

            if (!coverageData.containsKey(packageName)) {
                coverageData[packageName] = [
                    files: [:],
                    statements: 0,
                    covered: 0
                ]
            }

            coverageData[packageName].files[fileName] = [
                statements: statements,
                covered: covered
            ]
            coverageData[packageName].statements += statements
            coverageData[packageName].covered += covered

            totalStatements += statements
            totalCovered += covered
        }
    }

    coverageData.each { packageName, data ->
        data.coverage = data.covered / data.statements * 100
        data.files.each { fileName, fileData ->
            fileData.coverage = fileData.covered / fileData.statements * 100
        }
    }

    return [coverageData: coverageData, totalCoverage: totalCovered / totalStatements * 100]
}

def coverageReport = parseCoverageData("coverage.out")
def jsonOutput = JsonOutput.toJson(coverageReport)

new File("coverage.json").write(jsonOutput)
