package fudan.sq.repository;

import fudan.sq.entity.Repayment;
import org.springframework.data.repository.CrudRepository;

public interface RepaymentRepository extends CrudRepository<Repayment,Long> {
    Repayment findByIouNumAndPanNum(String iouNum,int planNum);
}
