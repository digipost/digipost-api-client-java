# Java client library for Digipost API

Online documentation: 
http://digipost.github.io/digipost-api-client-java/

## Creating documentation for a new version
* Copy the most recent versioned folder within docs/
* Update the documentation
* Update docs/_config.yml to reflect the latest version (in `currentVersion`, `versions`, and `collections`)
* Update the previous version's `index.html` - removing the forward-slash after "redirect_from" (-> `redirect_from:` within the old index-file, and `redirect_from: /` within the new).
