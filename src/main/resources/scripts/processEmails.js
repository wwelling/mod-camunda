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

  }
}
returnObj = args;
