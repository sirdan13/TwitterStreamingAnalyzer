package analysis;

import org.apache.spark.api.java.function.Function2;

public class SommaFrequenze implements Function2<Integer, Integer, Integer> {

	
	public Integer call(Integer v1, Integer v2) throws Exception {
		return v1+v2;
	}

}
