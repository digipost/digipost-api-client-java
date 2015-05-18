---
title: SMS notification
id: smsnotification
layout: default
description: Send SMS notifcation to recipient
isHome: false
---
<h3 id="smsnotification">SMS notification</h3>

Customers receive periodic notifications when they have new letters in Digipost. This is standard behaviour. However if desired it is also possible to send an SMS to a recipient of a letter. This feature costs extra.

It is possible to send SMS either after x hours, or at a given timestamp.

After X hours:
{% highlight csharp%}
//primary document
var primaryDocument = new Document("Primary document", "pdf", GetPrimaryDocument())
{
    SmsNotification = new SmsNotification(0) // SMS reminder after 0 hour
};
{% endhighlight%}

At given timestamp:
{% highlight csharp%}
//primary document
var primaryDocument = new Document("Primary document", "txt", GetPrimaryDocument())
{
    SmsNotification = new SmsNotification(new DateTime(year: 2015, month: 05, day: 17, hour: 12, minute: 00, second: 00)) // SMS reminder 17 of Mai 2015 12am
};
{% endhighlight%}