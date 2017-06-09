package analysis;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.spark.api.java.function.PairFlatMapFunction;

import scala.Tuple2;

public class HashtagsExtractor implements PairFlatMapFunction<List<String>, String, Integer> {

	@Override
	public Iterator<Tuple2<String, Integer>> call(List<String> list) throws Exception {
		ArrayList<Tuple2<String, Integer>> temp = new ArrayList<Tuple2<String, Integer>>();
		for(String h : list)
			temp.add(new Tuple2<String, Integer>(h, 1));
		return temp.iterator();
	}

}
