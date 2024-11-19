package fr.edyp.mascot.fasta;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Currently parse searches log and display found fasta bank with the last usage date.
 * TODO : Add information (with options ? ) : users / find if bank is active (should access mascot.dat or use parser... )
 */
public class DBUsageLog {
  private final static Logger logger = LoggerFactory.getLogger(DBUsageLog.class);
  private final static int COL_JOB_ID = 0;
  private final static int COL_PROCESS_ID = 1;
  private final static int COL_DBNAMES = 2;
  private final static int COL_USER = 3;
  private final static int COL_USER_MAIL = 4;
  private final static int COL_TITLE = 5;
  private final static int COL_DAT_PATH= 6;
  private final static int COL_START_TIME = 7;
  private final static int COL_DURATION = 8;
  private final static int COL_STATUS = 9;
  private final static int COL_PR = 10;
  private final static int COL_TYPE = 11;
  private final static int COL_ENZYME = 12;
  private final static int COL_IP_ADD = 13;
  private final static int COL_USER_ID = 14;
  private final static int COL_PEAKLIST_FILE = 15;

  private final static int LAST_COL = COL_PEAKLIST_FILE;

  private final static String MONITOR_USER_PREFIX = "Monitor Test DB";

  private final HashMap<String, Boolean> fastaDbStatusByName;
  private final File searchesFile;
  private final File mascotDatFile;

  public DBUsageLog(String searchesPath, String mascotDatFilePath) {
    searchesFile = new File(searchesPath);
    mascotDatFile = (StringUtils.isNotEmpty(mascotDatFilePath))? new File(mascotDatFilePath) : null;
    fastaDbStatusByName = new HashMap<>();
    if(mascotDatFile!=null){
      try {
        readDbStatusInMap();
      } catch (IOException e) {
        throw new IllegalArgumentException("Error reading mascot dat file "+mascotDatFilePath);
      }
    }
  }


  private void readDbStatusInMap() throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(mascotDatFile));

    String line = br.readLine();
    boolean dbSectionStart = false;
    while (line != null) {
      if(dbSectionStart && line.trim().equals("end"))
        break; //End database section

      if(line.trim().equals("Databases")) {
        dbSectionStart = true;
        logger.debug(" ---DB Status, Start Databases section");
        line = br.readLine();
        continue;
      }

      if(dbSectionStart) { //In Databases section
        boolean activeStatus = true;
        String dbName ;

        String[] part = line.split("[ \t]");
        if(part[0].equals("#")) {//InvalideDB
          activeStatus = false;
          dbName = part[1];
        } else {
          dbName = part[0];
        }
        this.fastaDbStatusByName.put(dbName, activeStatus);
//        logger.debug("- Added "+dbName+" isActive ? "+activeStatus);
      }
      line = br.readLine();
    }

    br.close();
  }

  private String getStatusStr(String dbName){
    if(!fastaDbStatusByName.containsKey(dbName))
     return "D";
    else if (fastaDbStatusByName.get(dbName))
      return "A";
    else
      return "I";
  }

  public void printFastaDBInfo(String dbName, String outFile){
    BufferedWriter writer = null;
    try {
      File outputFile = (StringUtils.isNotEmpty(outFile)) ? new File(outFile) : null;
      boolean writeInfo = false;
      if(outputFile!=null) {
        FileWriter fw = new FileWriter(outFile);
        writer = new BufferedWriter(fw);
        writer.write("Name\tLast Usage Date\tStatus (<A>ctive, <I>nactive, <D>eleted)\n");
        writeInfo = true;
      }

      HashMap<String, DbInfo> infos = readSearchesLog(dbName);
      List<String> dbNames =  infos.keySet().stream().sorted().toList();
      for(String name :dbNames){
        DbInfo info = infos.get(name);
        if(writeInfo){
          String date = (info.found) ?info.lastUsage.toString() :"-";
          writer.write(name+"\t"+date+"\t"+info.status+"\n");
        } else {
          if (info.found)
            logger.info("DB Fasta\t" + name + "\tLast search done on:\t" + info.lastUsage+"\tactive ?\t"+info.status);
          else
            logger.info("DB Fasta\t" + name + "\twas NOT Found in searches log.\t \tactive?\t"+info.status);
        }
      }

      if(writeInfo)
        writer.flush();

    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if(writer !=null) {
        try {
          writer.close();
        } catch (IOException ignore) {
        }
      }
    }

  }

  private HashMap<String, DbInfo> readSearchesLog(String dbName) throws IOException {

    BufferedReader tsvReader = null;

    HashMap<String, DbInfo> infoByDbName = new HashMap<>();
    boolean readAll = false;

    try {
      tsvReader = new BufferedReader(new FileReader(searchesFile));

      String line = tsvReader.readLine();
      DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("eee MMM d HH:mm:ss yyyy", Locale.ENGLISH);
      if(dbName != null)
        infoByDbName.put(dbName,new DbInfo(dbName));
      else
        readAll = true;
      int nbLineRead = 0;
      String msg = readAll ? "for all databases." : " for "+dbName;
      logger.info(" Start read searches log file "+msg);

      while (line != null) {
       // System.out.println(" -----------------  " );
        nbLineRead++;

        String[] lineItems = line.split("\t"); //splitting the line and adding its items in String[]
        int max = Math.min(LAST_COL+1, lineItems.length);
        int index =0;
        boolean goToNextLine = false;

        List<DbInfo> currentDbInfos = new ArrayList<>();
        if(!readAll)
          currentDbInfos.add(infoByDbName.get(dbName));

        while (index<max){
          String value = lineItems[index];

          //Col are read in index order. StartTime will be process only if previous col are corrects.
          switch (index) {

            case COL_DBNAMES :
              if(value==null || value.trim().isEmpty()) {
                goToNextLine = true;
                break;
              }
              List<String> dbs = Arrays.stream(value.split(",")).toList();
              if(readAll) {
                for (String nextDbname:  dbs) {
                  if(!infoByDbName.containsKey(nextDbname))
                    infoByDbName.put(nextDbname, new DbInfo(nextDbname));
                  DbInfo nextDbInfo = infoByDbName.get(nextDbname);
                  if(nextDbInfo.status.equals("-") && mascotDatFile!=null){
                      nextDbInfo.status = getStatusStr(nextDbname);
                  }
                  currentDbInfos.add(nextDbInfo);
                }
              } else {
                if(!dbs.contains(dbName)) {
                  goToNextLine = true;
                } else if (mascotDatFile!=null){
                  currentDbInfos.get(0).status = getStatusStr(dbName);
                }
              }
              break;

            case COL_USER:
              if(value !=null && value.startsWith(MONITOR_USER_PREFIX)) {//not a real search
                goToNextLine = true;
              } else{
                currentDbInfos.forEach(di -> di.found=true);
              }
              break;

            case COL_START_TIME:
              if(value!=null) {
                value = value.trim().replaceAll("  ", " ");
                try {
                  LocalDate currentDate = LocalDate.parse(value, dateFormat);
                  currentDbInfos.forEach(di -> {
                    if (di.lastUsage.isBefore(currentDate)) {
                      di.lastUsage = currentDate;
                    }
                  });
                } catch (DateTimeParseException dtpe){
                  logger.debug(" ------------ Line "+nbLineRead+" ERROR Parsing "+value+". Line skipped for :");
                  currentDbInfos.forEach(di -> {
                    logger.debug(" - "+di.name);
                  });
                }
              }
          }

          if(goToNextLine)
            break;
//          System.out.println("COL " + index + " VALUE " + value);
          index++;
        }
        line = tsvReader.readLine();
      }
      tsvReader.close();
      logger.info(" Read "+nbLineRead+" lines ...");
      return infoByDbName;

    } finally {
      if(tsvReader!= null)
        tsvReader.close();
    }
  }


  static class DbInfo {
    LocalDate lastUsage;
    String name;
    boolean found;
    String status;
    public  DbInfo(String dbname){
      name = dbname;
      found = false;
      status="-";
      lastUsage = LocalDate.of(1900, 1, 1);
    }
  }
}
