package hku.project;

import java.io.IOException;
import static hku.project.ChineseFrequentWordsRunner.*;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math.stat.regression.SimpleRegression;
import org.apache.hadoop.io.Text;
import org.junit.Test;

public class ChineseFrequentWordsRunnerTest {
@Test
public void TotalCountTokenizerTest() throws IOException{
	ChineseFrequentWordsRunner.TotalCountTokenizer totalCountTokenizer = new ChineseFrequentWordsRunner.TotalCountTokenizer();
Map<Integer, Integer> totalYearCount = new HashMap<Integer, Integer>();
	ConfigFileHelper.loadFromFile(totalYearCount, "total_counts.txt", totalCountTokenizer);
	System.out.println(totalYearCount);
}
}
