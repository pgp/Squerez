package it.pgp.squerez.requests;

import java.util.Collection;

import it.pgp.squerez.enums.RequestType;

public class RemoveRequest extends BaseRequest {

    public final boolean removeAlsoDownloadedFiles;
    public final int[] indices; // array with only 0 for remove all, or positions from 1 up for individual remove

    protected RemoveRequest(boolean removeAlsoDownloadedFiles, int... indices) {
        super(RequestType.REMOVE);
        this.removeAlsoDownloadedFiles = removeAlsoDownloadedFiles;
        this.indices = indices;
    }

    protected RemoveRequest(boolean removeAlsoDownloadedFiles, Collection<Integer> indices) {
        super(RequestType.REMOVE);
        this.removeAlsoDownloadedFiles = removeAlsoDownloadedFiles;
        this.indices = intArrayFromCollection(indices);
    }
}
