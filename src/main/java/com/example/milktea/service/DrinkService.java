package com.example.milktea.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.milktea.entity.Drink;
import com.example.milktea.mapper.DrinkMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DrinkService {

    @Autowired
    private DrinkMapper drinkMapper;

    public List<Drink> list() {
        return drinkMapper.selectList(null);
    }

    public Drink getById(Long id) {
        return drinkMapper.selectById(id);
    }

    public List<Drink> listByCategoryId(Long categoryId) {
        LambdaQueryWrapper<Drink> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Drink::getCategoryId, categoryId);
        return drinkMapper.selectList(wrapper);
    }

    public void add(Drink drink) {
        drinkMapper.insert(drink);
    }

    public void update(Drink drink) {
        drinkMapper.updateById(drink);
    }

    public void delete(Long id) {
        drinkMapper.deleteById(id);
    }

    public void updateStatus(Long id, Integer status) {
        Drink drink = new Drink();
        drink.setId(id);
        drink.setStatus(status);
        drinkMapper.updateById(drink);
    }
}
