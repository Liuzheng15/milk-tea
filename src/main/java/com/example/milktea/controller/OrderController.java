package com.example.milktea.controller;

import com.example.milktea.common.Result;
import com.example.milktea.dto.OrderRequest;
import com.example.milktea.entity.OrderItem;
import com.example.milktea.entity.Orders;
import com.example.milktea.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public Result<List<Orders>> list() {
        return Result.success(orderService.list());
    }

    @GetMapping("/{id}")
    public Result<Orders> getById(@PathVariable Long id) {
        return Result.success(orderService.getById(id));
    }

    @GetMapping("/{id}/items")
    public Result<List<OrderItem>> getOrderItems(@PathVariable Long id) {
        return Result.success(orderService.getOrderItems(id));
    }

    @PostMapping
    public Result<Orders> placeOrder(@RequestBody OrderRequest request) {
        try {
            Orders order = orderService.placeOrder(request);
            return Result.success(order);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        orderService.updateStatus(id, status);
        return Result.success("状态更新成功");
    }

    @PostMapping("/{id}/refund")
    public Result<String> refund(@PathVariable Long id) {
        try {
            orderService.refund(id);
            return Result.success("退款成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
