#!/usr/bin/env python3
"""Read-only preview: disId=160 route dispatch data."""
import re
import sys
from pathlib import Path

try:
    import pymysql
except ImportError:
    print("pymysql not installed", file=sys.stderr)
    sys.exit(1)

DIS_ID = 160
PROPS = Path(__file__).resolve().parents[1] / "src/main/resources/db.properties"


def load_props(path):
    cfg = {}
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        k, v = line.split("=", 1)
        cfg[k.strip()] = v.strip()
    url = cfg["jdbc.url"]
    m = re.search(r"jdbc:mysql://([^:/]+):(\d+)/([^?]+)", url)
    if not m:
        raise SystemExit("Cannot parse jdbc.url")
    return {
        "host": m.group(1),
        "port": int(m.group(2)),
        "database": m.group(3),
        "user": cfg["jdbc.username"],
        "password": cfg["jdbc.password"],
    }


def run(cur, title, sql):
    print(f"\n=== {title} ===")
    cur.execute(sql)
    rows = cur.fetchall()
    cols = [d[0] for d in cur.description]
    print("\t".join(cols))
    for row in rows:
        print("\t".join("" if v is None else str(v) for v in row))
    print(f"({len(rows)} rows)")


def main():
    db = load_props(PROPS)
    conn = pymysql.connect(
        host=db["host"],
        port=db["port"],
        user=db["user"],
        password=db["password"],
        database=db["database"],
        charset="utf8mb4",
    )
    cur = conn.cursor()
    run(cur, "distributer", f"""
        SELECT nx_distributer_id, nx_distributer_name
        FROM nx_distributer WHERE nx_distributer_id = {DIS_ID}
    """)
    run(cur, "table counts", f"""
        SELECT 'nx_dis_route_plan' AS tbl, COUNT(*) AS cnt
        FROM nx_dis_route_plan WHERE nx_drp_distributer_id = {DIS_ID}
        UNION ALL
        SELECT 'nx_dis_driver_route', COUNT(*)
        FROM nx_dis_driver_route dr
                 JOIN nx_dis_route_plan p ON p.nx_drp_id = dr.nx_ddr_plan_id
        WHERE p.nx_drp_distributer_id = {DIS_ID}
        UNION ALL
        SELECT 'nx_dis_shipment_task', COUNT(*)
        FROM nx_dis_shipment_task WHERE nx_dst_distributer_id = {DIS_ID}
        UNION ALL
        SELECT 'nx_dis_shipment_task_item', COUNT(*)
        FROM nx_dis_shipment_task_item ti
                 JOIN nx_dis_shipment_task t ON t.nx_dst_id = ti.nx_dsti_task_id
        WHERE t.nx_dst_distributer_id = {DIS_ID}
        UNION ALL
        SELECT 'nx_dis_route_stop', COUNT(*)
        FROM nx_dis_route_stop s
                 LEFT JOIN nx_dis_driver_route dr ON dr.nx_ddr_id = s.nx_drs_driver_route_id
                 LEFT JOIN nx_dis_route_plan p ON p.nx_drp_id = dr.nx_ddr_plan_id AND p.nx_drp_distributer_id = {DIS_ID}
                 LEFT JOIN nx_dis_shipment_task t ON t.nx_dst_id = s.nx_drs_shipment_task_id AND t.nx_dst_distributer_id = {DIS_ID}
        WHERE p.nx_drp_id IS NOT NULL OR t.nx_dst_id IS NOT NULL
        UNION ALL
        SELECT 'nx_dis_driver_duty', COUNT(*)
        FROM nx_dis_driver_duty WHERE nx_ddd_distributer_id = {DIS_ID}
    """)
    run(cur, "route plans", f"""
        SELECT nx_drp_id, nx_drp_route_date, nx_drp_dispatch_batch, nx_drp_status,
               nx_drp_driver_count, nx_drp_created_at
        FROM nx_dis_route_plan
        WHERE nx_drp_distributer_id = {DIS_ID}
        ORDER BY nx_drp_id DESC
        LIMIT 20
    """)
    run(cur, "driver routes", f"""
        SELECT dr.nx_ddr_id, dr.nx_ddr_plan_id, dr.nx_ddr_driver_user_id,
               du.nx_DIU_wx_nick_name AS driver_name,
               dr.nx_ddr_route_status, dr.nx_ddr_loading_entered_at,
               dr.nx_ddr_stop_count
        FROM nx_dis_driver_route dr
                 JOIN nx_dis_route_plan p ON p.nx_drp_id = dr.nx_ddr_plan_id
                 LEFT JOIN nx_distributer_user du ON du.nx_distributer_user_id = dr.nx_ddr_driver_user_id
        WHERE p.nx_drp_distributer_id = {DIS_ID}
        ORDER BY dr.nx_ddr_id DESC
        LIMIT 30
    """)
    run(cur, "shipment tasks", f"""
        SELECT t.nx_dst_id, t.nx_dst_route_date, t.nx_dst_dep_name, t.nx_dst_status,
               t.nx_dst_plan_id, t.nx_dst_driver_route_id,
               t.nx_dst_assigned_driver_user_id, t.nx_dst_order_count
        FROM nx_dis_shipment_task t
        WHERE t.nx_dst_distributer_id = {DIS_ID}
        ORDER BY t.nx_dst_id DESC
        LIMIT 50
    """)
    run(cur, "task items (live orders)", f"""
        SELECT ti.nx_dsti_id, ti.nx_dsti_task_id, ti.nx_dsti_live_order_id,
               ti.nx_dsti_item_status, t.nx_dst_dep_name, t.nx_dst_status
        FROM nx_dis_shipment_task_item ti
                 JOIN nx_dis_shipment_task t ON t.nx_dst_id = ti.nx_dsti_task_id
        WHERE t.nx_dst_distributer_id = {DIS_ID}
        ORDER BY ti.nx_dsti_id DESC
        LIMIT 50
    """)
    conn.close()


if __name__ == "__main__":
    main()
