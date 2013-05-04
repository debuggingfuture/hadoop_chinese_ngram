Project Hadoop

Group:

Raymond 2012950506 (Group Leader)
Lau Chun Yin Vincent, 2007024536
Yao Ning
Melienna


DataSet:

http://storage.googleapis.com/books/ngrams/books/datasetsv2.html

(or refer to commands.sh for script to download the above dataset)
Program:
This program is specialized for  language analysis.
This applied Unicode filtering, linear regression in a map reduce manner.

The ChineseFrequentWordsRunner Job, is a analysis which try to find out which n-grams are having a sharp change in occurence by years, either positive or negative.

Two mode:
1. consider all years. will fill empty record if not exist (e.g. word may appear only after 1950, we fill 0 before that)
1. consider only years with record, i.e. if there is 1950,1951,1952), slope will be calculated base on these 3 only

The percentile threshold is used to filter away that has very low count which will have a high correlation



Threshold of word percentile: i.e. considering its count and total words counted in that year, only word that is of higher freq than this will be considered. i.e. filter out non-frequent word
R is the coefficient, i.e. measure of slope in this context



usage:
```bin/hadoop jar hadoop-ngram.jar hku.project.ChineseFrequentWordsRunner <<input_folder_path>> <<output_folder_path>> $1 $2 $3 <<consider all years, Y/N>> <<threshold for word percentile >> <<threshold of R>> <<group by # years>>```

```example:
bin/hadoop jar hadoop-ngram.jar hku.project.ChineseFrequentWordsRunner ngram_data ngram_output Y 0.00001 0.8 5 ```

```hku.project.ChineseFrequentWordsRunner ngram_data ngram_output N 0.00001 0.8 100```

