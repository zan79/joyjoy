package joyjoy;

public class Item {

    private String ItemCode;
    private String ItemName;
    private int ItemQty;
    private double ItemRetail;
    private float ItemPrice;

    public Item(String ItemCode, String ItemName, int ItemQty, double ItemRetail, float ItemPrice) {
        this.ItemCode = ItemCode;
        this.ItemName = ItemName;
        this.ItemQty = ItemQty;
        this.ItemRetail = ItemRetail;
        this.ItemPrice = ItemPrice;
    }

    public String getItemCode() {
        return ItemCode;
    }

    public void setItemCode(String ItemCode) {
        this.ItemCode = ItemCode;
    }

    public String getItemName() {
        return ItemName;
    }

    public void setItemName(String ItemName) {
        this.ItemName = ItemName;
    }

    public int getItemQty() {
        return ItemQty;
    }

    public void setItemQty(int ItemQty) {
        this.ItemQty = ItemQty;
    }

    public double getItemRetail() {
        return ItemRetail;
    }

    public void setItemRetail(double ItemRetail) {
        this.ItemRetail = ItemRetail;
    }

    public float getItemPrice() {
        return ItemPrice;
    }

    public void setItemPrice(float ItemPrice) {
        this.ItemPrice = ItemPrice;
    }

}
