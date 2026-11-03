package com.example.milktea.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.milktea.entity.*;
import com.example.milktea.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatsService {

    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private DrinkMapper drinkMapper;

    /**
     * 今日营收统计
     */
    public Map<String, Object> todayRevenue() {
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(Orders::getCreateTime, start, end)
                .ne(Orders::getStatus, 4)
                .ne(Orders::getStatus, 5);
        List<Orders> orders = ordersMapper.selectList(wrapper);

        BigDecimal total = orders.stream()
                .map(Orders::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new HashMap<>();
        result.put("date", LocalDate.now().toString());
        result.put("orderCount", orders.size());
        result.put("totalRevenue", total);
        return result;
    }

    /**
     * 热销饮品排行（按销量）
     */
    public List<Map<String, Object>> popularDrinks(int limit) {
        List<OrderItem> allItems = orderItemMapper.selectList(null);
        Map<Long, Integer> drinkQty = new HashMap<>();
        Map<Long, String> drinkNames = new HashMap<>();

        for (OrderItem item : allItems) {
            drinkQty.merge(item.getDrinkId(), item.getQuantity(), Integer::sum);
            drinkNames.put(item.getDrinkId(), item.getDrinkName());
        }

        return drinkQty.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(limit)
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("drinkId", e.getKey());
                    m.put("drinkName", drinkNames.get(e.getKey()));
                    m.put("totalQuantity", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());
    }

    /**
     * 会员消费排行
     */
    public List<Map<String, Object>> memberConsumption(int limit) {
        List<Orders> orders = ordersMapper.selectList(
                new LambdaQueryWrapper<Orders>().ne(Orders::getStatus, 4).ne(Orders::getStatus, 5));

        Map<Long, BigDecimal> memberSpend = new HashMap<>();
        Map<Long, Integer> memberOrderCount = new HashMap<>();
        for (Orders o : orders) {
            if (o.getMemberId() != null) {
                memberSpend.merge(o.getMemberId(), o.getTotalAmount(), BigDecimal::add);
                memberOrderCount.merge(o.getMemberId(), 1, Integer::sum);
            }
        }

        return memberSpend.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(limit)
                .map(e -> {
                    Member m = memberMapper.selectById(e.getKey());
                    Map<String, Object> map = new HashMap<>();
                    map.put("memberId", e.getKey());
                    map.put("memberName", m != null ? m.getName() : "未知");
                    map.put("phone", m != null ? m.getPhone() : "");
                    map.put("totalSpent", e.getValue());
                    map.put("orderCount", memberOrderCount.get(e.getKey()));
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * 订单状态统计
     */
    public Map<String, Object> orderStatusStats() {
        List<Orders> all = ordersMapper.selectList(null);
        Map<Integer, Long> statusCount = all.stream()
                .collect(Collectors.groupingBy(Orders::getStatus, Collectors.counting()));

        Map<String, Object> result = new HashMap<>();
        result.put("total", all.size());
        result.put("pending", statusCount.getOrDefault(0, 0L));
        result.put("paid", statusCount.getOrDefault(1, 0L));
        result.put("making", statusCount.getOrDefault(2, 0L));
        result.put("completed", statusCount.getOrDefault(3, 0L));
        result.put("cancelled", statusCount.getOrDefault(4, 0L));
        result.put("refunded", statusCount.getOrDefault(5, 0L));
        return result;
    }
}
