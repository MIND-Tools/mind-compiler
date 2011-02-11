package org.ow2.mind.cli;

/**
 * Exception thrown when an error on the command line has been detected.
 */
public class InvalidCommandLineException extends Exception {

  protected final int exitValue;

  /**
   * @param message detail message.
   * @param exitValue exit value.
   */
  public InvalidCommandLineException(final String message, final int exitValue) {
    super(message);
    this.exitValue = exitValue;
  }

  /**
   * @return the exit value.
   */
  public int getExitValue() {
    return exitValue;
  }
}