package com.example.milktea.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.milktea.entity.Inventory;
import com.example.milktea.mapper.InventoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class InventoryService {

    @Autowired
    private InventoryMapper inventoryMapper;

    public List<Inventory> list() {
        return inventoryMapper.selectList(null);
    }

    public Inventory getById(Long id) {
        return inventoryMapper.selectById(id);
    }

    public void add(Inventory inventory) {
        inventoryMapper.insert(inventory);
    }

    public void update(Inventory inventory) {
        inventoryMapper.updateById(inventory);
    }

    public void delete(Long id) {
        inventoryMapper.deleteById(id);
    }

    /**
     * 入库
     */
    @Transactional
    public void stockIn(Long id, BigDecimal quantity) {
        Inventory inv = inventoryMapper.selectById(id);
        if (inv == null) {
            throw new RuntimeException("库存记录不存在");
        }
        inv.setQuantity(inv.getQuantity().add(quantity));
        inventoryMapper.updateById(inv);
    }

    /**
     * 出库
     */
    @Transactional
    public void stockOut(Long id, BigDecimal quantity) {
        Inventory inv = inventoryMapper.selectById(id);
        if (inv == null) {
            throw new RuntimeException("库存记录不存在");
        }
        if (inv.getQuantity().compareTo(quantity) < 0) {
            throw new RuntimeException("库存不足");
        }
        inv.setQuantity(inv.getQuantity().subtract(quantity));
        inventoryMapper.updateById(inv);
    }

    /**
     * 库存预警列表：quantity < warning_threshold
     */
    public List<Inventory> warningList() {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.apply("quantity < warning_threshold");
        return inventoryMapper.selectList(wrapper);
    }
}
