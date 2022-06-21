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

@Service
@Transactional
public class PolicyHandler {

    @Autowired
    PayRepository payRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString) {}

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderPlaced_PayWait(@Payload OrderPlaced orderPlaced) {
        if (!orderPlaced.validate()) return;
        OrderPlaced event = orderPlaced;
        System.out.println(
            "\n\n##### listener PayWait : " + orderPlaced.toJson() + "\n\n"
        );

        // Sample Logic //
        // Pay.payWait(event);

        Pay pay = new Pay();
        pay.setOrderId(orderPlaced.getId());
        pay.setProductId(orderPlaced.getProductId());
        pay.setQty(orderPlaced.getQty());
        payRepository.save(pay);

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderCanceled_PayCancel(
        @Payload OrderCanceled orderCanceled
    ) {
        if (!orderCanceled.validate()) return;
        OrderCanceled event = orderCanceled;
        System.out.println(
            "\n\n##### listener PayCancel : " + orderCanceled.toJson() + "\n\n"
        );

        // Sample Logic //
        Pay.payCancel(event);
    }
    // keep

}
