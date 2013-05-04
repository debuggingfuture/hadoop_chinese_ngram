package hku.project;

import org.apache.commons.math.stat.regression.SimpleRegression;
import org.junit.Before;
import org.junit.Test;

public class RegressionTest {

	SimpleRegression simpleRegression;

	@Before
	public void setup() {
		simpleRegression = new SimpleRegression();
	}

	@Test
	public void testPositive() {

		simpleRegression.addData(1950, 10);

		simpleRegression.addData(1960, 20);

		simpleRegression.addData(1970, 30);
		simpleRegression.addData(2000, 1000);
		printStat();
	}

	@Test
	public void testNegative() {
		simpleRegression.addData(1950, 200);
		simpleRegression.addData(1960, 20);
		simpleRegression.addData(1970, 30);
		simpleRegression.addData(2000, 20);
		printStat();
	}

	@Test
	public void testMiddle() {
		simpleRegression.addData(1940, 1000);
		simpleRegression.addData(1950, 1000);
		simpleRegression.addData(1960, 100000);
		simpleRegression.addData(1970, 1000);
		simpleRegression.addData(2000, 2000);
		printStat();
	}

	public void printStat() {
		double slope = simpleRegression.getSlope();
		double r = simpleRegression.getR();
		System.out.println(slope + "r:" + r);
	}
}
