---
title: Client response
id: response
layout: default
description: How to read the response from the client.
isHome: false
---
<h3 id="respone">Digipost client response</h3>

The client will return a placeholder with the most important status-codes and the raw xml response.

- StatusMessage is the status of the request/shipment
- DeliveryTime is the time of delivered mail. Will not be set(or set with 01/01/0001 00:00:00) if the shipment had errors.
- DelimeryMethod is the channel the mail was delivered. Either PRINT or DIGIPOST, where Print is physical and Digipost is digitally.
- ErrorCode is populated if relevant.
- ErrorType is the type of error. e.g client_data is where the client have wrong input.
- ResponseMessage is the the raw data from the request.

Example of a response where the digital recipient does not exist in Digipost:
{% highlight csharp%}

StatusMessage[The recipient does not have a Digipost account.]
Deliverytime[01/01/0001 00:00:00]
DeliveryMethod[]
ErrorCode[UNKNOWN_RECIPIENT]
ErrorType[CLIENT_DATA]
ResponseMessage[<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<error xmlns="http://api.digipost.no/schema/v6">
	<error-code>UNKNOWN_RECIPIENT</error-code>
	<error-message>The recipient does not have a Digipost account.</error-message>
	<error-type>CLIENT_DATA</error-type>
</error>]]

{% endhighlight%}

Example of a response where the letter have been delivered digitally OK:
{% highlight csharp%}

StatusMessage[Delivered]
DeliveryTime[30/04/2015 14:54:07]
DeliveryMethod[DIGIPOST]
ErrorCode[]
ErrorType[]
ResponseMessage[<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<message-delivery xmlns="http://api.digipost.no/schema/v6">
	<delivery-method>DIGIPOST</delivery-method>
	<status>DELIVERED</status>
	<delivery-time>2015-04-30T14:54:07.788+02:00</delivery-time>
	<primary-document>
		<uuid>424882ee-4db8-4585-b701-69e6da10c956</uuid>
		<subject>Primary document</subject>
		<file-type>txt</file-type>
		<authentication-level>PASSWORD</authentication-level>
		<sensitivity-level>NORMAL</sensitivity-level>
		<content-hash hash-algorithm="SHA256">XXX=</content-hash>
	</primary-document>
	<attachment>
		<uuid>a3069d0c-fc1d-4966-b17d-9395d410c126</uuid>
		<subject>Attachment</subject>
		<file-type>txt</file-type>
		<authentication-level>PASSWORD</authentication-level>
		<sensitivity-level>NORMAL</sensitivity-level>
		<content-hash hash-algorithm="SHA256">XXXX</content-hash>
	</attachment>
</message-delivery>]]

{% endhighlight%}

