package it.pgp.squerez.requests;

import java.util.Collection;

import it.pgp.squerez.enums.RequestType;

public class RecheckRequest extends BaseRequest {
    public final int[] indices; // array with only 0 for recheck all, or positions from 1 up for individual recheck

    protected RecheckRequest(int... indices) {
        super(RequestType.RECHECK);
        this.indices = indices;
    }

    protected RecheckRequest(Collection<Integer> indices) {
        super(RequestType.RECHECK);
        this.indices = intArrayFromCollection(indices);
    }

}
