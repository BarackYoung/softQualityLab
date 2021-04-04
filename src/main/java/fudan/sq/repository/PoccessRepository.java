package fudan.sq.repository;

import fudan.sq.entity.Poccess;
import fudan.sq.entity.Repayment;
import org.springframework.data.repository.CrudRepository;

public interface PoccessRepository extends CrudRepository<Poccess,Long> {
    //Poccess findByIouNumAndPanNum(String iouNum,int planNum);
}
