var isValidUrl = function(string) {
  var res = string.match(/(http(s)?:\\\/\\\/.)?(www\.)?[-a-zA-Z0-9@:%._\\\+~#=]{2,256}\\\.[a-z]{2,6}\b([-a-zA-Z0-9@:%_\\\+.~#?&//=]*)/g);
  return (res !== null);
};

var isURLLike = function(string) {
  return isValidUrl(string) || string.toLowerCase().indexOf("www.") !== -1;
};

var isEmail = function(string) {
  return /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/.test(string);
};