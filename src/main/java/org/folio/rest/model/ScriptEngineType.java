package org.folio.rest.model;

/**
 * Languages supported for use by scripting engine.
 *
 * This alone does not necessarily make the language available.
 * Additional changes to the pom.xml may be necessary to get any particular language working.
 */
public enum ScriptEngineType {
  NONE(null),
  GROOVY("groovy"),
  JAVA("java"),
  JS("js"),
  PERL("pl"),
  PYTHON("py"),
  RUBY("rb");

  public final String extension;

  /**
   * Initialize the scripting engine.
   *
   * @param extension
   *   The language extension name, lower cased.
   */
  private ScriptEngineType(String extension) {
      this.extension = extension;
  }

}