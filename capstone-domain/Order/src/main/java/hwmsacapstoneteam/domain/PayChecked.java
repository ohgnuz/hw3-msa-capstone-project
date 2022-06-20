package hwmsacapstoneteam.domain;

import hwmsacapstoneteam.infra.AbstractEvent;
import java.util.Date;
import lombok.Data;

@Data
public class PayChecked extends AbstractEvent {

    private Long id;
    private String orderId;
    private Integer price;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
