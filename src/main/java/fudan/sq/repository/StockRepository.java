package fudan.sq.repository;

import fudan.sq.entity.Repayment;
import fudan.sq.entity.Stock;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface StockRepository extends CrudRepository<Stock,Long> {
    List<Stock> findAll();
    List<Stock> findDistinctFirstByProductId(Long productId);
    List<Stock> findAllByProductName(String productName);
}
