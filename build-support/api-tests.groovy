def command = xalanTestCommand
def repo = new File(project.basedir, '..').canonicalFile
def results = new File(repo, 'results-api')

if (command == 'prepare-api') {
    def raw = new Properties()
    new File(repo, 'test.properties').withInputStream { raw.load(it) }
    def selected = new Properties()
    raw.stringPropertyNames().each { key ->
        if (key.startsWith('qetest.')) {
            selected.setProperty(key.substring('qetest.'.length()), raw.getProperty(key))
        }
    }
    raw.stringPropertyNames().each { key ->
        if (key.startsWith('api.')) {
            selected.setProperty(key.substring('api.'.length()), raw.getProperty(key))
        }
    }
    def out = new File(project.build.directory, 'api-tests.properties')
    out.parentFile.mkdirs()
    out.withOutputStream { selected.store(it, 'API tests selected from test.properties') }
    results.mkdirs()
} else if (command == 'verify-api') {
    def expected = [
            'Pass-DOMResultAPITest.xml',
            'Pass-DOMSourceAPITest.xml',
            'Pass-DTMDumpTest.xml',
            'Pass-EmbeddedStylesheetTest.xml',
            'Pass-ErrorListenerAPITest.xml',
            'Pass-ErrorListenerTest.xml',
            'Pass-SAXResultAPITest.xml',
            'Pass-SAXSourceAPITest.xml',
            'Pass-SAXTransformerFactoryAPITest.xml',
            'Pass-SerializedStylesheetTest.xml',
            'Pass-SystemIDResolverAPITest.xml',
            'Pass-TemplatesHandlerAPITest.xml',
            'Pass-TestDTM.xml',
            'Pass-TestDTMIter.xml',
            'Pass-TestDTMTrav.xml',
            'Pass-TestXPathAPI.xml',
            'Pass-TraceListenerTest.xml',
            'Pass-TransformerFactoryAPITest.xml',
            'Pass-TransformerHandlerAPITest.xml',
            'Pass-TransformerHandlerTest.xml',
            'Pass-TransformStateAPITest.xml',
            'Pass-URIResolverTest.xml'
    ]
    def missing = expected.findAll { !new File(results, it).isFile() }
    if (missing) {
        throw new IllegalStateException("API tests did not write ${missing}")
    }
    results.listFiles().findAll { it.name.startsWith('Pass-') && it.name.endsWith('.xml') }.each { file ->
        def status = file.readLines('UTF-8').collect { it.trim() }.find { it in ['Pass', 'Fail', 'Ambg', 'Errr', 'Incp'] }
        if (status != 'Pass') {
            throw new IllegalStateException("${file.name} teststatus is '${status}'")
        }
    }
} else {
    throw new IllegalArgumentException("Unknown command ${command}")
}
