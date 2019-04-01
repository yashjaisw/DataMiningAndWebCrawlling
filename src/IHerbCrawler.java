import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static sun.net.www.protocol.http.HttpURLConnection.userAgent;

public class IHerbCrawler {

    public static int counted = 0;
    public List<IHerb> getIHerbData(List<String> urlList, String searchKeyword) throws IOException {
        List<IHerb> iHerbList = new ArrayList<>();
        for(String url:urlList) {
            IHerb iHerb = new IHerb();
            try {
                Document document = null;
                InputStream response = CrawlerHttpClient.loadContentByHttpClientForIherb(url, HttpHeaderConstants.USER_AGENT4);
                if(response!=null) {
                    document = Jsoup.parse(response, "UTF-8", "");
                }
                int counter = 1;
                while (document == null) {
                    if (counter >= 5) {
                        break;
                    }
                    // 2nd attempt
                    System.out.println("Crawler blocked. Trying Another User-Agent");
                    response = CrawlerHttpClient.loadContentByHttpClientForIherb(url, HttpHeaderConstants.USER_AGENT4);
                    if (response != null) {
                        document = Jsoup.parse(response, "UTF-8", "");
                    }
                    counter++;
                    if (document == null) {
                        // 3rd attempt
                        System.out.println("Crawler blocked. Trying Another User-Agent");
                        response = CrawlerHttpClient.loadContentByHttpClientForIherb(url, HttpHeaderConstants.USER_AGENT2);
                        if (response != null) {
                            document = Jsoup.parse(response, "UTF-8", "");
                        }
                        counter++;
                    }
                    if (document == null) { // 4th attempt
                        System.out.println("Crawler blocked. Trying Another User-Agent");
                        response = CrawlerHttpClient.loadContentByHttpClientForIherb(url, HttpHeaderConstants.USER_AGENT1);
                        if (response != null) {
                            document = Jsoup.parse(response, "UTF-8", "");
                        }
                        counter++;
                    }
                    if (document == null) { // 5th attempt
                        System.out.println("Crawler blocked. Trying Another User-Agent");
                        response = CrawlerHttpClient.loadContentByHttpClientForIherb(url, HttpHeaderConstants.USER_AGENT3);
                        if (response != null) {
                            document = Jsoup.parse(response, "UTF-8", "");
                        }
                        counter++;
                    }
                    if (document == null) { // 6th attempt
                        System.out.println("Crawler blocked. Trying Another User-Agent");
                        response = CrawlerHttpClient.loadContentByHttpClientForIherb(url, HttpHeaderConstants.USER_AGENT6);
                        if (response != null) {
                            document = Jsoup.parse(response, "UTF-8", "");
                        }
                        counter++;
                    }
                }
                if (document == null) {
                    System.out.println("Can't connect");
                    continue;
                }

                System.out.println("Crawled Count: " + ++counted + " Crawling URL: " + url + " KeyWord: " + searchKeyword);

                //Product Name
                try {
                    Element proNameEle = document.getElementById("name");
                    String proName = proNameEle.text();
                    iHerb.setProductName(proName);
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                //Product Company
                try {
                    Element proBrandEle = document.getElementById("brand");
                    Element spanProBrand = proBrandEle.select("span").get(0);
                    String proBrand = spanProBrand.text();
                    iHerb.setProductCompany(proBrand);
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                //Description
                try {
                    Element descEle =  document.select("div[itemprop=description]").get(0);
                    String desc = descEle.text();
                    iHerb.setDescription(desc);
                } catch (Exception e) {
                   // e.printStackTrace();
                }

                //Item Code
                try {
                    Element proSpecs = document.getElementById("product-specs-list");
                    Elements proSpecsLi = proSpecs.select("li");
                    for(Element list:proSpecsLi){
                        if(list.text().contains("Product Code:")){
                            String proCode = list.text();
                            proCode = proCode.replaceAll("Product Code:","");
                            proCode = proCode.trim();
                            iHerb.setItemCode(proCode);
                        }

                        if(list.text().contains("Shipping Weight:")){
                            String proCode = list.text();
                            proCode = proCode.replaceAll("\\?","");
                            proCode = proCode.trim();
                            iHerb.setShippingWeightOrDimensions(proCode);
                        }
                        if(list.text().contains("Dimensions:")){
                            String proCode = list.text();
                            proCode = proCode.replaceAll("\\?","");
                            proCode = proCode.trim();
                            if(iHerb.getShippingWeightOrDimensions()==null) {
                                iHerb.setShippingWeightOrDimensions(proCode);
                            }
                            else{
                                iHerb.setShippingWeightOrDimensions(iHerb.getShippingWeightOrDimensions() + System.lineSeparator() + proCode);
                            }
                        }
                        if(list.text().contains("Package Quantity:")){
                            String proCode = list.text();
                            proCode = proCode.replaceAll("Package Quantity:","");
                            proCode = proCode.replaceAll("\\?","");
                            proCode = proCode.trim();
                            if(proCode.split(" ").length > 0){
                                proCode = proCode.split(" ")[0];
                            }
                            iHerb.setSizePerCountUnit(proCode);
                        }

                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                //Offer Price
                try {
                    Element offerPriceParent = document.getElementById("product-price");
                    Element offerPriceEle = offerPriceParent.getElementsByClass("col-xs-15 col-md-16 price our-price").get(0);
                    String offerPrice = offerPriceEle.text();
                    offerPrice = offerPrice.replaceAll("\\$","");
                    offerPrice = offerPrice.trim();
                    iHerb.setOfferPrice(offerPrice);
                } catch (Exception e) {
                   // e.printStackTrace();
                }

                try {
                    Element offerPriceParent = document.getElementById("product-price");
                    Element costPerUnitEle = offerPriceParent.
                            getElementsByClass("col-xs-offset-9 col-md-offset-8 col-xs-15 col-md-16 small price-per-unit").get(0);
                    String costPerUnit = costPerUnitEle.text();
                    iHerb.setCostPerUnit(costPerUnit);
                } catch (Exception e) {
                    //e.printStackTrace();
                }


                //MRP
                try {
                    Element mrpParent = document.getElementById("product-msrp");
                    Element mrpClass = mrpParent.getElementsByClass("col-xs-15 col-md-16 price").get(0);
                    String mrpPrice = mrpClass.text();
                    mrpPrice = mrpPrice.replaceAll("\\$","");
                    mrpPrice = mrpPrice.trim();
                    iHerb.setSuggestedRetail(mrpPrice);
                } catch (Exception e) {
                   // e.printStackTrace();
                }

                //Discount
                try {
                    Element discountParent = document.getElementById("product-discount");
                    Element discountClass = discountParent.getElementsByClass("col-xs-15 col-md-16 discount").get(0);
                    String discount = discountClass.text();
                    discount = discount.replaceAll("\\$","");
                    discount = discount.trim();
                    iHerb.setDiscountPrice(discount);
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                //Serving size, Supp Info
                try {
                    Element suppInfo = document.getElementsByClass("supplement-facts-container").get(0);
                    iHerb.setSuppInfo(suppInfo.text());
                    Element suppInfoTable = suppInfo.select("table").get(0);
                    Elements suppInfoTds = suppInfoTable.select("tr");
                    for(Element suppInfoTd:suppInfoTds){
                        if(suppInfoTd.text().contains("Serving Size:")){
                            String servingSize = suppInfoTd.text();
                            servingSize = servingSize.replaceAll("Serving Size:","");
                            servingSize = servingSize.trim();
                            servingSize = servingSize.split(" ")[0];
                            iHerb.setServingSize(servingSize);
                        }
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                //Servings Per Container
                try {
                    Integer servingSize = Integer.parseInt(iHerb.getServingSize());
                    Integer sizePerCount = Integer.parseInt(iHerb.getSizePerCountUnit());
                    Integer servingsPerContainer = sizePerCount/servingSize;
                    iHerb.setServingPerContainer(servingsPerContainer.toString());
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                //CostPerServing
                try {
                    String costPerUnitString = iHerb.getCostPerUnit();
                    costPerUnitString = costPerUnitString.split("/")[0];
                    costPerUnitString = costPerUnitString.replaceAll("\\$","");
                    costPerUnitString = costPerUnitString.trim();
                    Double costPerUnit = Double.parseDouble(costPerUnitString);
                    Integer serving = Integer.parseInt(iHerb.getServingSize());
                    Double costPerServing = costPerUnit * serving;
                    iHerb.setCostPerServing(costPerServing.toString());
                } catch (NumberFormatException e) {
                    //e.printStackTrace();
                }

                //Directions
                try {
                    Elements parentClass = document.getElementsByClass("col-xs-24 col-md-14").get(0).getElementsByClass("col-xs-24");
                    for(Element childClass:parentClass){
                        if(childClass.text().contains("Suggested Use")){
                            iHerb.setDirectionToUse(childClass.text());
                        }
                        if(childClass.text().contains("Warnings")){
                            iHerb.setWarnings(childClass.text());
                        }
                        if(childClass.text().contains("Disclaimer")){
                            iHerb.setMoreDescription(childClass.text());
                        }
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                iHerb.setCurrency("$");
                iHerb.setSearchKeyword(searchKeyword);
                iHerb.setUrl(url);

                //Review Count
                try {
                    iHerb = getRatingsAndReviews(iHerb);
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                iHerbList.add(iHerb);
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
        return iHerbList;
    }


    public List<IHerb> crawlDataSearchWise(List<Configuirations> configuirationsList) throws IOException {
        String searchUrl = "https://www.iherb.com/search?noi=192&kw=";
        Set<IHerb> iHerbHashSet = new HashSet<>();
        List<String> keywordsList = new ArrayList<>();
        for(Configuirations configuirations:configuirationsList){
            if(configuirations.getiHerb()){
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
                Document document = Jsoup.parse(response,"UTF-8","");
                int counter=1;
                while(document==null) {
                    if (counter >= 5) {
                        break;
                    }
                    // 2nd attempt
                    System.out.println("Crawler blocked. Trying Another User-Agent");
                    response = CrawlerHttpClient.loadContentByHttpClient(search,HttpHeaderConstants.USER_AGENT2);
                    document = Jsoup.parse(response,"UTF-8","");
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
                        response = CrawlerHttpClient.loadContentByHttpClient(search,HttpHeaderConstants.USER_AGENT4);
                        if(response!=null) {
                            document = Jsoup.parse(response, "UTF-8", "");
                        }
                        counter++;
                    }
                    if (document == null) { // 5th attempt
                        System.out.println("Crawler blocked. Trying Another User-Agent");
                        response = CrawlerHttpClient.loadContentByHttpClient(search,HttpHeaderConstants.USER_AGENT5);
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
                    Element searchClass = document.getElementsByClass("sub-header-title search").get(0);
                    Element spanClass = searchClass.getElementsByClass("orange").get(0);
                    String searchText = spanClass.text();
                    searchText = searchText.replace("\"","");
                    if(!searchText.equalsIgnoreCase(keyword)){
                        shouldSkip = true;
                    }

                } catch (Exception e) {
                    //  e.printStackTrace();
                }
                if(shouldSkip){
                    System.out.println("Found 0 results on IHerb Vitamins for: " + keyword);
                    continue;
                }

                //Total Results
                Integer totalResult = 0;
                Double totalPages = 0.0;
                Integer totalPagesUpper = 0;
                try {
                    Element searchContainer = document.getElementsByClass("sub-header-title display-items").get(0);
                    String totalResults = searchContainer.text();
                    totalResults = totalResults.split("Results")[0];
                    totalResults = totalResults.trim();
                    totalResult = Integer.parseInt(totalResults);
                    totalPages = totalResult/192.0;
                    totalPages = Math.ceil(totalPages);
                    totalPagesUpper = totalPages.intValue();
                } catch (NumberFormatException e) {
                    //e.printStackTrace();
                }

                Elements productNames = document.getElementsByClass("product ga-product col-xs-12 col-sm-12 col-md-8 col-lg-6");
                for (Element productName:productNames){
                    Element innerTile = productName.getElementsByClass("product-inner product-inner-wide").get(0);
                    Element href = innerTile.getElementsByTag("a").get(0);
                    String pdpLink = href.attr("abs:href");
                    urlsToCrawl.add(pdpLink);
                }
                for (int i=2;i<=totalPagesUpper;i++){

                    try {
                        String newSearch = search = search + "&p=" + i;
                        response = CrawlerHttpClient.loadContentByHttpClient(newSearch,HttpHeaderConstants.USER_AGENT2);
                        Document document2 = Jsoup.parse(response,"UTF-8","");
                        counter=1;
                        while(document2==null) {
                            if (counter >= 5) {
                                break;
                            }
                            // 2nd attempt
                            System.out.println("Crawler blocked. Trying Another User-Agent");
                            response = CrawlerHttpClient.loadContentByHttpClient(newSearch,HttpHeaderConstants.USER_AGENT1);
                            document2 = Jsoup.parse(response,"UTF-8","");
                            counter++;
                            if (document2 == null) {
                                // 3rd attempt
                                System.out.println("Crawler blocked. Trying Another User-Agent");
                                response = CrawlerHttpClient.loadContentByHttpClient(newSearch,HttpHeaderConstants.USER_AGENT3);
                                if(response!=null) {
                                    document2 = Jsoup.parse(response, "UTF-8", "");
                                }
                                counter++;
                            }
                            if (document2 == null) { // 4th attempt
                                System.out.println("Crawler blocked. Trying Another User-Agent");
                                response = CrawlerHttpClient.loadContentByHttpClient(newSearch,HttpHeaderConstants.USER_AGENT4);
                                if(response!=null) {
                                    document2 = Jsoup.parse(response, "UTF-8", "");
                                }
                                counter++;
                            }
                            if (document2 == null) { // 5th attempt
                                System.out.println("Crawler blocked. Trying Another User-Agent");
                                response = CrawlerHttpClient.loadContentByHttpClient(newSearch,HttpHeaderConstants.USER_AGENT5);
                                if(response!=null) {
                                    document2 = Jsoup.parse(response, "UTF-8", "");
                                }
                                counter++;
                            }
                            if (document2 == null) { // 6th attempt
                                System.out.println("Crawler blocked. Trying Another User-Agent");
                                response = CrawlerHttpClient.loadContentByHttpClient(newSearch,HttpHeaderConstants.USER_AGENT6);
                                if(response!=null) {
                                    document2 = Jsoup.parse(response, "UTF-8", "");
                                }
                                counter++;
                            }
                        }
                        if(document2==null){
                            break;
                        }

                        productNames = document2.getElementsByClass("product ga-product col-xs-12 col-sm-12 col-md-8 col-lg-6");
                        for (Element productName:productNames){
                            Element innerTile = productName.getElementsByClass("product-inner product-inner-wide").get(0);
                            Element href = innerTile.getElementsByTag("a").get(0);
                            String pdpLink = href.attr("abs:href");
                            urlsToCrawl.add(pdpLink);
                        }
                    } catch (IOException e) {
                        //e.printStackTrace();
                    }
                }

            } catch (Exception e) {
                //e.printStackTrace();
            }
            System.out.println("IHerb.com : Found " + urlsToCrawl.size() + " results for keyword: " + keyword);
            List<IHerb> iHerbList =  getIHerbData(urlsToCrawl,keyword);
            iHerbHashSet.addAll(iHerbList);
        }
        List<IHerb> sortedList = new ArrayList<>(iHerbHashSet);

        List<Reviews> reviewsList = new ArrayList<>();
        try {
            reviewsList = readReviewsFromExcel(sortedList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sortedList = readDataFromExcel(sortedList);
        try {
            sortedList.sort(Comparator.comparing(IHerb::getSearchKeyword));
        } catch (Exception e) {
            e.printStackTrace();
        }
        writeDataToExcel(sortedList);
        writeReviewsToExcel(reviewsList);
        return sortedList;


    }
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }


    public IHerb getRatingsAndReviews(IHerb iHerb) throws IOException, JSONException {

        List<Reviews> reviewsList = new ArrayList<>();
        String pId = iHerb.getUrl();
        pId = pId.substring(pId.lastIndexOf("/") + 1);
        String summaryApi = "https://www.iherb.com/ugc/api/product/" + pId + "/review/summary";
        HttpClient client1 = HttpClientBuilder.create().build();
        HttpGet request1 = new HttpGet(summaryApi);
        request1.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:47.0; en-US) Gecko/20100101 Firefox/47.0");
        HttpResponse response1 = client1.execute(request1);
        InputStream inputStream1 = response1.getEntity().getContent();
        if (inputStream1 != null) {
            Scanner sc = new Scanner(inputStream1);
            String inline = "";
            while (sc.hasNext()) {
                inline += sc.nextLine();
            }
            sc.close();
            JSONObject jsonObject1 = new JSONObject(inline);
            JSONObject ratingResponse = (JSONObject) jsonObject1.get("rating");
            JSONObject star1 = (JSONObject) ratingResponse.get("oneStar");
            Integer star1Count = (Integer) star1.get("count");
            iHerb.setReviews1Star(star1Count.toString());

            JSONObject star2 = (JSONObject) ratingResponse.get("twoStar");
            Integer star2Count = (Integer) star2.get("count");
            iHerb.setReviews2Star(star2Count.toString());

            JSONObject star3 = (JSONObject) ratingResponse.get("threeStar");
            Integer star3Count = (Integer) star3.get("count");
            iHerb.setReviews3Star(star3Count.toString());

            JSONObject star4 = (JSONObject) ratingResponse.get("fourStar");
            Integer star4Count = (Integer) star4.get("count");
            iHerb.setReviews4Star(star4Count.toString());

            JSONObject star5 = (JSONObject) ratingResponse.get("fiveStar");
            Integer star5Count = (Integer) star5.get("count");
            iHerb.setReviews5Star(star5Count.toString());

            Integer totalCount = (Integer) ratingResponse.get("count");
            iHerb.setReviewsCount(totalCount.toString());

            Double avgRating = (Double) ratingResponse.get("averageRating");
            iHerb.setReviewsRating(avgRating.toString());

        }


        String reviewApiCall = "https://www.iherb.com/ugc/api/review?limit=20&lc=en-US&translations=en-US&sortId=6&withUgcSummary=true&pid="
                + pId + "&page=" + 1;
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(reviewApiCall);
        request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:47.0; en-US) Gecko/20100101 Firefox/47.0");
        HttpResponse response = client.execute(request);
        InputStream inputStream = response.getEntity().getContent();
        if (inputStream != null) {
            Scanner sc = new Scanner(inputStream);
            String inline = "";
            while (sc.hasNext()) {
                inline += sc.nextLine();
            }
            sc.close();
            JSONObject jsonObject = new JSONObject(inline);
            JSONArray jsonArray = (JSONArray) jsonObject.get("items");
            for (int i = 0; i < jsonArray.length(); i++) {
                Reviews reviews = new Reviews();
                JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                String reviewTitle = (String) jsonObject1.get("reviewTitle");
                reviews.setReviewTitle(reviewTitle);

                String reviewText = (String) jsonObject1.get("reviewText");
                reviews.setReview(reviewText);

                try {
                    Integer rating = ((Integer) jsonObject1.get("ratingValue"))/10;
                    reviews.setRating(rating.toString());
                } catch (Exception e) {
                    //e.printStackTrace();
                }
                String userName = (String) jsonObject1.get("customerNickname");
                reviews.setUserName(userName);

                String date = (String) jsonObject1.get("postedDateLocalized");
                reviews.setDate(date);

                reviews.setItemCode(iHerb.getItemCode());
                reviews.setProductUrl(iHerb.getWebsite());
                reviews.setWebsite(iHerb.getWebsite());
                reviews.setProductName(iHerb.getProductName());

                reviewsList.add(reviews);
            }

            String nextPageToken = jsonObject.getString("nextPageToken");
            while (nextPageToken != null && !nextPageToken.isEmpty()) {

                Integer nextPage = null;
                if (nextPageToken != null && nextPageToken != "") {
                    nextPage = Integer.parseInt(nextPageToken);
                }
                if (nextPage != null) {

                    reviewApiCall = "https://www.iherb.com/ugc/api/review?limit=20&lc=en-US&translations=en-US&sortId=6&withUgcSummary=true&pid="
                            + pId + "&page=" + nextPage;
                    client = HttpClientBuilder.create().build();
                    request = new HttpGet(reviewApiCall);
                    request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:47.0; en-US) Gecko/20100101 Firefox/47.0");
                    response = client.execute(request);
                    inputStream = response.getEntity().getContent();
                    if (inputStream != null) {
                        sc = new Scanner(inputStream);
                        inline = "";
                        while (sc.hasNext()) {
                            inline += sc.nextLine();
                        }
                        sc.close();
                       JSONObject jsonObject3 = new JSONObject(inline);

                        jsonArray = (JSONArray) jsonObject3.get("items");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            Reviews reviews = new Reviews();
                            JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                            String reviewTitle = (String) jsonObject1.get("reviewTitle");
                            reviews.setReviewTitle(reviewTitle);

                            String reviewText = (String) jsonObject1.get("reviewText");
                            reviews.setReview(reviewText);

                            try {
                                Integer rating = ((Integer) jsonObject1.get("ratingValue"))/10;
                                reviews.setRating(rating.toString());
                            } catch (Exception e) {
                                //e.printStackTrace();
                            }
                            String userName = (String) jsonObject1.get("customerNickname");
                            reviews.setUserName(userName);

                            String date = (String) jsonObject1.get("postedDateLocalized");
                            reviews.setDate(date);

                            reviews.setItemCode(iHerb.getItemCode());
                            reviews.setProductUrl(iHerb.getWebsite());
                            reviews.setWebsite(iHerb.getWebsite());
                            reviews.setProductName(iHerb.getProductName());

                            reviewsList.add(reviews);
                            nextPageToken = jsonObject3.getString("nextPageToken");
                        }
                    } else {
                        break;
                    }
                }else{
                    break;
                }
            }
        }
        iHerb.setReviews(reviewsList);
        return iHerb;
    }


    public List<IHerb> readDataFromExcel(List<IHerb> iHerbList) throws IOException {
        List<IHerb> iHerbFinalList = new ArrayList<>();
        File file = new File("Consolidated Iherb Crawled Data.xlsx");
        if(!file.exists()){
            return iHerbList;
        }
        FileInputStream inputStream = new FileInputStream(file);
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet spreadsheet = workbook.getSheet("Iherb");
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
            IHerb iHerb = new IHerb();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                String cellValue = cell.getStringCellValue();
                if(colId==0){
                    iHerb.setSearchKeyword(cellValue);
                }
                if(colId==1){
                    iHerb.setWebsite(cellValue);
                }
                if(colId==2){
                    iHerb.setUrl(cellValue);

                }
                if(colId==3){
                    iHerb.setProductName(cellValue);

                }
                if(colId==4){
                    iHerb.setProductType(cellValue);

                }
                if(colId==5){
                    iHerb.setProductCompany(cellValue);

                }
                if(colId==6){
                    iHerb.setDescription(cellValue);

                }
                if(colId==7){
                    iHerb.setItemCode(cellValue);

                }
                if(colId==8){
                    iHerb.setContainerType(cellValue);

                }
                if(colId==9){
                    iHerb.setType(cellValue);

                }
                if(colId==10){
                    iHerb.setSizePerCountUnit(cellValue);

                }
                if(colId==11){
                    iHerb.setOfferPrice(cellValue);

                }
                if(colId==12){
                    iHerb.setDiscountPrice(cellValue);

                }
                if(colId==13){
                    iHerb.setSuggestedRetail(cellValue);

                }
                if(colId==14){
                    iHerb.setShippingFee(cellValue);

                }
                if(colId==15){
                    iHerb.setServingSize(cellValue);

                }
                if(colId==16){
                    iHerb.setServingPerContainer(cellValue);

                }
                if(colId==17){
                    iHerb.setCostPerServing(cellValue);

                }
                if(colId==18){
                    iHerb.setCostPerUnit(cellValue);

                }
                if(colId==19){
                    iHerb.setShippingWeightOrDimensions(cellValue);

                }
                if(colId==20){
                    iHerb.setDirectionToUse(cellValue);

                }
                if(colId==21){
                    iHerb.setSuppInfo(cellValue);

                }
                if(colId==22){
                    iHerb.setWarnings(cellValue);

                }
                if(colId==23){
                    iHerb.setMoreDescription(cellValue);

                }
                if(colId==24){
                    iHerb.setCurrency(cellValue);

                }
                if(colId==25){
                    iHerb.setReviewsCount(cellValue);

                }
                if(colId==26){
                    iHerb.setReviewsRating(cellValue);

                }
                if(colId==27){
                    iHerb.setReviews5Star(cellValue);

                }
                if(colId==28){
                    iHerb.setReviews4Star(cellValue);

                }
                if(colId==29){
                    iHerb.setReviews3Star(cellValue);

                }
                if(colId==30){
                    iHerb.setReviews2Star(cellValue);

                }
                if(colId==31){
                    iHerb.setReviews1Star(cellValue);

                }
                colId++;
            }
            iHerbFinalList.add(iHerb);
        }
        if(iHerbFinalList.size() > 0) {
            for (IHerb iHerb : iHerbList) {
                iHerbFinalList.removeIf(IHerb -> IHerb.getUrl().equalsIgnoreCase(iHerb.getUrl()));
            }
        }
        iHerbFinalList.addAll(iHerbList);

        return iHerbFinalList;
    }

    public List<Reviews> readReviewsFromExcel(List<IHerb> iHerbList) throws IOException {
        List<Reviews> iHerbReviewsList = new ArrayList<>();
        List<Reviews> iHerbNewReviewsList = new ArrayList<>();
        for(IHerb iHerb:iHerbList){
            List<Reviews> reviewsList = new ArrayList<>(iHerb.getReviews());
            iHerbNewReviewsList.addAll(reviewsList);
        }
        File file = new File("Iherb Reviews.xlsx");
        if(!file.exists()){
            return iHerbNewReviewsList;
        }
        FileInputStream inputStream = new FileInputStream(file);
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet spreadsheet = workbook.getSheet("Iherb Reviews");
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
            iHerbReviewsList.add(reviews);
        }
        if(iHerbReviewsList.size() > 0) {
            for (Reviews reviews : iHerbNewReviewsList) {
                iHerbReviewsList.removeIf(Reviews -> Reviews.getProductName().equalsIgnoreCase(reviews.getProductName()) &&
                        Reviews.getProductUrl().equalsIgnoreCase(reviews.getProductUrl())&&
                        Reviews.getReviewTitle().equalsIgnoreCase(reviews.getReviewTitle())&&
                        Reviews.getUserName().equalsIgnoreCase(reviews.getUserName())&&
                        Reviews.getReview().equalsIgnoreCase(reviews.getReview()));
            }
        }
        iHerbReviewsList.addAll(iHerbNewReviewsList);

        return iHerbReviewsList;
    }

    public void writeDataToExcel(List<IHerb> iHerbList) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet spreadsheet = workbook.createSheet("Iherb");

        //Create row object
        Row row;
        int rowid = 0;
        int cellid = 0;
        row = spreadsheet.createRow(rowid++);
        for(String col: getMainFileColumnNames()){
            Cell cell = row.createCell(cellid++);
            cell.setCellValue(col);
        }
        for (IHerb iHerb : iHerbList) {
            row = spreadsheet.createRow(rowid++);
            cellid = 0;

            for (String data : iHerb.getAllValues()){
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
                new File("Consolidated Iherb Crawled Data.xlsx"));
        workbook.write(out);
        out.close();
        System.out.println("MainConsolidated Iherb Crawled Data.xlsx written successfully");

    }

    public void writeReviewsToExcel(List<Reviews> reviewsList) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet spreadsheet = workbook.createSheet("Iherb Reviews");


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
                    new File("Iherb Reviews.xlsx"));
            workbook.write(out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Iherb Reviews.xlsx might be open");
            out.close();
            return;
        }
        System.out.println("Iherb Reviews.xlsx written successfully");
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
