package com.ecommerce.kafka;

import com.ecommerce.dto.OrderStatusEvent;
import com.ecommerce.service.SmsNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final SmsNotificationService smsNotificationService;

    @KafkaListener(topics = "notifications", groupId = "notification-service")
    public void handleOrderStatusEvent(OrderStatusEvent event) {
        log.info("Received OrderStatusEvent from Kafka: orderId={} status={} reason={}",
                event.getOrderId(), event.getStatus(), event.getReason());

        smsNotificationService.sendOrderStatusSms(event);
    }
}
