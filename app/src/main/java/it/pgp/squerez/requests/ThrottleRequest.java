package it.pgp.squerez.requests;

import java.util.Collection;

import it.pgp.squerez.enums.RequestType;

public class ThrottleRequest extends BaseRequest {
    public final int[] indices; // array with only 0 for show all, or positions from 1 up for individual show
    // throttle speeds, kbps, -1 for don't change, 0 for unlimited
    public final int upSpeed;
    public final int downSpeed;

    protected ThrottleRequest(int upSpeed, int downSpeed, int... indices) {
        super(RequestType.THROTTLE);
        this.upSpeed = upSpeed;
        this.downSpeed = downSpeed;
        this.indices = indices;
    }

    protected ThrottleRequest(int upSpeed, int downSpeed, Collection<Integer> indices) {
        super(RequestType.THROTTLE);
        this.upSpeed = upSpeed;
        this.downSpeed = downSpeed;
        this.indices = intArrayFromCollection(indices);
    }

}
