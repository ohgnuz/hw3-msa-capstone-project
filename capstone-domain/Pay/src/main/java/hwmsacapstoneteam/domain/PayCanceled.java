package hwmsacapstoneteam.domain;

import hwmsacapstoneteam.domain.*;
import hwmsacapstoneteam.infra.AbstractEvent;
import java.util.Date;
import lombok.Data;

@Data
public class PayCanceled extends AbstractEvent {

    private Long id;
    private String orderId;
    private Integer price;

    public PayCanceled(Pay aggregate) {
        super(aggregate);
    }

    public PayCanceled() {
        super();
    }
    // keep

}
