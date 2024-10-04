# mascot-swissknife



## Getting started

A set of small tools to process fasta file or get mascot bank information. Use the `run.bat` batch file to run the available commands. 
From Windows command prompt (cmd) move to the mascot-swissknife folder `cd mascot-swissknife-<version>`.

To display the list of commands (and their parameters) type:
* `run.bat --help` for all available commands


## Command examples

### on Fasta files

- To **Search** for duplicate accession in fasta file and compare their sequence.
```
run.bat cmp_duplicates -i <path/to/fasta> 
```
The result will be displayed in log output : standard output by default may be changed in logback.xml config file

- To **Rename** duplicate accessions in fasta file using indexes.
```
run.bat replace_duplicates -i <path/to/fasta> -o <path/to/new/fasta.file>
```
The result will be saved in specified output fasta file.

- To **Shorten** accessions in fasta file using specific characters as separator. Characters used to split accession are '_' or '/'
```
run.bat shorten -i <path/to/fasta> 
```
The result will be saved in output fasta file named as input file with _short as suffix.
*TODO* allow configurable separator to define where to split accession

---
*TODO*: currently accession/description separator used is space. Allow more generic regEx should be added.

---

### On Mascot searches and databases

- To get some information on one or all searched databank. 
```
run.bat db_usage -db "Sp_Trembl" -s "c:\mascot\log\searches.log" 
```

Where -db is the name of the bank to get information for. If not specified all bank will be considered.
To get bank status, the -m option should be specified to indicate path to mascot.dat file 
