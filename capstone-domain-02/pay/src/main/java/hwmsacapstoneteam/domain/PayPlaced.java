package hwmsacapstoneteam.domain;

import hwmsacapstoneteam.domain.*;
import hwmsacapstoneteam.infra.AbstractEvent;
import java.util.Date;
import lombok.Data;

@Data
public class PayPlaced extends AbstractEvent {

    private Long id;

    private Long orderId;

    private Long productId;

    private String productName;

    private String address;

    private Integer qty;

    private Integer price;

    public PayPlaced() {
        super();
    }

    public PayPlaced(Pay pay){
        super();
        this.setId(pay.getId());
        this.setPrice(pay.getPrice());
        this.setOrderId(pay.getOrderId());
        this.setProductId(pay.getProductId());
        this.setProductName(pay.getProductName());
        this.setQty(pay.getQty());
        this.setAddress(pay.getAddress());

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

    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }
}
