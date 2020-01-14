var ScriptEngineUtilityClass = Java.type("org.folio.rest.utility.ScriptEngineUtility");
var scriptEngineUtility = new ScriptEngineUtilityClass();

var counter = {};

function isFunction(functionToCheck) {
  return functionToCheck && {}.toString.call(functionToCheck) === '[object Function]';
}

if (!Array.isArray) {
  Array.isArray = function(arg) {
    return Object.prototype.toString.call(arg) === '[object Array]';
  };
}

var isValidUrlRegex = new RegExp('(http(s)?:\\\/\\\/.)?(www\.)?[-a-zA-Z0-9@:%._\\\+~#=]{2,256}\\\.[a-z]{2,6}\b([-a-zA-Z0-9@:%_\\\+.~#?&//=]*)', 'g');
var isValidUrl = function(string) {
  var res = string ? string.match(isValidUrlRegex) : null;
  return (res !== null);
};

var isURLLike = function(string) {
  if (!string) {
    return false;
  }
  var isLikeAUrl = string.toLowerCase().indexOf("www.") !== -1;
  if(!isLikeAUrl) isLikeAUrl = string.toLowerCase().indexOf(".org") !== -1;
  if(!isLikeAUrl) isLikeAUrl = string.toLowerCase().indexOf(".edu") !== -1;
  if(!isLikeAUrl) isLikeAUrl = string.toLowerCase().indexOf(".net") !== -1;
  if(!isLikeAUrl) isLikeAUrl = string.toLowerCase().indexOf(".us") !== -1;
  if(!isLikeAUrl) isLikeAUrl = string.toLowerCase().indexOf(".io") !== -1;
  if(!isLikeAUrl) isLikeAUrl = string.toLowerCase().indexOf(".co") !== -1;

  return (isValidUrl(string) || isLikeAUrl) && !isEmail(string);
};

var isEmailRegex = new RegExp('^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$');
var isEmail = function(string) {
  return isEmailRegex.test(string);
};

var isEmailLike = function(string) {
  return isEmail(string) || string ? string.toLowerCase().indexOf("@") !== -1 : false;
};

var isPhoneRegex = new RegExp('^[\+]?[(]?[0-9]{3}[)]?[-\s\.]?[0-9]{3}[-\s\.]?[0-9]{4,6}$', 'im');
var isPhone = function(string) {
  return isPhoneRegex.test(string);
};

var UUID = Java.type("java.util.UUID");
