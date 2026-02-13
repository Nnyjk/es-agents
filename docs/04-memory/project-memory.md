# 项目记忆（Project Memory）

## 长期有效口径
- 平台定位：目标驱动的自动化运维与部署平台（Environment -> Host -> Agent）。
- 部署模型：Host Agent（Runner）驻留在主机侧，Agent 以插件/任务形态被调度。
- 通信边界：Proxy/Gateway 为第三方组件，本仓库不实现其内部逻辑。

## 当前稳定决策
1. 文档主干分为 6 个域：overview / requirements / governance / api / memory / skills。
2. 需求以 `easy-station-requirements.md` 为主线，历史文档作为补充材料。
3. 规则文档与记忆文档独立，避免将“要求”与“事实”混写。

## 未决问题（需后续迭代）
- 是否将历史需求文档合并为单一版本并归档旧版。
- 是否增加自动化文档校验（链接检查、章节结构检查）。
- 是否建立跨端状态机单一事实源（server 与 agent 的状态定义一致性）。

## 变更记录
- 2026-02：完成文档结构重整，新增总览、规则、记忆、skill 指南文档。
