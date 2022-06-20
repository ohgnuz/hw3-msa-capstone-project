package hwmsacapstoneteam.domain;

import java.util.Date;
import java.util.List;
import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "OrderStatus_table")
@Data
public class OrderStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String orderId;
    private String productId;
    private String productName;
    private String qty;
    private String price;
    private String address;
    private String orderStatus;
}
