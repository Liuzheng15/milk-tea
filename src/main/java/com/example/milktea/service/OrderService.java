package com.example.milktea.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.milktea.dto.OrderItemDTO;
import com.example.milktea.dto.OrderRequest;
import com.example.milktea.entity.*;
import com.example.milktea.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderService {

    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private DrinkMapper drinkMapper;
    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private InventoryMapper inventoryMapper;
    @Autowired
    private CouponMapper couponMapper;
    @Autowired
    private DrinkIngredientMapper drinkIngredientMapper;
    @Autowired
    private MemberService memberService;
    @Autowired
    private CouponService couponService;

    /**
     * 下单（事务核心）：
     * 1. 校验会员、饮品
     * 2. 计算总金额 + 应用优惠券
     * 3. 按饮品原料映射扣减库存（不足则回滚）
     * 4. 创建订单 + 订单明细
     * 5. 按会员等级阶梯倍率累加积分
     */
    @Transactional
    public Orders placeOrder(OrderRequest request) {
        // 1. 校验会员
        Member member = memberMapper.selectById(request.getMemberId());
        if (member == null) {
            throw new RuntimeException("会员不存在");
        }

        // 2. 校验饮品并计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemDTO item : request.getItems()) {
            Drink drink = drinkMapper.selectById(item.getDrinkId());
            if (drink == null) {
                throw new RuntimeException("饮品不存在: " + item.getDrinkId());
            }
            if (drink.getStatus() != 1) {
                throw new RuntimeException("饮品已下架: " + drink.getName());
            }
            BigDecimal subtotal = drink.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
        }

        // 3. 应用优惠券
        BigDecimal discountAmount = BigDecimal.ZERO;
        Long couponId = request.getCouponId();
        if (couponId != null) {
            Coupon coupon = couponMapper.selectById(couponId);
            if (coupon == null || coupon.getStatus() != 1) {
                throw new RuntimeException("优惠券不可用");
            }
            discountAmount = couponService.calculateDiscount(coupon, totalAmount);
            if (discountAmount.compareTo(BigDecimal.ZERO) > 0) {
                totalAmount = totalAmount.subtract(discountAmount);
            }
        }

        // 4. 按饮品原料映射扣减库存
        Map<Long, BigDecimal> inventoryDeduction = new HashMap<>();
        for (OrderItemDTO item : request.getItems()) {
            List<DrinkIngredient> ingredients = drinkIngredientMapper.selectList(
                    new LambdaQueryWrapper<DrinkIngredient>().eq(DrinkIngredient::getDrinkId, item.getDrinkId()));
            for (DrinkIngredient ing : ingredients) {
                BigDecimal need = ing.getQuantity().multiply(BigDecimal.valueOf(item.getQuantity()));
                inventoryDeduction.merge(ing.getInventoryId(), need, BigDecimal::add);
            }
        }
        // 校验并扣减
        for (Map.Entry<Long, BigDecimal> entry : inventoryDeduction.entrySet()) {
            Inventory inv = inventoryMapper.selectById(entry.getKey());
            if (inv == null || inv.getQuantity().compareTo(entry.getValue()) < 0) {
                String invName = inv != null ? inv.getName() : "未知原料";
                throw new RuntimeException("库存不足: " + invName);
            }
            inv.setQuantity(inv.getQuantity().subtract(entry.getValue()));
            inventoryMapper.updateById(inv);
        }

        // 5. 创建订单
        Orders order = new Orders();
        order.setOrderNo(generateOrderNo());
        order.setMemberId(request.getMemberId());
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setCouponId(couponId);
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        ordersMapper.insert(order);

        // 6. 创建订单明细
        for (OrderItemDTO item : request.getItems()) {
            Drink drink = drinkMapper.selectById(item.getDrinkId());
            OrderItem oi = new OrderItem();
            oi.setOrderId(order.getId());
            oi.setDrinkId(drink.getId());
            oi.setDrinkName(drink.getName());
            oi.setQuantity(item.getQuantity());
            oi.setPrice(drink.getPrice());
            oi.setSubtotal(drink.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            orderItemMapper.insert(oi);
        }

        // 7. 按等级阶梯倍率累加积分
        int pointsEarned = memberService.addPoints(request.getMemberId(), totalAmount, order.getId(), "消费获得积分");
        order.setPointsEarned(pointsEarned);
        ordersMapper.updateById(order);

        return order;
    }

    /**
     * 退款（事务）：
     * 1. 校验订单状态（已支付/已完成才可退款）
     * 2. 回滚库存（按订单明细 + 原料映射）
     * 3. 扣回积分
     * 4. 更新订单状态为已退款
     */
    @Transactional
    public void refund(Long orderId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() == 5) {
            throw new RuntimeException("订单已退款，不可重复退款");
        }
        if (order.getStatus() != 1 && order.getStatus() != 3) {
            throw new RuntimeException("当前订单状态不支持退款");
        }

        // 1. 回滚库存
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        Map<Long, BigDecimal> inventoryRollback = new HashMap<>();
        for (OrderItem item : items) {
            List<DrinkIngredient> ingredients = drinkIngredientMapper.selectList(
                    new LambdaQueryWrapper<DrinkIngredient>().eq(DrinkIngredient::getDrinkId, item.getDrinkId()));
            for (DrinkIngredient ing : ingredients) {
                BigDecimal back = ing.getQuantity().multiply(BigDecimal.valueOf(item.getQuantity()));
                inventoryRollback.merge(ing.getInventoryId(), back, BigDecimal::add);
            }
        }
        for (Map.Entry<Long, BigDecimal> entry : inventoryRollback.entrySet()) {
            Inventory inv = inventoryMapper.selectById(entry.getKey());
            if (inv != null) {
                inv.setQuantity(inv.getQuantity().add(entry.getValue()));
                inventoryMapper.updateById(inv);
            }
        }

        // 2. 扣回积分
        if (order.getPointsEarned() != null && order.getPointsEarned() > 0 && order.getMemberId() != null) {
            memberService.deductPointsForRefund(order.getMemberId(), order.getPointsEarned(), orderId, "订单退款扣回积分");
        }

        // 3. 更新订单状态
        order.setStatus(5);
        order.setRefundTime(LocalDateTime.now());
        ordersMapper.updateById(order);
    }

    public List<Orders> list() {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Orders::getCreateTime);
        return ordersMapper.selectList(wrapper);
    }

    public Orders getById(Long id) {
        return ordersMapper.selectById(id);
    }

    public List<OrderItem> getOrderItems(Long orderId) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        return orderItemMapper.selectList(wrapper);
    }

    public void updateStatus(Long id, Integer status) {
        Orders order = new Orders();
        order.setId(id);
        order.setStatus(status);
        ordersMapper.updateById(order);
    }

    private String generateOrderNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rand = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "MT" + date + rand;
    }
}
