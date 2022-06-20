package hwmsacapstoneteam.domain;

import hwmsacapstoneteam.PayApplication;
import hwmsacapstoneteam.domain.PayCanceled;
import hwmsacapstoneteam.domain.PayChecked;
import hwmsacapstoneteam.domain.PayPlaced;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Pay_table")
@Data
public class Pay {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String orderId;

    private Integer price;

    @PostPersist
    public void onPostPersist() {
        PayChecked payChecked = new PayChecked(this);
        payChecked.publishAfterCommit();

        PayCanceled payCanceled = new PayCanceled(this);
        payCanceled.publishAfterCommit();

        PayPlaced payPlaced = new PayPlaced(this);
        payPlaced.publishAfterCommit();
    }

    public static PayRepository repository() {
        PayRepository payRepository = PayApplication.applicationContext.getBean(
            PayRepository.class
        );
        return payRepository;
    }

    public static void payWait(OrderPlaced orderPlaced) {
        /** Example 1:  new item 
        Pay pay = new Pay();
        repository().save(pay);

        */

        /** Example 2:  finding and process
        
        repository().findById(orderPlaced.get???()).ifPresent(pay->{
            
            pay // do something
            repository().save(pay);


         });
        */

    }

    public static void payCancel(OrderCanceled orderCanceled) {
        /** Example 1:  new item 
        Pay pay = new Pay();
        repository().save(pay);

        */

        /** Example 2:  finding and process
        
        repository().findById(orderCanceled.get???()).ifPresent(pay->{
            
            pay // do something
            repository().save(pay);


         });
        */

    }
}
