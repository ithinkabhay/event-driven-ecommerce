package com.ecommerce.service;


import com.ecommerce.dto.OrderStatusEvent;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsNotificationService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-number}")
    private String fromNumber;

    // For this demo, we send all SMS to a single phone number
    @Value("${notification.default-recipient}")
    private String defaultRecipient;

    @PostConstruct
    public void initTwilio(){
        Twilio.init(accountSid, authToken);
        log.info("Initialized Twilio client");
    }

    public void sendOrderStatusSms(OrderStatusEvent event){

        String to = defaultRecipient;

        String msg;
        if ("COMPLETED".equalsIgnoreCase(event.getStatus())) {
            msg = "Your order " + event.getOrderId() + " has been COMPLETED. Thank you for shopping!";
        } else if ("CANCELLED".equalsIgnoreCase(event.getStatus())) { // match enum spelling
            msg = "Your order " + event.getOrderId() + " was CANCELLED. Reason: "
                    + (event.getReason() != null ? event.getReason() : "Unknown");
        } else {
            msg = "Status update for order " + event.getOrderId() +
                    ": " + event.getStatus();
        }

        try {
            log.info("Sending sms to {}: {}", to , msg);
            Message message = Message.creator(
                            new com.twilio.type.PhoneNumber(to),
                            new com.twilio.type.PhoneNumber(fromNumber),
                            msg)
                    .create();
            log.info("SMS sent. Twilio SID={}", message.getSid());
        }catch (Exception e){
            log.error("Failed to send SMS via Twilio: {}", e.getMessage(), e);
        }
    }


}
