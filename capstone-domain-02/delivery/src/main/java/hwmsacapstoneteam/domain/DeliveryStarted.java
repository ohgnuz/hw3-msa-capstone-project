package hwmsacapstoneteam.domain;

import hwmsacapstoneteam.domain.*;
import hwmsacapstoneteam.infra.AbstractEvent;
import java.util.Date;
import lombok.Data;

@Data
public class DeliveryStarted extends AbstractEvent {

    private Long id;
    private String address;
    private Long orderId;

    public DeliveryStarted() {
        super();
    }

    public DeliveryStarted(Delivery delivery){
        super();
        this.setId(delivery.getId());
        this.setAddress(delivery.getAddress());
        this.setOrderId(delivery.getOrderId());

    }
    // keep

    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


}
