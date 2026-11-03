package com.example.milktea.controller;

import com.example.milktea.common.Result;
import com.example.milktea.entity.Drink;
import com.example.milktea.service.DrinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drinks")
public class DrinkController {

    @Autowired
    private DrinkService drinkService;

    @GetMapping
    public Result<List<Drink>> list() {
        return Result.success(drinkService.list());
    }

    @GetMapping("/{id}")
    public Result<Drink> getById(@PathVariable Long id) {
        return Result.success(drinkService.getById(id));
    }

    @GetMapping("/category/{categoryId}")
    public Result<List<Drink>> listByCategory(@PathVariable Long categoryId) {
        return Result.success(drinkService.listByCategoryId(categoryId));
    }

    @PostMapping
    public Result<Void> add(@RequestBody Drink drink) {
        drinkService.add(drink);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Drink drink) {
        drink.setId(id);
        drinkService.update(drink);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        drinkService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        drinkService.updateStatus(id, status);
        return Result.success();
    }
}
