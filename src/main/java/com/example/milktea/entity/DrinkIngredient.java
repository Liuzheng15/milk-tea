package com.example.milktea.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;

@TableName("drink_ingredient")
public class DrinkIngredient {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long drinkId;
    private Long inventoryId;
    private BigDecimal quantity;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDrinkId() { return drinkId; }
    public void setDrinkId(Long drinkId) { this.drinkId = drinkId; }
    public Long getInventoryId() { return inventoryId; }
    public void setInventoryId(Long inventoryId) { this.inventoryId = inventoryId; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
}
