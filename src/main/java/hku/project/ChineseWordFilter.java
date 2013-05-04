package hku.project;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChineseWordFilter {
	static List<int[]> chineseCodePointRange = ChineseWordFilter
			.createChineseCodePointRange();

	public static List<int[]> createChineseCodePointRange() {
		// start with common for better performance
		List<String[]> utf8Ranges = new ArrayList<String[]>();
		utf8Ranges.add(new String[] { "\u4E00", "\u4DBF" });
		utf8Ranges.add(new String[] { "\u3400", "\u9FFF" });
		utf8Ranges.add(new String[] { "\u20000", "\u2A6DF" });
		utf8Ranges.add(new String[] { "\u2A700", "\u2B73F" });
		utf8Ranges.add(new String[] { "\uB740", "\u2B81F" });
		utf8Ranges.add(new String[] { "\uF900", "\uFAFF" });
		utf8Ranges.add(new String[] { "\u2F800", "\u2FA1F" });

		List<int[]> codePointRanges = new ArrayList<int[]>();
		for (String[] aRange : utf8Ranges) {
			int startCodePoint = aRange[0].codePointAt(0);
			int endCodePoint = aRange[1].codePointAt(0);
			codePointRanges.add(new int[] { startCodePoint, endCodePoint });
//			System.out.println(startCodePoint + ":" + endCodePoint);
		}
		return codePointRanges;
	}

	public static boolean isChineseChar(int codePoint) {
		Iterator<int[]> iterator = chineseCodePointRange.iterator();
		while (iterator.hasNext()) {
			int[] range = iterator.next();
			if (codePoint >= range[0] && codePoint <= range[1]) {
				return true;
			}
		}
		return false;
	}

	// non chinese chars allowed to be inside the word
	List<String> exceptions;

	ChineseWordFilter(List exceptions) {
		this.exceptions = exceptions;
	}

	public boolean isChineseWord(String s) {
		boolean isChineseWord=true;
		for (int i = 0; i < s.length(); i++) {
			int codePoint = s.codePointAt(i);
			boolean isChineseChar = isChineseChar(codePoint);
			if (!isChineseChar) {
//				System.out.println("Not Chinese Char Found:" + s.charAt(i)+"in"+s);
				// OK if this index is inside the occurence of exceptions
				isChineseWord=false;

				for (String exceptionRegex : exceptions) {
					Pattern pattern = Pattern.compile(exceptionRegex);
					Matcher matcher = pattern.matcher(s.substring(i));
					// since we loop the char in order, use the substring to check
					// if here start the word of exceptions
					if (matcher.find()) {
//						System.out.println("FIND EXCEPTION:"+exceptionRegex);
						int start = matcher.start();//relative to the substring
						if (start != 0) {
							return false;
						} else {
							// skip the exception
							i = i+matcher.end()-1;
							isChineseWord=true;
							break;
						}
					} 
				}
				// if no exception find we can conclude this word exist non-chinese
				// word which are also not in exceptions
			}
		}
		return isChineseWord;
	}
}