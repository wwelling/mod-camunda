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

  public static <I, O> Parameters<I, O> of(I input, O expected) {
    return new Parameters<I, O>(input, expected);
  }

  public static <I, O> Parameters<I, O> of(I input, O expected, Exception exception) {
    return new Parameters<I, O>(input, expected, exception);
  }

}
