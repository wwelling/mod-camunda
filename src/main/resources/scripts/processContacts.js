if(args.sourceData.address_ids)
for(var i=0;i<args.sourceData.address_ids.length;i++) {
  var addressId = args.sourceData.address_ids[i];
  if(args.sourceData.contact_names[addressId]) {
    var contact = {};
    contact.firstName = args.sourceData.contact_names[addressId];
    contact.aliases = [];
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
