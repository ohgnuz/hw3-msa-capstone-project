package hwmsacapstoneteam.domain;

import hwmsacapstoneteam.OrderApplication;
import hwmsacapstoneteam.domain.OrderCanceled;
import hwmsacapstoneteam.domain.OrderClosed;
import hwmsacapstoneteam.domain.OrderPlaced;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Order_table")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String orderId;

    private String address;

    private String productId;

    private Integer qty;

    private String productName;

    private Integer price;

    @PostPersist
    public void onPostPersist() {
        OrderPlaced orderPlaced = new OrderPlaced(this);
        orderPlaced.publishAfterCommit();

        OrderCanceled orderCanceled = new OrderCanceled(this);
        orderCanceled.publishAfterCommit();

        OrderClosed orderClosed = new OrderClosed(this);
        orderClosed.publishAfterCommit();
    }

    public static OrderRepository repository() {
        OrderRepository orderRepository = OrderApplication.applicationContext.getBean(
            OrderRepository.class
        );
        return orderRepository;
    }
}
