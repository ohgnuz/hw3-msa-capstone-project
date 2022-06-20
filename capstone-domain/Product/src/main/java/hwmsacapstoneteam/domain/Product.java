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

    private String orderId;

    private String name;

    private Integer qty;

    private Integer price;

    @PostPersist
    public void onPostPersist() {
        QtyDecreased qtyDecreased = new QtyDecreased(this);
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
