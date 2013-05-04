package hku.project;

import java.io.IOException;
import java.util.Map;

import hku.project.NgramRunner.nGramPlugInClass;

import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.lib.aggregate.ValueAggregatorJob;
import org.junit.Test;

public class NgramRunnerTest {
	@Test
	public void test() throws IOException {

		NgramRunner runner = new NgramRunner();
		Map<String, Boolean> nameLists = runner.getNameLists();
		System.out.println(nameLists);
		
		JobConf conf = ValueAggregatorJob.createValueAggregatorJob(
				null, new Class[] { nGramPlugInClass.class });
		
		JobClient.runJob(conf);
	}
}
