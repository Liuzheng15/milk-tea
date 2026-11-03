package com.example.milktea.controller;

import com.example.milktea.common.Result;
import com.example.milktea.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    @GetMapping("/today-revenue")
    public Result<Map<String, Object>> todayRevenue() {
        return Result.success(statsService.todayRevenue());
    }

    @GetMapping("/popular-drinks")
    public Result<List<Map<String, Object>>> popularDrinks(@RequestParam(defaultValue = "5") int limit) {
        return Result.success(statsService.popularDrinks(limit));
    }

    @GetMapping("/member-consumption")
    public Result<List<Map<String, Object>>> memberConsumption(@RequestParam(defaultValue = "5") int limit) {
        return Result.success(statsService.memberConsumption(limit));
    }

    @GetMapping("/order-status")
    public Result<Map<String, Object>> orderStatus() {
        return Result.success(statsService.orderStatusStats());
    }
}
