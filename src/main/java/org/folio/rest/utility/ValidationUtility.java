package org.folio.rest.utility;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.MULTILINE;
import static java.util.regex.Pattern.compile;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class ValidationUtility {

  private static final String EMAIL_REGEX = "^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*(\\.\\w{2,3})+$";
  private static final String PHONE_REGEX = "^[\\+]?[(]?[0-9]{3}[)]?[-\\s\\.]?[0-9]{3}[-\\s\\.]?[0-9]{4,6}$";
  private static final String URL_REGEX = "(http(s)?:\\\\\\/\\\\\\/.)?(www\\.)?[-a-zA-Z0-9@:%._\\\\\\+~#=]{2,256}\\\\\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\\\\\+.~#?&//=]*)";

  private static final Pattern EMAIL_PATTERN = compile(EMAIL_REGEX, CASE_INSENSITIVE | MULTILINE);
  private static final Pattern PHONE_PATTERN = compile(PHONE_REGEX, CASE_INSENSITIVE | MULTILINE);
  private static final Pattern URL_PATTERN = compile(URL_REGEX, CASE_INSENSITIVE | MULTILINE);

  private ValidationUtility() {

  }

  /**
   * Check if a given string is a proper e-mail address.
   *
   * @param string The string to check.
   *
   * @return TRUE if matched, FALSE otherwise.
   */
  public static boolean isEmail(String string) {
    return EMAIL_PATTERN.matcher(string).find();
  }

  /**
   * Check if a given string might be an e-mail.
   *
   * @param string The string to check.
   *
   * @return TRUE if matched, FALSE otherwise.
   */
  public static boolean isEmailLike(String string) {
    return isEmail(string) || (StringUtils.isNotEmpty(string) && string.toLowerCase().indexOf("@") != -1);
  }

  /**
   * Check if a given string is a phone number.
   *
   * @param string The string to check.
   *
   * @return TRUE if matched, FALSE otherwise.
   */
  public static boolean isPhone(String string) {
    return PHONE_PATTERN.matcher(string).find();
  }

  /**
   * Check if a given string might be a URL.
   *
   * @param string The string to check.
   *
   * @return TRUE if matched, FALSE otherwise.
   */
  public static boolean isURLLike(String string) {
    if (string == null) {
      return false;
    }

    boolean isLikeUrl = string.toLowerCase().indexOf("www.") != -1;

    isLikeUrl = isLikeUrl || string.toLowerCase().indexOf(".org") != -1;
    isLikeUrl = isLikeUrl || string.toLowerCase().indexOf(".edu") != -1;
    isLikeUrl = isLikeUrl || string.toLowerCase().indexOf(".net") != -1;
    isLikeUrl = isLikeUrl || string.toLowerCase().indexOf(".us") != -1;
    isLikeUrl = isLikeUrl || string.toLowerCase().indexOf(".io") != -1;
    isLikeUrl = isLikeUrl || string.toLowerCase().indexOf(".co") != -1;

    return (isValidUrl(string) || isLikeUrl) && !isEmailLike(string);
  }

  /**
   * Check if a given string is a valid URL.
   *
   * @param string The string to check.
   *
   * @return TRUE if matched, FALSE otherwise.
   */
  public static boolean isValidUrl(String string) {
    if (string == null) {
      return false;
    }

    return URL_PATTERN.matcher(string).find();
  }

}
