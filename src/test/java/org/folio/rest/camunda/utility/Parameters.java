package org.folio.rest.camunda.utility;

/**
 * Class to encapsulate parameters passed into the test method. It should
 * describe the input, expected output to assert against, and any exceptions
 * that are expected.
 */
class Parameters<I, O> {

  I input;
  O expected;
  Exception exception;

  Parameters(I input, O expected) {
    this.input = input;
    this.expected = expected;
  }

  Parameters(I input, O expected, Exception exception) {
    this.input = input;
    this.expected = expected;
    this.exception = exception;
  }

  /**
   * Return typed Parameters when input and output are expected with no
   * exceptions.
   *
   * @param <I>      generic input
   * @param <O>      generic output
   * @param input    provided to use when calling method to test
   * @param expected output to make assertions against
   * @return
   */
  public static <I, O> Parameters<I, O> of(I input, O expected) {
    return new Parameters<I, O>(input, expected);
  }

  /**
   * Return typed Parameters when input and output are expected with expected
   * exceptions. The output can be used here to assert exception message.
   *
   * @param <I>       generic input
   * @param <O>       generic output
   * @param input     provided to use when calling method to test
   * @param expected  output to make assertions against
   * @param exception an exception that expected to be thrown for input
   * @return typed Parameter
   */
  public static <I, O> Parameters<I, O> of(I input, O expected, Exception exception) {
    return new Parameters<I, O>(input, expected, exception);
  }

}
