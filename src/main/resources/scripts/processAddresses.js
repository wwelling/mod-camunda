if(args.sourceData.address_ids)
for(var i=0;i<args.sourceData.address_ids.length;i++) {
  var address = {};
  var addressId = args.sourceData.address_ids[i];
  address.addressLine1 = args.sourceData.address_line1s[addressId];
  if(
    !isEmail(address.addressLine1) && 
    !isURLLike(address.addressLine1)
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
