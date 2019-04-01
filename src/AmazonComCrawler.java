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

public class AmazonComCrawler {

    public static int count = 0;

    public List<AmazonCom> getAmazonData(List<String> urlList, String searchKeyword) throws IOException {
        List<AmazonCom> amazonComList = new ArrayList<>();
        for(String url:urlList) {
            // url="https://www.amazon.com/Safed-Musli-Extract-Saponins-VegiCaps/dp/B00N50KSDQ/ref=sr_1_3_a_it/133-3574966-5026365?ie=UTF8&qid=1543965995&sr=8-3&keywords=Safed+Musli";
            try {
                AmazonCom amazonCom = new AmazonCom();
                System.out.println("Amazon.com : Crawl count: " + ++count + " Crawling URL: " + url);
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
                    amazonCom.setProductName(productName);
                } catch (Exception e) {
                    //e.printStackTrace();
                }
                //Product Type
                try {
                    Elements productTypeEle = document.getElementsByClass("zg_hrsr_ladder");
                    String productType = productTypeEle.text();
                    amazonCom.setProductType(productType);
                } catch (Exception e) {
                    // e.printStackTrace();
                }

                //Product Company
                try {
                    Element productCompanyEle = document.getElementById("bylineInfo");
                    String productCompany = productCompanyEle.text();
                    amazonCom.setProductCompany(productCompany);
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                //Description
                try {
                    Element descriptionEle = document.getElementById("productDescription");
                    String description = descriptionEle.text();
                    amazonCom.setDescription(description);
                } catch (Exception e) {
                    // e.printStackTrace();
                }

                //Item Code
                try {
                    Element itemCodeEle = document.getElementById("detail-bullets");
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
                    amazonCom.setItemCode(itemCode);
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                try {
                    if(amazonCom.getItemCode()==null || amazonCom.getItemCode().isEmpty()){
                        Element itemCodeEle = document.getElementById("productDetails_detailBullets_sections1");
                        Element tr = itemCodeEle.select("tr").get(0);
                        Elements td = tr.select("th");
                        if(td!=null && td.size() > 0 && td.get(0).text().equals("ASIN")){
                            String itemCode = tr.select("td").get(0).text();
                            amazonCom.setItemCode(itemCode);
                        }
                    }
                } catch (Exception e) {
                    //  e.printStackTrace();
                }

                try {
                    Element offerPriceEle = document.getElementById("priceblock_ourprice");
                    String offerPrice = offerPriceEle.text();
                    amazonCom.setOfferPrice(offerPrice);
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                try {
                    if(amazonCom.getOfferPrice()==null || amazonCom.getOfferPrice().isEmpty()){
                        Element offerPriceEle = document.getElementById("priceblock_saleprice");
                        String offerPrice = offerPriceEle.text();
                        amazonCom.setOfferPrice(offerPrice);
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
                    amazonCom.setDiscountPrice(discountPrice);
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
                    amazonCom.setSuggestedRetail(suggestedRetailPrice);
                } catch (Exception e) {
                    // e.printStackTrace();
                }

                //Delivery
                try {
                    Element shippingEle = document.getElementById("price");
                    Element shippingBTag = shippingEle.select("b").get(0);
                    String shipping = shippingBTag.text();
                    amazonCom.setShippingFee(shipping);
                } catch (Exception e) {
                    // e.printStackTrace();
                }

                try {
                    if(amazonCom.getShippingFee()==null || amazonCom.getShippingFee().isEmpty())
                    {
                        Element shippingEle = document.getElementById("soldByThirdParty");
                        Element shippingEleClass = shippingEle.getElementsByClass("a-size-small a-color-secondary shipping3P").get(0);
                        String shipping = shippingEleClass.text();
                        shipping = shipping.replace("+","");
                        amazonCom.setShippingFee(shipping);

                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                //Shipping Weight or Dimensions
                try {
                    Element shippingDimensionEle = document.getElementById("detail_bullets");
                    Elements shippingDimensionEleClass = shippingDimensionEle.getElementsByClass("content");
                    Elements shippingDimensionEleClassLi = shippingDimensionEleClass.get(0).select("li");
                    for(Element shippingDimensionLoop:shippingDimensionEleClassLi){
                        if(shippingDimensionLoop.text().contains("Shipping Dimensions:")
                                || shippingDimensionLoop.text().contains("Shipping Weight:")
                                ||shippingDimensionLoop.text().contains("Package Dimensions:")){
                            String shippingDimension = shippingDimensionLoop.text();
                            amazonCom.setShippingWeightOrDimensions(amazonCom.getShippingWeightOrDimensions() + shippingDimension);
                        }
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                //More Description
                try {
                    Element impInfo = document.getElementById("importantInformation");
                    String impInfoText = impInfo.text();
                    amazonCom.setMoreDescription(impInfoText);
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
                    amazonCom.setDirectionToUse(directionToUse.toString());
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
                    amazonCom.setReviewsCount(reviewCount);
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
                    amazonCom.setReviewsRating(reviewsRating);
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
                            Double fiveStarCount = percentStar.doubleValue() * (Integer.parseInt(amazonCom.getReviewsCount())) / 100;
                            amazonCom.setReviews5Star(((Long) Math.round(fiveStarCount)).toString());
                        }
                    }
                    else{
                        amazonCom.setReviews5Star("0");
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
                        Double StarCount = percentStar.doubleValue() * (Integer.parseInt(amazonCom.getReviewsCount())) / 100;
                        amazonCom.setReviews4Star(((Long) Math.round(StarCount)).toString());
                    }
                    else{
                        amazonCom.setReviews4Star("0");
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
                        Double StarCount = percentStar.doubleValue() * (Integer.parseInt(amazonCom.getReviewsCount())) / 100;
                        amazonCom.setReviews3Star(((Long) Math.round(StarCount)).toString());
                    }
                    else{
                        amazonCom.setReviews3Star("0");
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
                        Double StarCount = percentStar.doubleValue() * (Integer.parseInt(amazonCom.getReviewsCount())) / 100;
                        amazonCom.setReviews2Star(((Long) Math.round(StarCount)).toString());
                    }
                    else{
                        amazonCom.setReviews2Star("0");
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
                        Double StarCount = percentStar.doubleValue() * (Integer.parseInt(amazonCom.getReviewsCount())) / 100;
                        amazonCom.setReviews1Star(((Long) Math.round(StarCount)).toString());
                    }
                    else{
                        amazonCom.setReviews1Star("0");
                    }
                } catch (Exception e) {
                    // e.printStackTrace();
                }
                amazonCom.setUrl(url);
                //Reviews
                try {
                    Element reviewsUrl = document.getElementById("reviews-medley-footer");
                    Elements urlEle = reviewsUrl.select("a");
                    String finalReviewUrl = "";
                    for(Element urls:urlEle){
                        if(urls.text().contains("See all")){
                            finalReviewUrl = finalReviewUrl + urls.attr("href");
                            break;
                        }
                    }
                    if(finalReviewUrl!=null && finalReviewUrl!=""){
                        try {
                            List<Reviews> reviewsList = getReviews(finalReviewUrl,amazonCom);
                            amazonCom.setReviews(reviewsList);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                amazonCom.setSearchKeyword(searchKeyword);
                amazonCom.setCurrency("INR");
                amazonCom.setUrl(url);
                amazonComList.add(amazonCom);
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }

        return amazonComList;
    }

    public List<Reviews> getReviews(String url, AmazonCom amazonCom) throws IOException {
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
        if(document==null || document.getElementById("captchacharacters")!=null)
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
            reviews.setItemCode(amazonCom.getItemCode());
            reviews.setUserName(userName);
            reviews.setProductUrl(amazonCom.getUrl());
            reviews.setWebsite(amazonCom.getWebsite());
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
            if(document==null|| document.getElementById("captchacharacters")!=null)
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
                reviews.setItemCode(amazonCom.getItemCode());
                reviews.setUserName(userName);
                reviews.setProductUrl(amazonCom.getUrl());
                reviews.setWebsite(amazonCom.getWebsite());
                reviews.setRating(starRating);
                reviewsList.add(reviews);

            }

        }

        return reviewsList;
    }

    public List<AmazonCom> crawlDataSearchWise(List<Configuirations> configuirationsList) throws IOException{
        String searchUrlIntials = "https://www.amazon.com/s/ref=nb_sb_noss?url=search-alias%3Daps&field-keywords=";
        Set<AmazonCom> amazonComSet = new HashSet<>();
        List<String> keywordsList = new ArrayList<>();
        for(Configuirations configuirations:configuirationsList){
            if(configuirations.getAmazonCom()){
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
                    response = CrawlerHttpClient.loadContentByHttpClient(searchUrl,HttpHeaderConstants.USER_AGENT4);
                    if(response!=null) {
                        document = Jsoup.parse(response, "UTF-8", "");
                    }
                    counter++;
                    if (document == null || document.getElementById("captchacharacters")!=null) {
                        // 3rd attempt
                        System.out.println("Crawler blocked. Trying Another User-Agent");
                        response = CrawlerHttpClient.loadContentByHttpClient(searchUrl,HttpHeaderConstants.USER_AGENT3);
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
                        Double pages = totalSearchResults/16.0;
                        pages = Math.ceil(pages);
                        totalPages = pages.intValue();
                    } catch (Exception e) {
                        // e.printStackTrace();
                    }
                    //Crawling of 1st Page
                    int i;
                    for(i=0;i<24;i++){
                        String idSearchString = "result_"+i;
                        Element urlEle = document.getElementById(idSearchString);
                        if(urlEle!=null && urlEle.text().contains("Sponsored")){
                            continue;
                        }
                        if(urlEle!=null) {
                            Element urlClass = urlEle.getElementsByTag("a").get(0);
                            String url = urlClass.attr("abs:href");
                            urlsToCrawl.add(url);
                        }
                        else{
                            break;
                        }
                    }
                    if(totalPages > 1){
                        for(i=2;i<=totalPages;i++){
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
                                if (document1 == null || document1.getElementById("captchacharacters")!=null) {
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
                                if (document1 == null || document1.getElementById("captchacharacters")!=null) { // 5th attempt
                                    System.out.println("Crawler blocked. Trying Another User-Agent");
                                    response = CrawlerHttpClient.loadContentByHttpClient(searchUrl,HttpHeaderConstants.USER_AGENT6);
                                    if(response!=null) {
                                        document1 = Jsoup.parse(response, "UTF-8", "");
                                    }
                                    counter++;
                                }
                                if (document1 == null  || document1.getElementById("captchacharacters")!=null) { // 6th attempt
                                    System.out.println("Crawler blocked. Trying Another User-Agent");
                                    response = CrawlerHttpClient.loadContentByHttpClient(searchUrl,HttpHeaderConstants.USER_AGENT6);
                                    if(response!=null) {
                                        document1 = Jsoup.parse(response, "UTF-8", "");
                                    }
                                    counter++;
                                }
                            }

                            int j;
                            for(j=16*(i-1);j<24*i;j++){
                                String idSearchString = "result_"+j;
                                Element urlEle = document1.getElementById(idSearchString);
                                if(urlEle!=null && urlEle.text().contains("Sponsored")){
                                    continue;
                                }
                                if(urlEle!=null) {
                                    Element urlClass = urlEle.getElementsByTag("a").get(0);
                                    String url = urlClass.attr("abs:href");
                                    urlsToCrawl.add(url);
                                }
                                else{
                                    break;
                                }
                            }
                        }
                    }

                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
            System.out.println("Amazon.com : Found " + urlsToCrawl.size() + " results for keyword: " + keyword);
            List<AmazonCom> amazonComList =  getAmazonData(urlsToCrawl,keyword);
            amazonComSet.addAll(amazonComList);
        }
        List<AmazonCom> sortedList = new ArrayList<>(amazonComSet);
        List<Reviews> reviewsList = new ArrayList<>();
        try {
            reviewsList = readReviewsFromExcel(sortedList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sortedList = readDataFromExcel(sortedList);
        try {
            sortedList.sort(Comparator.comparing(AmazonCom::getSearchKeyword));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            writeDataToExcel(sortedList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            writeReviewsToExcel(reviewsList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sortedList;
    }

    public List<AmazonCom> readDataFromExcel(List<AmazonCom> amazonComList) throws IOException {
        List<AmazonCom> amazonComFinalList = new ArrayList<>();
        File file = new File("Consolidated Crawled Amazon Com Data.xlsx");
        if(!file.exists()){
            return amazonComList;
        }
        FileInputStream inputStream = new FileInputStream(file);
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet spreadsheet = workbook.getSheet("AmazonCom");
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
            AmazonCom amazonCom = new AmazonCom();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                String cellValue = cell.getStringCellValue();
                if(colId==0){
                    amazonCom.setSearchKeyword(cellValue);
                }
                if(colId==1){
                    amazonCom.setWebsite(cellValue);
                }
                if(colId==2){
                    amazonCom.setUrl(cellValue);

                }
                if(colId==3){
                    amazonCom.setProductName(cellValue);

                }
                if(colId==4){
                    amazonCom.setProductType(cellValue);

                }
                if(colId==5){
                    amazonCom.setProductCompany(cellValue);

                }
                if(colId==6){
                    amazonCom.setDescription(cellValue);

                }
                if(colId==7){
                    amazonCom.setItemCode(cellValue);

                }
                if(colId==8){
                    amazonCom.setContainerType(cellValue);

                }
                if(colId==9){
                    amazonCom.setType(cellValue);

                }
                if(colId==10){
                    amazonCom.setSizePerCountUnit(cellValue);

                }
                if(colId==11){
                    amazonCom.setOfferPrice(cellValue);

                }
                if(colId==12){
                    amazonCom.setDiscountPrice(cellValue);

                }
                if(colId==13){
                    amazonCom.setSuggestedRetail(cellValue);

                }
                if(colId==14){
                    amazonCom.setShippingFee(cellValue);

                }
                if(colId==15){
                    amazonCom.setServingSize(cellValue);

                }
                if(colId==16){
                    amazonCom.setServingPerContainer(cellValue);

                }
                if(colId==17){
                    amazonCom.setCostPerServing(cellValue);

                }
                if(colId==18){
                    amazonCom.setCostPerUnit(cellValue);

                }
                if(colId==19){
                    amazonCom.setShippingWeightOrDimensions(cellValue);

                }
                if(colId==20){
                    amazonCom.setDirectionToUse(cellValue);

                }
                if(colId==21){
                    amazonCom.setSuppInfo(cellValue);

                }
                if(colId==22){
                    amazonCom.setWarnings(cellValue);

                }
                if(colId==23){
                    amazonCom.setMoreDescription(cellValue);

                }
                if(colId==24){
                    amazonCom.setCurrency(cellValue);

                }
                if(colId==25){
                    amazonCom.setReviewsCount(cellValue);

                }
                if(colId==26){
                    amazonCom.setReviewsRating(cellValue);

                }
                if(colId==27){
                    amazonCom.setReviews5Star(cellValue);

                }
                if(colId==28){
                    amazonCom.setReviews4Star(cellValue);

                }
                if(colId==29){
                    amazonCom.setReviews3Star(cellValue);

                }
                if(colId==30){
                    amazonCom.setReviews2Star(cellValue);

                }
                if(colId==31){
                    amazonCom.setReviews1Star(cellValue);

                }
                colId++;
            }
            amazonComFinalList.add(amazonCom);
        }
        if(amazonComFinalList.size() > 0) {
            for (AmazonCom amazonCom : amazonComList) {
                amazonComFinalList.removeIf(AmazonCom -> AmazonCom.getUrl().equalsIgnoreCase(amazonCom.getUrl()));
            }
        }
        amazonComFinalList.addAll(amazonComList);

        return amazonComFinalList;
    }

    public List<Reviews> readReviewsFromExcel(List<AmazonCom> amazonComList) throws IOException {
        List<Reviews> amazonComReviewsList = new ArrayList<>();
        List<Reviews> amazonComNewReviewsList = new ArrayList<>();
        for(AmazonCom amazonCom:amazonComList){
            try {
                List<Reviews> reviewsList = new ArrayList<>(amazonCom.getReviews());
                amazonComNewReviewsList.addAll(reviewsList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        File file = new File("AmazonCom Reviews.xlsx");
        if(!file.exists()){
            return amazonComNewReviewsList;
        }
        FileInputStream inputStream = new FileInputStream(file);
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet spreadsheet = workbook.getSheet("AmazonCom Reviews");
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
            amazonComReviewsList.add(reviews);
        }
        if(amazonComReviewsList.size() > 0) {
            for (Reviews reviews : amazonComNewReviewsList) {
                amazonComReviewsList.removeIf(Reviews -> Reviews.getProductName().equalsIgnoreCase(reviews.getProductName()) &&
                        Reviews.getProductUrl().equalsIgnoreCase(reviews.getProductUrl())&&
                        Reviews.getReviewTitle().equalsIgnoreCase(reviews.getReviewTitle())&&
                        Reviews.getUserName().equalsIgnoreCase(reviews.getUserName())&&
                        Reviews.getReview().equalsIgnoreCase(reviews.getReview()));
            }
        }
        amazonComReviewsList.addAll(amazonComNewReviewsList);

        return amazonComReviewsList;
    }

    public void writeDataToExcel(List<AmazonCom> amazonComList) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet spreadsheet = workbook.createSheet("AmazonCom");


        //Create row object
        Row row;
        int rowid = 0;
        int cellid = 0;
        row = spreadsheet.createRow(rowid++);
        for(String col: getMainFileColumnNames()){
            Cell cell = row.createCell(cellid++);
            cell.setCellValue(col);
        }
        for (AmazonCom amazonCom : amazonComList) {
            row = spreadsheet.createRow(rowid++);
            cellid = 0;

            for (String data : amazonCom.getAllValues()){
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
                new File("Consolidated Crawled Amazon Com Data.xlsx"));
        workbook.write(out);
        out.close();
        System.out.println("Consolidated Crawled Amazon Com Data.xlsx written successfully");

    }

    public void writeReviewsToExcel(List<Reviews> reviewsList) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet spreadsheet = workbook.createSheet("AmazonCom Reviews");


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
                    new File("AmazonCom Reviews.xlsx"));
            workbook.write(out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("AmazonCom Reviews.xlsx might be open");
            out.close();
            return;
        }
        System.out.println("AmazonCom Reviews.xlsx written successfully");
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
