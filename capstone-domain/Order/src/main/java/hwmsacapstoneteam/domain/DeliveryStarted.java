package hwmsacapstoneteam.domain;

import hwmsacapstoneteam.infra.AbstractEvent;
import java.util.Date;
import lombok.Data;

@Data
public class DeliveryStarted extends AbstractEvent {

    private Long id;
    private String orderId;
    private String address;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
