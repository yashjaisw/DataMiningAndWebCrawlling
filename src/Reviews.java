import java.util.ArrayList;
import java.util.List;

public class Reviews {

    String website;
    String productUrl;
    String productName;
    String itemCode;
    String userName;
    String date;
    String userLocation;
    String reviewTitle;
    String review;
    String Rating;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String user) {
        this.userName = user;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getRating() {
        return Rating;
    }

    public void setRating(String rating) {
        Rating = rating;
    }

    public String getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getReviewTitle() {
        return reviewTitle;
    }

    public void setReviewTitle(String reviewTitle) {
        this.reviewTitle = reviewTitle;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public List<String> getAllValues() {
        List<String> values = new ArrayList<>();
        values.add(website);
        values.add(productUrl);
        values.add(productName);
        values.add(itemCode);
        values.add(userName);
        values.add(date);
        values.add(userLocation);
        values.add(reviewTitle);
        values.add(review);
        values.add(Rating);
        return values;
    }
}
