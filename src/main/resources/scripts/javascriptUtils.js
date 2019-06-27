var isValidUrl = function(string) {
  var res = string ? string.match(/(http(s)?:\\\/\\\/.)?(www\.)?[-a-zA-Z0-9@:%._\\\+~#=]{2,256}\\\.[a-z]{2,6}\b([-a-zA-Z0-9@:%_\\\+.~#?&//=]*)/g) : null;
  return (res !== null);
};

var isURLLike = function(string) {
  return isValidUrl(string) || string ? string.toLowerCase().indexOf("www.") !== -1 : false;
};

var isEmail = function(string) {
  return /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/.test(string);
};

var isPhone = function(string) {
  return /^[\+]?[(]?[0-9]{3}[)]?[-\s\.]?[0-9]{3}[-\s\.]?[0-9]{4,6}$/im.test(string);
};

function isFunction(functionToCheck) {
  return functionToCheck && {}.toString.call(functionToCheck) === '[object Function]';
 }