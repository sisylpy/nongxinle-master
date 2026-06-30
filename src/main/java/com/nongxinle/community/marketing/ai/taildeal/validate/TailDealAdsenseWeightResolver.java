package com.nongxinle.community.marketing.ai.taildeal.validate;

import com.nongxinle.community.marketing.ai.taildeal.contract.TailDealAdsenseSemanticContract;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TailDealAdsenseWeightResolver {

    private static final Pattern GROSS_WEIGHT = Pattern.compile("毛重\\s*([\\d.]+)\\s*斤");
    private static final Pattern NET_WEIGHT = Pattern.compile("净重\\s*([\\d.]+)\\s*斤");
    private static final Pattern GENERIC_JIN = Pattern.compile("([\\d.]+)\\s*斤");

    public void enrichContractFromRawText(TailDealAdsenseSemanticContract c, String rawText) {
        if (c == null) {
            return;
        }
        String combined = joinText(rawText, c.getGoodsSpec());
        if (combined.isEmpty()) {
            return;
        }
        if (c.getGrossWeight() == null) {
            c.setGrossWeight(extractLabeledWeight(combined, GROSS_WEIGHT));
        }
        if (c.getNetWeight() == null) {
            c.setNetWeight(extractLabeledWeight(combined, NET_WEIGHT));
        }
    }

    public WeightPack resolve(TailDealAdsenseSemanticContract c, String rawText) {
        WeightPack pack = new WeightPack();
        if (c == null || c.getDealPrice() == null || c.getDealPrice() <= 0) {
            return pack;
        }
        enrichContractFromRawText(c, rawText);

        Double gross = c.getGrossWeight();
        Double net = c.getNetWeight();
        if (gross == null && net == null) {
            Double generic = extractGenericWeight(rawText, c.getGoodsSpec());
            if (generic != null) {
                gross = generic;
            }
        }

        BigDecimal price = BigDecimal.valueOf(c.getDealPrice());
        if (gross != null && net != null) {
            pack.grossWeight = formatWeight(gross);
            pack.grossPrice = formatPrice(price, gross);
            pack.netWeight = formatWeight(net);
            pack.netPrice = formatPrice(price, net);
            pack.mode = "BOTH";
        } else if (net != null) {
            pack.netWeight = formatWeight(net);
            pack.netPrice = formatPrice(price, net);
            pack.mode = "NET_ONLY";
        } else if (gross != null) {
            pack.grossWeight = formatWeight(gross);
            pack.grossPrice = formatPrice(price, gross);
            pack.mode = "GROSS_ONLY";
        }
        return pack;
    }

    public void applyToMap(WeightPack pack, MapTarget target) {
        if (pack == null || !pack.hasAny()) {
            return;
        }
        if (pack.grossWeight != null) {
            target.put("nxCgGoodsGrossWeight", pack.grossWeight);
        }
        if (pack.grossPrice != null) {
            target.put("nxCgGoodsGrossPrice", pack.grossPrice);
        }
        if (pack.netWeight != null) {
            target.put("nxCgGoodsNetWeight", pack.netWeight);
        }
        if (pack.netPrice != null) {
            target.put("nxCgGoodsNetPrice", pack.netPrice);
        }
    }

    private Double extractLabeledWeight(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return parseDouble(matcher.group(1));
        }
        return null;
    }

    private Double extractGenericWeight(String rawText, String goodsSpec) {
        for (String src : new String[]{rawText, goodsSpec}) {
            if (src == null || src.trim().isEmpty()) {
                continue;
            }
            if (src.contains("毛重") || src.contains("净重")) {
                continue;
            }
            Matcher matcher = GENERIC_JIN.matcher(src);
            if (matcher.find()) {
                return parseDouble(matcher.group(1));
            }
        }
        return null;
    }

    private String joinText(String rawText, String goodsSpec) {
        StringBuilder sb = new StringBuilder();
        if (rawText != null && !rawText.trim().isEmpty()) {
            sb.append(rawText.trim());
        }
        if (goodsSpec != null && !goodsSpec.trim().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(goodsSpec.trim());
        }
        return sb.toString();
    }

    private Double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String formatWeight(double weight) {
        return BigDecimal.valueOf(weight).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    private String formatPrice(BigDecimal price, double weight) {
        if (weight <= 0) {
            return null;
        }
        return price.divide(BigDecimal.valueOf(weight), 2, RoundingMode.HALF_UP).toPlainString();
    }

    public interface MapTarget {
        void put(String key, String value);
    }

    @lombok.Getter
    public static class WeightPack {
        private String grossWeight;
        private String grossPrice;
        private String netWeight;
        private String netPrice;
        private String mode;

        public boolean hasAny() {
            return grossWeight != null || grossPrice != null || netWeight != null || netPrice != null;
        }
    }
}
