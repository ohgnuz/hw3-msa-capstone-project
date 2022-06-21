package hwmsacapstoneteam.domain;

import hwmsacapstoneteam.domain.*;
import hwmsacapstoneteam.infra.AbstractEvent;
import java.util.Date;
import lombok.Data;

@Data
public class QtyDecreased extends AbstractEvent {

    private Long id;
    private String name;
    private Integer qty;
    private Integer price;

    public QtyDecreased(Product aggregate) {
        super(aggregate);
    }

    public QtyDecreased() {
        super();
    }
    // keep

}
