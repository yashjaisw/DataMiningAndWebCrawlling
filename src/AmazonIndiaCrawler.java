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

public class AmazonIndiaCrawler {

    public static int count = 0;

    public List<AmazonIndia> getAmazonData(List<String> urlList,String searchKeyword) throws IOException {
        List<AmazonIndia> amazonIndiaList = new ArrayList<>();
        for(String url:urlList) {
            //   url="https://www.amazon.in/Organic-India-Ashwagandha-Capsules-Count/dp/B01MAWPTFK/ref=sr_1_3?s=hpc&ie=UTF8&qid=1544371916&sr=1-3&keywords=ashwagandha";
            try {
                AmazonIndia amazonIndia = new AmazonIndia();
                System.out.println("Crawl count: " + ++count + " Crawling URL: " + url);
                InputStream response = CrawlerHttpClient.loadContentByHttpClient(url,HttpHeaderConstants.GOOGLE_BOT);
                Document document = null;
                if(response!=null) {
                    document = Jsoup.parse(response, "UTF-8", "");
                }
                int counter=1;
                while (document == null || document.getElementById("captchacharacters")!=null) {
                    if (counter >= 5) {
                        break;
                    }
                    // 2nd attempt
                    System.out.println("Crawler blocked. Trying Another User-Agent");
                    response = CrawlerHttpClient.loadContentByHttpClient(url,HttpHeaderConstants.USER_AGENT4);
                    if(response!=null) {
                        document = Jsoup.parse(response, "UTF-8", "");
                    }
                    counter++;
                    if (document == null || document.getElementById("captchacharacters")!=null) {
                        // 3rd attempt
                        System.out.println("Crawler blocked. Trying Another User-Agent");
                        response = CrawlerHttpClient.loadContentByHttpClient(url,HttpHeaderConstants.USER_AGENT3);
                        if(response!=null) {
                            document = Jsoup.parse(response, "UTF-8", "");
                        }
                        counter++;
                    }
                    if (document == null || document.getElementById("captchacharacters")!=null) { // 4th attempt
                        System.out.println("Crawler blocked. Trying Another User-Agent");
                        response = CrawlerHttpClient.loadContentByHttpClient(url,HttpHeaderConstants.USER_AGENT1);
                        if(response!=null) {
                            document = Jsoup.parse(response, "UTF-8", "");
                        }
                        counter++;
                    }
                    if (document == null || document.getElementById("captchacharacters")!=null) { // 5th attempt
                        System.out.println("Crawler blocked. Trying Another User-Agent");
                        response = CrawlerHttpClient.loadContentByHttpClient(url,HttpHeaderConstants.USER_AGENT6);
                        if(response!=null) {
                            document = Jsoup.parse(response, "UTF-8", "");
                        }
                        counter++;
                    }
                    if (document == null || document.getElementById("captchacharacters")!=null) { // 6th attempt
                        System.out.println("Crawler blocked. Trying Another User-Agent");
                        response = CrawlerHttpClient.loadContentByHttpClient(url,HttpHeaderConstants.USER_AGENT6);
                        if(response!=null) {
                            document = Jsoup.parse(response, "UTF-8", "");
                        }
                        counter++;
                    }
                }
                if(document==null || document.getElementById("productTitle")==null){
                    continue;
                }

                //Product name
                try {
                    Element productEle = document.getElementById("productTitle");
                    String productName = productEle.text();
                    amazonIndia.setProductName(productName);
                } catch (Exception e) {
                    //e.printStackTrace();
                }
                //Product Type
                try {
                    Elements productTypeEle = document.getElementsByClass("zg_hrsr_ladder");
                    String productType = productTypeEle.text();
                    amazonIndia.setProductType(productType);
                } catch (Exception e) {
                    // e.printStackTrace();
                }

                //Product Company
                try {
                    Element productCompanyEle = document.getElementById("bylineInfo");
                    String productCompany = productCompanyEle.text();
                    amazonIndia.setProductCompany(productCompany);
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                //Description
                try {
                    Element descriptionEle = document.getElementById("productDescription");
                    String description = descriptionEle.text();
                    amazonIndia.setDescription(description);
                } catch (Exception e) {
                    // e.printStackTrace();
                }

                //Item Code
                try {
                    Element itemCodeEle = document.getElementById("detail_bullets_id");
                    Elements itemCodeClass = itemCodeEle.getElementsByClass("content");
                    Element itemCodeSelect = null;
                    for(Element element:itemCodeClass.get(0).select("li")) {
                        if (element.text().contains("ASIN")) {
                            itemCodeSelect = element;
                            break;
                        }
                    }
                    String itemCode = itemCodeSelect.text();
                    if (itemCode.split(":").length > 1) {
                        itemCode = itemCode.split(":")[1].replace(" ", "");
                    }
                    amazonIndia.setItemCode(itemCode);
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                try {
                    if(amazonIndia.getItemCode()==null || amazonIndia.getItemCode().isEmpty()){
                        Element itemCodeEle = document.getElementById("prodDetails");
                        Element itemCodeClass = itemCodeEle.getElementsByClass("pdTab").get(1);
                        Element tr = itemCodeClass.select("tr").get(0);
                        Elements td = tr.select("td");
                        if(td!=null && td.size() > 1 && td.get(0).text().equals("ASIN")){
                            String itemCode = td.get(1).text();
                            amazonIndia.setItemCode(itemCode);
                        }
                    }
                } catch (Exception e) {
                    //  e.printStackTrace();
                }

                try {
                    Element offerPriceEle = document.getElementById("priceblock_ourprice");
                    String offerPrice = offerPriceEle.text();
                    amazonIndia.setOfferPrice(offerPrice);
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                try {
                    if(amazonIndia.getOfferPrice()==null || amazonIndia.getOfferPrice().isEmpty()){
                        Element offerPriceEle = document.getElementById("priceblock_saleprice");
                        String offerPrice = offerPriceEle.text();
                        amazonIndia.setOfferPrice(offerPrice);
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                //Discount Price
                try {
                    Element discountPriceEle = document.getElementById("regularprice_savings");
                    Elements discountPriceClass = discountPriceEle.getElementsByClass("a-span12 a-color-price a-size-base");
                    String discountPrice = discountPriceClass.text();
                    if (discountPrice.split("\\(").length > 1) {
                        discountPrice = (discountPrice.split("\\("))[0].replace(" ", "");
                    }
                    amazonIndia.setDiscountPrice(discountPrice);
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                //Suggested Retail
                try {
                    Element suggestedRetailEle = document.getElementById("price");
                    Element suggestedRetailTable = suggestedRetailEle.select("table").get(0);
                    Element suggestedRetailTableTr = suggestedRetailTable.select("tr").get(0);
                    Element suggestedRetailTableTrClass = suggestedRetailTableTr.getElementsByClass("a-span12 a-color-secondary a-size-base").get(0);
                    String suggestedRetailPrice = suggestedRetailTableTrClass.text();
                    amazonIndia.setSuggestedRetail(suggestedRetailPrice);
                } catch (Exception e) {
                    // e.printStackTrace();
                }

                //Delivery
                try {
                    Element shippingEle = document.getElementById("price");
                    Element shippingBTag = shippingEle.select("b").get(0);
                    String shipping = shippingBTag.text();
                    amazonIndia.setShippingFee(shipping);
                } catch (Exception e) {
                    // e.printStackTrace();
                }

                try {
                    if(amazonIndia.getShippingFee()==null || amazonIndia.getShippingFee().isEmpty())
                    {
                        Element shippingEle = document.getElementById("soldByThirdParty");
                        Element shippingEleClass = shippingEle.getElementsByClass("a-size-small a-color-secondary shipping3P").get(0);
                        String shipping = shippingEleClass.text();
                        shipping = shipping.replace("+","");
                        amazonIndia.setShippingFee(shipping);

                    }
                } catch (Exception e) {
                    // e.printStackTrace();
                }

                //Shipping Weight or Dimensions
                try {
                    Element shippingDimensionEle = document.getElementById("detail_bullets_id");
                    Elements shippingDimensionEleClass = shippingDimensionEle.getElementsByClass("content");
                    Elements shippingDimensionEleClassLi = shippingDimensionEleClass.get(0).select("li");
                    for(Element shippingDimensionLoop:shippingDimensionEleClassLi){
                        if(shippingDimensionLoop.text().contains("Shipping Dimensions:")
                                || shippingDimensionLoop.text().contains("Shipping Weight:")
                                ||shippingDimensionLoop.text().contains("Package Dimensions:")){
                            String shippingDimension = shippingDimensionLoop.text();
                            amazonIndia.setShippingWeightOrDimensions(amazonIndia.getShippingWeightOrDimensions() + shippingDimension);
                        }
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                //Directions to use
                try {
                    Element directionToUseEle = document.getElementById("feature-bullets");
                    Elements directionToUseEleList = directionToUseEle.select("li");
                    StringBuilder directionToUse = new StringBuilder();
                    for (int i = 0; i < directionToUseEleList.size(); i++) {
                        directionToUse.append("*");
                        directionToUse.append(directionToUseEleList.get(i).text());
                        directionToUse.append("\n");
                    }
                    amazonIndia.setDirectionToUse(directionToUse.toString());
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                //ReviewCount
                try {
                    Element reviewCountEle = document.getElementById("dp-summary-see-all-reviews");
                    String reviewCount = reviewCountEle.text();
                    if (reviewCount.split(" ").length > 1) {
                        reviewCount = reviewCount.split(" ")[0];
                    }
                    amazonIndia.setReviewsCount(reviewCount);
                } catch (Exception e) {
                    //e.printStackTrace();
                }


                //Rating
                try {
                    Element reviewEle = document.getElementById("reviewsMedley");
                    Element reviewsRatingClass = reviewEle.getElementsByClass("arp-rating-out-of-text a-color-base").get(0);
                    String reviewsRating = reviewsRatingClass.text();
                    if (reviewsRating.split(" ").length > 1) {
                        reviewsRating = reviewsRating.split(" ")[0];
                    }
                    amazonIndia.setReviewsRating(reviewsRating);
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                //Star counts
                Element starsCommon = document.getElementById("histogramTable");
                //5 Star
                try {
                    Elements reviewsStarEle = starsCommon.
                            getElementsByClass("a-size-base a-link-normal 5star histogram-review-count a-color-secondary");
                    if(reviewsStarEle!=null && reviewsStarEle.size() > 0)
                    {
                        Element reviews5StarEle = starsCommon.
                                getElementsByClass("a-size-base a-link-normal 5star histogram-review-count a-color-secondary").get(0);
                        if(reviews5StarEle!=null) {
                            String reviews5Star = reviews5StarEle.text();
                            Integer percentStar = Integer.parseInt(reviews5Star.replace("%", ""));
                            Double fiveStarCount = percentStar.doubleValue() * (Integer.parseInt(amazonIndia.getReviewsCount())) / 100;
                            amazonIndia.setReviews5Star(((Long) Math.round(fiveStarCount)).toString());
                        }
                    }
                    else{
                        amazonIndia.setReviews5Star("0");
                    }
                } catch (Exception e) {
                    // e.printStackTrace();
                }
                //4 Star
                try {
                    Elements reviewsStarEle = starsCommon.
                            getElementsByClass("a-size-base a-link-normal 4star histogram-review-count a-color-secondary");
                    if(reviewsStarEle!=null && reviewsStarEle.size() > 0) {
                        Element reviews4StarEle = starsCommon.
                                getElementsByClass("a-size-base a-link-normal 4star histogram-review-count a-color-secondary").get(0);
                        String reviewsStar = reviews4StarEle.text();
                        Integer percentStar = Integer.parseInt(reviewsStar.replace("%", ""));
                        Double StarCount = percentStar.doubleValue() * (Integer.parseInt(amazonIndia.getReviewsCount())) / 100;
                        amazonIndia.setReviews4Star(((Long) Math.round(StarCount)).toString());
                    }
                    else{
                        amazonIndia.setReviews4Star("0");
                    }
                } catch (Exception e) {
                    // e.printStackTrace();
                }

                //3 Star
                try {
                    Elements reviewsStarEle = starsCommon.
                            getElementsByClass("a-size-base a-link-normal 3star histogram-review-count a-color-secondary");
                    if(reviewsStarEle!=null && reviewsStarEle.size() > 0) {
                        Element reviews3StarEle = starsCommon.
                                getElementsByClass("a-size-base a-link-normal 3star histogram-review-count a-color-secondary").get(0);
                        String reviewsStar = reviews3StarEle.text();
                        Integer percentStar = Integer.parseInt(reviewsStar.replace("%", ""));
                        Double StarCount = percentStar.doubleValue() * (Integer.parseInt(amazonIndia.getReviewsCount())) / 100;
                        amazonIndia.setReviews3Star(((Long) Math.round(StarCount)).toString());
                    }
                    else{
                        amazonIndia.setReviews3Star("0");
                    }
                } catch (Exception e) {
                    // e.printStackTrace();
                }

                //2 Star
                try {
                    Elements reviewsStarEle = starsCommon.
                            getElementsByClass("a-size-base a-link-normal 2star histogram-review-count a-color-secondary");
                    if(reviewsStarEle!=null && reviewsStarEle.size() > 0) {
                        Element reviews2StarEle = starsCommon.
                                getElementsByClass("a-size-base a-link-normal 2star histogram-review-count a-color-secondary").get(0);
                        String reviewsStar = reviews2StarEle.text();
                        Integer percentStar = Integer.parseInt(reviewsStar.replace("%", ""));
                        Double StarCount = percentStar.doubleValue() * (Integer.parseInt(amazonIndia.getReviewsCount())) / 100;
                        amazonIndia.setReviews2Star(((Long) Math.round(StarCount)).toString());
                    }
                    else{
                        amazonIndia.setReviews2Star("0");
                    }
                } catch (Exception e) {
                    // e.printStackTrace();
                }
                //1 Star
                try {
                    Elements reviewsStarEle = starsCommon.
                            getElementsByClass("a-size-base a-link-normal 1star histogram-review-count a-color-secondary");
                    if(reviewsStarEle!=null && reviewsStarEle.size() > 0) {
                        Element reviews1StarEle = starsCommon.
                                getElementsByClass("a-size-base a-link-normal 1star histogram-review-count a-color-secondary").get(0);
                        String reviewsStar = reviews1StarEle.text();
                        Integer percentStar = Integer.parseInt(reviewsStar.replace("%", ""));
                        Double StarCount = percentStar.doubleValue() * (Integer.parseInt(amazonIndia.getReviewsCount())) / 100;
                        amazonIndia.setReviews1Star(((Long) Math.round(StarCount)).toString());
                    }
                    else{
                        amazonIndia.setReviews1Star("0");
                    }
                } catch (Exception e) {
                    // e.printStackTrace();
                }
                amazonIndia.setUrl(url);
                //Reviews
                try {
                    Element reviewsUrl = document.getElementById("reviews-medley-footer");
                    Elements urlEle = reviewsUrl.select("a");
                    String finalReviewUrl = "https://www.amazon.in";
                    for(Element urls:urlEle){
                        if(urls.text().contains("See all")){
                            finalReviewUrl = finalReviewUrl + urls.attr("href");

                            break;
                        }
                    }
                    if(finalReviewUrl!=null && finalReviewUrl!=""){
                        try {
                            List<Reviews> reviewsList = getReviews(finalReviewUrl,amazonIndia);
                            amazonIndia.setReviews(reviewsList);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                amazonIndia.setSearchKeyword(searchKeyword);
                amazonIndia.setCurrency("INR");
                amazonIndia.setUrl(url);
                amazonIndiaList.add(amazonIndia);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return amazonIndiaList;
    }

    public List<Reviews> getReviews(String url,AmazonIndia amazonIndia) throws IOException {
        List<Reviews> reviewsList = new ArrayList<>();
        InputStream response = CrawlerHttpClient.loadContentByHttpClient(url,HttpHeaderConstants.GOOGLE_BOT);
        Document document = null;
        if(response!=null) {
            document = Jsoup.parse(response, "UTF-8", "");
        }
        int counter=1;
        while (document == null || document.getElementById("captchacharacters")!=null) {
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
            if (document == null || document.getElementById("captchacharacters")!=null) {
                // 3rd attempt
                System.out.println("Crawler blocked. Trying Another User-Agent");
                response = CrawlerHttpClient.loadContentByHttpClient(url,HttpHeaderConstants.USER_AGENT3);
                if(response!=null) {
                    document = Jsoup.parse(response, "UTF-8", "");
                }
                counter++;
            }
            if (document == null || document.getElementById("captchacharacters")!=null) { // 4th attempt
                System.out.println("Crawler blocked. Trying Another User-Agent");
                response = CrawlerHttpClient.loadContentByHttpClient(url,HttpHeaderConstants.USER_AGENT1);
                if(response!=null) {
                    document = Jsoup.parse(response, "UTF-8", "");
                }
                counter++;
            }
            if (document == null || document.getElementById("captchacharacters")!=null) { // 5th attempt
                System.out.println("Crawler blocked. Trying Another User-Agent");
                response = CrawlerHttpClient.loadContentByHttpClient(url,HttpHeaderConstants.USER_AGENT6);
                if(response!=null) {
                    document = Jsoup.parse(response, "UTF-8", "");
                }
                counter++;
            }
            if (document == null || document.getElementById("captchacharacters")!=null) { // 6th attempt
                System.out.println("Crawler blocked. Trying Another User-Agent");
                response = CrawlerHttpClient.loadContentByHttpClient(url,HttpHeaderConstants.USER_AGENT6);
                if(response!=null) {
                    document = Jsoup.parse(response, "UTF-8", "");
                }
                counter++;
            }
        }
        if(document==null)
        {
            System.out.println("Unresponsive Reviews Page");
            return reviewsList;
        }
        Integer totalPages =1;
        try {
            Element paginationUl =document.getElementsByClass("a-pagination").get(0);
            Element pageButtonsClass = paginationUl.getElementsByClass("page-button").last();
            String totalPagesString = pageButtonsClass.text();
            totalPages = Integer.parseInt(totalPagesString);
        } catch (Exception e) {
            //e.printStackTrace();
        }


        Element reviewsIdEle = document.getElementById("cm_cr-review_list");
        Elements reviewsClasses = reviewsIdEle.getElementsByClass("a-section review");
        for(Element eachReview:reviewsClasses)
        {
            Reviews reviews = new Reviews();
            String title,date,userName,reviewText,starRating;
            Element profileName = eachReview.select("span.a-profile-name").get(0);
            userName = profileName.text();
            Element reviewTitle = eachReview.getElementsByClass("a-size-base a-link-normal review-title a-color-base a-text-bold")
                    .get(0);
            title = reviewTitle.text();
            Element ratingSpan = eachReview.select("span.a-icon-alt").get(0);
            starRating = ratingSpan.text();
            Element dateEle = eachReview.getElementsByClass("a-size-base a-color-secondary review-date").get(0);
            date = dateEle.text();

            Element reviewTextEle = eachReview.getElementsByClass("a-size-base review-text").get(0);
            reviewText = reviewTextEle.text();

            reviews.setReview(reviewText);
            reviews.setReviewTitle(title);
            reviews.setUserLocation("");
            reviews.setDate(date);
            reviews.setItemCode(amazonIndia.getItemCode());
            reviews.setUserName(userName);
            reviews.setProductUrl(amazonIndia.getUrl());
            reviews.setWebsite(amazonIndia.getWebsite());
            reviews.setRating(starRating);
            reviewsList.add(reviews);

        }
        for (int i=2;i<=totalPages;i++){

            String nextUrl = url + "&pageNumber=" + i;
            response = CrawlerHttpClient.loadContentByHttpClient(nextUrl,HttpHeaderConstants.GOOGLE_BOT);
            document = null;
            if(response!=null) {
                document = Jsoup.parse(response, "UTF-8", "");
            }
            counter=1;
            while (document == null || document.getElementById("captchacharacters")!=null) {
                if (counter >= 5) {
                    break;
                }
                // 2nd attempt
                System.out.println("Crawler blocked. Trying Another User-Agent");
                response = CrawlerHttpClient.loadContentByHttpClient(nextUrl,HttpHeaderConstants.USER_AGENT2);
                if(response!=null) {
                    document = Jsoup.parse(response, "UTF-8", "");
                }
                counter++;
                if (document == null || document.getElementById("captchacharacters")!=null) {
                    // 3rd attempt
                    System.out.println("Crawler blocked. Trying Another User-Agent");
                    response = CrawlerHttpClient.loadContentByHttpClient(nextUrl,HttpHeaderConstants.USER_AGENT3);
                    if(response!=null) {
                        document = Jsoup.parse(response, "UTF-8", "");
                    }
                    counter++;
                }
                if (document == null || document.getElementById("captchacharacters")!=null) { // 4th attempt
                    System.out.println("Crawler blocked. Trying Another User-Agent");
                    response = CrawlerHttpClient.loadContentByHttpClient(nextUrl,HttpHeaderConstants.USER_AGENT1);
                    if(response!=null) {
                        document = Jsoup.parse(response, "UTF-8", "");
                    }
                    counter++;
                }
                if (document == null || document.getElementById("captchacharacters")!=null) { // 5th attempt
                    System.out.println("Crawler blocked. Trying Another User-Agent");
                    response = CrawlerHttpClient.loadContentByHttpClient(nextUrl,HttpHeaderConstants.USER_AGENT6);
                    if(response!=null) {
                        document = Jsoup.parse(response, "UTF-8", "");
                    }
                    counter++;
                }
                if (document == null || document.getElementById("captchacharacters")!=null) { // 6th attempt
                    System.out.println("Crawler blocked. Trying Another User-Agent");
                    response = CrawlerHttpClient.loadContentByHttpClient(nextUrl,HttpHeaderConstants.USER_AGENT6);
                    if(response!=null) {
                        document = Jsoup.parse(response, "UTF-8", "");
                    }
                    counter++;
                }
            }
            if(document==null)
            {
                continue;
            }
            reviewsIdEle = document.getElementById("cm_cr-review_list");
            reviewsClasses = reviewsIdEle.getElementsByClass("a-section review");
            for(Element eachReview:reviewsClasses)
            {
                Reviews reviews = new Reviews();
                String title,date,userName,reviewText,starRating;
                Element profileName = eachReview.select("span.a-profile-name").get(0);
                userName = profileName.text();
                Element reviewTitle = eachReview.getElementsByClass("a-size-base a-link-normal review-title a-color-base a-text-bold")
                        .get(0);
                title = reviewTitle.text();
                Element ratingSpan = eachReview.select("span.a-icon-alt").get(0);
                starRating = ratingSpan.text();
                Element dateEle = eachReview.getElementsByClass("a-size-base a-color-secondary review-date").get(0);
                date = dateEle.text();

                Element reviewTextEle = eachReview.getElementsByClass("a-size-base review-text").get(0);
                reviewText = reviewTextEle.text();

                reviews.setReview(reviewText);
                reviews.setReviewTitle(title);
                reviews.setUserLocation("");
                reviews.setDate(date);
                reviews.setItemCode(amazonIndia.getItemCode());
                reviews.setUserName(userName);
                reviews.setProductUrl(amazonIndia.getUrl());
                reviews.setWebsite(amazonIndia.getWebsite());
                reviews.setRating(starRating);
                reviewsList.add(reviews);

            }

        }

        return reviewsList;
    }

    public List<AmazonIndia> crawlDataSearchWise(List<Configuirations> configuirationsList) throws IOException{
        String searchUrlIntials = "https://www.amazon.in/s?url=search-alias%3Dhpc&field-keywords=";
        Set<AmazonIndia> amazonIndiaSet = new HashSet<>();
        List<String> keywordsList = new ArrayList<>();
        for(Configuirations configuirations:configuirationsList){
            if(configuirations.getAmazonIn()){
                keywordsList.add(configuirations.getKeyword());
            }
        }
        if(keywordsList.size()<1){
            return new ArrayList<>();
        }
        for (String keyword:keywordsList){
            List<String> urlsToCrawl = new ArrayList<>();
            try {
                String searchUrl = searchUrlIntials + (keyword.replaceAll(" ","+"));;
                InputStream response = CrawlerHttpClient.loadContentByHttpClient(searchUrl,HttpHeaderConstants.GOOGLE_BOT);
                Document document = null;
                if(response!=null) {
                    document = Jsoup.parse(response, "UTF-8", "");
                }
                int counter=1;
                while (document == null || document.getElementById("captchacharacters")!=null) {
                    if (counter >= 5) {
                        break;
                    }
                    // 2nd attempt
                    System.out.println("Crawler blocked. Trying Another User-Agent");
                    response = CrawlerHttpClient.loadContentByHttpClient(searchUrl,HttpHeaderConstants.USER_AGENT3);
                    if(response!=null) {
                        document = Jsoup.parse(response, "UTF-8", "");
                    }
                    counter++;
                    if (document == null || document.getElementById("captchacharacters")!=null) {
                        // 3rd attempt
                        System.out.println("Crawler blocked. Trying Another User-Agent");
                        response = CrawlerHttpClient.loadContentByHttpClient(searchUrl,HttpHeaderConstants.GOOGLE_BOT);
                        if(response!=null) {
                            document = Jsoup.parse(response, "UTF-8", "");
                        }
                        counter++;
                    }
                    if (document == null || document.getElementById("captchacharacters")!=null) { // 4th attempt
                        System.out.println("Crawler blocked. Trying Another User-Agent");
                        response = CrawlerHttpClient.loadContentByHttpClient(searchUrl,HttpHeaderConstants.USER_AGENT1);
                        if(response!=null) {
                            document = Jsoup.parse(response, "UTF-8", "");
                        }
                        counter++;
                    }
                    if (document == null || document.getElementById("captchacharacters")!=null) { // 5th attempt
                        System.out.println("Crawler blocked. Trying Another User-Agent");
                        response = CrawlerHttpClient.loadContentByHttpClient(searchUrl,HttpHeaderConstants.USER_AGENT6);
                        if(response!=null) {
                            document = Jsoup.parse(response, "UTF-8", "");
                        }
                        counter++;
                    }
                    if (document == null || document.getElementById("captchacharacters")!=null) { // 6th attempt
                        System.out.println("Crawler blocked. Trying Another User-Agent");
                        response = CrawlerHttpClient.loadContentByHttpClient(searchUrl,HttpHeaderConstants.USER_AGENT6);
                        if(response!=null) {
                            document = Jsoup.parse(response, "UTF-8", "");
                        }
                        counter++;
                    }
                }
                if(document.getElementById("captchacharacters")!=null || document.getElementById("noResultsTitle")!=null || !document.getElementById("s-result-count").text().contains(keyword)){
                    continue;
                }
                Element searchCount = document.getElementById("s-result-count");
                if(searchCount!=null) {
                    Integer totalSearchResults = new Integer(0);
                    Integer totalPages = new Integer(1);
                    try {
                        String searchResult = searchCount.text();
                        if(searchResult.contains(" results")) {
                            searchResult = searchResult.split(" results")[0];
                        }
                        searchResult = searchResult.substring(searchResult.lastIndexOf(" ")+1);
                        searchResult = searchResult.replaceAll(",","");
                        totalSearchResults = Integer.parseInt(searchResult);
                        Double pages = totalSearchResults/24.0;
                        pages = Math.ceil(pages);
                        totalPages = pages.intValue();
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                    //Crawling of 1st Page
                    for(int i=0;i<24;i++){
                        String idSearchString = "result_"+i;
                        Element urlEle = document.getElementById(idSearchString);
                        if(urlEle!=null) {
                            Element urlClass = urlEle.getElementsByTag("a").get(0);
                            String url = urlClass.attr("abs:href");
                            urlsToCrawl.add(url);
                        }
                    }

                    if(totalPages > 1){
                        for(int i=2;i<=totalPages;i++){
                            searchUrl = searchUrlIntials + keyword + "&page=" + i;
                            response = CrawlerHttpClient.loadContentByHttpClient(searchUrl,HttpHeaderConstants.GOOGLE_BOT);
                            Document document1 = null;
                            if(response!=null) {
                                document1 = Jsoup.parse(response, "UTF-8", "");
                            }
                            counter=1;
                            while(document1==null || document1.getElementById("captchacharacters")!=null) {
                                if (counter >= 5) {
                                    break;
                                }
                                // 2nd attempt
                                System.out.println("Crawler blocked. Trying Another User-Agent");
                                response = CrawlerHttpClient.loadContentByHttpClient(searchUrl,HttpHeaderConstants.USER_AGENT4);
                                if(response!=null) {
                                    document1 = Jsoup.parse(response, "UTF-8", "");
                                }
                                counter++;
                                if (document1 == null|| document1.getElementById("captchacharacters")!=null) {
                                    // 3rd attempt
                                    System.out.println("Crawler blocked. Trying Another User-Agent");
                                    response = CrawlerHttpClient.loadContentByHttpClient(searchUrl,HttpHeaderConstants.USER_AGENT3);
                                    if(response!=null) {
                                        document1 = Jsoup.parse(response, "UTF-8", "");
                                    }
                                    counter++;
                                }
                                if (document1 == null || document1.getElementById("captchacharacters")!=null) { // 4th attempt
                                    System.out.println("Crawler blocked. Trying Another User-Agent");
                                    response = CrawlerHttpClient.loadContentByHttpClient(searchUrl,HttpHeaderConstants.USER_AGENT1);
                                    if(response!=null) {
                                        document1 = Jsoup.parse(response, "UTF-8", "");
                                    }
                                    counter++;
                                }
                                if (document1 == null|| document1.getElementById("captchacharacters")!=null) { // 5th attempt
                                    System.out.println("Crawler blocked. Trying Another User-Agent");
                                    response = CrawlerHttpClient.loadContentByHttpClient(searchUrl,HttpHeaderConstants.USER_AGENT6);
                                    if(response!=null) {
                                        document1 = Jsoup.parse(response, "UTF-8", "");
                                    }
                                    counter++;
                                }
                                if (document1 == null|| document1.getElementById("captchacharacters")!=null) { // 6th attempt
                                    System.out.println("Crawler blocked. Trying Another User-Agent");
                                    response = CrawlerHttpClient.loadContentByHttpClient(searchUrl,HttpHeaderConstants.USER_AGENT6);
                                    if(response!=null) {
                                        document1 = Jsoup.parse(response, "UTF-8", "");
                                    }
                                    counter++;
                                }
                            }
                            for(int j=24*(i-1);j<24*i;j++){
                                try {
                                    String idSearchString = "result_"+j;
                                    Element urlEle = document1.getElementById(idSearchString);
                                    if(urlEle!=null) {
                                        Element urlClass = urlEle.getElementsByTag("a").get(0);
                                        String url = urlClass.attr("abs:href");
                                        urlsToCrawl.add(url);
                                    }
                                } catch (Exception e) {
                                    //e.printStackTrace();
                                }
                            }
                        }
                    }

                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
            System.out.println("Found " + urlsToCrawl.size() + " results for keyword: " + keyword);
            List<AmazonIndia> amazonIndiaList =  getAmazonData(urlsToCrawl,keyword);
            amazonIndiaSet.addAll(amazonIndiaList);
        }
        List<AmazonIndia> sortedList = new ArrayList<>(amazonIndiaSet);
        List<Reviews> reviewsList = new ArrayList<>();
        try {
            reviewsList = readReviewsFromExcel(sortedList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sortedList = readDataFromExcel(sortedList);
        try {
            sortedList.sort(Comparator.comparing(AmazonIndia::getSearchKeyword));
        } catch (Exception e) {
            e.printStackTrace();
        }
        writeDataToExcel(sortedList);
        writeReviewsToExcel(reviewsList);
        return sortedList;
    }


    public List<AmazonIndia> readDataFromExcel(List<AmazonIndia> amazonIndiaList) throws IOException {
        List<AmazonIndia> amazonIndiaFinalList = new ArrayList<>();
        File file = new File("Consolidated Crawled AmazonIN Data.xlsx");
        if(!file.exists()){
            return amazonIndiaList;
        }
        FileInputStream inputStream = new FileInputStream(file);
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet spreadsheet = workbook.getSheet("AmazonIN");
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
            AmazonIndia amazonIndia = new AmazonIndia();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                String cellValue = cell.getStringCellValue();
                if(colId==0){
                    amazonIndia.setSearchKeyword(cellValue);
                }
                if(colId==1){
                    amazonIndia.setWebsite(cellValue);
                }
                if(colId==2){
                    amazonIndia.setUrl(cellValue);

                }
                if(colId==3){
                    amazonIndia.setProductName(cellValue);

                }
                if(colId==4){
                    amazonIndia.setProductType(cellValue);

                }
                if(colId==5){
                    amazonIndia.setProductCompany(cellValue);

                }
                if(colId==6){
                    amazonIndia.setDescription(cellValue);

                }
                if(colId==7){
                    amazonIndia.setItemCode(cellValue);

                }
                if(colId==8){
                    amazonIndia.setContainerType(cellValue);

                }
                if(colId==9){
                    amazonIndia.setType(cellValue);

                }
                if(colId==10){
                    amazonIndia.setSizePerCountUnit(cellValue);

                }
                if(colId==11){
                    amazonIndia.setOfferPrice(cellValue);

                }
                if(colId==12){
                    amazonIndia.setDiscountPrice(cellValue);

                }
                if(colId==13){
                    amazonIndia.setSuggestedRetail(cellValue);

                }
                if(colId==14){
                    amazonIndia.setShippingFee(cellValue);

                }
                if(colId==15){
                    amazonIndia.setServingSize(cellValue);

                }
                if(colId==16){
                    amazonIndia.setServingPerContainer(cellValue);

                }
                if(colId==17){
                    amazonIndia.setCostPerServing(cellValue);

                }
                if(colId==18){
                    amazonIndia.setCostPerUnit(cellValue);

                }
                if(colId==19){
                    amazonIndia.setShippingWeightOrDimensions(cellValue);

                }
                if(colId==20){
                    amazonIndia.setDirectionToUse(cellValue);

                }
                if(colId==21){
                    amazonIndia.setSuppInfo(cellValue);

                }
                if(colId==22){
                    amazonIndia.setWarnings(cellValue);

                }
                if(colId==23){
                    amazonIndia.setMoreDescription(cellValue);

                }
                if(colId==24){
                    amazonIndia.setCurrency(cellValue);

                }
                if(colId==25){
                    amazonIndia.setReviewsCount(cellValue);

                }
                if(colId==26){
                    amazonIndia.setReviewsRating(cellValue);

                }
                if(colId==27){
                    amazonIndia.setReviews5Star(cellValue);

                }
                if(colId==28){
                    amazonIndia.setReviews4Star(cellValue);

                }
                if(colId==29){
                    amazonIndia.setReviews3Star(cellValue);

                }
                if(colId==30){
                    amazonIndia.setReviews2Star(cellValue);

                }
                if(colId==31){
                    amazonIndia.setReviews1Star(cellValue);

                }
                colId++;
            }
            amazonIndiaFinalList.add(amazonIndia);
        }
        if(amazonIndiaFinalList.size() > 0) {
            for (AmazonIndia amazonIndia : amazonIndiaList) {
                amazonIndiaFinalList.removeIf(AmazonIndia -> AmazonIndia.getUrl().equalsIgnoreCase(amazonIndia.getUrl()));
            }
        }
        amazonIndiaFinalList.addAll(amazonIndiaList);

        return amazonIndiaFinalList;
    }

    public List<Reviews> readReviewsFromExcel(List<AmazonIndia> amazonIndiaList) throws IOException {
        List<Reviews> amazonIndiaReviewsList = new ArrayList<>();
        List<Reviews> amazonIndiaNewReviewsList = new ArrayList<>();
        for(AmazonIndia amazonIndia:amazonIndiaList){
            try {
                List<Reviews> reviewsList = new ArrayList<>(amazonIndia.getReviews());
                amazonIndiaNewReviewsList.addAll(reviewsList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        File file = new File("AmazonIN Reviews.xlsx");
        if(!file.exists()){
            return amazonIndiaNewReviewsList;
        }
        FileInputStream inputStream = new FileInputStream(file);
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet spreadsheet = workbook.getSheet("AmazonIN Reviews");
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
            amazonIndiaReviewsList.add(reviews);
        }
        if(amazonIndiaReviewsList.size() > 0) {
            for (Reviews reviews : amazonIndiaNewReviewsList) {
                amazonIndiaReviewsList.removeIf(Reviews -> Reviews.getProductName().equalsIgnoreCase(reviews.getProductName()) &&
                        Reviews.getProductUrl().equalsIgnoreCase(reviews.getProductUrl())&&
                        Reviews.getReviewTitle().equalsIgnoreCase(reviews.getReviewTitle())&&
                        Reviews.getUserName().equalsIgnoreCase(reviews.getUserName())&&
                        Reviews.getReview().equalsIgnoreCase(reviews.getReview()));
            }
        }
        amazonIndiaReviewsList.addAll(amazonIndiaNewReviewsList);

        return amazonIndiaReviewsList;
    }

    public void writeDataToExcel(List<AmazonIndia> amazonIndiaList) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet spreadsheet = workbook.createSheet("AmazonIN");


        //Create row object
        Row row;
        int rowid = 0;
        int cellid = 0;
        row = spreadsheet.createRow(rowid++);
        for(String col: getMainFileColumnNames()){
            Cell cell = row.createCell(cellid++);
            cell.setCellValue(col);
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
        spreadsheet.autoSizeColumn(6);
        spreadsheet.createFreezePane(0, 1);
        //Write the workbook in file system
        FileOutputStream out = new FileOutputStream(
                new File("Consolidated Crawled AmazonIN Data.xlsx"));
        workbook.write(out);
        out.close();
        System.out.println("Consolidated Crawled AmazonIN Data.xlsx written successfully");

    }

    public void writeReviewsToExcel(List<Reviews> reviewsList) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet spreadsheet = workbook.createSheet("AmazonIN Reviews");


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
                    new File("AmazonIN Reviews.xlsx"));
            workbook.write(out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("AmazonIN Reviews.xlsx might be open");
            out.close();
            return;
        }
        System.out.println("AmazonIN Reviews.xlsx written successfully");
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
