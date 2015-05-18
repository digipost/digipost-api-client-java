---
title: ClientConfig
id: clientconfig
layout: default
description:  Clientconfiguration setup
isHome: false
---

ClientConfig is a container for all the dynamic paramters that you can change. All of the attributes, except of one(TechnicalSenderID), have default values. So you do not need to change them if you do not have an explicit need. However, the technical sender id have to be an input parameter to contstruct the class. The technical sender id can be retrieved from the [Digipost organisation admin page](https://www.digipost.no/app/post#/org/config/detaljer) 

Usage:
{% highlight csharp%}
private const string SenderId = "123456"; 
var config = new ClientConfig(SenderId);
Logging.Initialize(config);

{% endhighlight%}