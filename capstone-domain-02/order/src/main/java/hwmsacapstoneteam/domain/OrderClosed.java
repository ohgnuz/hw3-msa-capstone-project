package hwmsacapstoneteam.domain;

import hwmsacapstoneteam.domain.*;
import hwmsacapstoneteam.infra.AbstractEvent;
import java.util.Date;
import lombok.Data;

@Data
public class OrderClosed extends AbstractEvent {

    private Long id;
    private String address;
    private Integer qty;
    private String productName;
    private Long productId;

    public OrderClosed() {
        super();
    }

    public OrderClosed(Order order){
        super();
        this.setId(order.getId());
        this.setAddress(order.getAddress());
        this.setQty(order.getQty());
        this.setProductName(order.getProductName());
        this.setProductId(order.getProductId());

    }
    // keep

    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }


}
