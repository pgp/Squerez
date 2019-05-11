package it.pgp.squerez.requests;

import java.util.Collection;

import it.pgp.squerez.enums.RequestType;

public abstract class BaseRequest {

    public final RequestType type;

    protected BaseRequest(RequestType type) {
        this.type = type;
    }

    public static int[] intArrayFromCollection(Collection<Integer> items) {
        int[] ret = new int[items.size()];
        int i=0;
        for (int item : items) ret[i++] = item;
        return ret;
    }

}
