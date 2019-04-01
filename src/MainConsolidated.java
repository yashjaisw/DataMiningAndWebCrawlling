import java.util.ArrayList;
import java.util.List;

public class MainConsolidated {

    List<AmazonCom> amazonComList = new ArrayList<>();
    List<AmazonIndia> amazonIndiaList = new ArrayList<>();
    List<VitaminShoppe> vitaminShoppeList = new ArrayList<>();
    List<GNC> gncList = new ArrayList<>();
    List<IHerb> iHerbList = new ArrayList<>();

    public List<AmazonCom> getAmazonComList() {
        return amazonComList;
    }

    public void setAmazonComList(List<AmazonCom> amazonComList) {
        this.amazonComList = amazonComList;
    }

    public List<AmazonIndia> getAmazonIndiaList() {
        return amazonIndiaList;
    }

    public void setAmazonIndiaList(List<AmazonIndia> amazonIndiaList) {
        this.amazonIndiaList = amazonIndiaList;
    }

    public List<VitaminShoppe> getVitaminShoppeList() {
        return vitaminShoppeList;
    }

    public void setVitaminShoppeList(List<VitaminShoppe> vitaminShoppeList) {
        this.vitaminShoppeList = vitaminShoppeList;
    }

    public List<GNC> getGncList() {
        return gncList;
    }

    public void setGncList(List<GNC> gncList) {
        this.gncList = gncList;
    }

    public List<IHerb> getiHerbList() {
        return iHerbList;
    }

    public void setiHerbList(List<IHerb> iHerbList) {
        this.iHerbList = iHerbList;
    }
}
