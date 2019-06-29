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
  
    /*if(args.sourceData.phone_type) {
      phoneNumberObj.isPrimary = args.sourceData.phone_type[addressId] === '0';
      phoneNumberObj.type = phoneNumberObj.isPrimary ? 'Other' : args.phoneType[args.sourceData.phone_type[addressId]] ? args.phoneType[args.sourceData.phone_type[addressId]] : 'Other';
    } else {
      phoneNumberObj.type = 'Other';
    }*/
    
    phoneNumberObj.categories = [];

    if(args.sourceData.order_addresses[addressId]==='Y') 
      phoneNumberObj.categories.push(args.categories.ORDER);

    if(args.sourceData.payment_addreses[addressId]==='Y') 
      phoneNumberObj.categories.push(args.categories.PAYMENT);

    if(args.sourceData.claim_addresses[addressId]==='Y') 
      phoneNumberObj.categories.push(args.categories.CLAIM);

    if(args.sourceData.return_addresses[addressId]==='Y') 
      phoneNumberObj.categories.push(args.categories.RETURN);
    
    if(args.sourceData.other_addresses[addressId]==='Y') 
      phoneNumberObj.categories.push(args.categories.OTHER);

    if(args.sourceData.contact_names[addressId]) {
      for(var j=0;j<args.vendorResponseBody.contacts.length;j++) {
        var c = args.vendorResponseBody.contacts[j];
        if(c.firstName === args.sourceData.contact_names[addressId]) {
          c.phoneNumbers.push(phoneNumberObj);
        }
      }
    } else {
      args.vendorResponseBody.phoneNumbers.push(phoneNumberObj);
    }

  } 
}
returnObj = args;
