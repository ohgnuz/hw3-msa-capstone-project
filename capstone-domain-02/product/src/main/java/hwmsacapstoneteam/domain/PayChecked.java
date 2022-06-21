package hwmsacapstoneteam.domain;

import hwmsacapstoneteam.domain.*;
import hwmsacapstoneteam.infra.AbstractEvent;
import java.util.Date;
import lombok.Data;

@Data
public class PayChecked extends AbstractEvent {

    private Long id;
    private Integer price;
    private Long orderId;
    // keep

}
