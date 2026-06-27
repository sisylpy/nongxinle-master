package com.nongxinle.route;

/**
 * 今日派单沙盘：司机/route 在「新单分派」语境下的统一阶段。
 * <p>
 * 口径说明见 {@code docs/route-dispatch/Route-Dispatch-Sandbox-Driver-State.md}。
 */
public final class DisRouteSandboxDriverDispatchPhase {

    /** 无 LOADING/IN_DELIVERY active route；含上一趟 task 全 terminal 后的空闲可派。 */
    public static final String IDLE = "IDLE";

    /** 本趟已无在途站，但有新 confirmed/simulated 待装车站。 */
    public static final String REDISPATCH_PRE_DEPART = "REDISPATCH_PRE_DEPART";

    /** 已进入装车流程，未完成。 */
    public static final String LOADING = "LOADING";

    /** 仍有未完成 task（IN_DELIVERY / EXCEPTION 等），不可再接沙盘新单。 */
    public static final String ACTIVE_EXECUTION = "ACTIVE_EXECUTION";

    private DisRouteSandboxDriverDispatchPhase() {
    }

    public static boolean blocksSandboxComputeDispatch(String phase) {
        return LOADING.equals(phase) || ACTIVE_EXECUTION.equals(phase);
    }

    public static boolean blocksAvailableIdleSlot(String phase) {
        return blocksSandboxComputeDispatch(phase);
    }
}
