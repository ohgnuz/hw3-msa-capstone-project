package hwmsacapstoneteam.infra;

import hwmsacapstoneteam.domain.*;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface OrderStatusRepository
    extends CrudRepository<OrderStatus, Long> {
    List<OrderStatus> findByOrderId(String orderId);

    // keep

}
