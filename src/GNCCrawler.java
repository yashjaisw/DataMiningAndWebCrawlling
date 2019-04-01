import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class GNCCrawler {

    public static int counted = 0;

    public List<GNC> getGNCCrawledData(List<String> urlList, String searchKeyword) throws IOException {
        List<GNC> gncList = new ArrayList<>();
        for(String url:urlList) {
            GNC gnc = new GNC();
            try {
                System.out.println("GNC : Crawl count: " + ++counted + " Crawling URL: " + url);
                InputStream response = CrawlerHttpClient.loadContentByHttpClient(url,HttpHeaderConstants.USER_AGENT4);
                Document document = null;
                if(response!=null) {
                    document = Jsoup.parse(response, "UTF-8", "");
                }
                int counter=1;
                while(document==null) {
                    if (counter >= 5) {
                        break;
                    }
                    // 2nd attempt
                    System.out.println("Crawler blocked. Trying Another User-Agent");
                    response = CrawlerHttpClient.loadContentByHttpClient(url,HttpHeaderConstants.USER_AGENT2);
                    if(response!=null) {
                        document = Jsoup.parse(response, "UTF-8", "");
                    }
                    counter++;
                    if (document == null) {
                        // 3rd attempt
                        System.out.println("Crawler blocked. Trying Another User-Agent");
                        response = CrawlerHttpClient.loadContentByHttpClient(url,HttpHeaderConstants.USER_AGENT3);
                        if(response!=null) {
                            document = Jsoup.parse(response, "UTF-8", "");
                        }
                        counter++;
                    }
                    if (document == null) { // 4th attempt
                        System.out.println("Crawler blocked. Trying Another User-Agent");
                        response = CrawlerHttpClient.loadContentByHttpClient(url,HttpHeaderConstants.USER_AGENT1);
                        if(response!=null) {
                            document = Jsoup.parse(response, "UTF-8", "");
                        }
                        counter++;
                    }
                    if (document == null) { // 5th attempt
                        System.out.println("Crawler blocked. Trying Another User-Agent");
                        response = CrawlerHttpClient.loadContentByHttpClient(url,HttpHeaderConstants.USER_AGENT6);
                        if(response!=null) {
                            document = Jsoup.parse(response, "UTF-8", "");
                        }
                        counter++;
                    }
                    if (document == null) { // 6th attempt
                        System.out.println("Crawler blocked. Trying Another User-Agent");
                        response = CrawlerHttpClient.loadContentByHttpClient(url,HttpHeaderConstants.USER_AGENT6);
                        if(response!=null) {
                            document = Jsoup.parse(response, "UTF-8", "");
                        }
                        counter++;
                    }
                }
                if(document==null){
                    System.out.println("Couln't crawl url: " + url + " because server is unresponsive0");
                    continue;
                }

                document.outputSettings(new Document.OutputSettings().prettyPrint(false));
                document.select("br").after("\\n");
                Element productDetailsEle = document.getElementById("product-content");

                //Pro Name
                try {
                    Element productNameClass = productDetailsEle.getElementsByClass("product-name").get(0);
                    String productName = productNameClass.text();
                    gnc.setProductName(productName);
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                ////Product Type

                //Product Comapny

                //Description
                try {
                    Element productDescription = document.getElementsByClass("product-information").get(0);
                    Elements productContent = productDescription.getElementsByClass("content");
                    for(Element productContentClass:productContent){
                        if(productContentClass.select("h4").get(0).text().contains("Description")){
                            String desc = productContentClass.getElementsByClass("output").get(0).text();
                            gnc.setDescription(desc);
                        }
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                //Item Code
                try {
                    Element itemClass = productDetailsEle.getElementsByClass("product-number").get(0);
                    String itemNo = itemClass.select("span").get(0).text();
                    gnc.setItemCode(itemNo);
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                //OfferPrice
                try {
                    Element priceEle = productDetailsEle.getElementsByClass("price-sales").get(0);
                    String offerPrice = priceEle.text();
                    gnc.setOfferPrice(offerPrice);
                } catch (Exception e) {
                  //  e.printStackTrace();
                }

                //Shipping Fee
                try {
                    Element shippingFeeParent = productDetailsEle.getElementsByClass("promotion-callout").get(0);
                    Element shippingFeeChild = shippingFeeParent.getElementsByClass("promo-item").get(0);
                    String shippingFee = shippingFeeChild.text();
                    gnc.setShippingFee(shippingFee);
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                //Serving Size
                try {
                    Elements supplimentInformationParentClass = document.getElementsByClass("supplement-information");
                    Elements tdSuppInfo = supplimentInformationParentClass.select("table").get(1).select("td");
                    for(Element tdSuppInfoEle:tdSuppInfo){
                        if(tdSuppInfoEle.text().contains("Serving Size")){
                            String servingSize = tdSuppInfoEle.text();
                            servingSize = servingSize.split("\\\\n")[0];
                            servingSize = servingSize.split("Serving Size")[1].trim();
                            servingSize = servingSize.split(" ")[0];
                            gnc.setServingSize(servingSize);
                            break;
                        }
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }
                //Serving Per Container
                try {
                    Elements supplimentInformationParentClass = document.getElementsByClass("supplement-information");
                    Elements tdSuppInfo = supplimentInformationParentClass.select("table").get(1).select("td");
                    for(Element tdSuppInfoEle:tdSuppInfo){
                        if(tdSuppInfoEle.text().contains("Servings Per Container")){
                            String servingPerContainer = tdSuppInfoEle.text();
                            servingPerContainer = servingPerContainer.split("\\\\n")[1];
                            servingPerContainer = servingPerContainer.split("Servings Per Container")[1].trim();
                            servingPerContainer = servingPerContainer.split(" ")[0];
                            gnc.setServingPerContainer(servingPerContainer);
                            break;
                        }
                    }
                } catch (Exception e) {
                   // e.printStackTrace();
                }

                //Supp Info
                try {
                    Element supplimentInformationParentClass = document.getElementsByClass("supplement-information").get(0)
                            .getElementsByClass("info").get(0);
                    String suppInfo = supplimentInformationParentClass.text().replaceAll("\\\\n",System.lineSeparator());
                    gnc.setSuppInfo(suppInfo);
                } catch (Exception e) {
                   // e.printStackTrace();
                }
                //Directions to use
                try {
                    Element supplimentInformationParentClass = document.getElementsByClass("supplement-information").get(0)
                            .getElementsByClass("info").get(1);
                    String directions = supplimentInformationParentClass.text().replaceAll("\\\\n",System.lineSeparator());
                    gnc.setDirectionToUse(directions);
                } catch (Exception e) {
                  //  e.printStackTrace();
                }

                //Warnings
                try {
                    Element supplimentInformationParentClass = document.getElementsByClass("supplement-information").get(0)
                            .getElementsByClass("info").get(1);

                    Elements outputs = supplimentInformationParentClass.getElementsByClass("output");
                    for(Element output:outputs){
                        if(output.text().contains("Warnings")){
                            String warning = output.text();
                            warning = warning.split("\\\\n")[1];
                            gnc.setWarnings(warning);
                            break;
                        }
                    }
                } catch (Exception e) {
                   // e.printStackTrace();
                }

                try {
                    Integer capsules = Integer.parseInt(gnc.getServingPerContainer());
                    Integer serveSize = Integer.parseInt(gnc.getServingSize());
                    Integer sizePercount = capsules * serveSize;
                    gnc.setSizePerCountUnit(sizePercount.toString());
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                //Cost Per Serving
                try {
                    Integer servingSizeCount = Integer.parseInt(gnc.getSizePerCountUnit());
                    String offerPrice = gnc.getOfferPrice().replace("$","");
                    offerPrice = offerPrice.replace(" ","");
                    Double offerPriceDouble = Double.parseDouble(offerPrice);
                    Double costPerCount = offerPriceDouble/servingSizeCount;
                    costPerCount = round(costPerCount,2);
                    gnc.setCostPerServing(costPerCount.toString());
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                //Cost PerCapsule
                try {
                    Integer totalUnits = Integer.parseInt(gnc.getServingPerContainer());
                    String offerPrice = gnc.getOfferPrice().replace("$","");
                    offerPrice = offerPrice.replace(" ","");
                    Double offerPriceDouble = Double.parseDouble(offerPrice);
                    Double costPerUnit = offerPriceDouble/totalUnits;
                    costPerUnit = round(costPerUnit,2);
                    gnc.setCostPerUnit(costPerUnit.toString());
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                //Review count

                try {
                    Element reviewId = document.getElementById("TurnToReviewsContent");
                    Elements reviewClass = reviewId.getElementsByClass("TTreviewCount");
                    String reviewCount = reviewClass.text();
                    if(reviewCount.split(" ").length > 1){
                        reviewCount = reviewCount.split(" ")[0];
                    }
                    gnc.setReviewsCount(reviewCount);
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                //Review rating
                try {
                    Element reviewRatingId = document.getElementById("TTreviewSummaryAverageRating");
                    String reviewRating = reviewRatingId.text();
                    if(reviewRating.split(" ").length > 1){
                        reviewRating = reviewRating.split(" ")[0];
                    }
                    gnc.setReviewsRating(reviewRating);
                } catch (Exception e) {
                   // e.printStackTrace();
                }

                //star
                try {
                    Element fiveStarId = document.getElementById("TTreviewSummaryBreakdown-5");
                    String fiveStar = fiveStarId.text();
                    gnc.setReviews5Star(fiveStar);
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                try {
                    Element fourStarId = document.getElementById("TTreviewSummaryBreakdown-4");
                    String fourStar = fourStarId.text();
                    gnc.setReviews4Star(fourStar);
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                try {
                    Element threeStarId = document.getElementById("TTreviewSummaryBreakdown-3");
                    String threeStar = threeStarId.text();
                    gnc.setReviews3Star(threeStar);
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                try {
                    Element twoStarId = document.getElementById("TTreviewSummaryBreakdown-2");
                    String twoStar = twoStarId.text();
                    gnc.setReviews2Star(twoStar);
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                gnc.setContainerType("");
                gnc.setDiscountPrice("");
                gnc.setSuggestedRetail("");
                gnc.setShippingWeightOrDimensions("");
                gnc.setMoreDescription("");
                gnc.setSearchKeyword(searchKeyword);
                gnc.setUrl(url);
                gnc.setCurrency("$");

                //Reviews
                try {
                    Element reviewsId = document.getElementById("TTreviews");
                    List<Reviews> reviewsList = new ArrayList<>();
                    Elements reviewsClasses = reviewsId.getElementsByClass("TTreview");
                    for(Element reviews1:reviewsClasses){
                        Reviews reviews = new Reviews();
                        //Review Title
                        Element reviewTitleEle = reviews1.getElementsByClass("TTreviewTitle").get(0);
                        String reviewTitle = reviewTitleEle.text();
                        reviews.setReviewTitle(reviewTitle);

                        //Review
                        Element reviewEle = reviews1.getElementsByClass("TTreviewBody").get(0);
                        String review = reviewEle.text();
                        reviews.setReview(review);

                        //Rating
                        try {
                            Element ratingEle = reviews1.getElementsByClass("TTrevCol1").get(0);
                            Element ratingMeta = ratingEle.getElementsByTag("meta").get(0);
                            String ratingValue = ratingMeta.attr("content");
                            reviews.setRating(ratingValue);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        //User Data
                        try {
                            Element userDataEle = reviews1.getElementsByClass("TTrevCol3").get(0);
                            Elements userDataClass = userDataEle.select("div");
                            String date = userDataClass.get(1).text();
                            String userName = userDataClass.get(2).text();
                            String userLocation = userDataClass.get(3).text();
                            reviews.setUserName(userName);
                            reviews.setUserLocation(userLocation);
                            reviews.setDate(date);
                            reviews.setProductName(gnc.getProductName());
                            reviews.setWebsite(gnc.getWebsite());
                            reviews.setProductUrl(gnc.getUrl());
                            reviews.setItemCode(gnc.getItemCode());
                            reviewsList.add(reviews);
                        } catch (Exception e) {
                          //  e.printStackTrace();
                        }
                    }

                    gnc.setReviews(reviewsList);
                } catch (Exception e) {
                    //e.printStackTrace();
                }
                gncList.add(gnc);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return gncList;
    }


    public List<GNC> crawlDataSearchWise(List<Configuirations> configuirationsList) throws IOException {
        String searchUrl ="https://www.gnc.com/search?lang=default&sz=10000&q=";
        Set<GNC> gncHashSet = new HashSet<>();
        List<String> keywordsList = new ArrayList<>();
        for(Configuirations configuirations:configuirationsList){
            if(configuirations.getGnc()){
                keywordsList.add(configuirations.getKeyword());
            }
        }
        if(keywordsList.size()<1){
            return new ArrayList<>();
        }
        for(String keyword:keywordsList) {
            List<String> urlsToCrawl = new ArrayList<>();
            String search = searchUrl + (keyword.replaceAll(" ","+"));
            try {
                InputStream response = CrawlerHttpClient.loadContentByHttpClient(search,HttpHeaderConstants.USER_AGENT4);
                Document document = null;
                if(response!=null) {
                    document = Jsoup.parse(response, "UTF-8", "");
                }
                int counter=1;
                while(document==null) {
                    if (counter >= 5) {
                        break;
                    }
                    // 2nd attempt
                    System.out.println("Crawler blocked. Trying Another User-Agent");
                    response = CrawlerHttpClient.loadContentByHttpClient(search,HttpHeaderConstants.USER_AGENT2);
                    if(response!=null) {
                        document = Jsoup.parse(response, "UTF-8", "");
                    }
                    counter++;
                    if (document == null) {
                        // 3rd attempt
                        System.out.println("Crawler blocked. Trying Another User-Agent");
                        response = CrawlerHttpClient.loadContentByHttpClient(search,HttpHeaderConstants.USER_AGENT3);
                        if(response!=null) {
                            document = Jsoup.parse(response, "UTF-8", "");
                        }
                        counter++;
                    }
                    if (document == null) { // 4th attempt
                        System.out.println("Crawler blocked. Trying Another User-Agent");
                        response = CrawlerHttpClient.loadContentByHttpClient(search,HttpHeaderConstants.USER_AGENT1);
                        if(response!=null) {
                            document = Jsoup.parse(response, "UTF-8", "");
                        }
                        counter++;
                    }
                    if (document == null) { // 5th attempt
                        System.out.println("Crawler blocked. Trying Another User-Agent");
                        response = CrawlerHttpClient.loadContentByHttpClient(search,HttpHeaderConstants.USER_AGENT6);
                        if(response!=null) {
                            document = Jsoup.parse(response, "UTF-8", "");
                        }
                        counter++;
                    }
                    if (document == null) { // 6th attempt
                        System.out.println("Crawler blocked. Trying Another User-Agent");
                        response = CrawlerHttpClient.loadContentByHttpClient(search,HttpHeaderConstants.USER_AGENT6);
                        if(response!=null) {
                            document = Jsoup.parse(response, "UTF-8", "");
                        }
                        counter++;
                    }
                }
                if(document==null)
                {
                    System.out.println("Unresponsive website");
                    continue;
                }

                Boolean shouldSkip = false;
                // Check for substitute results
                try {
                    Element breadCrumbClass = document.getElementsByClass("breadcrumb").get(0);
                    if(breadCrumbClass.select("a").size() > 1) {
                        Element aTag = breadCrumbClass.select("a").get(1);
                        if(!aTag.text().equalsIgnoreCase(keyword)){
                            System.out.println("Skiping Keyword: " + keyword + " Showing results for : " + aTag.text() + " instead");
                            shouldSkip = true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(shouldSkip){
                    continue;
                }
                try {
                    Element liId = document.getElementById("search-result-items");
                    Elements liClass = liId.getElementsByClass("grid-tile");
                    for (Element liEle:liClass){
                        Element productTitle = liEle.getElementsByClass("product-name").get(0);
                        Element aHref = productTitle.select("a").get(0);
                        String href = aHref.attr("abs:href");
                        urlsToCrawl.add(href);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("GNC.com : Found " + urlsToCrawl.size() + " results for keyword: " + keyword);
            List<GNC> gncList =  getGNCCrawledData(urlsToCrawl,keyword);
            gncHashSet.addAll(gncList);
        }
        List<GNC> sortedList = new ArrayList<>(gncHashSet);
        System.out.println("Done: "+ gncHashSet.size());
        List<Reviews> reviewsList = new ArrayList<>();
        try {
            reviewsList = readReviewsFromExcel(sortedList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sortedList = readDataFromExcel(sortedList);
        try {
            sortedList.sort(Comparator.comparing(GNC::getSearchKeyword));
        } catch (Exception e) {
            e.printStackTrace();
        }
        writeDataToExcel(sortedList);
        writeReviewsToExcel(reviewsList);
        return sortedList;
    }

    public List<GNC> readDataFromExcel(List<GNC> gncList) throws IOException {
        List<GNC> gncFinalList = new ArrayList<>();
        File file = new File("Consolidated GNC Crawled Data.xlsx");
        if(!file.exists()){
            return gncList;
        }
        FileInputStream inputStream = new FileInputStream(file);
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet spreadsheet = workbook.getSheet("GNC");
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
            GNC gnc = new GNC();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                String cellValue = cell.getStringCellValue();
                if(colId==0){
                    gnc.setSearchKeyword(cellValue);
                }
                if(colId==1){
                    gnc.setWebsite(cellValue);
                }
                if(colId==2){
                    gnc.setUrl(cellValue);

                }
                if(colId==3){
                    gnc.setProductName(cellValue);

                }
                if(colId==4){
                    gnc.setProductType(cellValue);

                }
                if(colId==5){
                    gnc.setProductCompany(cellValue);

                }
                if(colId==6){
                    gnc.setDescription(cellValue);

                }
                if(colId==7){
                    gnc.setItemCode(cellValue);

                }
                if(colId==8){
                    gnc.setContainerType(cellValue);

                }
                if(colId==9){
                    gnc.setType(cellValue);

                }
                if(colId==10){
                    gnc.setSizePerCountUnit(cellValue);

                }
                if(colId==11){
                    gnc.setOfferPrice(cellValue);

                }
                if(colId==12){
                    gnc.setDiscountPrice(cellValue);

                }
                if(colId==13){
                    gnc.setSuggestedRetail(cellValue);

                }
                if(colId==14){
                    gnc.setShippingFee(cellValue);

                }
                if(colId==15){
                    gnc.setServingSize(cellValue);

                }
                if(colId==16){
                    gnc.setServingPerContainer(cellValue);

                }
                if(colId==17){
                    gnc.setCostPerServing(cellValue);

                }
                if(colId==18){
                    gnc.setCostPerUnit(cellValue);

                }
                if(colId==19){
                    gnc.setShippingWeightOrDimensions(cellValue);

                }
                if(colId==20){
                    gnc.setDirectionToUse(cellValue);

                }
                if(colId==21){
                    gnc.setSuppInfo(cellValue);

                }
                if(colId==22){
                    gnc.setWarnings(cellValue);

                }
                if(colId==23){
                    gnc.setMoreDescription(cellValue);

                }
                if(colId==24){
                    gnc.setCurrency(cellValue);

                }
                if(colId==25){
                    gnc.setReviewsCount(cellValue);

                }
                if(colId==26){
                    gnc.setReviewsRating(cellValue);

                }
                if(colId==27){
                    gnc.setReviews5Star(cellValue);

                }
                if(colId==28){
                    gnc.setReviews4Star(cellValue);

                }
                if(colId==29){
                    gnc.setReviews3Star(cellValue);

                }
                if(colId==30){
                    gnc.setReviews2Star(cellValue);

                }
                if(colId==31){
                    gnc.setReviews1Star(cellValue);

                }
                colId++;
            }
            gncFinalList.add(gnc);
        }
        if(gncFinalList.size() > 0) {
            for (GNC gnc : gncList) {
                gncFinalList.removeIf(GNC -> GNC.getUrl().equalsIgnoreCase(gnc.getUrl()));
            }
        }
        gncFinalList.addAll(gncList);

        return gncFinalList;
    }

    public List<Reviews> readReviewsFromExcel(List<GNC> gncList) throws IOException {
        List<Reviews> gncReviewsList = new ArrayList<>();
        List<Reviews> gncNewReviewsList = new ArrayList<>();
        for(GNC gnc:gncList){
            List<Reviews> reviewsList = new ArrayList<>(gnc.getReviews());
            gncNewReviewsList.addAll(reviewsList);
        }
        File file = new File("GNC Reviews.xlsx");
        if(!file.exists()){
            return gncNewReviewsList;
        }
        FileInputStream inputStream = new FileInputStream(file);
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet spreadsheet = workbook.getSheet("GNC Reviews");
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
            Reviews reviews = new Reviews();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                String cellValue = cell.getStringCellValue();
                if(colId==0){
                    reviews.setWebsite(cellValue);
                }
                if(colId==1){
                    reviews.setProductUrl(cellValue);
                }
                if(colId==2){
                    reviews.setProductName(cellValue);

                }
                if(colId==3){
                    reviews.setItemCode(cellValue);

                }
                if(colId==4){
                    reviews.setUserName(cellValue);

                }
                if(colId==5){
                    reviews.setDate(cellValue);

                }
                if(colId==6){
                    reviews.setUserLocation(cellValue);

                }
                if(colId==7){
                    reviews.setReviewTitle(cellValue);

                }
                if(colId==8){
                    reviews.setReview(cellValue);

                }
                if(colId==9){
                    reviews.setReview(cellValue);

                }
                colId++;
            }
            gncReviewsList.add(reviews);
        }
        if(gncReviewsList.size() > 0) {
            for (Reviews reviews : gncNewReviewsList) {
                gncReviewsList.removeIf(Reviews -> Reviews.getProductName().equalsIgnoreCase(reviews.getProductName()) &&
                        Reviews.getProductUrl().equalsIgnoreCase(reviews.getProductUrl())&&
                        Reviews.getReviewTitle().equalsIgnoreCase(reviews.getReviewTitle())&&
                        Reviews.getUserName().equalsIgnoreCase(reviews.getUserName())&&
                        Reviews.getReview().equalsIgnoreCase(reviews.getReview()));
            }
        }
        gncReviewsList.addAll(gncNewReviewsList);

        return gncReviewsList;
    }

    public void writeDataToExcel(List<GNC> gncList) throws IOException {
        /*FileInputStream inputStream = new FileInputStream(new File("MainConsolidated GNC Crawled Data.xlsx"));
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet spreadsheet = workbook.getSheet("GNC");
        int rowid = 0;
        Row row;
        int cellid = 0;
        if (inputStream == null || spreadsheet == null){
            workbook = new XSSFWorkbook();
            spreadsheet = workbook.getSheet("GNC");
            row = spreadsheet.createRow(rowid++);
            for(String col: getMainFileColumnNames()){
                Cell cell = row.createCell(cellid++);
                cell.setCellValue(col);
            }

        }
        else {
            rowid = spreadsheet.getLastRowNum();
        }
*/
        Workbook workbook = new XSSFWorkbook();
        Sheet spreadsheet = workbook.createSheet("GNC");


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
        spreadsheet.autoSizeColumn(1);
        spreadsheet.autoSizeColumn(2);
        spreadsheet.autoSizeColumn(6);
        spreadsheet.createFreezePane(0, 1);
        FileOutputStream out = null;
        //Write the workbook in file system
        try {
            out = new FileOutputStream(
                    new File("Consolidated GNC Crawled Data.xlsx"));
            workbook.write(out);
            out.close();
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("GNC File might be already open");
            out.close();
            return;
        }
        System.out.println("MainConsolidated GNC Crawled Data.xlsx written successfully");

    }


    public void writeReviewsToExcel(List<Reviews> reviewsList) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet spreadsheet = workbook.createSheet("GNC Reviews");


        //Create row object
        Row row;
        int rowid = 0;
        int cellid = 0;
        row = spreadsheet.createRow(rowid++);
        for (String col : getReviewsFileColumnNames()) {
            Cell cell = row.createCell(cellid++);
            cell.setCellValue(col);
        }
                for (Reviews reviews : reviewsList) {
                    row = spreadsheet.createRow(rowid++);
                    cellid = 0;
                    for (String data : reviews.getAllValues()) {
                        Cell cell = row.createCell(cellid++);
                        cell.setCellValue(data);
                    }
                }

        spreadsheet.autoSizeColumn(5);
        spreadsheet.createFreezePane(0, 1);
        //Write the workbook in file system
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(
                    new File("GNC Reviews.xlsx"));
            workbook.write(out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("GNC Reviews.xlsx might be open");
            out.close();
            return;
        }
        System.out.println("GNC Reviews.xlsx written successfully");
    }



    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
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

    public List<String> getReviewsFileColumnNames(){
        List<String> values = new ArrayList<>();
        values.add("website");
        values.add("productUrl");
        values.add("productName");
        values.add("itemCode");
        values.add("user");
        values.add("date");
        values.add("userLocation");
        values.add("reviewTitle");
        values.add("review");
        values.add("Rating");
        return values;
    }
}
