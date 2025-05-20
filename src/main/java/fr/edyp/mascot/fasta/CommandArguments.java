package fr.edyp.mascot.fasta;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

public class CommandArguments {

  public final static String DB_USAGE_COMMAND= "db_usage";
  public final static String SHORTEN_COMMAND= "shorten";
  public final static String REMOVE_EMPTY_COMMAND= "rem_empty";
  public final static String DUPLICATE_CMP_COMMAND= "cmp_duplicates";

  public final static String DUPLICATE_REPLACE_COMMAND= "replace_duplicates";
  public final static String EXTRACT_TAXO_COMMAND= "extract_taxo";


  @Parameters(commandNames =  {DB_USAGE_COMMAND}, commandDescription = "get information (last date...) on the usage of fasta db", separators = "=")
  public static class FastaDBUsageCommand {

    @Parameter(names = {"-db"}, description = "search information for specific fasta bank. If not specified, all fasta banks will be processed ", required = false, order = 0)
    public String dbName;
    @Parameter(names = {"-s"}, description = "path to the mascot searches log file", required = true, order = 1)
    public String searchesLogPath;

    @Parameter(names = {"-m"}, description = "path to the mascot dat file to read databases file status from. ")
    public String mascotDatPath;

    @Parameter(names = {"-o"}, description = "tsv file path to save result to (*.tsv). if not specified standard output will be used")
    public String outputFile;

    @Parameter(names = {"-h", "--help"}, help = true)
    public boolean help;

    @Parameter(names = {"-a", "--all"}, description = "get All usage dates, ,not only last ")
    public boolean all;
  }

  @Parameters(commandNames =  {SHORTEN_COMMAND}, commandDescription = "Try to shorten fasta accession by using car _ or / to split", separators = "=")
  public static class FastaShortenAccCommand {
    @Parameter(names = {"-i"}, description = "path to the input fasta file to process. Same file with _short suffix will be created", required = true)
    public String inputFile;
    @Parameter(names = {"-h", "--help"}, help = true)
    public boolean help;
  }

  @Parameters(commandNames =  {REMOVE_EMPTY_COMMAND}, commandDescription = "remove entries which have no sequences", separators = "=")
  public static class FastaRemoveEmptyCommand {
    @Parameter(names = {"-i"}, description = "path to the input fasta file to process. Same file with _clean suffix will be created", required = true)
    public String inputFile;
    @Parameter(names = {"-h", "--help"}, help = true)
    public boolean help;
  }

  @Parameters(commandNames =  {DUPLICATE_CMP_COMMAND}, commandDescription = "Search for duplicate in fasta file and compare their sequence. Result is given in (log) output.", separators = "=")
  public static class FastaCompareDuplicateCommand {
    @Parameter(names = {"-i"}, description = "path to the input fasta file to process.", required = true)
    public String inputFile;
    @Parameter(names = {"-h", "--help"}, help = true)
    public boolean help;
  }

  @Parameters(commandNames =  {DUPLICATE_REPLACE_COMMAND}, commandDescription = "Search for duplicate in fasta file and rename them using index. Result is saved in new fasta file.", separators = "=")
  public static class FastaReplaceDuplicateCommand {
    @Parameter(names = {"-i"}, description = "path to the input fasta file to process.", required = true)
    public String inputFile;
    @Parameter(names = {"-o"}, description = "path to the resulting fasta file.", required = true)
    public String outputFile;
    @Parameter(names = {"-h", "--help"}, help = true)
    public boolean help;
  }

  @Parameters(commandNames =  {EXTRACT_TAXO_COMMAND}, commandDescription = "Extract a taxomy from a fasta using mnemonic. Result is saved in new fasta file.", separators = "=")
  public static class FastaExtractTaxoCommand {
    @Parameter(names = {"-i"}, description = "path to the input fasta file to process.", required = true)
    public String inputFile;
    @Parameter(names = {"-o"}, description = "path to the resulting fasta file. Same as input with taxo mnemonic as suffix will be used if not specified", required = false)
    public String outputFile;
    @Parameter(names = {"-t"}, description = "taxonomy mnemonic to extract from 'input' fasta file.", required = true)
    public String taxoMnemo;

    @Parameter(names = {"-h", "--help"}, help = true)
    public boolean help;
  }
}
