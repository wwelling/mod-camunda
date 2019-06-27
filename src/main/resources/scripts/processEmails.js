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
  
    args.vendorResponseBody.emails.push(emailObj);
  }
}
returnObj = args;
