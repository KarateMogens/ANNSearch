package lsh;

import java.util.List;

public interface Searchable {

    public List<Integer> search(float[] qVec);

    public void fit(float[][] corpusMatrix);

}
