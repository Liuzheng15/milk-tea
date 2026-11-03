package com.example.milktea.controller;

import com.example.milktea.common.Result;
import com.example.milktea.entity.Coupon;
import com.example.milktea.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @GetMapping
    public Result<List<Coupon>> list() {
        return Result.success(couponService.list());
    }

    @GetMapping("/available")
    public Result<List<Coupon>> available() {
        return Result.success(couponService.listAvailable());
    }

    @GetMapping("/{id}")
    public Result<Coupon> getById(@PathVariable Long id) {
        return Result.success(couponService.getById(id));
    }

    @PostMapping
    public Result<String> add(@RequestBody Coupon coupon) {
        return couponService.add(coupon) ? Result.success("添加成功") : Result.error("添加失败");
    }

    @PutMapping
    public Result<String> update(@RequestBody Coupon coupon) {
        return couponService.update(coupon) ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        return couponService.delete(id) ? Result.success("删除成功") : Result.error("删除失败");
    }
}
