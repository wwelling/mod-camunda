package org.folio.rest.utility;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class FormatUtility {

  private static final Logger log = LoggerFactory.getLogger(FormatUtility.class);

  private static final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

  private FormatUtility() {

  }

  /**
   * Escape the text to ensure it can be safely used in CQL.
   *
   * This will escape the CQL and further escape CQL as a URL argument.
   *
   * Perform the most minimalistic URL escaping possible for a single URL Argument.
   *
   * This only escapes what is necessary when passing a URL to an endpoint.
   * So far this only appears to be the ampersand character.
   *
   * Use this instead of URLEncoder.encode(url, StandardCharsets.UTF_8); for when CQL is involved.
   *
   * @param text The text to normalize.
   * @return The normalized text for use inside the CQL as a value.
   *
   * @see https://github.com/folio-org/raml-module-builder/blob/2c39990c96c22262b02c98dd2b51cbeedc90fb9d/util/src/main/java/org/folio/util/StringUtil.java#L39
   */
  public static String normalizeCqlUrlArgument(String text) {
    if (text == null) {
      return "\"\"";
    }

    StringBuilder builder = new StringBuilder(text.length() + 2);

    builder.append('"');
    if (text != null) {
      for (int i = 0; i < text.length(); i++) {
        char c = text.charAt(i);

        switch (c) {
        case '\\':
        case '*':
        case '?':
        case '^':
        case '"':
          builder.append('\\').append(c);
          break;

        default:
          builder.append(c);
        }
      }
    }
    builder.append('"');

    return builder.toString().replaceAll("&", "%26");
  }

  public static String normalizePostalCode(String postalCode) {
    if (StringUtils.isNotEmpty(postalCode)) {
      // simple fix for trailing hyphens
      postalCode = StringUtils.removeEnd(postalCode, "-");
      // add hyphen if 9 digit postal code
      if (postalCode.length() == 9 && !postalCode.contains("-")) {
        postalCode = String.format("%s-%s", postalCode.substring(0, 5), postalCode.substring(5));
      }
    }
    return postalCode;
  }

  public static String normalizePhoneNumber(String phoneNumber) {
    if (StringUtils.isNotEmpty(phoneNumber)) {
      if (phoneNumber.startsWith("#") || phoneNumber.startsWith("*")) {
        return phoneNumber;
      }
      try {
        String defaultRegion = "US";
        PhoneNumber numberProto = phoneUtil.parseAndKeepRawInput(phoneNumber, defaultRegion);
        if (numberProto.getCountryCode() == phoneUtil.getCountryCodeForRegion(defaultRegion)) {
          phoneNumber = phoneUtil.format(numberProto, PhoneNumberFormat.NATIONAL);
        } else {
          phoneNumber = phoneUtil.format(numberProto, PhoneNumberFormat.INTERNATIONAL);
        }
      } catch (NumberParseException e) {
        log.error(phoneNumber + " could not be parsed. " + e.getMessage());
      }
    }
    return phoneNumber;
  }

}
