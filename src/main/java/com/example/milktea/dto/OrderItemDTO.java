package com.example.milktea.dto;

public class OrderItemDTO {
    private Long drinkId;
    private Integer quantity;

    public Long getDrinkId() { return drinkId; }
    public void setDrinkId(Long drinkId) { this.drinkId = drinkId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
