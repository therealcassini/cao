package com.cassini.graph.util;

import com.cassini.graph.entity.Edge;
import com.cassini.graph.entity.Node;
import com.cassini.graph.entity.Theme;
import com.cassini.graph.service.EdgeService;
import com.cassini.graph.service.NodeService;
import com.cassini.graph.service.ThemeService;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private EdgeService edgeService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 检查是否已经存在数据，如果存在则不初始化
        if (!themeService.getAllThemes().isEmpty()) {
            return;
        }

        // 创建主题
        Theme personTheme = new Theme();
        personTheme.setName("人物");
        personTheme.setDescription("知识图谱中的人物节点");
        personTheme.setDefaultNodeColor("#4285F4");
        personTheme.setDefaultNodeSize(25);
        personTheme.setDefaultNodeIcon("👤");
        personTheme = themeService.saveTheme(personTheme);

        Theme organizationTheme = new Theme();
        organizationTheme.setName("组织");
        organizationTheme.setDescription("知识图谱中的组织节点");
        organizationTheme.setDefaultNodeColor("#34A853");
        organizationTheme.setDefaultNodeSize(30);
        organizationTheme.setDefaultNodeIcon("🏢");
        organizationTheme = themeService.saveTheme(organizationTheme);

        Theme conceptTheme = new Theme();
        conceptTheme.setName("概念");
        conceptTheme.setDescription("知识图谱中的概念节点");
        conceptTheme.setDefaultNodeColor("#FBBC05");
        conceptTheme.setDefaultNodeSize(20);
        conceptTheme.setDefaultNodeIcon("💡");
        conceptTheme = themeService.saveTheme(conceptTheme);

        // 创建人物节点
        Node alice = createNode("张三", personTheme, createProperties("职位", "软件工程师", "年龄", "30", "技能", "Java, Vue"));
        Node bob = createNode("李四", personTheme, createProperties("职位", "产品经理", "年龄", "32", "技能", "需求分析 项目管理"));
        Node charlie = createNode("王五", personTheme, createProperties("职位", "UI设计师", "年龄", "28", "技能", "Figma, Photoshop"));

        // 创建组织节点
        Node companyA = createNode("科技有限公司", organizationTheme, createProperties("行业", "互联网", "成立时间", "2015", "规模", "50-100人"));
        Node departmentB = createNode("研发部", organizationTheme, createProperties("负责人", "张三", "成员数", "30"));

        // 创建概念节点
        Node java = createNode("Java", conceptTheme, createProperties("类型", "编程语言", "创立时间", "1995", "应用领域", "后端开发"));
        Node vue = createNode("Vue", conceptTheme, createProperties("类型", "前端框架", "创立时间", "2014", "应用领域", "前端开发"));

        // 创建关系
        createEdge(alice, companyA, "就职于");
        createEdge(bob, companyA, "就职于");
        createEdge(charlie, companyA, "就职于");
        createEdge(alice, departmentB, "属于");
        createEdge(bob, departmentB, "属于");
        createEdge(alice, java, "精通");
        createEdge(alice, vue, "熟悉");
        createEdge(bob, java, "了解");
        createEdge(companyA, java, "使用");
        createEdge(companyA, vue, "使用");

        System.out.println("示例数据初始化完成");
    }

    private Node createNode(String name, Theme theme, ObjectNode properties) {
        Node node = new Node();
        node.setName(name);
        node.setTheme(theme);
        node.setProperties(properties.toString());
        return nodeService.saveNode(node);
    }

    private ObjectNode createProperties(String... keyValues) {
        ObjectNode properties = JsonNodeFactory.instance.objectNode();
        for (int i = 0; i < keyValues.length; i += 2) {
            if (i + 1 < keyValues.length) {
                properties.put(keyValues[i], keyValues[i + 1]);
            }
        }
        return properties;
    }

    private Edge createEdge(Node source, Node target, String relation) {
        Edge edge = new Edge();
        edge.setSourceNodeName(source.getName());
        edge.setTargetNodeName(target.getName());
        edge.setRelation(relation);
        // 设置边的主题为源节点的主题
        edge.setTheme(source.getTheme());
        return edgeService.saveEdge(edge);
    }
}
