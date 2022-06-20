package hwmsacapstoneteam.infra;

import hwmsacapstoneteam.config.kafka.KafkaProcessor;
import hwmsacapstoneteam.domain.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class OrderStatusViewHandler {

    @Autowired
    private OrderStatusRepository orderStatusRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrderPlaced_then_CREATE_1(
        @Payload OrderPlaced orderPlaced
    ) {
        try {
            if (!orderPlaced.validate()) return;

            // view 객체 생성
            OrderStatus orderStatus = new OrderStatus();
            // view 객체에 이벤트의 Value 를 set 함
            orderStatus.setOrderId(orderPlaced.getOrderId());
            orderStatus.setProductId(orderPlaced.getProductId());
            orderStatus.setProductName(orderPlaced.getProductName());
            orderStatus.setQty(String.valueOf(orderPlaced.getQty()));
            orderStatus.setPrice(String.valueOf(orderPlaced.getPrice()));
            orderStatus.setAddress(orderPlaced.getAddress());
            orderStatus.setOrderStatus("주문접수");
            // view 레파지 토리에 save
            orderStatusRepository.save(orderStatus);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenPayPlaced_then_UPDATE_1(@Payload PayPlaced payPlaced) {
        try {
            if (!payPlaced.validate()) return;
            // view 객체 조회

            List<OrderStatus> orderStatusList = orderStatusRepository.findByOrderId(
                payPlaced.getProdutId()
            );
            for (OrderStatus orderStatus : orderStatusList) {
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                orderStatus.setOrderStatus("입금확인중");
                // view 레파지 토리에 save
                orderStatusRepository.save(orderStatus);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenPayChecked_then_UPDATE_2(@Payload PayChecked payChecked) {
        try {
            if (!payChecked.validate()) return;
            // view 객체 조회

            List<OrderStatus> orderStatusList = orderStatusRepository.findByOrderId(
                payChecked.getProdutId()
            );
            for (OrderStatus orderStatus : orderStatusList) {
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                orderStatus.setOrderStatus("입금확인완료");
                // view 레파지 토리에 save
                orderStatusRepository.save(orderStatus);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenPayCanceled_then_UPDATE_3(
        @Payload PayCanceled payCanceled
    ) {
        try {
            if (!payCanceled.validate()) return;
            // view 객체 조회

            List<OrderStatus> orderStatusList = orderStatusRepository.findByOrderId(
                payCanceled.getProdutId()
            );
            for (OrderStatus orderStatus : orderStatusList) {
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                orderStatus.setOrderStatus("입금취소");
                // view 레파지 토리에 save
                orderStatusRepository.save(orderStatus);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrderCanceled_then_UPDATE_4(
        @Payload OrderCanceled orderCanceled
    ) {
        try {
            if (!orderCanceled.validate()) return;
            // view 객체 조회

            List<OrderStatus> orderStatusList = orderStatusRepository.findByOrderId(
                orderCanceled.getOrderId()
            );
            for (OrderStatus orderStatus : orderStatusList) {
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                orderStatus.setOrderStatus("주문취소접수");
                // view 레파지 토리에 save
                orderStatusRepository.save(orderStatus);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenQtyDecreased_then_UPDATE_5(
        @Payload QtyDecreased qtyDecreased
    ) {
        try {
            if (!qtyDecreased.validate()) return;
            // view 객체 조회

            List<OrderStatus> orderStatusList = orderStatusRepository.findByOrderId(
                qtyDecreased.getOrderId()
            );
            for (OrderStatus orderStatus : orderStatusList) {
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                orderStatus.setOrderStatus("재고감소");
                // view 레파지 토리에 save
                orderStatusRepository.save(orderStatus);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenDeliveryStarted_then_UPDATE_6(
        @Payload DeliveryStarted deliveryStarted
    ) {
        try {
            if (!deliveryStarted.validate()) return;
            // view 객체 조회

            List<OrderStatus> orderStatusList = orderStatusRepository.findByOrderId(
                deliveryStarted.getOrderId()
            );
            for (OrderStatus orderStatus : orderStatusList) {
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                orderStatus.setOrderStatus("배송시작");
                // view 레파지 토리에 save
                orderStatusRepository.save(orderStatus);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrderClosed_then_UPDATE_7(
        @Payload OrderClosed orderClosed
    ) {
        try {
            if (!orderClosed.validate()) return;
            // view 객체 조회

            List<OrderStatus> orderStatusList = orderStatusRepository.findByOrderId(
                orderClosed.getOrderId()
            );
            for (OrderStatus orderStatus : orderStatusList) {
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                orderStatus.setOrderStatus("수령확인완료");
                // view 레파지 토리에 save
                orderStatusRepository.save(orderStatus);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // keep

}
