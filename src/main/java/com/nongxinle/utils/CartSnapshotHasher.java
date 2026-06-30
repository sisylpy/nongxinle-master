package com.nongxinle.utils;

import com.nongxinle.dto.coupon.CartLineSnapshot;
import com.nongxinle.dto.coupon.CartSnapshot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * CartSnapshot 指纹：communityId + userId + 商品行，供 CouponEngine 结果复用。
 */
public final class CartSnapshotHasher {

    private CartSnapshotHasher() {
    }

    public static String hash(CartSnapshot snapshot) {
        if (snapshot == null) {
            return md5("empty");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(snapshot.getCommunityId() == null ? 0 : snapshot.getCommunityId()).append('|');
        sb.append(snapshot.getUserId() == null ? 0 : snapshot.getUserId()).append('|');
        List<CartLineSnapshot> lines = snapshot.getLines() == null
                ? new ArrayList<>() : new ArrayList<>(snapshot.getLines());
        lines.sort(Comparator
                .comparing((CartLineSnapshot l) -> l.getGoodsId() == null ? 0 : l.getGoodsId())
                .thenComparing(l -> l.getQuantity() == null ? 0 : l.getQuantity())
                .thenComparing(l -> lineSubtotal(l)));
        for (CartLineSnapshot line : lines) {
            sb.append(line.getGoodsId() == null ? 0 : line.getGoodsId()).append(':');
            sb.append(line.getQuantity() == null ? 0 : line.getQuantity()).append(':');
            sb.append(lineSubtotal(line)).append(';');
        }
        return md5(sb.toString());
    }

    private static String lineSubtotal(CartLineSnapshot line) {
        if (line.getSubtotal() == null) {
            return "0.00";
        }
        return line.getSubtotal().setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static String md5(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 not available", e);
        }
    }
}
