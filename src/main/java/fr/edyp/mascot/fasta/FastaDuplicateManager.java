package fr.edyp.mascot.fasta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;

public class FastaDuplicateManager {

  private final static Logger logger = LoggerFactory.getLogger(FastaDuplicateManager.class);

  /**
   * Read all fasta file entries, if duplicate are found rename them with index suffixes.
   * A separator to identify accession in entries should be provided.
   * TODO allow more complex regEx for separator
   *
   * @param fastaIn : fasta file to search duplicates from
   * @param fastaOut : new fasta file with duplicates renamed
   * @param separator : simple separator between acc & description in entry.
   */
  public static void removeDuplicateInFasta(File fastaIn, File fastaOut, String separator) throws IOException {
    logger.info("Search Duplicate entries in "+fastaIn.getAbsolutePath()+" using acc separator <"+separator+">");
    FileReader fr = new FileReader(fastaIn);
    FileWriter fw = new FileWriter(fastaOut);
    BufferedWriter writer = new BufferedWriter(fw);
    BufferedReader br = new BufferedReader(fr);
    HashSet<String> names = new HashSet<>();
    String line;
    int nbDuplicate = 0;
    while ((line = br.readLine()) != null) {

      if (line.startsWith(">")) { //new fasta entry

        int index = line.indexOf(separator);
        boolean hasDesc = index>=0;
        String currentAcc = line.substring(1);
        String desc = "";
        if(hasDesc) {
          currentAcc = line.substring(1, index);
          desc = line.substring(index);
        }

        if (names.contains(currentAcc)) {
          //this entry was already found
          nbDuplicate++;
          logger.debug(" found duplicate for " + currentAcc);
          String accPref = currentAcc;
          int nbDup = 1;
          while (names.contains(currentAcc)) {
            currentAcc = accPref + "_"+nbDup;
            nbDup++;
          }
          logger.info("-- Renamed "+accPref+" to " + currentAcc);
          names.add(currentAcc);
        } else {
          names.add(currentAcc);
        }

        //Write new accessions & description to fastaOut
        writer.write(">");
        writer.write(currentAcc);
        writer.write(" ");
        writer.write(desc);
        writer.write('\n');

      } else {
        //sequence lines, just write as it is
        writer.write(line);
        writer.write('\n');
      }
    }
    writer.flush();
    logger.info("\n ** Found "+nbDuplicate+" duplicate entries ");
    fr.close();
    fw.close();
  }

  /**
   * Read all fasta file entries, if duplicate are found, compare their sequences.
   * A separator to identify accession in entries should be provided
   * TODO allow more complex regEx
   *
   * @param fasta : fasta file to get dupliacte from
   * @param separator : simple separator between acc & description in entry.
   */
  public static void compareDuplicateInFasta(File fasta, String separator) throws IOException {
    logger.info("Search Duplicate entries in "+fasta.getAbsolutePath()+" using acc separator <"+separator+">");
    FileReader fr = new FileReader(fasta);
    BufferedReader br = new BufferedReader(fr);
    HashMap<String,String> seqByAcc = new HashMap<>();
    int nbDuplicate = 0;
    int nbDupDiff = 0;

    String line;
    StringBuilder seq = new StringBuilder();
    String currentAcc = null;
    while ((line = br.readLine()) != null) {
      if (line.startsWith(">")) {
        //reset/save previous properties
        if(currentAcc!=null) {
          //Read entry was already found in fasta file ... compare sequence
          if(seqByAcc.containsKey(currentAcc)){
            nbDuplicate++;
            String prevSeq = seqByAcc.get(currentAcc);
            if(prevSeq.contentEquals(seq)){
              logger.info("-- Duplicate\t" + currentAcc+"\tSame sequences ");
            } else {
              nbDupDiff++;
              logger.info("-- Duplicate\t" + currentAcc + "\t!!! DIFF sequences !!!");
            }
          } else
            seqByAcc.put(currentAcc,seq.toString()); // First time entry found, save in map
        }

        //New entry, reset sequence
        seq = new StringBuilder();
        currentAcc = null;

        int index = line.indexOf(" ");
        boolean hasDesc = index>=0;
        currentAcc = line.substring(1);
        String desc = "";
        if(hasDesc) {
          currentAcc = line.substring(1, index);
          desc = line.substring(index);
        }

      } else { //read seq of current entry
        seq.append(line);
      }
    }

    logger.info("\n ** Found "+nbDuplicate+" duplicate entries with "+nbDupDiff+" with different sequences");
    fr.close();

  }

}
