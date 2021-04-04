package fudan.sq.repository;

import fudan.sq.entity.Repayment;
import fudan.sq.entity.Stock;
import org.springframework.data.repository.CrudRepository;

public interface StockRepository extends CrudRepository<Stock,Long> {
    //Stock findByIouNumAndPanNum(String iouNum,int planNum);
}
