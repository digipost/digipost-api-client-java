---
title: Access controll
id: accesscontroll
layout: default
description: Control the sensitivety and controll level of a document.
isHome: false
---

<blockquote>If one of the documents in a message is marked as 'Sensitive', the whole message is considered sensitive. The same goes for authentication level.</blockquote>

<h3 id="Authentication">Authentication</h3>

It is possible to explicit set the level of authentication you require for someone to open the document. The default authentication level is social security number + password(Level 2). The next level Digipost supports is two factor authentication(Level 4), which require the user to log in with either BankID or BuyPass.


{% highlight csharp%}
var primaryDocument = new Document(subject: "Primary document", fileMimeType: "pdf", contentBytes: File.ReadAllBytes(@"c:\....\Primarydokument.pdf"));

primaryDocument.AuthenticationLevel = AuthenticationLevel.Password; //Default
primaryDocument.AuthenticationLevel = AuthenticationLevel.TwoFactor; // Require BankID or BuyPass to open
{% endhighlight%}

<h3 id="Sensitivety">Sensitivity level</h3>

It is possible to mark a document as sensitive. If the document is marked as sensitive the metadata about the message, like the sender and subject, will be hidden until logged in at the appropriate security level specified for the message.

The default for every document is 'Normal'. Non sensitive message. Metadata about the message, like the sender and subject, will be revealed in user notifications (eg. email and SMS), and can also be seen when logged in at a security level below the one specified for the message.


{% highlight csharp%}
var primaryDocument = new Document(subject: "Primary document", fileMimeType: "pdf", contentBytes: File.ReadAllBytes(@"c:\....\Primarydokument.pdf"));

primaryDocument.SensitivityLevel = SensitivityLevel.Normal; //Default
primaryDocument.SensitivityLevel = SensitivityLevel.Sensitive; // Mark as sensitive

{% endhighlight%}



