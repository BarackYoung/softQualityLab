package fudan.sq.entity;



import java.io.Serializable;
import java.sql.Date;

public class StockId implements Serializable {
    private Long productId;
    private Date date;

    public StockId(){}
    public StockId(Long id,Date date){
        this.productId=id;
        this.date=date;

    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
