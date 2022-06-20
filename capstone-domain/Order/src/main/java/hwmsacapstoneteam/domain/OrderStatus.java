package hwmsacapstoneteam.domain;

import java.util.Date;
import java.util.List;
import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "OrderStatus_table")
@Data
public class OrderStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String orderId;
    private String productId;
    private String productName;
    private String qty;
    private String price;
    private String address;
    private String orderStatus;

    public OrderStatus(){
        super();
    }

    public OrderStatus(Order order){
        this();
        this.setOrderId(order.getOrderId());
        this.setProductId(order.getProductId());
        this.setProductName(order.getProductName());
        this.setQty(String.valueOf(order.getQty()));
        this.setPrice(String.valueOf(order.getPrice()));
        this.setAddress(order.getAddress());
        this.setOrderStatus(order.getOrderStatus());

    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

}

