package no.digipost.api.client.util;

import no.digipost.api.client.representations.DigipostUri;
import no.digipost.api.client.representations.Link;
import no.digipost.api.client.representations.Relation;

import java.net.URI;

public class LinkFactory {

    public static Link createAddData(String uuid, URI apiRoot) {
        return new Link(
                Relation.ADD_DATA
                , new DigipostUri(apiRoot.resolve(String.format("/documents/%s/data", uuid))
        ));
    }
}
