package hwmsacapstoneteam.domain;

import hwmsacapstoneteam.domain.*;
import hwmsacapstoneteam.infra.AbstractEvent;
import java.util.Date;
import lombok.Data;

@Data
public class QtyDecreased extends AbstractEvent {

    private Long id;
    private String name;
    private Integer qty;
    private Integer price;


    public QtyDecreased() {
        super();
    }

    public QtyDecreased(Product product){
        super();
        this.setId(product.getId());
        this.setName(product.getName());
        this.setQty(product.getQty());
        this.setPrice(product.getPrice());

    }

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

}
