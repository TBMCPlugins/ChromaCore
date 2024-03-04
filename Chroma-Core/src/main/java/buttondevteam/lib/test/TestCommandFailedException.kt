package buttondevteam.lib.test

/**
 * Indicates that the command execution failed for some reason. Note that this doesn't necessarily mean the test failed.
 */
class TestCommandFailedException(message: String) : Exception(message)
