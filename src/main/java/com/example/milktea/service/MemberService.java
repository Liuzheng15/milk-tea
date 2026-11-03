package com.example.milktea.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.milktea.entity.Member;
import com.example.milktea.entity.PointsLog;
import com.example.milktea.mapper.MemberMapper;
import com.example.milktea.mapper.PointsLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class MemberService {

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private PointsLogMapper pointsLogMapper;

    public List<Member> list() {
        List<Member> members = memberMapper.selectList(null);
        for (Member m : members) {
            m.setLevel(calcLevel(m.getPoints()));
        }
        return members;
    }

    public Member getById(Long id) {
        Member member = memberMapper.selectById(id);
        if (member != null) {
            member.setLevel(calcLevel(member.getPoints()));
        }
        return member;
    }

    public Member getByPhone(String phone) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getPhone, phone);
        return memberMapper.selectOne(wrapper);
    }

    public void register(Member member) {
        member.setPoints(0);
        member.setLevel(1);
        memberMapper.insert(member);
    }

    /**
     * 增加积分（消费获得）— 按会员等级阶梯倍率
     * 普通(1): 1元=1积分, 银卡(2): 1元=1.2积分, 金卡(3): 1元=1.5积分
     */
    @Transactional
    public int addPoints(Long memberId, BigDecimal amount, Long orderId, String remark) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new RuntimeException("会员不存在");
        }
        int level = calcLevel(member.getPoints());
        BigDecimal rate = getPointsRate(level);
        int pointsEarned = amount.multiply(rate).setScale(0, RoundingMode.DOWN).intValue();

        int newPoints = member.getPoints() + pointsEarned;
        member.setPoints(newPoints);
        member.setLevel(calcLevel(newPoints));
        memberMapper.updateById(member);

        PointsLog log = new PointsLog();
        log.setMemberId(memberId);
        log.setPoints(pointsEarned);
        log.setType(1);
        log.setOrderId(orderId);
        log.setRemark(remark + "(倍率" + rate.toPlainString() + "x)");
        pointsLogMapper.insert(log);

        return pointsEarned;
    }

    /**
     * 扣减积分（兑换消耗）
     */
    @Transactional
    public void redeemPoints(Long memberId, int points, String remark) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new RuntimeException("会员不存在");
        }
        if (member.getPoints() < points) {
            throw new RuntimeException("积分不足");
        }
        int newPoints = member.getPoints() - points;
        member.setPoints(newPoints);
        member.setLevel(calcLevel(newPoints));
        memberMapper.updateById(member);

        PointsLog log = new PointsLog();
        log.setMemberId(memberId);
        log.setPoints(-points);
        log.setType(2);
        log.setRemark(remark);
        pointsLogMapper.insert(log);
    }

    /**
     * 退款扣回积分
     */
    @Transactional
    public void deductPointsForRefund(Long memberId, int points, Long orderId, String remark) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            return;
        }
        int actualDeduct = Math.min(points, member.getPoints());
        int newPoints = member.getPoints() - actualDeduct;
        member.setPoints(newPoints);
        member.setLevel(calcLevel(newPoints));
        memberMapper.updateById(member);

        PointsLog log = new PointsLog();
        log.setMemberId(memberId);
        log.setPoints(-actualDeduct);
        log.setType(3);
        log.setOrderId(orderId);
        log.setRemark(remark);
        pointsLogMapper.insert(log);
    }

    /**
     * 根据积分计算等级
     * <500 普通(1), 500-1999 银卡(2), >=2000 金卡(3)
     */
    private int calcLevel(int points) {
        if (points >= 2000) return 3;
        if (points >= 500) return 2;
        return 1;
    }

    /**
     * 根据等级获取积分倍率
     */
    private BigDecimal getPointsRate(int level) {
        switch (level) {
            case 3: return new BigDecimal("1.5");
            case 2: return new BigDecimal("1.2");
            default: return BigDecimal.ONE;
        }
    }
}
