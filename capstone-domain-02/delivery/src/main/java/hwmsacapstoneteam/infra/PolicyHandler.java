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
    DeliveryRepository deliveryRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString) {}

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverQtyDecreased_DeliveryStart(
        @Payload QtyDecreased qtyDecreased
    ) {
        if (!qtyDecreased.validate()) return;
        QtyDecreased event = qtyDecreased;
        System.out.println(
            "\n\n##### listener DeliveryStart : " +
            qtyDecreased.toJson() +
            "\n\n"
        );

        // Sample Logic //
        // Delivery.deliveryStart(event);
        
        Delivery delivery = new Delivery();

        delivery.setOrderId(qtyDecreased.getOrderId());
        deliveryRepository.save(delivery);



    }
    // keep

}
