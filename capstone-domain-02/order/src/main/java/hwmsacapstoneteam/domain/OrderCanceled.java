package hwmsacapstoneteam.domain;

import hwmsacapstoneteam.domain.*;
import hwmsacapstoneteam.infra.AbstractEvent;
import java.util.Date;
import lombok.Data;

@Data
public class OrderCanceled extends AbstractEvent {

    private Long id;
    private String address;
    private Integer qty;
    private String productName;
    private Long productId;

    public OrderCanceled(Order aggregate) {
        super(aggregate);
    }

    public OrderCanceled() {
        super();
    }
    // keep

}
