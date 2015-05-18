---
title: Physical mail
id: physicalprint
layout: default
description: Send directly to print or use fallback to print(physical delivery)
isHome: false
---

<h3 id="physicalprint">Fallback to print(physical delivery)</h3>

In cases where the user is not a registered user, it is also possible to attach physical name and address for physical delivery. In this way it is much easier and send to the entire customer base, instead of filtering out recipients who are not registered users. This service has its own price and have to be manually activated to work.

The physical print option is defined as a object with an recipient and an return address.

{% highlight csharp%}
//printdetails for fallback to print (physical mail)
var printDetails =
    new PrintDetails(
        recipient: new PrintRecipient("Ola Nordmann", "Prinsensveien 123", "0460", "Oslo"),
        returnAddress: new PrintRecipient("Kari Nordmann", "Akers Àle 2", "0400", "Oslo")
        );

//recipientIdentifier for digital mail
var recipientByNameAndAddress = new RecipientByNameAndAddress(
    fullName: "Ola Nordmann",
    addressLine: "Prinsensveien 123", 
    postalCode: "0460",
    city: "Oslo");

//recipient
var digitalRecipientWithFallbackPrint = new Recipient(recipientByNameAndAddress,printDetails);

{% endhighlight%}

<h3 id="directprint">Send directly to print</h3>

This functionality have to be explicit activated for your business. If you prefer to send a letter physical you can do this by not sending in the digital identifier- only the physical print information.

{% highlight csharp%}
//printdetails for fallback to print (physical mail)
var printDetails =
    new PrintDetails(
        recipient: new PrintRecipient("Ola Nordmann", "Prinsensveien 123", "0460", "Oslo"),
        returnAddress: new PrintRecipient("Kari Nordmann", "Akers Àle 2", "0400", "Oslo")
        );

//recipient
var digitalRecipientWithFallbackPrint = new Recipient(printDetails);

{% endhighlight%}

<h3 id="nondeliverablehandling">Nondeliverable handling</h3>

You must also have an understanding of how you will deal with physical letters that is undeliverable. There are only one options in our API; return to sender, which is self-explanatory. 

{% highlight csharp%}
    
printDetails.NondeliverableHandling = NondeliverableHandling.ReturnToSender; // (default) 

{% endhighlight%}
