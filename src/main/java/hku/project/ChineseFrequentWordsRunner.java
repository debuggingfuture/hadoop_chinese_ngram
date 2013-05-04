package hku.project;

import hku.project.ConfigFileHelper.ConfigFileTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.iterators.ArrayIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math.stat.regression.SimpleRegression;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class ChineseFrequentWordsRunner {
	/*
	 * Threadshold = occurence / total words that year relative percentage will
	 * not be affected after words removed. i.e. TopK is still TopK
	 */
	private static final int FIRST_YEAR = 1510;
	private static final int LAST_YEAR = 2009;

	static double PERCENTILE_THRESHOLD;
	static double PERCENTILE_WEIGHT_BOOST = 100000; // help distinguish
													// no-occurence vs occurence
	static double R_THRESHOLD;
	static List<String> exceptions = new ArrayList<String>();
	static {
		exceptions.add("_*NOUN_*");
		exceptions.add("_*ADJ_*");
		exceptions.add("_*ADP_*");
		exceptions.add("_*ADV_*");
		exceptions.add("_*CONJ_*");
		exceptions.add("_*DET_*");
		exceptions.add("_*NOUN_*");
		exceptions.add("_*NUM_*");
		exceptions.add("_*PRON_*");
		exceptions.add("_*PRT_*");
		exceptions.add("_*VERB_*");
		exceptions.add("\\s+");
	}
	static ChineseWordFilter chineseWordFilter = new ChineseWordFilter(
			exceptions);
	static TotalCountTokenizer totalCountTokenizer = new TotalCountTokenizer();

	// load from original dataset
	static Map<Integer, Integer> totalYearCount = new HashMap<Integer, Integer>();
	private static boolean calculateRForAllYears;
	private static int YEAR_GROUP_SIZE;
	static {
		try {
			ConfigFileHelper.loadFromFile(totalYearCount, "total_counts.txt",
					totalCountTokenizer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	};
	private static void initParams(String[] params) {
		if ("Y".equalsIgnoreCase(params[0])) {
			calculateRForAllYears = true;
		}
		PERCENTILE_THRESHOLD = Double.parseDouble(params[1]);
		R_THRESHOLD = Double.parseDouble(params[2]);
		YEAR_GROUP_SIZE = Integer.parseInt(params[3]);
	}

	public static class ChineseWordMapper extends
			Mapper<Object, Text, Text, NGramMetaWritable> {

		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			
			String[] params = context.getConfiguration().getStrings("ARGS");
			initParams(params);
			
			
			boolean isChineseWord = chineseWordFilter.isChineseWord(key
					.toString());
			if (!isChineseWord) {
				return;
			}
			Text ngram = new Text(key.toString());
			// ngram = key.toString();
			String[] ngramDataEntry = value.toString().split("\\s+");
			if (ngramDataEntry.length == 3) {
				int year = Integer.parseInt(ngramDataEntry[0]);
				int yearMatchCount = Integer.parseInt(ngramDataEntry[1]);
				double yearTotalCount = totalYearCount.get(year);
				double percentile = yearMatchCount / yearTotalCount;

				// remove in map phase to improve efficiency
				if (!isMoreFrequentThanTresholdInYear(percentile)) {
					return;
				}
				if (year < FIRST_YEAR || year > LAST_YEAR) {
					System.out.println("Ignore year:" + year);
					return;
				}
				// use 0 as R
				// NgramRKey ngramRKey = new NgramRKey(ngram,0.0);
				//
				NGramMetaWritable ngramMeta = new NGramMetaWritable(year,
						yearMatchCount, percentile);
				// context.write(ngram, new DoubleWritable(percentile));
				// System.out.println(ngram.toString()+":"+ngramMeta.toString());
				context.write(ngram, ngramMeta);
			} else {
				System.out.println("Incorrect format");
			}

		}

		

		private boolean isMoreFrequentThanTresholdInYear(double percentile) {
			return percentile >= PERCENTILE_THRESHOLD;
		}

	}

	// Sort by R only
	// Filter it for report
	public static class ChineseWordReducer extends
			Reducer<Text, NGramMetaWritable, Text, Text> {

		public void reduce(Text key, Iterable<NGramMetaWritable> values,
				Context context) throws IOException, InterruptedException {

			String[] params = context.getConfiguration().getStrings("ARGS");
			initParams(params);
			// assume the occurence before the dataset, i.e. year 1509 is 0
			// and current occurence is 0 if not found
			// to find the long term trend
			SimpleRegression simpleRegression;

			String averagePercentileAndCount = "";
			simpleRegression = new SimpleRegression();
			if (calculateRForAllYears) {
				// for accuracy, fill in dummy 0s into data
				int years = LAST_YEAR - FIRST_YEAR + 1;
				double[][] data = new double[years][2];
				int[] countArray = new int[years];

				for (NGramMetaWritable val : values) {
					int offset = val.year - FIRST_YEAR;
					data[offset][0] = val.year; // x
					data[offset][1] = val.percentile * PERCENTILE_WEIGHT_BOOST; // y
					countArray[offset] = val.yearMatchCount;
					// System.out.println("YEAR:"+val.year+"PERCENTILE"+val.percentile);
				}
				simpleRegression.addData(data);

				averagePercentileAndCount = calculateAverage(years, data,
						countArray);
			} else {
				for (NGramMetaWritable val : values) {
					simpleRegression.addData(val.year, val.percentile);
				}
			}

			double r = simpleRegression.getR();

			if (Math.abs(r) > R_THRESHOLD) {
				context.write(key, new Text(String.valueOf(r) + "\t"
						+ averagePercentileAndCount));
			}
		}

		/**
		 * @param years
		 * @param data
		 * @param group
		 */
		private String calculateAverage(int years, double[][] data,
				int[] countArray) {
			int groupCount = years / YEAR_GROUP_SIZE;
			double group[] = new double[groupCount];
			double groupByCount[] = new double[groupCount];

			double sum = 0;
			double sumCount = 0;
			for (int i = 0; i < years; i++) {
				sum += (data[i][1] / PERCENTILE_WEIGHT_BOOST);
				sumCount += countArray[i];
				int termCount = ((i + 1) % YEAR_GROUP_SIZE == 0) ? YEAR_GROUP_SIZE
						: ((i + 1) % YEAR_GROUP_SIZE);

				if ((i + 1 == years) || (termCount == YEAR_GROUP_SIZE)) {
					double average_percentile = sum / termCount;
					double average_count = sumCount / termCount;
					int groupIndex = (i / YEAR_GROUP_SIZE);
					group[groupIndex] = average_percentile;
					groupByCount[groupIndex] = average_count;

					groupIndex++;
					sum = 0;
					sumCount=0;
				}
			}
			ArrayIterator arrayIterator = new ArrayIterator(group);
			ArrayIterator groupByCountIterator = new ArrayIterator(groupByCount);

			return StringUtils.join(arrayIterator, ",") + "\t"
					+ StringUtils.join(groupByCountIterator, ",");
		}
	}

	public static class TotalCountTokenizer implements ConfigFileTokenizer {

		@Override
		public void toResult(Map result, String line) {
			String[] entries = line.split("\\s+");
			for (String aYearEntry : entries) {
				String[] counts = aYearEntry.split(",");
				int year = Integer.parseInt(counts[0]);
				int yearTotal = Integer.parseInt(counts[1]);
				result.put(year, yearTotal);
			}
		}

	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException,
			InterruptedException, ClassNotFoundException {
		// JobConf conf = new JobConf();
		String calculateRForAllYearsArg = args[2];
		
		Configuration conf = new Configuration();

		String percentileThreshold = args[3];
		String rThreshold = args[4];
		String yearGroupSize = args[5];

		System.out.println("calculateRForAllYears:" + calculateRForAllYears);
		System.out.println("percentileThreshold:" + percentileThreshold);
		System.out.println("rThreshold:" + rThreshold);
		System.out.println("yearGroupSize:" + yearGroupSize);

		conf.setStrings("ARGS",
				calculateRForAllYearsArg,percentileThreshold,rThreshold,yearGroupSize);
		
		Job job = new Job(conf, "word count");
		job.setJarByClass(ChineseFrequentWordsRunner.class);
		job.setMapperClass(ChineseWordMapper.class);
		job.setReducerClass(ChineseWordReducer.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(NGramMetaWritable.class);
		job.setInputFormatClass(KeyValueTextInputFormat.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));


		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}

}
