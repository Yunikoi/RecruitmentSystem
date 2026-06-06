package com.recruitment.config;

import com.recruitment.entity.PositionStatus;

import java.util.List;

/**
 * 演示用岗位种子数据（24 个），覆盖技术 / 产品 / 运营 / 市场 / 职能等方向。
 */
public final class DemoPositionSeeds {

    private DemoPositionSeeds() {
    }

    public record Seed(
            String title,
            String description,
            PositionStatus status,
            String department,
            String positionType,
            String skillTags
    ) {
    }

    public static List<Seed> all() {
        return List.of(
                seed("Java后端开发工程师",
                        "岗位职责：负责高并发后端服务设计与开发，参与微服务架构演进。\n任职要求：3年以上Java开发经验，熟悉Spring Boot、MySQL、Redis。\n加分项：有高并发/分布式系统经验、开源贡献。",
                        PositionStatus.PUBLISHED, "技术部", "TECH", "Java,Spring Boot,微服务,MySQL,Redis"),

                seed("高级Java开发工程师",
                        "岗位职责：主导核心业务模块开发，指导初级工程师，参与技术方案评审。\n任职要求：5年以上Java经验，精通Spring Cloud、消息队列、分布式事务。\n加分项：有电商/金融/支付领域经验。",
                        PositionStatus.PUBLISHED, "技术部", "SENIOR", "Java,Spring Cloud,Kafka,分布式,架构设计"),

                seed("前端开发工程师",
                        "岗位职责：负责Web前端页面开发与组件库维护，与后端联调接口。\n任职要求：熟悉Vue3或React，掌握TypeScript、HTML5、CSS3。\n加分项：有组件库、可视化大屏开发经验。",
                        PositionStatus.PUBLISHED, "技术部", "TECH", "Vue,React,TypeScript,Element Plus"),

                seed("高级前端开发工程师",
                        "岗位职责：负责前端架构设计、性能优化、工程化建设。\n任职要求：5年前端经验，精通Vue/React生态，熟悉Webpack/Vite。\n加分项：有微前端、SSR、跨端经验。",
                        PositionStatus.PUBLISHED, "技术部", "SENIOR", "Vue,React,TypeScript,工程化,性能优化"),

                seed("全栈开发工程师",
                        "岗位职责：独立完成前后端功能开发，快速交付业务需求。\n任职要求：熟悉Java/Spring Boot + Vue/React，了解数据库设计。\n加分项：有从0到1项目经验。",
                        PositionStatus.PUBLISHED, "技术部", "TECH", "Java,Vue,Spring Boot,全栈,MySQL"),

                seed("Go语言开发工程师",
                        "岗位职责：负责云原生中间件、API网关、高并发服务开发。\n任职要求：2年以上Go经验，熟悉Gin/gRPC、Docker、Kubernetes。\n加分项：有开源项目或云原生实践经验。",
                        PositionStatus.PUBLISHED, "技术部", "TECH", "Go,Gin,gRPC,Docker,Kubernetes"),

                seed("Python开发工程师",
                        "岗位职责：负责数据处理脚本、自动化工具、内部平台开发。\n任职要求：熟悉Python、Django/FastAPI，了解SQL与Linux。\n加分项：有爬虫、ETL或DevOps经验。",
                        PositionStatus.PUBLISHED, "技术部", "TECH", "Python,Django,FastAPI,Linux,SQL"),

                seed("算法工程师（机器学习）",
                        "岗位职责：负责推荐、搜索、风控等场景的算法模型研发与上线。\n任职要求：硕士及以上，熟悉Python、PyTorch/TensorFlow，有工业界落地经验。\n加分项：顶会论文或Kaggle获奖经历。",
                        PositionStatus.PUBLISHED, "技术部", "TECH", "Python,机器学习,PyTorch,深度学习,算法"),

                seed("算法工程师（NLP）",
                        "岗位职责：负责大模型应用、文本理解、智能问答等NLP方向研发。\n任职要求：熟悉Transformer、LLM微调，有RAG/Agent实践经验。\n加分项：有对话系统或知识图谱项目经验。",
                        PositionStatus.PUBLISHED, "技术部", "TECH", "NLP,大模型,RAG,Python,深度学习"),

                seed("大数据开发工程师",
                        "岗位职责：负责数据仓库建设、ETL开发、实时计算链路搭建。\n任职要求：熟悉Hadoop/Spark/Flink、Hive、Kafka。\n加分项：有数据治理或实时数仓经验。",
                        PositionStatus.PUBLISHED, "技术部", "TECH", "Spark,Flink,Hive,Kafka,数据仓库"),

                seed("数据分析师",
                        "岗位职责：负责业务数据分析、报表搭建、A/B实验设计与解读。\n任职要求：熟练使用SQL、Excel、Python，有BI工具经验。\n加分项：有增长分析或用户行为分析经验。",
                        PositionStatus.PUBLISHED, "产品部", "GENERAL", "SQL,Python,数据分析,BI,A/B测试"),

                seed("测试开发工程师",
                        "岗位职责：负责自动化测试框架、接口测试、性能测试与质量保障。\n任职要求：熟悉Java/Python，掌握Selenium/JMeter/Postman。\n加分项：有CI/CD集成经验。",
                        PositionStatus.PUBLISHED, "技术部", "TECH", "自动化测试,Java,Python,JMeter,CI/CD"),

                seed("DevOps工程师",
                        "岗位职责：负责CI/CD流水线、容器化部署、监控告警体系搭建。\n任职要求：熟悉Docker、Kubernetes、Jenkins、Prometheus。\n加分项：有云平台（AWS/阿里云）运维经验。",
                        PositionStatus.PUBLISHED, "技术部", "TECH", "Docker,Kubernetes,Jenkins,CI/CD,监控"),

                seed("运维工程师",
                        "岗位职责：负责服务器、网络、数据库日常运维与故障排查。\n任职要求：熟悉Linux、Shell、Nginx、MySQL，有7x24值班经验。\n加分项：有IDC或云平台运维背景。",
                        PositionStatus.PUBLISHED, "技术部", "GENERAL", "Linux,Shell,Nginx,MySQL,运维"),

                seed("iOS开发工程师",
                        "岗位职责：负责iOS客户端功能开发与性能优化。\n任职要求：熟悉Swift/Objective-C，了解UIKit/SwiftUI。\n加分项：有App Store上架经验。",
                        PositionStatus.PUBLISHED, "技术部", "TECH", "Swift,iOS,Objective-C,移动端"),

                seed("Android开发工程师",
                        "岗位职责：负责Android客户端开发与版本迭代。\n任职要求：熟悉Kotlin/Java，了解Android SDK与常用框架。\n加分项：有性能优化或跨平台经验。",
                        PositionStatus.PUBLISHED, "技术部", "TECH", "Kotlin,Android,Java,移动端"),

                seed("网络安全工程师",
                        "岗位职责：负责安全漏洞扫描、渗透测试、安全合规与应急响应。\n任职要求：熟悉Web安全、网络协议，持有CISP/CISSP等证书优先。\n加分项：有SRC漏洞挖掘或红蓝对抗经验。",
                        PositionStatus.PUBLISHED, "技术部", "TECH", "网络安全,渗透测试,合规,应急响应"),

                seed("产品经理",
                        "岗位职责：负责需求调研、原型设计、PRD撰写与项目推进。\n任职要求：3年以上互联网产品经验，熟练使用Axure/Figma。\n加分项：有B端SaaS或招聘/HR领域经验。",
                        PositionStatus.PUBLISHED, "产品部", "GENERAL", "产品设计,需求分析,Axure,PRD,用户研究"),

                seed("高级产品经理",
                        "岗位职责：负责核心产品线规划、竞品分析、数据驱动决策。\n任职要求：5年产品经验，有从0到1或规模化产品经验。\n加分项：有AI产品或企业服务产品背景。",
                        PositionStatus.PENDING, "产品部", "SENIOR", "产品规划,数据分析,竞品分析,AI产品"),

                seed("UI/UX设计师",
                        "岗位职责：负责产品界面设计、交互原型、设计规范维护。\n任职要求：熟练使用Figma/Sketch，有完整项目作品集。\n加分项：有B端后台或移动端设计经验。",
                        PositionStatus.PUBLISHED, "产品部", "GENERAL", "UI,UX,Figma,交互设计,视觉设计"),

                seed("项目经理",
                        "岗位职责：负责跨部门项目协调、进度管控、风险识别与资源调配。\n任职要求：3年以上项目管理经验，持有PMP证书优先。\n加分项：有敏捷Scrum实践经验。",
                        PositionStatus.PUBLISHED, "产品部", "GENERAL", "项目管理,PMP,敏捷,Scrum,沟通协调"),

                seed("内容运营",
                        "岗位职责：负责公众号/社群内容策划、撰写与活动策划。\n任职要求：文笔流畅，熟悉新媒体运营，有爆款内容案例。\n加分项：有短视频脚本或直播运营经验。",
                        PositionStatus.PUBLISHED, "运营部", "GENERAL", "内容运营,新媒体,文案,社群运营"),

                seed("用户增长运营",
                        "岗位职责：负责拉新、促活、留存策略制定与落地，监控核心增长指标。\n任职要求：熟悉增长黑客方法论，有渠道投放或裂变活动经验。\n加分项：有招聘/教育/电商增长案例。",
                        PositionStatus.PUBLISHED, "运营部", "GENERAL", "用户增长,数据分析,渠道投放,裂变,A/B测试"),

                seed("市场营销经理",
                        "岗位职责：负责品牌传播、市场活动策划、媒介合作与效果评估。\n任职要求：5年市场营销经验，有互联网行业背景。\n加分项：有大型发布会或品牌联名项目经验。",
                        PositionStatus.PUBLISHED, "市场部", "GENERAL", "市场营销,品牌传播,活动策划,媒介"),

                seed("人力资源专员",
                        "岗位职责：负责招聘执行、员工关系、入离职办理与HR系统维护。\n任职要求：人力资源相关专业，熟悉招聘流程与劳动法规。\n加分项：有互联网行业HR经验。",
                        PositionStatus.PUBLISHED, "人事部", "GENERAL", "招聘,员工关系,HR,劳动法规"),

                seed("招聘专员",
                        "岗位职责：负责简历筛选、面试安排、候选人跟进与offer谈判。\n任职要求：1年以上招聘经验，沟通能力强。\n加分项：有技术岗或高端岗位招聘经验。",
                        PositionStatus.DRAFT, "人事部", "GENERAL", "招聘,面试安排,候选人跟进,offer"),

                seed("客户服务专员",
                        "岗位职责：负责用户咨询接待、问题解答、投诉处理与客户满意度提升。\n任职要求：普通话标准，服务意识强，能抗压。\n加分项：有客服质检或培训经验。",
                        PositionStatus.PUBLISHED, "运营部", "GENERAL", "客户服务,沟通,投诉处理,满意度"),

                seed("财务分析师",
                        "岗位职责：负责预算编制、成本分析、财务报表与经营分析支持。\n任职要求：财务/会计专业，熟悉Excel与财务软件，有CPA优先。\n加分项：有互联网行业财务经验。",
                        PositionStatus.PUBLISHED, "财务部", "GENERAL", "财务分析,预算,CPA,Excel,报表")
        );
    }

    private static Seed seed(String title, String description, PositionStatus status,
                             String department, String positionType, String skillTags) {
        return new Seed(title, description, status, department, positionType, skillTags);
    }
}
