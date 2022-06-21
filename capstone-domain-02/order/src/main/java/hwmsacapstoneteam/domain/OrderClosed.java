package hwmsacapstoneteam.domain;

import hwmsacapstoneteam.domain.*;
import hwmsacapstoneteam.infra.AbstractEvent;
import java.util.Date;
import lombok.Data;

@Data
public class OrderClosed extends AbstractEvent {

    private Long id;
    private String address;
    private Integer qty;
    private String productName;
    private Integer price;
    private Long productId;

    public OrderClosed(Order aggregate) {
        super(aggregate);
    }

    public OrderClosed() {
        super();
    }
    // keep

}
