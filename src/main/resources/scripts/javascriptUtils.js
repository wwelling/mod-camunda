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

if (!Array.isArray) {
  Array.isArray = function(arg) {
    return Object.prototype.toString.call(arg) === '[object Array]';
  };
}

var setupVendorData = function(args, returnObj) {
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
          if(sourceData[key][keyValParts[0]]) {
            if(!Array.isArray(sourceData[key][keyValParts[0]])) {
              var currentValue = sourceData[key][keyValParts[0]];
              sourceData[key][keyValParts[0]] = [];
              sourceData[key][keyValParts[0]].push(currentValue);
            }
            sourceData[key][keyValParts[0]].push(keyValParts[1] === '' ? null : keyValParts[1]);
          } else {
            sourceData[key][keyValParts[0]] = keyValParts[1] === '' ? null : keyValParts[1];
          }
          
        }
      } else if(typeof value === 'string' && value.indexOf(';;') !== -1) {
        sourceData[key] = value.split(';;');
      } else if(typeof value === 'string' && value.indexOf('::') !== -1) {
        var valueParts = value.split('::');
        sourceData[key] = {};
        sourceData[key][valueParts[0]] = valueParts[1];
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
      RETURN: '544459af-fc5e-4e64-9b40-acb84ac4d3aa',
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
  return returnObj;
};

var processFlatData = function(args, returnObj) {

  if(typeof args.sourceData.address_ids === 'string') {
    args.sourceData.address_ids = [args.sourceData.address_ids];
  }

  if(typeof args.sourceData.account_ids === 'string') {
    args.sourceData.account_ids = [args.sourceData.account_ids];
  }

  args.vendorResponseBody.name = args.sourceData.VENDOR_NAME;
  args.vendorResponseBody.code = args.sourceData.VENDOR_CODE;
  args.vendorResponseBody.description = args.sourceData.VENDOR_TYPE;
  args.vendorResponseBody.status = args.statuses[0];
  args.vendorResponseBody.taxId = args.sourceData.FEDERAL_TAX_ID;
  args.vendorResponseBody.isVendor = true;
  args.vendorResponseBody.vendorCurrencies.push(args.sourceData.DEFAULT_CURRENCY);
  args.vendorResponseBody.claimingInterval = args.sourceData.CLAIM_INTERVAL;
  returnObj = args;
  return returnObj;
};

var processContacts = function(args, returnObj) {
  if(args.sourceData.address_ids)
  for(var i=0;i<args.sourceData.address_ids.length;i++) {
    var addressId = args.sourceData.address_ids[i];
    if(args.sourceData.contact_names[addressId]) {
      var contact = {};
      contact.firstName = args.sourceData.contact_names[addressId];
      contact.lastName = "";
      contact.addresses = [];
      contact.phoneNumbers = [];
      contact.emails = [];
      contact.urls = [];
  
      contact.categories = [];
  
      if(args.sourceData.order_addresses[addressId]==='Y') 
        contact.categories.push(args.categories.ORDER);
  
      if(args.sourceData.payment_addreses[addressId]==='Y') 
        contact.categories.push(args.categories.PAYMENT);
  
      if(args.sourceData.claim_addresses[addressId]==='Y') 
        contact.categories.push(args.categories.CLAIM);
  
      if(args.sourceData.return_addresses[addressId]==='Y') 
        contact.categories.push(args.categories.RETURN);
  
      if(args.sourceData.other_addresses[addressId]==='Y') 
        contact.categories.push(args.categories.OTHER);
  
      args.vendorResponseBody.contacts.push(contact);
    }
  }
  returnObj = args;
  return returnObj;
};

var processAddresses = function(args, returnObj) {
  if(args.sourceData.address_ids)
  for(var i=0;i<args.sourceData.address_ids.length;i++) {
    var address = {};
    var addressId = args.sourceData.address_ids[i];
    address.addressLine1 = args.sourceData.address_line1s[addressId];

    address.addressLine2 = args.sourceData.address_line2s[addressId] + " " +
                           args.sourceData.address_line3s[addressId] + " " +
                           args.sourceData.address_line4s[addressId] + " " +
                           args.sourceData.address_line5s[addressId] + " ";
    if(
      (!isEmail(address.addressLine1) &
      !isURLLike(address.addressLine1))
    ) {
      address.addressLine2 = args.sourceData.address_line2s[addressId];
      address.city = args.sourceData.cities[addressId];
      address.stateRegion =  args.sourceData.state_provinces[addressId];
      address.zipCode = args.sourceData.zip_postals[addressId];
      address.country = args.sourceData.countries[addressId];
      address.categories = [];
  
      if(args.sourceData.order_addresses[addressId]==='Y') 
        address.categories.push(args.categories.ORDER);
  
      if(args.sourceData.payment_addreses[addressId]==='Y') 
        address.categories.push(args.categories.PAYMENT);
  
      if(args.sourceData.claim_addresses[addressId]==='Y') 
        address.categories.push(args.categories.CLAIM);
  
      if(args.sourceData.return_addresses[addressId]==='Y') 
        address.categories.push(args.categories.RETURN);
      
      if(args.sourceData.other_addresses[addressId]==='Y') 
        address.categories.push(args.categories.OTHER);
      if(args.sourceData.contact_names[addressId]) {
        for(var j=0;j<args.vendorResponseBody.contacts.length;j++) {
          var c = args.vendorResponseBody.contacts[j];
          if(c.firstName === args.sourceData.contact_names[addressId]) {
            c.addresses.push(address);
          }
        }
      } else {
        args.vendorResponseBody.addresses.push(address);
      }
    }
  }
  returnObj = args;
  return returnObj;
};

var processPhoneNumbers = function(args, returnObj) {
  if(args.sourceData.address_ids)
  for(var i=0;i<args.sourceData.address_ids.length;i++) {
    var phoneNumberObj = {};
    var addressId = args.sourceData.address_ids[i];

    if(isPhone(args.sourceData.address_line1s[addressId])) {
      phoneNumberObj.phoneNumber = args.sourceData.address_line1s[addressId];
    } else if(typeof args.sourceData.phone_number == 'string') {
      phoneNumberObj.phoneNumber = args.sourceData.phone_number;
    } else if(args.sourceData.phone_number) {
      phoneNumberObj.phoneNumber = args.sourceData.phone_number[addressId];
    }

    if(phoneNumberObj.phoneNumber) {

      var makePhoneNumber = function(pn, index) {
        if(args.sourceData.phone_type) {
          if(Array.isArray(args.sourceData.phone_type[addressId])) {
            pn.isPrimary = args.sourceData.phone_type[addressId][index] === '0';
            pn.type = pn.isPrimary ? 'Other' : args.phoneTypes[args.sourceData.phone_type[addressId][index]] ? args.phoneTypes[args.sourceData.phone_type[addressId][index]] : 'Other';
          } else {
            pn.isPrimary = args.sourceData.phone_type[addressId] === '0';
          pn.type = pn.isPrimary ? 'Other' : args.phoneTypes[args.sourceData.phone_type[addressId]] ? args.phoneTypes[args.sourceData.phone_type[addressId]] : 'Other';
          }
        } else {
          pn.type = 'Other';
        }

        pn.categories = [];

        if(args.sourceData.order_addresses[addressId]==='Y') 
          pn.categories.push(args.categories.ORDER);

        if(args.sourceData.payment_addreses[addressId]==='Y') 
          pn.categories.push(args.categories.PAYMENT);
    
        if(args.sourceData.claim_addresses[addressId]==='Y') 
          pn.categories.push(args.categories.CLAIM);
    
        if(args.sourceData.return_addresses[addressId]==='Y') 
          pn.categories.push(args.categories.RETURN);
        
        if(args.sourceData.other_addresses[addressId]==='Y') 
          pn.categories.push(args.categories.OTHER);
    
        if(args.sourceData.contact_names[addressId]) {
          for(var j=0;j<args.vendorResponseBody.contacts.length;j++) {
            var c = args.vendorResponseBody.contacts[j];
            if(c.firstName === args.sourceData.contact_names[addressId]) {
              c.phoneNumbers.push(pn);
            }
          }
        } else {
          args.vendorResponseBody.phoneNumbers.push(pn);
        }
      };

      if(Array.isArray(phoneNumberObj.phoneNumber)) {
        for(var j=0;j<phoneNumberObj.phoneNumber.length;j++) {
          var number = phoneNumberObj.phoneNumber[j];
          makePhoneNumber({phoneNumber:number}, j);
        }
      } else {
        makePhoneNumber({phoneNumber: phoneNumberObj.phoneNumber});
      }
  
    } 
  }
  returnObj = args;
  return returnObj;
};

var processEmails = function(args, returnObj) {
  if(args.sourceData.address_ids)
  for(var i=0;i<args.sourceData.address_ids.length;i++) {
    var emailObj = {};
    var addressId = args.sourceData.address_ids[i];
    emailObj.value = args.sourceData.address_line1s[addressId];
    if(isEmail(emailObj.value)) {
      emailObj.description = null;
      emailObj.categories = [];
  
      if(args.sourceData.order_addresses[addressId]==='Y') 
        emailObj.categories.push(args.categories.ORDER);
  
      if(args.sourceData.payment_addreses[addressId]==='Y') 
        emailObj.categories.push(args.categories.PAYMENT);
  
      if(args.sourceData.claim_addresses[addressId]==='Y') 
        emailObj.categories.push(args.categories.CLAIM);
  
      if(args.sourceData.return_addresses[addressId]==='Y') 
        emailObj.categories.push(args.categories.RETURN);
      
      if(args.sourceData.other_addresses[addressId]==='Y') 
        emailObj.categories.push(args.categories.OTHER);
      
      if(args.sourceData.contact_names[addressId]) {
        for(var j=0;j<args.vendorResponseBody.contacts.length;j++) {
          var c = args.vendorResponseBody.contacts[j];
          if(c.firstName === args.sourceData.contact_names[addressId]) {
            c.emails.push(emailObj);
          }
        }
      } else {
        args.vendorResponseBody.emails.push(emailObj);
      }
      if(args.sourceData.address_line2s[addressId])
        args.vendorResponseBody.description += " " + args.sourceData.address_line2s[addressId];
    }
  }
  returnObj = args;
  return returnObj;  
};

var processURLs = function(args, returnObj) {
  if(args.sourceData.address_ids)
  for(var i=0;i<args.sourceData.address_ids.length;i++) {
    var urlObj = {};
    var addressId = args.sourceData.address_ids[i];
    urlObj.value = args.sourceData.address_line1s[addressId];
    if(isURLLike(urlObj.value)) {

      if(!isValidUrl(urlObj.value)) {
        urlObj.value = 'http://'+urlObj.value;
      }

      urlObj.description = null;
      urlObj.categories = [];

      if(args.sourceData.order_addresses[addressId]==='Y') 
        urlObj.categories.push(args.categories.ORDER);

      if(args.sourceData.payment_addreses[addressId]==='Y') 
        urlObj.categories.push(args.categories.PAYMENT);

      if(args.sourceData.claim_addresses[addressId]==='Y') 
        urlObj.categories.push(args.categories.CLAIM);

      if(args.sourceData.return_addresses[addressId]==='Y') 
        urlObj.categories.push(args.categories.RETURN);
      
      if(args.sourceData.other_addresses[addressId]==='Y') 
        urlObj.categories.push(args.categories.OTHER);

      if(args.sourceData.contact_names[addressId]) {
        for(var j=0;j<args.vendorResponseBody.contacts.length;j++) {
          var c = args.vendorResponseBody.contacts[j];
          if(c.firstName === args.sourceData.contact_names[addressId]) {
            c.urls.push(urlObj);
          }
        }
      } else {
        args.vendorResponseBody.urls.push(urlObj);
      }
      if(args.sourceData.address_line2s[addressId])
        args.vendorResponseBody.description += " " + args.sourceData.address_line2s[addressId];
    }
  }
  returnObj = args;
  return returnObj;
};

var processAccounts = function(args, returnObj) {
  if(args.sourceData.account_ids)
  for(var i=0;i<args.sourceData.account_ids.length;i++) {
    var account = {};
    var account_id = args.sourceData.account_ids[i];
    account.name = args.sourceData.account_names[account_id] ? args.sourceData.account_names[account_id] : '';
    account.accountNo = args.sourceData.account_numbers[account_id] ? args.sourceData.account_numbers[account_id] : '';
    account.accountStatus = args.sourceData.account_statuses[account_id] ? args.statuses[args.sourceData.account_statuses[account_id]] :  args.statuses[1];
    account.paymentMethod = args.sourceData.deposits[account_id] === 'Y' ? 'Deposit Account' :'EFT';
    account.notes = args.sourceData.account_notes[account_id] ? args.sourceData.account_notes[account_id] : '';
    account.libraryCode = '';
    account.libraryEdiCode = '';
    args.vendorResponseBody.accounts.push(account);
  }
  returnObj = args;
  return returnObj;
};

var finalizeVendorData = function(args, returnObj) {
  if(args.sourceData.vendor_notes)
    args.vendorResponseBody.description += " " + args.sourceData.vendor_notes;
  returnObj = args.vendorResponseBody;
  return returnObj;
};