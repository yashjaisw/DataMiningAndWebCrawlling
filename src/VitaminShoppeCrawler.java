
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static java.net.URLEncoder.*;

public class VitaminShoppeCrawler {

    public static int counted = 0;
    public List<VitaminShoppe> getVitaminShoppeData(List<String> urlList, String searchKeyword) throws IOException {
        List<VitaminShoppe> vitaminShoppeList = new ArrayList<>();
        for(String url:urlList) {
            VitaminShoppe vitaminShoppe = new VitaminShoppe();
            InputStream response = CrawlerHttpClient.loadContentByHttpClient(url,HttpHeaderConstants.GOOGLE_BOT);
            Document document = Jsoup.parse(response,"UTF-8","");
            int counter=1;
            while(document==null){
                if(counter>=5) {
                    break;
                }
                // 2nd attempt
                System.out.println("Crawler blocked. Trying Another User-Agent");
                response = CrawlerHttpClient.loadContentByHttpClient(url,HttpHeaderConstants.GOOGLE_BOT);
                if(response!=null) {
                    document = Jsoup.parse(response, "UTF-8", "");
                }
                counter++;
                if(document == null) {
                    // 3rd attempt
                    System.out.println("Crawler blocked. Trying Another User-Agent");
                    response = CrawlerHttpClient.loadContentByHttpClient(url,HttpHeaderConstants.GOOGLE_BOT);
                    if(response!=null) {
                        document = Jsoup.parse(response, "UTF-8", "");
                    }
                    counter++;
                }
                if(document == null) { // 4th attempt
                    System.out.println("Crawler blocked. Trying Another User-Agent");
                    response = CrawlerHttpClient.loadContentByHttpClient(url,HttpHeaderConstants.GOOGLE_BOT);
                    if(response!=null) {
                        document = Jsoup.parse(response, "UTF-8", "");
                    }
                    counter++;
                }
                if(document == null) { // 5th attempt
                    System.out.println("Crawler blocked. Trying Another User-Agent");
                    response = CrawlerHttpClient.loadContentByHttpClient(url,HttpHeaderConstants.GOOGLE_BOT);
                    if(response!=null) {
                        document = Jsoup.parse(response, "UTF-8", "");
                    }
                    counter++;
                }
                if(document == null) { // 6th attempt
                    System.out.println("Crawler blocked. Trying Another User-Agent");
                    response = CrawlerHttpClient.loadContentByHttpClient(url,HttpHeaderConstants.GOOGLE_BOT);
                    if(response!=null) {
                        document = Jsoup.parse(response, "UTF-8", "");
                    }
                    counter++;
                }
            }
            if(document==null){
                System.out.println("Can't connect");
                continue;
            }

            System.out.println("Crawled Count: "+ ++counted + " Crawling URL: "+ url+ " KeyWord: "+ searchKeyword);
            try {
                Element proNameEle = document.getElementsByClass("Product-block").get(0);
                String productName = proNameEle.select("h1").get(0).text();
                vitaminShoppe.setProductName(productName);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Product Type


            //Product Company
            try {
                Element proBrandNameEle = document.getElementsByClass("productBrandName").get(0);
                String  productCompany = proBrandNameEle.select("a").get(0).text();
                vitaminShoppe.setProductCompany(productCompany);
            } catch (Exception e) {
               // e.printStackTrace();
            }

            //Description
            try {
                Element descEle = document.getElementById("link1");
                Elements descEleClass = descEle.getElementsByClass("product-label direction");
                String description = descEleClass.text();
                description = description.replace("Product Label","Product Label \n");
                vitaminShoppe.setDescription(description);
            } catch (Exception e) {
               // e.printStackTrace();
            }

            //ItemCode
            try {
                Element itemCodeEle = document.getElementsByClass("item").get(0);
                Element itemCodeSpan = itemCodeEle.select("span").get(0);
                if(itemCodeSpan!=null){
                String itemCode = itemCodeSpan.text();
                itemCode = itemCode.replace("Item # : ","");
                vitaminShoppe.setItemCode(itemCode);
                }
                else{
                String itemCode = itemCodeEle.text();
                itemCode = itemCode.replace("Item # : ","");
                vitaminShoppe.setItemCode(itemCode);
                }
            } catch (Exception e) {
               // e.printStackTrace();
            }

            //Container Type
            //Type
            try {
                Element typeEle = document.getElementsByClass("productInfoRow").get(0);
                String type = typeEle.select("p").get(0).text();
                vitaminShoppe.setType(type);
            } catch (Exception e) {
               // e.printStackTrace();
            }

            //Size Percount
            try {
                Element sizePerCountEle = document.getElementsByClass("servingSize").get(1);
                String sizePerCount = sizePerCountEle.select("p").get(0).text();
                vitaminShoppe.setServingPerContainer(sizePerCount);
            } catch (Exception e) {
              //  e.printStackTrace();
            }

            //Offer Price
            try {
                Element offerPriceEle = document.getElementsByClass("priceCurrencyLabel").get(0);
                String offerPrice = offerPriceEle.text();
                vitaminShoppe.setOfferPrice(offerPrice);
            } catch (Exception e) {
                //e.printStackTrace();
            }

            //Shipping
            String shippingFee = "FREE STANDARD SHIPPING on orders of $25 or more";
            vitaminShoppe.setShippingFee(shippingFee);

            //Serving Size
            try {
                Element servingSizeEle = document.getElementsByClass("servingSize").get(0);
                String servingSize = servingSizeEle.select("p").get(0).text();
                if(servingSize.split(" ").length > 1){
                    servingSize = servingSize.split(" ")[0];
                }
                vitaminShoppe.setServingSize(servingSize);
            } catch (Exception e) {
               // e.printStackTrace();
            }

            //Directions
            try {
                Elements directionsParentClass = document.getElementsByClass("product-label direction col-md-12");
                String directions = "";
                for (Element directionIterate: directionsParentClass){
                    if(directionIterate.getElementsByClass("productInfoHeadings").text().equals("Directions")){
                        directions = directionIterate.select("p").get(0).text();
                    }
                }
                vitaminShoppe.setDirectionToUse(directions);
            } catch (Exception e) {
               // e.printStackTrace();
            }

            //Ingrredients
            try {
                Element suppInfoParent = document.getElementById("link2");
                Element suppInfoClas = suppInfoParent.getElementsByClass("row").get(1);
                String suppInfo = suppInfoClas.text();
                vitaminShoppe.setSuppInfo(suppInfo);
            } catch (Exception e) {
              //  e.printStackTrace();
            }

            //warning
            try {
                Elements warningParentClass = document.getElementsByClass("product-label direction col-md-12");
                String warning = "";
                for (Element warningIterate: warningParentClass){
                    if(warningIterate.getElementsByClass("productInfoHeadings").text().equals("Warning")){
                        warning = warningIterate.select("p").get(0).text();
                    }
                }
                vitaminShoppe.setWarnings(warning);
            } catch (Exception e) {
              //  e.printStackTrace();
            }

            vitaminShoppe.setUrl(url);
            vitaminShoppe.setWebsite("www.vitaminshoppe.com");
            vitaminShoppe.setCurrency("$");

            //Reviews count

            try {
                Element reviewId = document.getElementById("TurnToReviewsContent");
                Elements reviewClass = reviewId.getElementsByClass("TTreviewCount");
                String reviewCount = reviewClass.text();
                if(reviewCount.split(" ").length > 1){
                    reviewCount = reviewCount.split(" ")[0];
                }
                vitaminShoppe.setReviewsCount(reviewCount);
            } catch (Exception e) {
               // e.printStackTrace();
            }

            //Review rating
            try {
                Element reviewRatingId = document.getElementById("TTreviewSummaryAverageRating");
                String reviewRating = reviewRatingId.text();
                if(reviewRating.split(" ").length > 1){
                    reviewRating = reviewRating.split(" ")[0];
                }
                vitaminShoppe.setReviewsRating(reviewRating);
            } catch (Exception e) {
              //  e.printStackTrace();
            }

            //star
            try {
                Element fiveStarId = document.getElementById("TTreviewSummaryBreakdown-5");
                String fiveStar = fiveStarId.text();
                vitaminShoppe.setReviews5Star(fiveStar);
            } catch (Exception e) {
               // e.printStackTrace();
            }

            try {
                Element fourStarId = document.getElementById("TTreviewSummaryBreakdown-4");
                String fourStar = fourStarId.text();
                vitaminShoppe.setReviews4Star(fourStar);
            } catch (Exception e) {
              //  e.printStackTrace();
            }

            try {
                Element threeStarId = document.getElementById("TTreviewSummaryBreakdown-3");
                String threeStar = threeStarId.text();
                vitaminShoppe.setReviews3Star(threeStar);
            } catch (Exception e) {
             //   e.printStackTrace();
            }

            try {
                Element twoStarId = document.getElementById("TTreviewSummaryBreakdown-2");
                String twoStar = twoStarId.text();
                vitaminShoppe.setReviews2Star(twoStar);
            } catch (Exception e) {
              //  e.printStackTrace();
            }

            //Size per count
            try {
                Integer capsules = Integer.parseInt(vitaminShoppe.getServingPerContainer());
                Integer serveSize = Integer.parseInt(vitaminShoppe.getServingSize());
                Integer sizePercount = capsules * serveSize;
                vitaminShoppe.setSizePerCountUnit(sizePercount.toString());
            } catch (NumberFormatException e) {
              //  e.printStackTrace();
            }

            try {
                Element oneStarId = document.getElementById("TTreviewSummaryBreakdown-1");
                String oneStar = oneStarId.text();
                vitaminShoppe.setReviews1Star(oneStar);
            } catch (Exception e) {
              //  e.printStackTrace();
            }

            //Cost Per Serving
            try {
                Integer servingSizeCount = Integer.parseInt(vitaminShoppe.getSizePerCountUnit());
                String offerPrice = vitaminShoppe.getOfferPrice().replace("$","");
                offerPrice = offerPrice.replace(" ","");
                Double offerPriceDouble = Double.parseDouble(offerPrice);
                Double costPerCount = offerPriceDouble/servingSizeCount;
                costPerCount = round(costPerCount,2);
                vitaminShoppe.setCostPerServing(costPerCount.toString());
            } catch (NumberFormatException e) {
              //  e.printStackTrace();
            }

            //Cost PerCapsule
            try {
                Integer totalUnits = Integer.parseInt(vitaminShoppe.getServingPerContainer());
                String offerPrice = vitaminShoppe.getOfferPrice().replace("$","");
                offerPrice = offerPrice.replace(" ","");
                Double offerPriceDouble = Double.parseDouble(offerPrice);
                Double costPerUnit = offerPriceDouble/totalUnits;
                costPerUnit = round(costPerUnit,2);
                vitaminShoppe.setCostPerUnit(costPerUnit.toString() + "/" + vitaminShoppe.getType());
            } catch (NumberFormatException e) {
                //e.printStackTrace();
            }

            vitaminShoppe.setContainerType("");
            vitaminShoppe.setDiscountPrice("");
            vitaminShoppe.setSuggestedRetail("");
            vitaminShoppe.setShippingWeightOrDimensions("");
            vitaminShoppe.setMoreDescription("");
            vitaminShoppe.setSearchKeyword(searchKeyword);

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
                        //e.printStackTrace();
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
                        reviews.setProductName(vitaminShoppe.getProductName());
                        reviews.setWebsite(vitaminShoppe.getWebsite());
                        reviews.setProductUrl(vitaminShoppe.getUrl());
                        reviews.setItemCode(vitaminShoppe.getItemCode());
                        reviewsList.add(reviews);
                    } catch (Exception e) {
                       // e.printStackTrace();
                    }
                }

                vitaminShoppe.setReviews(reviewsList);
            } catch (Exception e) {
                //e.printStackTrace();
            }
            vitaminShoppeList.add(vitaminShoppe);

        }

        return vitaminShoppeList;
    }

    public List<VitaminShoppe> crawlDataSearchWise(List<Configuirations> configuirationsList) throws IOException, JSONException {
        String searchApi = "https://www.vitaminshoppe.com/rest/model/vitaminshoppe/groupby/actor/v2/VSIGroupByActor/search?rpp=96&query=";
        Set<VitaminShoppe> vitaminShoppeList = new HashSet<>();
        List<String> keywordsList = new ArrayList<>();
        for(Configuirations configuirations:configuirationsList){
            if(configuirations.getVitaminShoppe()){
                keywordsList.add(configuirations.getKeyword());
            }
        }
        if(keywordsList.size()<1){
            return new ArrayList<>();
        }
        //String defaultUrl = "https://www.vitaminshoppe.com/search?search=Maca%20Extract";
        for(String keyword:keywordsList) {
            List<String> urlsToCrawl = new ArrayList<>();
            String urlEncoded = encode(keyword, "UTF-8");
            String searchUrl = searchApi + urlEncoded;
            JSONObject jsonObject = connectWithWeb(searchUrl);
            if(jsonObject!=null) {
                try {
                    JSONObject obj = (JSONObject) jsonObject.get("response");
                    Integer Pages = 1;
                    try {
                        Integer totalProdInt = (Integer) obj.get("numProducts");
                        Double totalPage  = totalProdInt/96.0;
                        totalPage = Math.ceil(totalPage);
                        Pages = totalPage.intValue();
                    } catch (Exception e) {
                        //e.printStackTrace();

                    }
                    JSONArray jsonArray = (JSONArray) obj.get("products");
                    Boolean isSubstitute = false;
                    try {
                        JSONArray jsonArray1 = (JSONArray) obj.get("breadCrumbs");
                        JSONObject jsonObject1 = (JSONObject) jsonArray1.get(1);
                        String str = (String) jsonObject1.get("label");
                        str = str.split(": ")[1];
                        str = str.replace("\"","");
                        if(!str.equals(keyword)){
                            isSubstitute = true;
                        }
                    } catch (Exception e) {
                       // e.printStackTrace();
                    }
                    if(!isSubstitute) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                            String pdpUrl = (String) jsonObject1.get("pdpUrl");
                            if (pdpUrl != null && pdpUrl != "") {
                                urlsToCrawl.add("https://www.vitaminshoppe.com" + pdpUrl);
                            }
                        }
                        try {
                            for(int j=2;j<=Pages;j++){
                                String newSearch = searchUrl + "&pageno=" + j;
                                JSONObject jsonObject2 = connectWithWeb(newSearch);
                                JSONObject obj2 = (JSONObject) jsonObject2.get("response");
                                JSONArray jsonArray2 = (JSONArray) obj2.get("products");
                                for (int i = 0; i < jsonArray2.length(); i++) {
                                    JSONObject jsonObject3 = (JSONObject) jsonArray2.get(i);
                                    String pdpUrl = (String) jsonObject3.get("pdpUrl");
                                    if (pdpUrl != null && pdpUrl != "") {
                                        urlsToCrawl.add("https://www.vitaminshoppe.com" + pdpUrl);
                                    }
                                }
                            }
                        } catch (Exception e) {
                           // e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                   // e.printStackTrace();
                }
                try {
                    System.out.println("Found " + urlsToCrawl.size() + " products for Keyword: "+ keyword);
                    List<VitaminShoppe> vitaminShoppeList2 = getVitaminShoppeData(urlsToCrawl,keyword);
                    vitaminShoppeList.addAll(vitaminShoppeList2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        List<VitaminShoppe> sortedList = new ArrayList<>(vitaminShoppeList);
        List<Reviews> reviewsList = new ArrayList<>();
        try {
            reviewsList = readReviewsFromExcel(sortedList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sortedList = readDataFromExcel(sortedList);
        try {
            sortedList.sort(Comparator.comparing(VitaminShoppe::getSearchKeyword));
        } catch (Exception e) {
            e.printStackTrace();
        }
        writeDataToExcel(sortedList);
        writeReviewsToExcel(reviewsList);
        return sortedList;

    }

    public List<VitaminShoppe> readDataFromExcel(List<VitaminShoppe> vitaminShoppeList) throws IOException {
        List<VitaminShoppe> vitaminShoppeFinalList = new ArrayList<>();
        File file = new File("Consolidated Vitamin-Shoppe Crawled Data.xlsx");
        if(!file.exists()){
            return vitaminShoppeList;
        }
        FileInputStream inputStream = new FileInputStream(file);
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet spreadsheet = workbook.getSheet("Vitamin-Shoppe");
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
            VitaminShoppe vitaminShoppe = new VitaminShoppe();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                String cellValue = cell.getStringCellValue();
                if(colId==0){
                    vitaminShoppe.setSearchKeyword(cellValue);
                }
                if(colId==1){
                    vitaminShoppe.setWebsite(cellValue);
                }
                if(colId==2){
                    vitaminShoppe.setUrl(cellValue);

                }
                if(colId==3){
                    vitaminShoppe.setProductName(cellValue);

                }
                if(colId==4){
                    vitaminShoppe.setProductType(cellValue);

                }
                if(colId==5){
                    vitaminShoppe.setProductCompany(cellValue);

                }
                if(colId==6){
                    vitaminShoppe.setDescription(cellValue);

                }
                if(colId==7){
                    vitaminShoppe.setItemCode(cellValue);

                }
                if(colId==8){
                    vitaminShoppe.setContainerType(cellValue);

                }
                if(colId==9){
                    vitaminShoppe.setType(cellValue);

                }
                if(colId==10){
                    vitaminShoppe.setSizePerCountUnit(cellValue);

                }
                if(colId==11){
                    vitaminShoppe.setOfferPrice(cellValue);

                }
                if(colId==12){
                    vitaminShoppe.setDiscountPrice(cellValue);

                }
                if(colId==13){
                    vitaminShoppe.setSuggestedRetail(cellValue);

                }
                if(colId==14){
                    vitaminShoppe.setShippingFee(cellValue);

                }
                if(colId==15){
                    vitaminShoppe.setServingSize(cellValue);

                }
                if(colId==16){
                    vitaminShoppe.setServingPerContainer(cellValue);

                }
                if(colId==17){
                    vitaminShoppe.setCostPerServing(cellValue);

                }
                if(colId==18){
                    vitaminShoppe.setCostPerUnit(cellValue);

                }
                if(colId==19){
                    vitaminShoppe.setShippingWeightOrDimensions(cellValue);

                }
                if(colId==20){
                    vitaminShoppe.setDirectionToUse(cellValue);

                }
                if(colId==21){
                    vitaminShoppe.setSuppInfo(cellValue);

                }
                if(colId==22){
                    vitaminShoppe.setWarnings(cellValue);

                }
                if(colId==23){
                    vitaminShoppe.setMoreDescription(cellValue);

                }
                if(colId==24){
                    vitaminShoppe.setCurrency(cellValue);

                }
                if(colId==25){
                    vitaminShoppe.setReviewsCount(cellValue);

                }
                if(colId==26){
                    vitaminShoppe.setReviewsRating(cellValue);

                }
                if(colId==27){
                    vitaminShoppe.setReviews5Star(cellValue);

                }
                if(colId==28){
                    vitaminShoppe.setReviews4Star(cellValue);

                }
                if(colId==29){
                    vitaminShoppe.setReviews3Star(cellValue);

                }
                if(colId==30){
                    vitaminShoppe.setReviews2Star(cellValue);

                }
                if(colId==31){
                    vitaminShoppe.setReviews1Star(cellValue);

                }
                colId++;
            }
            vitaminShoppeFinalList.add(vitaminShoppe);
        }
        if(vitaminShoppeFinalList.size() > 0) {
            for (VitaminShoppe vitaminShoppe : vitaminShoppeList) {
                vitaminShoppeFinalList.removeIf(VitaminShoppe -> VitaminShoppe.getUrl().equalsIgnoreCase(vitaminShoppe.getUrl()));
            }
        }
        vitaminShoppeFinalList.addAll(vitaminShoppeList);

        return vitaminShoppeFinalList;
    }

    public List<Reviews> readReviewsFromExcel(List<VitaminShoppe> vitaminShoppeList) throws IOException {
        List<Reviews> vitaminShoppeReviewsList = new ArrayList<>();
        List<Reviews> vitaminShoppeNewReviewsList = new ArrayList<>();
        for(VitaminShoppe vitaminShoppe:vitaminShoppeList){
            List<Reviews> reviewsList = new ArrayList<>(vitaminShoppe.getReviews());
            vitaminShoppeNewReviewsList.addAll(reviewsList);
        }
        File file = new File("Vitamin-Shoppe Reviews.xlsx");
        if(!file.exists()){
            return vitaminShoppeNewReviewsList;
        }
        FileInputStream inputStream = new FileInputStream(file);
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet spreadsheet = workbook.getSheet("Vitamin-Shoppe Reviews");
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
            vitaminShoppeReviewsList.add(reviews);
        }
        if(vitaminShoppeReviewsList.size() > 0) {
            for (Reviews reviews : vitaminShoppeNewReviewsList) {
                vitaminShoppeReviewsList.removeIf(Reviews -> Reviews.getProductName().equalsIgnoreCase(reviews.getProductName()) &&
                        Reviews.getProductUrl().equalsIgnoreCase(reviews.getProductUrl())&&
                        Reviews.getReviewTitle().equalsIgnoreCase(reviews.getReviewTitle())&&
                        Reviews.getUserName().equalsIgnoreCase(reviews.getUserName())&&
                        Reviews.getReview().equalsIgnoreCase(reviews.getReview()));
            }
        }
        vitaminShoppeReviewsList.addAll(vitaminShoppeNewReviewsList);

        return vitaminShoppeReviewsList;
    }

    public JSONObject connectWithWeb(String searchUrl) throws IOException, JSONException {
        try {
            URL url = new URL(searchUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Scanner sc = new Scanner(url.openStream());
                String inline = "";
                while (sc.hasNext()) {
                    inline += sc.nextLine();
                }
                sc.close();
                JSONObject jsonObject = new JSONObject(inline);
                return jsonObject;
            }
        } catch (Exception e) {
          //  e.printStackTrace();
        }
        return null;

    }


    public void writeDataToExcel(List<VitaminShoppe> vitaminShoppeList) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet spreadsheet = workbook.createSheet("Vitamin-Shoppe");


        //Create row object
        Row row;
        int rowid = 0;
        int cellid = 0;
        row = spreadsheet.createRow(rowid++);
        for(String col: getMainFileColumnNames()){
            Cell cell = row.createCell(cellid++);
            cell.setCellValue(col);
        }
        for (VitaminShoppe vitaminShoppe : vitaminShoppeList) {
            row = spreadsheet.createRow(rowid++);
            cellid = 0;

            for (String data : vitaminShoppe.getAllValues()){
                Cell cell = row.createCell(cellid++);
                cell.setCellValue(data);
            }
        }
        spreadsheet.autoSizeColumn(1);
        spreadsheet.autoSizeColumn(2);
        spreadsheet.autoSizeColumn(6);
        spreadsheet.createFreezePane(0, 1);
        //Write the workbook in file system
        FileOutputStream out = new FileOutputStream(
                new File("Consolidated Vitamin-Shoppe Crawled Data.xlsx"));
        workbook.write(out);
        out.close();
        System.out.println("MainConsolidated Vitamin-Shoppe Crawled Data.xlsx written successfully");

    }

    public void writeReviewsToExcel(List<Reviews> reviewsList) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet spreadsheet = workbook.createSheet("Vitamin-Shoppe Reviews");


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
                    new File("Vitamin-Shoppe Reviews.xlsx"));
            workbook.write(out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Vitamin-Shoppe Reviews.xlsx might be open");
            out.close();
            return;
        }
        System.out.println("Vitamin-Shoppe Reviews.xlsx written successfully");
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

