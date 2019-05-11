package it.pgp.squerez.requests;

import java.util.Collection;

import it.pgp.squerez.enums.RequestType;

public class ShowRequest extends BaseRequest {
    public final int[] indices; // array with only 0 for show all, or positions from 1 up for individual show

    protected ShowRequest(int... indices) {
        super(RequestType.SHOW);
        this.indices = indices;
    }

    protected ShowRequest(Collection<Integer> indices) {
        super(RequestType.SHOW);
        this.indices = intArrayFromCollection(indices);
    }

}
