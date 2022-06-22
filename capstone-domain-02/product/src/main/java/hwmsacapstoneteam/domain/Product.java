package hwmsacapstoneteam.domain;

import hwmsacapstoneteam.ProductApplication;
import hwmsacapstoneteam.domain.QtyDecreased;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Product_table")
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    private Integer qty;

    private Integer price;

    private Long orderId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getPrice() {
        return price;
    }

    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    @PostUpdate
    public void onPostUpdate() {
        QtyDecreased qtyDecreased = new QtyDecreased(this);
        
        try {
            Thread.currentThread().sleep((long) (400 + Math.random() * 220));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        qtyDecreased.publishAfterCommit();
    }

    public static ProductRepository repository() {
        ProductRepository productRepository = ProductApplication.applicationContext.getBean(
            ProductRepository.class
        );
        return productRepository;
    }

    public static void qtyDecrease(PayChecked payChecked) {
        /** Example 1:  new item 
        Product product = new Product();
        repository().save(product);
        */





        /** Example 2:  finding and process
        
        repository().findById(payChecked.get???()).ifPresent(product->{
            
            product // do something
            repository().save(product);


         });
        */

    }
}
