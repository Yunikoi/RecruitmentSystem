/// <reference path="../Declarations/forguncy.d.ts" />
/// <reference path="../Declarations/forguncy.Plugin.d.ts" />

class MarkedRendererCellType extends Forguncy.Plugin.CellTypeBase {
    createContent() {
        const uniqueId = `md-rendering-area-${Date.now()}-${Math.floor(Math.random() * 10000)}`;
        this.markdownContainerId = uniqueId;

        this.content = $(`<div class='markdown-body' id='${uniqueId}' style='width:100%;height:100%;overflow: auto;'></div>`);

        if (this.isDesignerPreview &&
            !this.CellElement.CellType.DefaultValue) {
            this.content.append($("<div style='color:gray; margin-left: 15px;'>请设置 Markdown 文本内容</div>"));
        }
        return this.content;
    }

    async onPageLoaded() {
        this.onFormulaResultChanged(this.CellElement.CellType.DefaultValue, result => {
            const initialMarkdown = this.evaluateFormula(result);
            this.markdownToHtml(initialMarkdown);
        });
    }

    setValueToElement(_, value) {
        value = value?.toString() ?? "";
        this.markdownToHtml(value);
    }

    set_DefaultValue(value) {
        this.markdownToHtml(value);
    }

    markdownToHtml(markdownContent) {
        if (markdownContent == null) {
            return;
        }

        const outputArea = document.getElementById(this.markdownContainerId);

        if (!outputArea) {
            console.warn(`未找到对应 markdown 容器: ${this.markdownContainerId}`);
            return;
        }
        
        try {
            // 解析Markdown内容
            try {
                // 活字格通过公式单元格传递的文本内容不会自动识别为换行符，因此需要强制转化一次
                const parsedText = markdownContent.replace(/\\n/g, '\n')
                // markdown文本转化
                outputArea.innerHTML = marked.parse(parsedText);
            } catch (parseError) {
                console.error('Markdown解析失败:', parseError);
                outputArea.innerHTML = '<div class="error">无法解析Markdown内容</div>';
                return;
            }

            // 代码块高亮
            try {
                document.querySelectorAll('#md-rendering-area pre code').forEach(block => {
                    hljs.highlightElement(block);
                });
            } catch (highlightAllError) {
                console.error('代码块高亮失败:', highlightAllError);
            }

            if (this.CellElement.CellType.SupportKatex) {
                const url = Forguncy.Helper.SpecialPath.getPluginRootPath("d63bdf82-52dc-4274-8bb6-9f16f37a7ceb");
                // 资源异步顺序加载
                Promise.all([
                    this.loadCssAsync(url + 'Resources/katex.min.css'),
                    this.loadScriptAsync(url + 'Resources/katex.min.js')
                ])
                .then(() => {
                    return this.loadScriptAsync(url + 'Resources/auto-render.min.js');
                })
                .then(() => {
                    renderMathInElement(outputArea, {
                        delimiters: [
                            { left: "$$", right: "$$", display: true },
                            { left: "\$", right: "\$", display: false },
                            { left: "$", right: "$", display: false }
                        ],
                        output: "html"
                    });
                })
                .catch((err) => {
                    console.error('加载失败：', err);
                });
            }

        } catch (mainError) {
            console.error('Markdown 渲染器初始化失败:', mainError);
            if (outputArea) {
                outputArea.innerHTML = '<div style="color: gray; margin - left: 15px;" class="error">初始化失败，请刷新页面重试</div><div>' + mainError +'</div>';
            }
        }
    }

    loadScriptAsync(url) {
        return new Promise((resolve, reject) => {
            const script = document.createElement('script');
            script.src = url;
            script.async = true;
            script.onload = () => resolve();
            script.onerror = () => reject(new Error(`Script 加载失败: ${url}`));
            document.head.appendChild(script);
        });
    }

    loadCssAsync(url) {
        return new Promise((resolve, reject) => {
            const link = document.createElement('link');
            link.rel = 'stylesheet';
            link.href = url;
            link.onload = () => resolve();
            link.onerror = () => reject(new Error(`CSS 加载失败: ${url}`));
            document.head.appendChild(link);
        });
    }
}
Forguncy.Plugin.CellTypeHelper.registerCellType("MarkedRenderer.MarkedRendererCellType, MarkedRenderer", MarkedRendererCellType);