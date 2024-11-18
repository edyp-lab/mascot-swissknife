package fr.edyp.mascot.fasta;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.MissingCommandException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashSet;

public class FastaUtil {

  private final static Logger logger = LoggerFactory.getLogger(FastaUtil.class);

  static final JCommander jCmd = new JCommander();

  public static String parseCommand(String[] args) {

    try {
      jCmd.parse(args);
      String parsedCmd = jCmd.getParsedCommand();
      if (parsedCmd == null) {
        jCmd.usage();
        System.exit(1);
      }
      logger.info("Running " + parsedCmd + " command ...");
      return parsedCmd;
    } catch (MissingCommandException mce) {
      logger.warn("Invalid command specified ");
      jCmd.usage();
      System.exit(0);
    }
    return "";
  }


  /**
   * Entry formatted as Accession<separator>YYY
   * if Accession length is > 50 char, cut it using '/' or '_'
   * Clean Accession by replacing : , or " or ' by _
   * Write back accession +  entry as previously formatted
   *
   * @param fin specify fasta file to shorten accession for
   * @param fout specify output file to write new fasta file
   */
  public static void shortenAccession(File fin, File fout, String separator) throws IOException{

    FileReader fr = new FileReader(fin);
    FileWriter fw = new FileWriter(fout);
    BufferedWriter writer = new BufferedWriter(fw);
    BufferedReader br = new BufferedReader(fr);

    HashSet<String> names = new HashSet<>();
    String line;
    while ((line = br.readLine()) != null) {
      if (line.startsWith(">")) {
        int index = line.indexOf(separator); //"_" ...
        if (index != -1) {
          String accession = line.substring(index + 1); //Get accession using separator
          if (accession.length() > 50) {
            index = accession.indexOf('/');
            if (index != -1) {
              accession = accession.substring(0, index);
              if (accession.length() > 50) {
                System.out.println("error too long " + accession);
              }
            } else {
              index = accession.lastIndexOf('_');
              if (index != -1) {
                accession = accession.substring(0, index);
                if (accession.length() > 50) {
                  System.out.println("error too long " + accession);
                }
              } else {

                System.out.println("error too long " + accession);
              }
            }

          }
          if (names.contains(accession)) {
            System.out.println("duplicate " + accession);
          } else {
            names.add(accession);
          }

          accession = accession.replaceAll(",", "_");
          accession = accession.replaceAll("\"", "_");
          accession = accession.replaceAll("\'", "_");

          writer.write(">");
          writer.write(accession);
          writer.write(" ");
          writer.write(line.substring(1));
          writer.write('\n');
        } else {
          writer.write(line);
          writer.write('\n');
        }
      } else {
        writer.write(line);
        writer.write('\n');
      }
    }
    writer.flush();
    fr.close();
    fw.close();

  }

  public static void removeEmptyEntries(File fin, File fout) throws IOException{

    FileReader fr = new FileReader(fin);
    FileWriter fw = new FileWriter(fout);
    BufferedWriter writer = new BufferedWriter(fw);
    BufferedReader br = new BufferedReader(fr);

    String prevEntry = "";
    boolean correctEntry = false;
    String line;
    while ((line = br.readLine()) != null) {
      if (line.startsWith(">")) {
        prevEntry = line;
        correctEntry = false; //suppose incorrect entry
      } else {
        //sequence line. If prevEntry not empty, first sequence line. Verify it is correct
        if(StringUtils.isNotEmpty(prevEntry)) { //first seq line
          if(StringUtils.isNotEmpty(line)) {
            correctEntry = true;
            writer.write(prevEntry);
            writer.write('\n');
            prevEntry = ""; //reset entry
          }
        }
        if(correctEntry) {
          //write sequence
          writer.write(line); //write sequence
          writer.write('\n');
        }
      }
    }
    writer.flush();
    fr.close();
    fw.close();

  }

  public static void main(String[] args) {
    CommandArguments.FastaDBUsageCommand fastaDBUsageCommand = new CommandArguments.FastaDBUsageCommand();
    CommandArguments.FastaShortenAccCommand fastaShortenCommand = new CommandArguments.FastaShortenAccCommand();
    CommandArguments.FastaCompareDuplicateCommand fastaCmpDupCommand = new CommandArguments.FastaCompareDuplicateCommand();
    CommandArguments.FastaReplaceDuplicateCommand fastaReplaceDupCommand = new CommandArguments.FastaReplaceDuplicateCommand();
    CommandArguments.FastaExtractTaxoCommand fastaExtractTaxoCommand = new CommandArguments.FastaExtractTaxoCommand();
    CommandArguments.FastaRemoveEmptyCommand fastaRemoveEmptyCommand = new CommandArguments.FastaRemoveEmptyCommand();

    jCmd.addCommand(fastaDBUsageCommand);
    jCmd.addCommand(fastaShortenCommand);
    jCmd.addCommand(fastaCmpDupCommand);
    jCmd.addCommand(fastaReplaceDupCommand);
    jCmd.addCommand(fastaExtractTaxoCommand);
    jCmd.addCommand(fastaRemoveEmptyCommand);

    try {
      String parsedCmd = parseCommand(args);
      switch (parsedCmd) {
        case CommandArguments.DB_USAGE_COMMAND: {
          if (fastaDBUsageCommand.help) {
            jCmd.usage();
            System.exit(0);
          }

          DBUsageLog dbUsage = new DBUsageLog(fastaDBUsageCommand.searchesLogPath, fastaDBUsageCommand.mascotDatPath);
          dbUsage.printFastaDBInfo(fastaDBUsageCommand.dbName, fastaDBUsageCommand.outputFile);
          break;
        }

        case CommandArguments.SHORTEN_COMMAND: {
          if (fastaShortenCommand.help) {
            jCmd.usage();
            System.exit(0);
          }

          File fIn = new File(fastaShortenCommand.inputFile);
          if (!fIn.exists()) {
            logger.error("Can't find specified file " + fIn.getAbsolutePath());
            jCmd.usage();
            System.exit(1);
          }

          File fOut =  createFileWithSuffix(fIn, "_short" );

          FastaUtil.shortenAccession(fIn, fOut, "_");
          break;
        }

        case CommandArguments.REMOVE_EMPTY_COMMAND: {
          if (fastaRemoveEmptyCommand.help) {
            jCmd.usage();
            System.exit(0);
          }

          File fIn = new File(fastaRemoveEmptyCommand.inputFile);
          if (!fIn.exists()) {
            logger.error("Can't find specified file " + fIn.getAbsolutePath());
            jCmd.usage();
            System.exit(1);
          }

          File fOut =  createFileWithSuffix(fIn, "_clean" );
          FastaUtil.removeEmptyEntries(fIn, fOut);
          break;
        }


        case CommandArguments.DUPLICATE_CMP_COMMAND: {
          if (fastaCmpDupCommand.help) {
            jCmd.usage();
            System.exit(0);
          }

          File fIn = new File(fastaCmpDupCommand.inputFile);
          if (!fIn.exists()) {
            logger.error("Can't find specified file " + fIn.getAbsolutePath());
            jCmd.usage();
            System.exit(1);
          }

          FastaDuplicateManager.compareDuplicateInFasta(fIn, " ");
          break;
        }

        case CommandArguments.DUPLICATE_REPLACE_COMMAND: {
          if (fastaReplaceDupCommand.help) {
            jCmd.usage();
            System.exit(0);
          }
          File fIn = new File(fastaReplaceDupCommand.inputFile);
          File fOut = new File(fastaReplaceDupCommand.outputFile);
          if (!fIn.exists()) {
            logger.error("Can't find specified file " + fIn.getAbsolutePath());
            jCmd.usage();
            System.exit(1);
          }

          if (!fOut.exists()) {
            logger.error(" !!! Output file already exist. Can't run replace duplicate from" + fIn.getAbsolutePath());
            System.exit(1);
          }

          FastaDuplicateManager.compareDuplicateInFasta(fIn, " ");
          break;
        }

        case CommandArguments.EXTRACT_TAXO_COMMAND: {
          if (fastaExtractTaxoCommand.help) {
            jCmd.usage();
            System.exit(0);
          }
          File fIn = new File(fastaExtractTaxoCommand.inputFile);
          String outFileName =fastaExtractTaxoCommand.outputFile;
          File fOut = (StringUtils.isNotEmpty(outFileName)) ? new File(fIn.getParentFile(), outFileName) : createFileWithSuffix(fIn,fastaExtractTaxoCommand.taxoMnemo );
          FastaTaxoUtil.extractTaxonomy(fIn,fOut,fastaExtractTaxoCommand.taxoMnemo);
        }
      }
    } catch(Exception e) {
      logger.error("Error in FastaUtil: "+e.getMessage(), e);
      jCmd.usage();
      System.exit(1);
    }
  }

  private static File
  createFileWithSuffix(File sourceFile, String suffix){
    return new File(sourceFile.getParentFile(), FilenameUtils.getBaseName(sourceFile.getName()) + "_" + suffix+"."+  FilenameUtils.getExtension(sourceFile.getName()));
  }
}
