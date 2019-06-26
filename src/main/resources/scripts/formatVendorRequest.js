var mapValues = function(baseObejectString, predicate) {
  var builtObjects = [];
  if(baseObejectString) {
    var bases = baseObejectString.split(';;');
    for(var i in bases) {
      var voyagerId = bases[i];
      var baseObj = {};
      for(var j=2;j<arguments.length;j++) {
        var arg = arguments[j];
        var argParts = arg.values.split(';;');
        for(var k in argParts) {
          var argPart = argParts[k];
          if(argPart) {
            var argKeyValue = argPart.indexOf('::') ? argPart.split('::') : argPart.split(',');
            if(argKeyValue[0]===voyagerId && argKeyValue[1] && argKeyValue[1] !== '') {
              baseObj[arg.label] = argKeyValue[1];
            }
          }
        }
      }
      if(predicate(baseObj)) {
        builtObjects.push(baseObj);
      }
    }
  }
  return builtObjects;
};
returnObj = {
  'id': null,
  'name': args.VENDOR_NAME,
  'code': args.VENDOR_CODE,
  'description': args.VENDOR_TYPE,
  'status': 0,
  'language': null,
  'isVendor': true,
  'sanCode': null,
  'aliases': [
    args.vendor_aliases
  ],
  'addresses': mapValues(args.address_ids,
    function(baseObj) {
      return !isEmail(baseObj.addressLine1) && !isURLLike(baseObj.addressLine1);
    },
    {
      label: 'addressLine1',
      values: args.address_line1s
    },
    {
      label: 'addressLine2',
      values: args.address_line2s
    },
    {
      label: 'addressLine3',
      values: args.address_line3s
    },
    {
      label: 'addressLine4',
      values: args.address_line4s
    },
    {
      label: 'addressLine5',
      values: args.address_line5s
    },
    {
      label: 'city',
      values: args.cities
    },
    {
      label: 'stateRegion',
      values: args.state_provinces
    },
    {
      label: 'zipCode',
      values: args.zip_postals
    },
    {
      label: 'country',
      values: args.countries
    }
  ),
  'phoneNumbers': [],
  'emails': mapValues(args.address_ids,
    function(baseObj) {
      return false;
    },
    {
      label: 'email_address',
      values: args.email_addresses
    }
  ),
  'urls': [],
  'contacts': [
    mapValues(args.address_ids,
      function(baseObj) {
        return false;
      },
      {
        label: 'contact_name',
        values: args.contact_names
      },
      {
        label: 'contact_title',
        values: args.contact_titles
      }
    )
  ],
  'agreements': [],
  'vendorCurrencies': [
    args.DEFAULT_CURRENCY
  ],
  'claimingInterval': args.CLAIM_INTERVAL,
  'interfaces': [],
  'accounts': mapValues(args.account_ids,
    function(baseObj) {
      return false;
    }, 
    {
      label: 'default_po_type',
      values: args.default_po_types
    },
    {
      label: 'default_discount',
      values: args.default_discounts
    },
    {
      label: 'account_status',
      values: args.account_statuses
    },
    {
      label: 'status_date',
      values: args.status_dates
    },
    {
      label: 'deposit',
      values: args.deposits
    },
    {
      label: 'account_number',
      values: args.account_numbers
    },
    {
      label: 'account_name',
      values: args.account_names
    },
    {
      label: 'account_note',
      values: args.account_notes
    }
  ),
  'changelogs': []
};