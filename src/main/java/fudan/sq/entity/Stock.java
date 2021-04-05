package fudan.sq.entity;


import javax.persistence.*;
import java.sql.Date;

@Entity
@IdClass(StockId.class)

public class Stock {

    //@GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long productId;
    private String productName;
    private int productPrice;
    @Id
    private Date date;

    public Long getStockId() {
        return productId;
    }

    public void setStockId(Long stockId) {
        this.productId = stockId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(int productPrice) {
        this.productPrice = productPrice;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
