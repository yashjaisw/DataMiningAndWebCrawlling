import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainConsolidatedWriting {


    public void writeMainFile() throws IOException {

        VitaminShoppeCrawler vitaminShoppeCrawler = new VitaminShoppeCrawler();
        IHerbCrawler IHerbCrawler = new IHerbCrawler();
        GNCCrawler gncCrawler = new GNCCrawler();
        AmazonIndiaCrawler amazonIndiaCrawler = new AmazonIndiaCrawler();
        AmazonComCrawler amazonComCrawler = new AmazonComCrawler();
        List<AmazonCom> amazonComList = new ArrayList<>();
        List<AmazonIndia> amazonIndiaList = new ArrayList<>();
        List<IHerb> iHerbList = new ArrayList<>();
        List<VitaminShoppe> vitaminShoppeList = new ArrayList<>();
        List<GNC> gncList = new ArrayList<>();
        try {
            amazonComList = amazonComCrawler.readDataFromExcel(new ArrayList<>());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            amazonIndiaList = amazonIndiaCrawler.readDataFromExcel(new ArrayList<>());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            iHerbList = IHerbCrawler.readDataFromExcel(new ArrayList<>());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            vitaminShoppeList = vitaminShoppeCrawler.readDataFromExcel(new ArrayList<>());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            gncList = gncCrawler.readDataFromExcel(new ArrayList<>());
        } catch (Exception e) {
            e.printStackTrace();
        }


        Workbook workbook = new XSSFWorkbook();
        Sheet spreadsheet = workbook.createSheet("Main Consolidated");


        //Create row object
        Row row;
        int rowid = 0;
        int cellid = 0;
        row = spreadsheet.createRow(rowid++);
        for(String col: getMainFileColumnNames()){
            Cell cell = row.createCell(cellid++);
            cell.setCellValue(col);
        }
        //Create row object
        for (GNC gnc : gncList) {
            row = spreadsheet.createRow(rowid++);
            cellid = 0;

            for (String data : gnc.getAllValues()){
                Cell cell = row.createCell(cellid++);
                cell.setCellValue(data);
            }
        }
        for (VitaminShoppe vitaminShoppe : vitaminShoppeList) {
            row = spreadsheet.createRow(rowid++);
            cellid = 0;

            for (String data : vitaminShoppe.getAllValues()){
                Cell cell = row.createCell(cellid++);
                cell.setCellValue(data);
            }
        }

        for (IHerb iHerb : iHerbList) {
            row = spreadsheet.createRow(rowid++);
            cellid = 0;

            for (String data : iHerb.getAllValues()){
                Cell cell = row.createCell(cellid++);
                cell.setCellValue(data);
            }
        }

        for (AmazonCom amazonCom : amazonComList) {
            row = spreadsheet.createRow(rowid++);
            cellid = 0;

            for (String data : amazonCom.getAllValues()){
                Cell cell = row.createCell(cellid++);
                cell.setCellValue(data);
            }
        }

        for (AmazonIndia amazonIndia : amazonIndiaList) {
            row = spreadsheet.createRow(rowid++);
            cellid = 0;

            for (String data : amazonIndia.getAllValues()){
                Cell cell = row.createCell(cellid++);
                cell.setCellValue(data);
            }
        }
        spreadsheet.autoSizeColumn(1);
        spreadsheet.autoSizeColumn(2);
        spreadsheet.createFreezePane(0, 1);
        FileOutputStream out = null;
        //Write the workbook in file system
        try {
            out = new FileOutputStream(
                    new File("Main Consolidated Crawled Data.xlsx"));
            workbook.write(out);
            out.close();
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("Main Consolidated Crawled Data File might be already open");
            out.close();
            return;
        }

        System.out.println("Main Consolidated Crawled Data.xlsx written sucessfully");

    }


    public List<String> getMainFileColumnNames(){
        List<String> values = new ArrayList<>();
        values.add("searchKeyword");
        values.add("website");
        values.add("url");
        values.add("productName");
        values.add("productType");
        values.add("productCompany");
        values.add("description");
        values.add("itemCode");
        values.add("containerType");
        values.add("type");
        values.add("sizePerCountUnit");
        values.add("offerPrice");
        values.add("discountPrice");
        values.add("suggestedRetail");
        values.add("shippingFee");
        values.add("servingSize");
        values.add("servingsPerContainer");
        values.add("costPerServing");
        values.add("costPerUnit");
        values.add("shippingWeightOrDimensions");
        values.add("directionToUse");
        values.add("suppInfo");
        values.add("warnings");
        values.add("moreDescription");
        values.add("currency");
        values.add("reviewsCount");
        values.add("reviewsRating");
        values.add("reviews5Star");
        values.add("reviews4Star");
        values.add("reviews3Star");
        values.add("reviews2Star");
        values.add("reviews1Star");

        return values;
    }
}
