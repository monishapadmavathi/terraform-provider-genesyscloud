import groovy.json.JsonBuilder

def parseCoverage(coverageFile) {
    def coverageData = new File(coverageFile).text
    def packages = [:]
    
    coverageData.eachLine { line ->
        if (line.startsWith("mode:")) return // Skip mode line

        def parts = line.split(" ")
        def filePath = parts[0]
        def packageName = filePath.substring(0, filePath.lastIndexOf("/"))
        def coverage = parts[1..-1].join(" ")

        if (!packages.containsKey(packageName)) {
            packages[packageName] = []
        }
        packages[packageName] << [file: filePath, coverage: coverage]
    }
    
    return packages
}

def packages = parseCoverage('coverage.out')

def json = new JsonBuilder(packages).toPrettyString()
new File('coverage.json').write(json)
