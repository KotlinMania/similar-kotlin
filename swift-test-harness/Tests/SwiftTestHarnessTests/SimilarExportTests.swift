import Testing
import Similar

@Suite struct SimilarExportTests {
    @Test func testSwiftModuleLoads() throws {
        #expect(Bool(true), "Similar swift module imported cleanly")
    }
}
