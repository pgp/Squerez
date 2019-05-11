package it.pgp.squerez.requests;

import it.pgp.squerez.enums.RequestType;

public class AddRequest extends BaseRequest {

    public final String origin; // torrent file or magnet link

    protected AddRequest(String origin) {
        super(RequestType.ADD);
        this.origin = origin;
    }
}
