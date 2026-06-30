package com.nongxinle.service.impl;

import com.nongxinle.config.DisRouteDispatchSettings;
import com.nongxinle.dao.NxDisShipmentTaskDao;
import com.nongxinle.dao.NxDistributerUserDao;
import com.nongxinle.dto.route.DeliveryHistoryPreferenceAggRow;
import com.nongxinle.dto.route.DeliveryHistoryPreferenceBatchResult;
import com.nongxinle.dto.route.DeliveryHistoryPreferenceCandidateDto;
import com.nongxinle.dto.route.DeliveryHistoryPreferenceDto;
import com.nongxinle.dto.route.DeliveryHistoryPreferenceResolveRequest;
import com.nongxinle.entity.NxDistributerUserEntity;
import com.nongxinle.route.DisRouteDeliveryHistoryReason;
import com.nongxinle.route.RouteDispatchDateFormat;
import com.nongxinle.service.DisRouteDeliveryHistoryPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class DisRouteDeliveryHistoryPreferenceServiceImpl implements DisRouteDeliveryHistoryPreferenceService {

    private static final BigDecimal CONFIDENCE_INSUFFICIENT_CAP = new BigDecimal("0.35");

    @Autowired
    private NxDisShipmentTaskDao nxDisShipmentTaskDao;
    @Autowired
    private NxDistributerUserDao nxDistributerUserDao;
    @Autowired
    private DisRouteDispatchSettings disRouteDispatchSettings;

    @Override
    public DeliveryHistoryPreferenceBatchResult resolve(DeliveryHistoryPreferenceResolveRequest request) {
        DeliveryHistoryPreferenceBatchResult batch = new DeliveryHistoryPreferenceBatchResult();
        if (request == null || request.getDisId() == null) {
            batch.setResolvedAt(RouteDispatchDateFormat.format(new Date()));
            return batch;
        }

        int lookbackDays = request.getLookbackDays() != null
                ? request.getLookbackDays()
                : disRouteDispatchSettings.getHistoryLookbackDays();
        int minDeliveredTimes = request.getMinDeliveredTimes() != null
                ? request.getMinDeliveredTimes()
                : disRouteDispatchSettings.getHistoryMinDeliveredTimes();
        double manualLockedWeight = disRouteDispatchSettings.getHistoryManualLockedWeight();

        batch.setDisId(request.getDisId());
        batch.setLookbackDays(lookbackDays);
        batch.setResolvedAt(RouteDispatchDateFormat.format(new Date()));

        List<Integer> depFatherIds = request.getDepFatherIds() != null
                ? request.getDepFatherIds()
                : Collections.<Integer>emptyList();
        Set<Integer> eligibleDriverIds = new HashSet<Integer>();
        if (request.getEligibleDriverUserIds() != null) {
            eligibleDriverIds.addAll(request.getEligibleDriverUserIds());
        }

        if (depFatherIds.isEmpty()) {
            return batch;
        }

        boolean noEligibleDrivers = eligibleDriverIds.isEmpty();
        List<DeliveryHistoryPreferenceAggRow> aggRows = nxDisShipmentTaskDao.queryDeliveryHistoryAggByDepAndDriver(
                request.getDisId(), depFatherIds, lookbackDays, manualLockedWeight);
        if (aggRows == null) {
            aggRows = Collections.emptyList();
        }

        Map<Integer, List<DeliveryHistoryPreferenceAggRow>> rowsByDep = groupByDep(aggRows);
        Map<Integer, String> driverNameById = loadDriverNames(request.getDisId(), aggRows, eligibleDriverIds);

        for (Integer depFatherId : depFatherIds) {
            List<DeliveryHistoryPreferenceAggRow> depRows = rowsByDep.get(depFatherId);
            if (depRows == null) {
                depRows = Collections.emptyList();
            }
            batch.getPreferencesByDepFatherId().put(
                    depFatherId,
                    buildPreferenceForDep(depFatherId, depRows, eligibleDriverIds, noEligibleDrivers,
                            minDeliveredTimes, driverNameById));
        }
        return batch;
    }

    private static Map<Integer, List<DeliveryHistoryPreferenceAggRow>> groupByDep(
            List<DeliveryHistoryPreferenceAggRow> aggRows) {
        Map<Integer, List<DeliveryHistoryPreferenceAggRow>> grouped =
                new LinkedHashMap<Integer, List<DeliveryHistoryPreferenceAggRow>>();
        for (DeliveryHistoryPreferenceAggRow row : aggRows) {
            if (row == null || row.getDepFatherId() == null) {
                continue;
            }
            if (!grouped.containsKey(row.getDepFatherId())) {
                grouped.put(row.getDepFatherId(), new ArrayList<DeliveryHistoryPreferenceAggRow>());
            }
            grouped.get(row.getDepFatherId()).add(row);
        }
        return grouped;
    }

    private Map<Integer, String> loadDriverNames(Integer disId,
                                                 List<DeliveryHistoryPreferenceAggRow> aggRows,
                                                 Set<Integer> eligibleDriverIds) {
        Set<Integer> driverIds = new HashSet<Integer>(eligibleDriverIds);
        for (DeliveryHistoryPreferenceAggRow row : aggRows) {
            if (row != null && row.getDriverUserId() != null) {
                driverIds.add(row.getDriverUserId());
            }
        }
        Map<Integer, String> names = new HashMap<Integer, String>();
        if (driverIds.isEmpty()) {
            return names;
        }
        List<NxDistributerUserEntity> users = nxDistributerUserDao.queryAllUsersByDisId(disId);
        if (users == null) {
            return names;
        }
        for (NxDistributerUserEntity user : users) {
            if (user == null || user.getNxDistributerUserId() == null) {
                continue;
            }
            if (driverIds.contains(user.getNxDistributerUserId())) {
                names.put(user.getNxDistributerUserId(), user.getNxDiuWxNickName());
            }
        }
        return names;
    }

    private DeliveryHistoryPreferenceDto buildPreferenceForDep(Integer depFatherId,
                                                                List<DeliveryHistoryPreferenceAggRow> depRows,
                                                                Set<Integer> eligibleDriverIds,
                                                                boolean noEligibleDrivers,
                                                                int minDeliveredTimes,
                                                                Map<Integer, String> driverNameById) {
        DeliveryHistoryPreferenceDto dto = new DeliveryHistoryPreferenceDto();
        dto.setDepFatherId(depFatherId);
        zeroPreferredCounters(dto);

        int totalDelivered = 0;
        for (DeliveryHistoryPreferenceAggRow row : depRows) {
            if (row.getDeliveredTimes() != null) {
                totalDelivered += row.getDeliveredTimes();
            }
        }
        dto.setTotalDeliveredTimesAllDrivers(totalDelivered);

        if (depRows.isEmpty()) {
            dto.setReason(DisRouteDeliveryHistoryReason.NO_HISTORY);
            return dto;
        }

        SelectionResult historicalTop = selectBestCandidate(depRows, null);
        fillHistoricalTop(dto, historicalTop, driverNameById);

        if (noEligibleDrivers) {
            dto.setReason(DisRouteDeliveryHistoryReason.NO_ELIGIBLE_DRIVER);
            return dto;
        }

        List<DeliveryHistoryPreferenceAggRow> eligibleRows = filterEligibleRows(depRows, eligibleDriverIds);
        for (DeliveryHistoryPreferenceAggRow row : eligibleRows) {
            dto.getCandidateDrivers().add(toCandidateDto(row, driverNameById));
        }

        if (eligibleRows.isEmpty()) {
            dto.setReason(DisRouteDeliveryHistoryReason.PREFERRED_DRIVER_NOT_ELIGIBLE);
            dto.setHistoricalTopNotEligibleReason("DRIVER_NOT_IN_ELIGIBLE_POOL");
            return dto;
        }

        SelectionResult preferred = selectBestCandidate(eligibleRows, eligibleDriverIds);
        if (preferred.row == null) {
            dto.setReason(DisRouteDeliveryHistoryReason.NO_HISTORY);
            return dto;
        }

        fillPreferred(dto, preferred, driverNameById);

        if (preferred.row.getDeliveredTimes() == null
                || preferred.row.getDeliveredTimes() < minDeliveredTimes) {
            dto.setReason(DisRouteDeliveryHistoryReason.INSUFFICIENT_HISTORY);
            dto.setConfidence(capConfidence(computeConfidence(preferred.row, eligibleRows), CONFIDENCE_INSUFFICIENT_CAP));
            return dto;
        }

        if (preferred.multipleEqualCandidates) {
            dto.setReason(DisRouteDeliveryHistoryReason.MULTIPLE_EQUAL_CANDIDATES);
        } else if (preferred.tieBrokenByRecency) {
            dto.setReason(DisRouteDeliveryHistoryReason.HISTORY_TIE_BROKEN_BY_RECENCY);
        } else {
            dto.setReason(DisRouteDeliveryHistoryReason.HISTORY_DOMINANT_DRIVER);
        }
        dto.setConfidence(computeConfidence(preferred.row, eligibleRows));
        return dto;
    }

    private static void zeroPreferredCounters(DeliveryHistoryPreferenceDto dto) {
        dto.setDeliveredTimes(0);
        dto.setManualLockedTimes(0);
        dto.setTotalDeliveredTimesAllDrivers(0);
        dto.setConfidence(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }

    private static List<DeliveryHistoryPreferenceAggRow> filterEligibleRows(
            List<DeliveryHistoryPreferenceAggRow> depRows,
            Set<Integer> eligibleDriverIds) {
        List<DeliveryHistoryPreferenceAggRow> eligible = new ArrayList<DeliveryHistoryPreferenceAggRow>();
        for (DeliveryHistoryPreferenceAggRow row : depRows) {
            if (row.getDriverUserId() != null && eligibleDriverIds.contains(row.getDriverUserId())) {
                eligible.add(row);
            }
        }
        return eligible;
    }

    private static void fillHistoricalTop(DeliveryHistoryPreferenceDto dto,
                                          SelectionResult historicalTop,
                                          Map<Integer, String> driverNameById) {
        if (historicalTop.row == null) {
            return;
        }
        dto.setHistoricalTopDriverUserId(historicalTop.row.getDriverUserId());
        dto.setHistoricalTopDriverName(driverNameById.get(historicalTop.row.getDriverUserId()));
        dto.setHistoricalTopDeliveredTimes(historicalTop.row.getDeliveredTimes());
        dto.setHistoricalTopRecentDeliveredAt(historicalTop.row.getRecentDeliveredAt());
        dto.setHistoricalTopAvgStopSeq(scaleStopSeq(historicalTop.row.getWeightedAvgStopSeq()));
        dto.setHistoricalTopManualLockedTimes(historicalTop.row.getManualLockedTimes());
    }

    private static void fillPreferred(DeliveryHistoryPreferenceDto dto,
                                       SelectionResult preferred,
                                       Map<Integer, String> driverNameById) {
        dto.setPreferredDriverUserId(preferred.row.getDriverUserId());
        dto.setPreferredDriverName(driverNameById.get(preferred.row.getDriverUserId()));
        dto.setDeliveredTimes(preferred.row.getDeliveredTimes());
        dto.setRecentDeliveredAt(preferred.row.getRecentDeliveredAt());
        dto.setAvgStopSeq(scaleStopSeq(preferred.row.getWeightedAvgStopSeq()));
        dto.setManualLockedTimes(preferred.row.getManualLockedTimes());
    }

    private static DeliveryHistoryPreferenceCandidateDto toCandidateDto(
            DeliveryHistoryPreferenceAggRow row,
            Map<Integer, String> driverNameById) {
        DeliveryHistoryPreferenceCandidateDto candidate = new DeliveryHistoryPreferenceCandidateDto();
        candidate.setDriverUserId(row.getDriverUserId());
        candidate.setDriverName(driverNameById.get(row.getDriverUserId()));
        candidate.setDeliveredTimes(row.getDeliveredTimes());
        candidate.setRecentDeliveredAt(row.getRecentDeliveredAt());
        candidate.setManualLockedTimes(row.getManualLockedTimes());
        candidate.setWeightedDeliveredScore(scaleScore(row.getWeightedDeliveredScore()));
        candidate.setWeightedAvgStopSeq(scaleStopSeq(row.getWeightedAvgStopSeq()));
        return candidate;
    }

    private static SelectionResult selectBestCandidate(List<DeliveryHistoryPreferenceAggRow> rows,
                                                       Set<Integer> eligibleFilter) {
        List<DeliveryHistoryPreferenceAggRow> candidates = new ArrayList<DeliveryHistoryPreferenceAggRow>();
        for (DeliveryHistoryPreferenceAggRow row : rows) {
            if (eligibleFilter != null
                    && (row.getDriverUserId() == null || !eligibleFilter.contains(row.getDriverUserId()))) {
                continue;
            }
            candidates.add(row);
        }
        if (candidates.isEmpty()) {
            return new SelectionResult(null, false, false);
        }

        Collections.sort(candidates, CANDIDATE_COMPARATOR);
        DeliveryHistoryPreferenceAggRow best = candidates.get(0);
        boolean multipleEqual = false;
        boolean tieBrokenByRecency = false;
        if (candidates.size() > 1) {
            DeliveryHistoryPreferenceAggRow second = candidates.get(1);
            if (sameWeightedScore(best, second)) {
                multipleEqual = true;
                if (!sameRecentDeliveredAt(best, second)) {
                    tieBrokenByRecency = true;
                    multipleEqual = false;
                }
            } else if (sameWeightedScore(best, second)
                    && sameDeliveredTimes(best, second)
                    && !sameRecentDeliveredAt(best, second)) {
                tieBrokenByRecency = true;
            }
        }
        return new SelectionResult(best, tieBrokenByRecency, multipleEqual);
    }

    private static boolean sameWeightedScore(DeliveryHistoryPreferenceAggRow a, DeliveryHistoryPreferenceAggRow b) {
        return normalizeScore(a).compareTo(normalizeScore(b)) == 0;
    }

    private static boolean sameDeliveredTimes(DeliveryHistoryPreferenceAggRow a, DeliveryHistoryPreferenceAggRow b) {
        int left = a.getDeliveredTimes() != null ? a.getDeliveredTimes() : 0;
        int right = b.getDeliveredTimes() != null ? b.getDeliveredTimes() : 0;
        return left == right;
    }

    private static boolean sameRecentDeliveredAt(DeliveryHistoryPreferenceAggRow a,
                                                 DeliveryHistoryPreferenceAggRow b) {
        if (a.getRecentDeliveredAt() == null && b.getRecentDeliveredAt() == null) {
            return true;
        }
        if (a.getRecentDeliveredAt() == null || b.getRecentDeliveredAt() == null) {
            return false;
        }
        return a.getRecentDeliveredAt().getTime() == b.getRecentDeliveredAt().getTime();
    }

    private static BigDecimal normalizeScore(DeliveryHistoryPreferenceAggRow row) {
        return row.getWeightedDeliveredScore() != null ? row.getWeightedDeliveredScore() : BigDecimal.ZERO;
    }

    private static final Comparator<DeliveryHistoryPreferenceAggRow> CANDIDATE_COMPARATOR =
            new Comparator<DeliveryHistoryPreferenceAggRow>() {
                @Override
                public int compare(DeliveryHistoryPreferenceAggRow a, DeliveryHistoryPreferenceAggRow b) {
                    int byScore = normalizeScore(b).compareTo(normalizeScore(a));
                    if (byScore != 0) {
                        return byScore;
                    }
                    Date recentA = a.getRecentDeliveredAt();
                    Date recentB = b.getRecentDeliveredAt();
                    if (recentA != null && recentB != null) {
                        int byRecent = recentB.compareTo(recentA);
                        if (byRecent != 0) {
                            return byRecent;
                        }
                    } else if (recentA != null) {
                        return -1;
                    } else if (recentB != null) {
                        return 1;
                    }
                    int timesA = a.getDeliveredTimes() != null ? a.getDeliveredTimes() : 0;
                    int timesB = b.getDeliveredTimes() != null ? b.getDeliveredTimes() : 0;
                    int byTimes = Integer.compare(timesB, timesA);
                    if (byTimes != 0) {
                        return byTimes;
                    }
                    int idA = a.getDriverUserId() != null ? a.getDriverUserId() : Integer.MAX_VALUE;
                    int idB = b.getDriverUserId() != null ? b.getDriverUserId() : Integer.MAX_VALUE;
                    return Integer.compare(idA, idB);
                }
            };

    private static BigDecimal computeConfidence(DeliveryHistoryPreferenceAggRow preferred,
                                                List<DeliveryHistoryPreferenceAggRow> eligibleRows) {
        BigDecimal preferredWeight = normalizeScore(preferred);
        BigDecimal totalWeight = BigDecimal.ZERO;
        for (DeliveryHistoryPreferenceAggRow row : eligibleRows) {
            totalWeight = totalWeight.add(normalizeScore(row));
        }
        BigDecimal share = BigDecimal.ZERO;
        if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
            share = preferredWeight.divide(totalWeight, 4, RoundingMode.HALF_UP);
        }

        int deliveredTimes = preferred.getDeliveredTimes() != null ? preferred.getDeliveredTimes() : 0;
        BigDecimal freq = BigDecimal.valueOf(Math.min(1.0d, deliveredTimes / 3.0d));

        int manualLocked = preferred.getManualLockedTimes() != null ? preferred.getManualLockedTimes() : 0;
        BigDecimal manualBoost = BigDecimal.valueOf(Math.min(0.2d, manualLocked * 0.1d));

        BigDecimal recencyBoost = resolveRecencyBoost(preferred.getRecentDeliveredAt());

        BigDecimal raw = share.multiply(new BigDecimal("0.55"))
                .add(freq.multiply(new BigDecimal("0.30")))
                .add(recencyBoost.multiply(new BigDecimal("0.15")))
                .add(manualBoost);
        return clampConfidence(raw);
    }

    private static BigDecimal resolveRecencyBoost(Date recentDeliveredAt) {
        if (recentDeliveredAt == null) {
            return BigDecimal.ZERO;
        }
        long days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - recentDeliveredAt.getTime());
        if (days < 0) {
            days = 0;
        }
        if (days <= 7) {
            return BigDecimal.ONE;
        }
        if (days <= 30) {
            return new BigDecimal("0.7");
        }
        if (days <= 90) {
            return new BigDecimal("0.4");
        }
        return new BigDecimal("0.2");
    }

    private static BigDecimal clampConfidence(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal clamped = value;
        if (clamped.compareTo(BigDecimal.ZERO) < 0) {
            clamped = BigDecimal.ZERO;
        }
        if (clamped.compareTo(BigDecimal.ONE) > 0) {
            clamped = BigDecimal.ONE;
        }
        return clamped.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal capConfidence(BigDecimal value, BigDecimal cap) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.min(cap).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal scaleStopSeq(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal scaleScore(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static final class SelectionResult {
        private final DeliveryHistoryPreferenceAggRow row;
        private final boolean tieBrokenByRecency;
        private final boolean multipleEqualCandidates;

        private SelectionResult(DeliveryHistoryPreferenceAggRow row,
                                boolean tieBrokenByRecency,
                                boolean multipleEqualCandidates) {
            this.row = row;
            this.tieBrokenByRecency = tieBrokenByRecency;
            this.multipleEqualCandidates = multipleEqualCandidates;
        }
    }
}
