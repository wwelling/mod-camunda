args.vendorResponseBody.name = args.sourceData.VENDOR_NAME;
args.vendorResponseBody.code = args.sourceData.VENDOR_CODE;
args.vendorResponseBody.description = args.sourceData.vendor_type;
args.vendorResponseBody.status = args.statuses[0];
args.vendorResponseBody.isVendor = true;
args.vendorResponseBody.vendorCurrencies.push(args.sourceData.DEFAULT_CURRENCY);
args.vendorResponseBody.claimingInterval = args.sourceData.CLAIM_INTERVAL;
returnObj = args;
