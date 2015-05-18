---
title: Logging
id: logging
layout: default
description: Integrate the client into your own logging framework.
isHome: false
---


The need for logging is different from project to project. To deal with this we have added the ability to set a separate log function on the [ClientConfig]({{site.coreUrl}}/#clientconfig). `ClientConfig.Logger` can be set to a `Action<TraceEventType, Guid?, String, String>`,  where `TraceEventType` is the type of log message is, `Guid` is Id of the message, penultimate parameter is the method that was logged in and finally we have the actual message.

Here is an example:

{% highlight csharp %}
var clientConfig = new ClientConfig()
{
    Logger = (severity, traceID, metode, message) =>
    {
        System.Diagnostics.Debug.WriteLine("{0} - {1} [{2}]", 
        	DateTime.Now, 
        	message, 
        	traceID.GetValueOrDefault()
        );
    }
};
{% endhighlight %}

Do you want to log all messages sent and received, this can be defined in the client configuration:

{% highlight csharp %}
clientConfig.DebugToFile = true;
clientConfig.StandardLogPath = @"\LoggPath";
{% endhighlight%}