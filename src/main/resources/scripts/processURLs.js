if(args.sourceData.address_ids)
for(var i=0;i<args.sourceData.address_ids.length;i++) {
  var urlObj = {};
  var addressId = args.sourceData.address_ids[i];
  urlObj.value = args.sourceData.address_line1s[addressId];
  if(isURLLike(urlObj.value)) {
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

  }
}
returnObj = args;
