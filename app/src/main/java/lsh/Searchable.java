package lsh;

import java.util.Collection;

public interface Searchable {

    public Collection<Integer> search(float[] qVec);

    public void fit(float[][] corpusMatrix);

}
