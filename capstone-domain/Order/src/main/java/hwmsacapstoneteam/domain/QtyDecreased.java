package hwmsacapstoneteam.domain;

import hwmsacapstoneteam.infra.AbstractEvent;
import java.util.Date;
import lombok.Data;

@Data
public class QtyDecreased extends AbstractEvent {

    private Long id;
    private String orderId;
    private String name;
    private Integer qty;
    private Integer price;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
}
