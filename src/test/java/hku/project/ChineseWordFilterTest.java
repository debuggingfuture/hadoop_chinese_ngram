package hku.project;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

public class ChineseWordFilterTest {

	String[] strings = { "我是", "我a", "a我", "abc", "$我", "!我" };

	static Map<String, Boolean> testWords = new HashMap<String, Boolean>();

	static {
		testWords.put("我是", true);
		testWords.put("我a", false);
		testWords.put("我_NOUN", true);
		testWords.put("我_NOUN_我", true);
		testWords.put("我_NOUN_$", false);
		testWords.put("_NOUN_$", false);
		testWords.put("_NOUN_ADJ_我", true);
		testWords.put("我_NNOUN_$", false);
	}

	@Test
	public void testStringWithSimpleASCIIFilter() {
		// simple filter
		for (String aString : strings) {
			String regex = "^[\u0000-\u0080]+$";
			boolean b = aString.matches(regex);
			System.out.println(b);
		}
	}

	@Test
	public void testWithIsChineseChar() {
		Map<String, Boolean> testChars = new HashMap<String, Boolean>();
		testChars.put("我", true);
		testChars.put("a", false);
		testChars.put("!", false);
		testChars.put("$", false);
		testChars.put("們", true);
		testChars.put("あ", false);
		testChars.put("한", false);
		for (Entry<String, Boolean> entry : testChars.entrySet()) {
			String aString = entry.getKey();
			char[] charArray = aString.toCharArray();
			for (int i = 0; i < charArray.length; i++) {
				boolean expected = entry.getValue();
				int codePointAt = Character.codePointAt(charArray, i);
				boolean isChineseChar = ChineseWordFilter
						.isChineseChar(codePointAt);
				assertTrue(charArray[i] + "- expected:" + expected,
						isChineseChar == expected);
			}
		}
	}

	@Test
	public void testIsChineseWord() {
		List<String> exceptions = new ArrayList<String>();
		exceptions.add("_*NOUN_*");
		exceptions.add("_*ADJ_*");
		exceptions.add("\\s+");

		ChineseWordFilter filter = new ChineseWordFilter(exceptions);

		for (Entry<String, Boolean> entry : testWords.entrySet()) {
			String aString = entry.getKey();
			Boolean expected = entry.getValue();
			boolean isChineseWord = filter.isChineseWord(aString);
			assertTrue(aString + "- expected:" + expected,
					isChineseWord == expected);
		}
	}

}
