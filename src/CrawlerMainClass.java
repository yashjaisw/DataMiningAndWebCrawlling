import org.apache.poi.ss.usermodel.*;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CrawlerMainClass {

    public static void main(String args[])throws IOException {

        try {

            List<Configuirations> configuirationsList = new ArrayList<>();
            try {
                configuirationsList = readConfiguiration();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error while parsing Configuiration file. Please Maintain config file properly");
                return;
            }
            if(configuirationsList==null || configuirationsList.size()==0){
                System.out.println("Problem with config file");
                return;
            }
/*            List<String> keywords = new ArrayList<>();
            *//*keywords.add("vitality");
            keywords.add("Sexual");
            keywords.add("virility");*//*
            keywords.add("Ashwagandha");
           // keywords.add("melatonin");
            *//*
            keywords.add("Ashwagandha");/
            keywords.add("Shilajit Shuddha");
            keywords.add("Brahmi");
            keywords.add("Kali Musli");
            keywords.add("Safed Musli");
            keywords.add("Kaucha Beej");
            keywords.add("Shatavari");
            keywords.add("Gokhshur");
            keywords.add("Vitis Vinifera");
            keywords.add("Centella asiatica");
            keywords.add("Curculigo orchioides");
            keywords.add("Chlorophytum arundinaceum");
            keywords.add("Asparagus racemosus");
            keywords.add("Tribulus terrestris");
            keywords.add("Ashwagandha");
            keywords.add("Kapikacchu");
            keywords.add("Gokhshur");
            keywords.add("Safed musli ");
            keywords.add("Erandmula");
            keywords.add("Amalaki");
            keywords.add("Marich");
            keywords.add("Withania somnifera");
            keywords.add("Mucuna pruriens");
            keywords.add("Tribulus terrestris");
            keywords.add("Chlorophytum arundinaceum ");
            keywords.add("Ricinus communis");
            keywords.add("Sida cordifolia");
            keywords.add("Emblica officinalis");
            keywords.add("Piper nigrum");
            keywords.add("Bala");*/
            List<VitaminShoppe> vitaminShoppeList= new ArrayList<>();
            List<AmazonCom> amazonComList= new ArrayList<>();
            List<AmazonIndia> amazonIndiaList= new ArrayList<>();
            List<IHerb> iHerbList= new ArrayList<>();
            List<GNC> gncList= new ArrayList<>();

            try {
                VitaminShoppeCrawler vitaminShoppeCrawler = new VitaminShoppeCrawler();
                vitaminShoppeList = vitaminShoppeCrawler.crawlDataSearchWise(configuirationsList);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                IHerbCrawler IHerbCrawler = new IHerbCrawler();
                iHerbList = IHerbCrawler.crawlDataSearchWise(configuirationsList);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                GNCCrawler gncCrawler = new GNCCrawler();
                gncList = gncCrawler.crawlDataSearchWise(configuirationsList);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                AmazonIndiaCrawler amazonIndiaCrawler = new AmazonIndiaCrawler();
                amazonIndiaList = amazonIndiaCrawler.crawlDataSearchWise(configuirationsList);
            } catch (Exception e) {
                e.printStackTrace();
            }

         try {
                AmazonComCrawler amazonComCrawler = new AmazonComCrawler();
                amazonComList = amazonComCrawler.crawlDataSearchWise(configuirationsList);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                MainConsolidatedWriting mainConsolidatedWriting = new MainConsolidatedWriting();
                mainConsolidatedWriting.writeMainFile();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }catch (Exception e){
        }
    }

    public static List<Configuirations> readConfiguiration() throws IOException {
        List<Configuirations> configuirationsList = new ArrayList<>();
        File file = new File("config.xlsx");
        if(!file.exists()){
            return null;
        }
        FileInputStream inputStream = new FileInputStream(file);
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet spreadsheet = workbook.getSheet("config");
        Iterator<Row> rowIterator = spreadsheet.iterator();
        int rowId = 0;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            //For each row, iterate through all the columns
            if(rowId==0){
                rowId++;
                continue;
            }
            Iterator<Cell> cellIterator = row.cellIterator();
            int colId = 0;
            Configuirations configuirations = new Configuirations();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                if(colId==0){
                    String cellValue = cell.getStringCellValue();
                    configuirations.setKeyword(cellValue);
                }
                if(colId==1){
                    Boolean cellValue = cell.getBooleanCellValue();
                    configuirations.setAmazonIn(cellValue);
                }
                if(colId==2){
                    Boolean cellValue = cell.getBooleanCellValue();
                    configuirations.setAmazonCom(cellValue);

                }
                if(colId==3){
                    Boolean cellValue = cell.getBooleanCellValue();
                    configuirations.setVitaminShoppe(cellValue);

                }
                if(colId==4){
                    Boolean cellValue = cell.getBooleanCellValue();
                    configuirations.setiHerb(cellValue);

                }
                if(colId==5){
                    Boolean cellValue = cell.getBooleanCellValue();
                    configuirations.setGnc(cellValue);

                }
                colId++;
            }
            configuirationsList.add(configuirations);
        }
        return configuirationsList;
    }


}
