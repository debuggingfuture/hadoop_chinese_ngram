package hku.project;

import hku.project.ConfigFileHelper.ConfigFileTokenizer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.KeyValueTextInputFormat;
import org.apache.hadoop.mapred.lib.aggregate.ValueAggregatorBaseDescriptor;
import org.apache.hadoop.mapred.lib.aggregate.ValueAggregatorJob;

public class NgramRunner {

	private static Map<String, Boolean> nameLists = new HashMap<String, Boolean>();

//	static NameTokenizer nameTokenizer = new NameTokenizer();

	static {
		try {
			readListFromFile(nameLists);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static class nGramPlugInClass extends ValueAggregatorBaseDescriptor {
		// Count is case sensitive
		@Override
		public ArrayList<Entry<Text, Text>> generateKeyValPairs(Object key,
				Object val) {
			String countType = LONG_VALUE_SUM;
			ArrayList<Entry<Text, Text>> retv = new ArrayList<Entry<Text, Text>>();
			String line = val.toString();
			String[] ngramDataEntry = line.split("\\s+");
			if (ngramDataEntry.length > 0) {
				String ngram = key.toString();
				String year = ngramDataEntry[0];
				System.out.println(ngram);
				if (isTokenToAdd(ngram)) {
					Entry<Text, Text> e = generateEntry(countType, ngram + " "
							+ year, ONE);
					if (e != null) {
						retv.add(e);
					}
				}
			} else {
				System.out.println("Incorrect Entry, skip");
			}
			return retv;
		}

		private boolean filterByName(String token) {
			return getNameLists().containsKey(token);
		}

		private boolean isTokenToAdd(String token) {
			return filterByName(token);
		}
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
		JobConf conf = ValueAggregatorJob.createValueAggregatorJob(args,
				new Class[] { nGramPlugInClass.class });
		conf.setInputFormat(KeyValueTextInputFormat.class);
		JobClient.runJob(conf);
	}

	private static void readListFromFile(Map<String, Boolean> result)
			throws IOException {
		System.out.println("reading filter input");
		String inputFileName = "/name_input.txt";
//		ConfigFileHelper.loadFromFile(result, inputFileName, nameTokenizer);
	}

	public class NameTokenizer implements ConfigFileTokenizer {

		@Override
		public void toResult(Map result, String line) {
			StringTokenizer itr = new StringTokenizer(line);
			while (itr.hasMoreTokens()) {
				result.put(itr.nextToken(), true);
			}
		}

	}

	/**
	 * @param nameLists
	 *            the nameLists to set
	 */
	public static void setNameLists(Map<String, Boolean> nameLists) {
		NgramRunner.nameLists = nameLists;
	}

	/**
	 * @return the nameLists
	 */
	public static Map<String, Boolean> getNameLists() {
		return nameLists;
	}

}
