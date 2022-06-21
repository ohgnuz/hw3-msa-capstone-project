package hwmsacapstoneteam.infra;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import hwmsacapstoneteam.config.kafka.KafkaProcessor;
import hwmsacapstoneteam.domain.*;
import javax.naming.NameParser;
import javax.naming.NameParser;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@Transactional
public class PolicyHandler {

    @Autowired
    ProductRepository productRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString) {}

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayChecked_QtyDecrease(@Payload PayChecked payChecked) {
        if (!payChecked.validate()) return;
        PayChecked event = payChecked;
        System.out.println(
            "\n\n##### listener QtyDecrease!!!!!!!!!!!!!! : " + payChecked.toJson() + "\n\n"
        );

        // Sample Logic //
        // Product.qtyDecrease(event);

        Optional<Product> productOptional = productRepository.findById(payChecked.getProductId());
        Product product = productOptional.get();

        product.setOrderId(payChecked.getOrderId());
        product.setId(payChecked.getProductId());
        product.setQty(product.getQty() - payChecked.getQty());
        productRepository.save(product);

    }
    // keep

}
