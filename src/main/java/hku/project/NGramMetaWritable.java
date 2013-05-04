package hku.project;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

//TODO writable comparable
public class NGramMetaWritable implements Writable {

	NGramMetaWritable() {

	}

	public NGramMetaWritable(int year, int yearMatchCount, double percentile) {
		super();
		this.year = year;
		this.yearMatchCount = yearMatchCount;
		this.percentile=percentile;
	}

	// public String nGram;
	public int year;
	public int yearMatchCount;
	public double percentile;

	@Override
	public void write(DataOutput out) throws IOException {
		// out.writeChars(nGram);
		out.writeInt(year);
		out.writeInt(yearMatchCount);
		out.writeDouble(percentile);

	}

	@Override
	public void readFields(DataInput in) throws IOException {
		year = in.readInt();
		yearMatchCount = in.readInt();
		percentile = in.readDouble();
	}

}
