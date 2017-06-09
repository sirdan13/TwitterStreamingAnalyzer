package analysis;
import org.apache.spark.api.java.function.PairFunction;

import scala.Tuple2;

public class SwapKeyValue implements PairFunction<Tuple2<String, Integer>, Integer, String> {

	@Override
	public Tuple2<Integer, String> call(Tuple2<String, Integer> arg0) throws Exception {
		return arg0.swap();
	}

}
