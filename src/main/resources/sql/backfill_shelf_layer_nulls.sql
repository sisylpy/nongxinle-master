-- 回填 nx_DGSG_shelf_layer / nx_DGSG_shelf_layer_last（适合旧数据：同货架仅部分行有层号，中间行为 NULL）
--
-- 逻辑（与你的描述一致）：
--  1）按 nx_DGSG_shelf_id 分组，把「当前为 NULL」的 nx_DGSG_shelf_layer 用同货架、按排序方向上最近一条「非 NULL」的层号补上（先向前找，没有则向后找；仍没有则置 1，表示整架从未标过层）。
--  2）nx_DGSG_shelf_layer_last：回填开始前曾是非 NULL 的 nx_DGSG_shelf_layer 的行（旧接口里的层尾标记行）置 1，其余置 0。
--  3）若某货架上没有任何一条曾带层号（临时表里无该 shelf），则该货架按排序最后一行层尾=1，其余=0（整架一层时的默认）。
--
-- 注意：若你已经执行过「把所有 NULL 层号都改成 1」且没有备份，旧版「哪一行是层尾」已无法从数据区分，本脚本无法还原多层分界；只能从业务重设层。
--
-- 要求：MySQL 8.0+（步骤 ③ 使用窗口函数）。执行前请已存在列 nx_DGSG_shelf_layer、nx_DGSG_shelf_layer_last。

DROP TEMPORARY TABLE IF EXISTS tmp_shelf_layer_marker;
CREATE TEMPORARY TABLE tmp_shelf_layer_marker (
    nx_distributer_goods_shelf_goods_id INT PRIMARY KEY,
    nx_DGSG_shelf_id INT NOT NULL
);

INSERT INTO tmp_shelf_layer_marker (nx_distributer_goods_shelf_goods_id, nx_DGSG_shelf_id)
SELECT nx_distributer_goods_shelf_goods_id, nx_DGSG_shelf_id
FROM nx_distributer_goods_shelf_goods
WHERE nx_DGSG_shelf_layer IS NOT NULL;

-- ① 仅更新层号为 NULL 的行：用同 shelf 上「更近」的非空层号填充（排序：nx_DGSG_sort、主键）
UPDATE nx_distributer_goods_shelf_goods AS g
INNER JOIN (
    SELECT
        o.nx_distributer_goods_shelf_goods_id,
        COALESCE(
            o.nx_DGSG_shelf_layer,
            (SELECT o2.nx_DGSG_shelf_layer
             FROM nx_distributer_goods_shelf_goods AS o2
             WHERE o2.nx_DGSG_shelf_id = o.nx_DGSG_shelf_id
               AND o2.nx_DGSG_shelf_layer IS NOT NULL
               AND (
                   o2.nx_DGSG_sort < o.nx_DGSG_sort
                   OR (o2.nx_DGSG_sort <=> o.nx_DGSG_sort
                       AND o2.nx_distributer_goods_shelf_goods_id < o.nx_distributer_goods_shelf_goods_id)
               )
             ORDER BY o2.nx_DGSG_sort DESC, o2.nx_distributer_goods_shelf_goods_id DESC
             LIMIT 1),
            (SELECT o3.nx_DGSG_shelf_layer
             FROM nx_distributer_goods_shelf_goods AS o3
             WHERE o3.nx_DGSG_shelf_id = o.nx_DGSG_shelf_id
               AND o3.nx_DGSG_shelf_layer IS NOT NULL
               AND (
                   o3.nx_DGSG_sort > o.nx_DGSG_sort
                   OR (o3.nx_DGSG_sort <=> o.nx_DGSG_sort
                       AND o3.nx_distributer_goods_shelf_goods_id > o.nx_distributer_goods_shelf_goods_id)
               )
             ORDER BY o3.nx_DGSG_sort ASC, o3.nx_distributer_goods_shelf_goods_id ASC
             LIMIT 1),
            1
        ) AS v_layer
    FROM nx_distributer_goods_shelf_goods AS o
) AS x ON g.nx_distributer_goods_shelf_goods_id = x.nx_distributer_goods_shelf_goods_id
SET g.nx_DGSG_shelf_layer = x.v_layer
WHERE g.nx_DGSG_shelf_layer IS NULL;

-- ② 层尾先清零
UPDATE nx_distributer_goods_shelf_goods
SET nx_DGSG_shelf_layer_last = 0;

-- ③ 旧数据里「曾带层号」的行 = 层尾 1（与当时只标在层尾上的约定一致）
UPDATE nx_distributer_goods_shelf_goods AS g
INNER JOIN tmp_shelf_layer_marker AS m
    ON g.nx_distributer_goods_shelf_goods_id = m.nx_distributer_goods_shelf_goods_id
SET g.nx_DGSG_shelf_layer_last = 1;

-- ④ 从未有过层号标记的货架：只有排序最后一行层尾 1（② 已把其余置 0）
UPDATE nx_distributer_goods_shelf_goods AS g
INNER JOIN (
    SELECT
        t.nx_distributer_goods_shelf_goods_id
    FROM (
        SELECT
            nx_distributer_goods_shelf_goods_id,
            nx_DGSG_shelf_id,
            ROW_NUMBER() OVER (
                PARTITION BY nx_DGSG_shelf_id
                ORDER BY nx_DGSG_sort DESC, nx_distributer_goods_shelf_goods_id DESC
            ) AS rn_last
        FROM nx_distributer_goods_shelf_goods
    ) AS t
    WHERE t.rn_last = 1
      AND t.nx_DGSG_shelf_id NOT IN (SELECT mm.nx_DGSG_shelf_id FROM tmp_shelf_layer_marker AS mm)
) AS z ON g.nx_distributer_goods_shelf_goods_id = z.nx_distributer_goods_shelf_goods_id
SET g.nx_DGSG_shelf_layer_last = 1;

DROP TEMPORARY TABLE IF EXISTS tmp_shelf_layer_marker;
