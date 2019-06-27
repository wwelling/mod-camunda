for(var i=0;i<args.sourceData.address_ids.length;i++) {
  var phoneNumberObj = {};
  var addressId = args.sourceData.address_ids[i];
  phoneNumberObj.phoneNumber = isPhone(args.sourceData.address_line1s[addressId]) ? args.sourceData.address_line1s[addressId] : args.sourceData.phone_number[addressId] ;
  if(phoneNumberObj.phoneNumber) {
    phoneNumberObj.isPrimary = args.sourceData.phone_type[addressId] === '0';
    phoneNumberObj.type = phoneNumberObj.isPrimary ? 'Other' : args.phoneType[args.sourceData.phone_type[addressId]] ? args.phoneType[args.sourceData.phone_type[addressId]] : 'Other';

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
  
    args.vendorResponseBody.phoneNumbers.push(phoneNumberObj);
  } 
}
returnObj = args;
