package com.example.milktea.controller;

import com.example.milktea.common.Result;
import com.example.milktea.entity.Inventory;
import com.example.milktea.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    public Result<List<Inventory>> list() {
        return Result.success(inventoryService.list());
    }

    @GetMapping("/{id}")
    public Result<Inventory> getById(@PathVariable Long id) {
        return Result.success(inventoryService.getById(id));
    }

    @GetMapping("/warning")
    public Result<List<Inventory>> warningList() {
        return Result.success(inventoryService.warningList());
    }

    @PostMapping
    public Result<Void> add(@RequestBody Inventory inventory) {
        inventoryService.add(inventory);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Inventory inventory) {
        inventory.setId(id);
        inventoryService.update(inventory);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        inventoryService.delete(id);
        return Result.success();
    }

    @PostMapping("/{id}/stock-in")
    public Result<Void> stockIn(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        BigDecimal quantity = new BigDecimal(String.valueOf(body.getOrDefault("quantity", 0)));
        inventoryService.stockIn(id, quantity);
        return Result.success();
    }

    @PostMapping("/{id}/stock-out")
    public Result<Void> stockOut(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        BigDecimal quantity = new BigDecimal(String.valueOf(body.getOrDefault("quantity", 0)));
        inventoryService.stockOut(id, quantity);
        return Result.success();
    }
}
