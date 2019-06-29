var formatSourceData = function(args) {
  var sourceData = {};
  var argKeys = Object.keys(args);
  for(var i=0;i<argKeys.length;i++) {
    var key = argKeys[i];
    var value = args[key];
    if(typeof value === 'string' && value.indexOf(';;') !== -1  && value.indexOf('::') !== -1) {
      sourceData[key] = {};
      var valueParts = value.split(';;');
      for(var j=0;j<valueParts.length;j++) {
        var keyValParts = valueParts[j].split('::');
        sourceData[key][keyValParts[0]] = keyValParts[1] === '' ? null : keyValParts[1];
      }
    } else if(typeof value === 'string' && value.indexOf(';;') !== -1) {
      sourceData[key] = value.split(';;');
    } else if(typeof value === 'string' && value.indexOf('::') !== -1) {
      sourceData[key] = value.split('::')[1];
    } else {
      sourceData[key] = value;
    }
  }
  return sourceData;
};
returnObj = {
  statuses: [
    'Active',
    'Inactive',
    'Pending'
  ],
  categories:{
    ORDER: '9718aa38-8fb4-49e4-910b-bbdc2b1aa579',
    PAYMENT:'ac6528cc-8ba0-4678-9b08-627ca2314ffd',
    CLAIM:'d931bdc4-ef47-4871-98d7-2c48f5ff4fe0',
    RETURN: 'b8057736-3ac9-4b33-a009-9c6859f9e2d7',
    OTHER: '04f39c67-b212-4fe7-87f0-0875c8995d21'
  },
  phoneTypes: {
    "1": 'Mobile',
    "2": 'Fax',
    "3": 'Other'
  },
  sourceData: formatSourceData(args),
  vendorResponseBody: {
    'id': null,
    'name': null,
    'code': null,
    'description': null,
    'status': null,
    'language': null,
    'isVendor': null,
    'sanCode': null,
    'aliases': [],
    'addresses': [],
    'phoneNumbers': [],
    'emails': [],
    'urls': [],
    'contacts': [],
    'agreements': [],
    'vendorCurrencies': [],
    'claimingInterval': null,
    'interfaces': [],
    'accounts': [],
    'changelogs': []
  }
};