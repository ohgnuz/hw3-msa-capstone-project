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

    public PayPlaced() {
        super();
    }

    public PayPlaced(Pay pay){
        super();
        this.setId(pay.getId());
        this.setPrice(pay.getPrice());
        this.setOrderId(pay.getOrderId());

    }
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


    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
}
