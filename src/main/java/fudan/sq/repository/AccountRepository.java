package fudan.sq.repository;

import fudan.sq.entity.Account;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AccountRepository extends CrudRepository<Account,Long> {
    //Repayment findByIouNumAndPanNum(String iouNum,int planNum);
    Account findByAccountNum(String account);
    List<Account> findAllByCustomerNum(String customerNum);
    Account findByAccountNumAndCustomerNum(String accountNum,String customerNum);
}
