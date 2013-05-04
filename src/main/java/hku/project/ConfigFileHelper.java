package hku.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class ConfigFileHelper {
	public interface ConfigFileTokenizer{
		public void toResult(Map result, String line);
		
	}

	

	
	/**
	 * @param <V>
	 * @param <K>
	 * @param result
	 * @param inputFileName
	 * @throws IOException
	 */
	public static <K,V> void loadFromFile(Map<K, V> result,
			String inputFileName, ConfigFileTokenizer tokenizer) throws IOException {
		InputStream in = ConfigFileHelper.class
				.getResourceAsStream("/"+inputFileName);

		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		try {
			String line = br.readLine();
			while (line != null) {
				tokenizer.toResult(result,line);
				line = br.readLine();
			}

		} finally {
			System.out.println("finish reading filter input");
			System.out.println(result);
			br.close();
		}
	}
}
