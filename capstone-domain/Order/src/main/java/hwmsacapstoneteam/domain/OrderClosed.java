package hwmsacapstoneteam.domain;

import hwmsacapstoneteam.domain.*;
import hwmsacapstoneteam.infra.AbstractEvent;
import java.util.Date;
import lombok.Data;

@Data
public class OrderClosed extends AbstractEvent {

    private Long id;
    private String orderId;
    private String address;
    private String productId;
    private Integer qty;
    private String productName;
    private Integer price;

    public OrderClosed(Order aggregate) {
        super(aggregate);
    }

    public OrderClosed() {
        super();
    }
    // keep

}
