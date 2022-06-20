package hwmsacapstoneteam.domain;

import hwmsacapstoneteam.domain.*;
import hwmsacapstoneteam.infra.AbstractEvent;
import java.util.Date;
import lombok.Data;

@Data
public class PayChecked extends AbstractEvent {

    private Long id;
    private String orderId;
    private Integer price;

    public PayChecked(Pay aggregate) {
        super(aggregate);
    }

    public PayChecked() {
        super();
    }
    // keep

}
