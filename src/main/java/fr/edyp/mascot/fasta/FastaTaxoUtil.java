package fr.edyp.mascot.fasta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashSet;

public class FastaTaxoUtil {

  private final static Logger logger = LoggerFactory.getLogger(FastaTaxoUtil.class);

  /**
   * Read all fasta file entries, if UP format entry ends with specified taxo mnemonic
   * the entry will be copied into output file.
   * TODO : allow more complex regEx to get taxo from entry
   *
   * @param fastaIn : fasta file to search duplicates from
   * @param fastaOut : new fasta file with duplicates renamed
   * @param taxoMnemo : taxonomy mnemonic to extract
   *
   */
  public static void extractTaxonomy(File fastaIn, File fastaOut, String taxoMnemo) throws IOException {
    logger.info("Extract taxonomy "+taxoMnemo+" from "+fastaIn.getAbsolutePath()+" to "+fastaOut.getAbsolutePath());
    FileReader fr = new FileReader(fastaIn);
    FileWriter fw = new FileWriter(fastaOut);
    BufferedWriter writer = new BufferedWriter(fw);
    BufferedReader br = new BufferedReader(fr);
    String separator = " ";
    String line;
    int nbExtracted = 0;
    boolean sequenceMustBeWritten = false;
    while ((line = br.readLine()) != null) {

      if (line.startsWith(">")) { //new fasta entry
        sequenceMustBeWritten = false;
        int index = line.indexOf(separator);
        boolean hasDesc = index>=0;
        String currentAcc = line.substring(1);
        String desc = "";
        if(hasDesc) {
          currentAcc = line.substring(1, index);
          desc = line.substring(index);
        }

        if (currentAcc.endsWith(taxoMnemo)) {
          //this entry is of interest
          sequenceMustBeWritten = true;
          nbExtracted++;
          logger.debug(" found taxonomy in entry " + currentAcc);

          //Write new accessions & description to fastaOut
          writer.write(">");
          writer.write(currentAcc);
          writer.write(" ");
          writer.write(desc);
          writer.write('\n');
        }

      } else {
        //sequence lines, write as it is if entry is of interest
        if( sequenceMustBeWritten ) {
          writer.write(line);
          writer.write('\n');
        }
      }
    }

    writer.flush();
    logger.info("\n ** Found "+nbExtracted+" entries for taxonomy "+taxoMnemo);
    fr.close();
    fw.close();
  }
}
