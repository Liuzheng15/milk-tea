package com.example.milktea.controller;

import com.example.milktea.common.Result;
import com.example.milktea.entity.Member;
import com.example.milktea.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @GetMapping
    public Result<List<Member>> list() {
        return Result.success(memberService.list());
    }

    @GetMapping("/{id}")
    public Result<Member> getById(@PathVariable Long id) {
        return Result.success(memberService.getById(id));
    }

    @PostMapping
    public Result<Void> register(@RequestBody Member member) {
        memberService.register(member);
        return Result.success();
    }

    @PostMapping("/{id}/points/redeem")
    public Result<Void> redeemPoints(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        int points = body.getOrDefault("points", 0);
        String remark = body.get("remark") != null ? String.valueOf(body.get("remark")) : "积分兑换";
        memberService.redeemPoints(id, points, remark);
        return Result.success();
    }
}
