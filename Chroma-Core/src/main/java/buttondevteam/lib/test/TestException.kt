package buttondevteam.lib.test

/**
 * An exception that occurs just because we're running tests. It's used to test for various error cases.
 */
class TestException(message: String, cause: Throwable) : Exception(message, cause)
