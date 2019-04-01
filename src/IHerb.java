import java.util.ArrayList;
import java.util.List;

public class IHerb {
    String searchKeyword;
    String website = "iherb.com";
    String url;
    String productName;
    String productType = "";
    String productCompany;
    String description;
    String itemCode;
    String containerType;
    String type;
    String sizePerCountUnit;
    String offerPrice;
    String discountPrice;
    String suggestedRetail;
    String shippingFee;
    String servingSize;
    String servingPerContainer;
    String costPerServing;
    String costPerUnit;
    String shippingWeightOrDimensions;
    String directionToUse;
    String suppInfo;
    String warnings;
    String moreDescription;
    String currency;
    String reviewsCount;
    String reviewsRating;
    String reviews5Star;
    String reviews4Star;
    String reviews3Star;
    String reviews2Star;
    String reviews1Star;

    public List<Reviews> reviews;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getProductCompany() {
        return productCompany;
    }

    public void setProductCompany(String productCompany) {
        this.productCompany = productCompany;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getContainerType() {
        return containerType;
    }

    public void setContainerType(String containerType) {
        this.containerType = containerType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSizePerCountUnit() {
        return sizePerCountUnit;
    }

    public void setSizePerCountUnit(String sizePerCountUnit) {
        this.sizePerCountUnit = sizePerCountUnit;
    }

    public String getOfferPrice() {
        return offerPrice;
    }

    public void setOfferPrice(String offerPrice) {
        offerPrice = offerPrice.replace(",","");
        this.offerPrice = offerPrice;
    }

    public String getDiscountPrice() {
        return discountPrice;
    }

    public void setDiscountPrice(String discountPrice) {
        offerPrice = offerPrice.replace(",","");
        this.discountPrice = discountPrice;
    }

    public String getSuggestedRetail() {
        return suggestedRetail;
    }

    public void setSuggestedRetail(String suggestedRetail) {
        suggestedRetail = suggestedRetail.replace(",","");
        this.suggestedRetail = suggestedRetail;
    }

    public String getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(String shippingFee) {
        this.shippingFee = shippingFee;
    }

    public String getServingSize() {
        return servingSize;
    }

    public void setServingSize(String servingSize) {
        this.servingSize = servingSize;
    }

    public String getServingPerContainer() {
        return servingPerContainer;
    }

    public void setServingPerContainer(String servingPerContainer) {
        this.servingPerContainer = servingPerContainer;
    }

    public String getShippingWeightOrDimensions() {
        return shippingWeightOrDimensions;
    }

    public void setShippingWeightOrDimensions(String shippingWeightOrDimensions) {
        this.shippingWeightOrDimensions = shippingWeightOrDimensions;
    }

    public String getDirectionToUse() {
        return directionToUse;
    }

    public void setDirectionToUse(String directionToUse) {
        this.directionToUse = directionToUse;
    }

    public String getSuppInfo() {
        return suppInfo;
    }

    public void setSuppInfo(String suppInfo) {
        this.suppInfo = suppInfo;
    }

    public String getWarnings() {
        return warnings;
    }

    public void setWarnings(String warnings) {
        this.warnings = warnings;
    }

    public String getMoreDescription() {
        return moreDescription;
    }

    public void setMoreDescription(String moreDescription) {
        this.moreDescription = moreDescription;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getReviewsCount() {
        return reviewsCount;
    }

    public void setReviewsCount(String reviewsCount) {
        this.reviewsCount = reviewsCount;
    }

    public String getReviewsRating() {
        return reviewsRating;
    }

    public void setReviewsRating(String reviewsRating) {
        this.reviewsRating = reviewsRating;
    }

    public String getReviews5Star() {
        return reviews5Star;
    }

    public void setReviews5Star(String reviews5Star) {
        this.reviews5Star = reviews5Star;
    }

    public String getReviews4Star() {
        return reviews4Star;
    }

    public void setReviews4Star(String reviews4Star) {
        this.reviews4Star = reviews4Star;
    }

    public String getReviews3Star() {
        return reviews3Star;
    }

    public void setReviews3Star(String reviews3Star) {
        this.reviews3Star = reviews3Star;
    }

    public String getReviews2Star() {
        return reviews2Star;
    }

    public void setReviews2Star(String reviews2Star) {
        this.reviews2Star = reviews2Star;
    }

    public String getReviews1Star() {
        return reviews1Star;
    }

    public void setReviews1Star(String reviews1Star) {
        this.reviews1Star = reviews1Star;
    }

    public String getCostPerServing() {
        return costPerServing;
    }

    public void setCostPerServing(String costPerServing) {
        this.costPerServing = costPerServing;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public String getCostPerUnit() {
        return costPerUnit;
    }

    public void setCostPerUnit(String costPerUnit) {
        this.costPerUnit = costPerUnit;
    }

    public List<Reviews> getReviews() {
        return reviews;
    }

    public void setReviews(List<Reviews> reviews) {
        this.reviews = reviews;
    }

    public List<String> getAllValues(){
        List<String> values = new ArrayList<>();
        values.add(searchKeyword);
        values.add(website);
        values.add(url);
        values.add(productName);
        values.add(productType);
        values.add(productCompany);
        values.add(description);
        values.add(itemCode);
        values.add(containerType);
        values.add(type);
        values.add(sizePerCountUnit);
        values.add(offerPrice);
        values.add(discountPrice);
        values.add(suggestedRetail);
        values.add(shippingFee);
        values.add(servingSize);
        values.add(servingPerContainer);
        values.add(costPerServing);
        values.add(costPerUnit);
        values.add(shippingWeightOrDimensions);
        values.add(directionToUse);
        values.add(suppInfo);
        values.add(warnings);
        values.add(moreDescription);
        values.add(currency);
        values.add(reviewsCount);
        values.add(reviewsRating);
        values.add(reviews5Star);
        values.add(reviews4Star);
        values.add(reviews3Star);
        values.add(reviews2Star);
        values.add(reviews1Star);

        return values;
    }
}
