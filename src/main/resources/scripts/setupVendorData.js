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
      sourceData[key] = value.split('::')[0];
    }
    else {
      sourceData[key] = value;
    }
  }
  return sourceData;
};
returnObj = {
  statuses: [
    'active',
    'inactive',
    'pending'
  ],
  categories:{
    ORDER: {
      value: 'order'
    },
    PAYMENT: {
      value: 'payment'
    },
    CLAIM: {
      value: 'claim'
    },
    RETURN: {
      value: 'return'
    },
    OTHER: {
      value: 'other'
    }
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