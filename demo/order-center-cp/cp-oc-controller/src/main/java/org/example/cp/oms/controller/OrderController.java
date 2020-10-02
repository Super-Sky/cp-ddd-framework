package org.example.cp.oms.controller;

import lombok.extern.slf4j.Slf4j;
import org.cdf.ddd.api.RequestProfile;
import org.cdf.ddd.runtime.registry.Container;
import org.example.cp.oms.client.dto.CancelOrderRequest;
import org.example.cp.oms.client.dto.SubmitOrderRequest;
import org.example.cp.oms.domain.model.OrderModel;
import org.example.cp.oms.domain.model.OrderModelCreator;
import org.example.cp.oms.domain.service.SubmitOrder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

@RestController
@Slf4j
public class OrderController {

    // DDD Application Layer depends on Domain Layer
    @Resource
    private SubmitOrder submitOrder;

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    @ResponseBody
    public String hello() {
        return "Hello cp-ddd-framework!";
    }

    @RequestMapping(value = "/reloadIsv")
    @ResponseBody
    public String reloadIsv() {
        log.info("CWD:{}", System.getProperty("user.dir"));

        try {
            Container.getInstance().loadPartnerPlugin("isv", "order-center-bp-isv/target/order-center-bp-isv-0.0.1.jar", true);
        } catch (Throwable cause) {
            log.error("fails to reload ISV Plugin Jar", cause);
            return cause.getMessage();
        }

        return "Reloaded!";
    }

    // 下单服务
    @RequestMapping(value = "/order", method = RequestMethod.POST)
    @ResponseBody
    public String submitOrder(@RequestBody SubmitOrderRequest submitOrderRequest) {
        // DTO 转换为 domain model，通过creator保护、封装domain model
        // 具体项目使用MapStruct会更方便，这里为了演示，全手工进行对象转换了
        RequestProfile requestProfile = new RequestProfile();
        requestProfile.setTraceId("1034344");
        OrderModelCreator creator = new OrderModelCreator();
        creator.setRequestProfile(requestProfile);
        creator.setSource(submitOrderRequest.getSource());
        creator.setCustomerNo(submitOrderRequest.getCustomerNo());
        creator.setExternalNo(submitOrderRequest.getExternalNo());
        OrderModel model = OrderModel.createWith(creator);

        // 调用domain service完成该use case
        submitOrder.submit(model);
        return "Order accepted!";
    }

    // 订单取消
    @RequestMapping(value = "/order", method = RequestMethod.DELETE)
    public String cancelOrder(@NotNull RequestProfile requestProfile, @NotNull CancelOrderRequest cancelOrderRequest) {
        return "Order cancelled!";
    }
}