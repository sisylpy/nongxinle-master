-- Community 司机角色与值班：将 roleId=5 回填 nxCouAdmin=5（与 NX nxDiuAdmin=5 对齐）
UPDATE nx_community_user
SET nx_COU_admin = 5
WHERE nx_COU_role_id = 5
  AND (nx_COU_admin IS NULL OR nx_COU_admin <> 5);
