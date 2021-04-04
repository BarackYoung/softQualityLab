package fudan.sq.entity;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Repayment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String iouNum;
    private int panNum;
    private double remainAmount;
    private double remainPrincipal;
    private double remainInterest;
    private double penaltyInterest;

    public double getPenaltyInterest() {
        return penaltyInterest;
    }

    public void setPenaltyInterest(double penaltyInterest) {
        this.penaltyInterest = penaltyInterest;
    }

    public double getRemainAmount() {
        return remainAmount;
    }

    public void setRemainAmount(double remainAmount) {
        this.remainAmount = remainAmount;
    }

    public double getRemainPrincipal() {
        return remainPrincipal;
    }

    public void setRemainPrincipal(double remainPrincipal) {
        this.remainPrincipal = remainPrincipal;
    }

    public double getRemainInterest() {
        return remainInterest;
    }

    public void setRemainInterest(double remainInterest) {
        this.remainInterest = remainInterest;
    }

    public Long getId() {
        return id;
    }

    public String getIouNum() {
        return iouNum;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setIouNum(String iouNum) {
        this.iouNum = iouNum;
    }

    public int getPanNum() {
        return panNum;
    }

    public void setPanNum(int panNum) {
        this.panNum = panNum;
    }

}
