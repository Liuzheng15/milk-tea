package com.example.milktea.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.milktea.entity.Coupon;
import com.example.milktea.mapper.CouponMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CouponService {

    @Autowired
    private CouponMapper couponMapper;

    public List<Coupon> list() {
        return couponMapper.selectList(null);
    }

    public List<Coupon> listAvailable() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Coupon::getStatus, 1)
                .and(w -> w.isNull(Coupon::getStartTime).or().le(Coupon::getStartTime, now))
                .and(w -> w.isNull(Coupon::getEndTime).or().ge(Coupon::getEndTime, now));
        return couponMapper.selectList(wrapper);
    }

    public Coupon getById(Long id) {
        return couponMapper.selectById(id);
    }

    public boolean add(Coupon coupon) {
        return couponMapper.insert(coupon) > 0;
    }

    public boolean update(Coupon coupon) {
        return couponMapper.updateById(coupon) > 0;
    }

    public boolean delete(Long id) {
        return couponMapper.deleteById(id) > 0;
    }

    /**
     * 计算优惠券优惠金额
     * type: 1满减 2折扣 3立减
     */
    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderAmount) {
        if (coupon == null || orderAmount == null) {
            return BigDecimal.ZERO;
        }
        // 检查最低消费
        BigDecimal minAmount = coupon.getMinAmount() != null ? coupon.getMinAmount() : BigDecimal.ZERO;
        if (orderAmount.compareTo(minAmount) < 0) {
            return BigDecimal.ZERO;
        }
        switch (coupon.getType()) {
            case 1: // 满减
                return coupon.getValue();
            case 2: // 折扣
                BigDecimal discounted = orderAmount.multiply(coupon.getValue());
                return orderAmount.subtract(discounted);
            case 3: // 立减
                return coupon.getValue().min(orderAmount);
            default:
                return BigDecimal.ZERO;
        }
    }
}
