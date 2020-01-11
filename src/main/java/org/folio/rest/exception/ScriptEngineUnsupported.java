package org.folio.rest.exception;

public class ScriptEngineUnsupported extends Exception {

  private static final long serialVersionUID = 2176459311584173842L;

  private static final String MESSAGE = "The Scripting Engine, %s, is not supported.";

  public ScriptEngineUnsupported(String extension) {
    super(String.format(MESSAGE, extension));
  }

}