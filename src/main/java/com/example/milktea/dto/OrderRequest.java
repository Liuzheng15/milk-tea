package com.example.milktea.dto;

import java.util.List;

public class OrderRequest {
    private Long memberId;
    private Long couponId;
    private List<OrderItemDTO> items;

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public Long getCouponId() { return couponId; }
    public void setCouponId(Long couponId) { this.couponId = couponId; }
    public List<OrderItemDTO> getItems() { return items; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }
}
