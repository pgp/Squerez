package it.pgp.squerez.requests;

import it.pgp.squerez.enums.RequestType;

public class QuitRequest extends BaseRequest {
    protected QuitRequest() {
        super(RequestType.QUIT);
    }
}
