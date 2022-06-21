package hwmsacapstoneteam.domain;

import hwmsacapstoneteam.domain.*;
import hwmsacapstoneteam.infra.AbstractEvent;
import java.util.Date;
import lombok.Data;

@Data
public class PayPlaced extends AbstractEvent {

    private Long id;
    private Integer price;
    private Long orderId;

    public PayPlaced(Pay aggregate) {
        super(aggregate);
    }

    public PayPlaced() {
        super();
    }
    // keep

}
