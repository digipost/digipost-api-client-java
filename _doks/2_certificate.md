---
title: Install certificate on server
id: installcert
layout: default
description: Install certificate for signing and encrypting requests.
isHome: false
---

To send over HTTPS you need to sign your request with an certificate. This step will show you how to install the certificate in the certificate store. This should be done on the server where the application will run.

<h3 id="businesscertificate">Installing the business certificate in the certificate store</h3>

<blockquote> SSL Certificates are small data files that digitally bind a cryptographic key to an organization's details. When installed on a web server, it activates the padlock and the https protocol (over port 443) and allows secure connections from a web server to a browser.  </blockquote>

1.  Double click on the certificate (CertificateName.p12)
2.  Choose that the certificate should be saved in _Current User_ and push _Next_
3.  The filename should already be comleted. Push _Next_
4.  Enter password for private key and select _Mark this key as exportable ..._ press _Next_
5.  Select _Automatically select the certificate store based on the type of certificate_
6.  _Next_ and _Finish_
7.  If you are prompted to accept the certificate then do it.
8.  You should get a dialog stating that the import was successful. Press _Ok_.



<h3 id="find_businesscertificate">Find the thumbprint for the business certificate in the certificate store</h3>
1. Start mmc.exe (Push windowsbutton and type mmc.exe)
2. Choose File -> Add/Remove Snap-in…(Ctrl + M)
3. Mark Certificates and click Add >
4. Choose 'My user account' followed by Finish, then 'OK'.
5. Double click on 'Certificates' 
6. Double click on the installed certificate
7. Go to 'Details' -tab
8. Scroll down to 'Thumbprint'



