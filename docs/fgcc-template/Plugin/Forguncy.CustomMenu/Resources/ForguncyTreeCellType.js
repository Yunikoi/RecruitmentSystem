var __extends = (this && this.__extends) || (function () {
    var extendStatics = function (d, b) {
        extendStatics = Object.setPrototypeOf ||
            ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
            function (d, b) { for (var p in b) if (Object.prototype.hasOwnProperty.call(b, p)) d[p] = b[p]; };
        return extendStatics(d, b);
    };
    return function (d, b) {
        if (typeof b !== "function" && b !== null)
            throw new TypeError("Class extends value " + String(b) + " is not a constructor or null");
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __generator = (this && this.__generator) || function (thisArg, body) {
    var _ = { label: 0, sent: function() { if (t[0] & 1) throw t[1]; return t[1]; }, trys: [], ops: [] }, f, y, t, g;
    return g = { next: verb(0), "throw": verb(1), "return": verb(2) }, typeof Symbol === "function" && (g[Symbol.iterator] = function() { return this; }), g;
    function verb(n) { return function (v) { return step([n, v]); }; }
    function step(op) {
        if (f) throw new TypeError("Generator is already executing.");
        while (_) try {
            if (f = 1, y && (t = op[0] & 2 ? y["return"] : op[0] ? y["throw"] || ((t = y["return"]) && t.call(y), 0) : y.next) && !(t = t.call(y, op[1])).done) return t;
            if (y = 0, t) op = [op[0] & 2, t.value];
            switch (op[0]) {
                case 0: case 1: t = op; break;
                case 4: _.label++; return { value: op[1], done: false };
                case 5: _.label++; y = op[1]; op = [0]; continue;
                case 7: op = _.ops.pop(); _.trys.pop(); continue;
                default:
                    if (!(t = _.trys, t = t.length > 0 && t[t.length - 1]) && (op[0] === 6 || op[0] === 2)) { _ = 0; continue; }
                    if (op[0] === 3 && (!t || (op[1] > t[0] && op[1] < t[3]))) { _.label = op[1]; break; }
                    if (op[0] === 6 && _.label < t[1]) { _.label = t[1]; t = op; break; }
                    if (t && _.label < t[2]) { _.label = t[2]; _.ops.push(op); break; }
                    if (t[2]) _.ops.pop();
                    _.trys.pop(); continue;
            }
            op = body.call(thisArg, _);
        } catch (e) { op = [6, e]; y = 0; } finally { f = t = 0; }
        if (op[0] & 5) throw op[1]; return { value: op[0] ? op[1] : void 0, done: true };
    }
};
var __spreadArray = (this && this.__spreadArray) || function (to, from, pack) {
    if (pack || arguments.length === 2) for (var i = 0, l = from.length, ar; i < l; i++) {
        if (ar || !(i in from)) {
            if (!ar) ar = Array.prototype.slice.call(from, 0, i);
            ar[i] = from[i];
        }
    }
    return to.concat(ar || Array.prototype.slice.call(from));
};
var Forguncy;
(function (Forguncy) {
    var ForguncyTreeCellTypeStyleTemplateHelper = (function (_super) {
        __extends(ForguncyTreeCellTypeStyleTemplateHelper, _super);
        function ForguncyTreeCellTypeStyleTemplateHelper() {
            var _this = _super.call(this) || this;
            _this.CellTypeString = "Forguncy.CustomMenu.ForguncyTreeCellType";
            _this.TemplateNameParts = ["Tree"];
            return _this;
        }
        ForguncyTreeCellTypeStyleTemplateHelper.prototype.MapPartsNameToDom = function (container) {
            this.Container = container;
            var tree = container.find("a");
            return {
                Tree: tree
            };
        };
        return ForguncyTreeCellTypeStyleTemplateHelper;
    }(Forguncy.CellTypeStyleTemplateBase));
    Forguncy.ForguncyTreeCellTypeStyleTemplateHelper = ForguncyTreeCellTypeStyleTemplateHelper;
    var ZtreeIconType;
    (function (ZtreeIconType) {
        ZtreeIconType["docu"] = "docu";
        ZtreeIconType["open"] = "open";
        ZtreeIconType["close"] = "close";
    })(ZtreeIconType || (ZtreeIconType = {}));
    var ForguncyTreeCellType = (function (_super) {
        __extends(ForguncyTreeCellType, _super);
        function ForguncyTreeCellType() {
            var _this = _super !== null && _super.apply(this, arguments) || this;
            _this.currentTreeValue = null;
            _this.getAllSelectorList = function (type) {
                return ["noline", "center", "bottom", "roots", "root"].map(function (v) {
                    return v + "_" + type;
                });
            };
            _this._initLineCss = function () {
                var element = _this.CellElement;
                var fontSize = element.StyleInfo.FontSize;
                var scaleNum = 0.075 * fontSize + 0.76;
                var cellTypeMetaData = element.CellType;
                var foreColor = "#D9D9D9";
                var svg = "<svg xmlns=\"http://www.w3.org/2000/svg\">\n  <line x1=\"50%\" y1=\"0\" x2=\"50%\" y2=\"100%\" stroke=\"".concat(foreColor, "\" />\n</svg>");
                var image = "background-image: url(\"data:image/svg+xml,".concat(encodeURIComponent(svg), "\");");
                var commonStyle = "\n                    background-repeat: no-repeat;\n                    background-position: 0% 0em;\n                    background-size: 16px 100%;\n                    transform: scaleY(".concat(scaleNum, ");\n                    transform-origin: center center;\n                    ");
                var styles = [{
                        selectors: [".ztree ul.line"],
                        content: "\n                ".concat(image, "\n                    background-repeat: repeat-y;\n                    background-position: 0% 0em;\n                    background-size: 16px 100%;\n                    ")
                    },
                    cellTypeMetaData.LeafIcon ? undefined : {
                        selectors: [".ztree li span.switch.center_docu"],
                        content: "\n                    ".concat(_this._getCenter_docuSvg(foreColor, scaleNum), "\n                    ").concat(commonStyle, "\n                    ")
                    },
                    cellTypeMetaData.LeafIcon ? undefined : {
                        selectors: [".ztree li span.switch.bottom_docu"],
                        content: "\n                    ".concat(_this._getBottom_docu(foreColor, scaleNum), "\n                    ").concat(commonStyle, "\n                    ")
                    }].filter(function (v) { return v; });
                _this.appendStyleToBody(styles, "zTreeShowLine");
            };
            _this.nodeId = 1;
            return _this;
        }
        ForguncyTreeCellType.prototype.getValueFromElement = function () {
            return this.currentTreeValue;
        };
        ForguncyTreeCellType.prototype.setValueToElement = function (element, value) {
            if (this.currentTreeValue !== value) {
                this.currentTreeValue = value;
                this.updateTreeActiveState();
            }
            this.selectTreeNode(value);
        };
        ForguncyTreeCellType.prototype.selectTreeNode = function (value) {
            var treeObj = this.getZTree(this.treeID);
            if (!treeObj) {
                return;
            }
            var success = this.updateSelectionStyle(treeObj, value);
            if (success) {
                return;
            }
            var selectedNodes = treeObj.getSelectedNodes();
            if (selectedNodes.length > 0) {
                treeObj.cancelSelectedNode(selectedNodes[0]);
            }
            if (this.currentTreeValue !== null) {
                this.currentTreeValue = null;
                this.commitValue();
            }
        };
        Object.defineProperty(ForguncyTreeCellType.prototype, "treeID", {
            get: function () {
                return this.ID + "_" + this._pageID;
            },
            enumerable: false,
            configurable: true
        });
        ForguncyTreeCellType.prototype.getZTree = function (id) {
            return $.fn.zTree.getZTreeObj(id);
        };
        ForguncyTreeCellType.prototype.updateSelectionStyle = function (treeObj, value) {
            var selectedNodesBefore = treeObj.getSelectedNodes();
            var beforeSelectNode = null;
            if (selectedNodesBefore && selectedNodesBefore.length === 1 && selectedNodesBefore[0].value === value) {
                beforeSelectNode = selectedNodesBefore[0];
            }
            var nodes = treeObj.transformToArray(treeObj.getNodes());
            var switcher = [];
            if (beforeSelectNode) {
                switcher = $("#" + beforeSelectNode.tId + "_switch");
            }
            $("#" + this.ID + " .selected").removeClass("selected");
            $("#".concat(this.ID, " span.switch")).attr("data-selected", "");
            if (beforeSelectNode) {
                $("#" + this.ID + " li#" + beforeSelectNode.tId + ">a").addClass("selected");
                if (switcher.length) {
                    switcher.attr("data-selected", "selected");
                }
                return true;
            }
            else {
                for (var i = 0; i < nodes.length; i++) {
                    if (nodes[i].tag == value) {
                        treeObj.selectNode(nodes[i]);
                        $("#" + this.ID + " li#" + nodes[i].tId + ">.switch").attr("data-selected", "selected");
                        $("#" + this.ID + " li#" + nodes[i].tId + ">a").addClass("selected");
                        return true;
                    }
                }
            }
            return false;
        };
        ForguncyTreeCellType.prototype.createContent = function () {
            var container = $("<div id=\"".concat(this.ID, "\" data-page-id=\"").concat(this._pageID, "\" class=\"treeContainer\"/>"));
            this.treeContainer = container;
            var ul = $("<ul id='" + this.treeID + "' class= 'ztree' > </ul>");
            container.append(ul);
            if (this.isDesignerPreview) {
                var _a = this.designerPreviewCustomArgs, _ = _a[0], isStyleTemplate = _a[1], previewMaxCountText = _a[2];
                if (isStyleTemplate) {
                    container.css("border", "1px solid #C0C0C0");
                    var cellTypeMetaData = this.CellElement.CellType;
                    cellTypeMetaData.DefaultExpandStyle = 0;
                }
                else {
                    if (previewMaxCountText) {
                        var MaxPreviewCount = $("<div>".concat(previewMaxCountText, "</div>"));
                        MaxPreviewCount.css("color", "gray");
                        MaxPreviewCount.css("font-size", "12px");
                        MaxPreviewCount.css("margin", "15px 0 0 5px");
                        container.append(MaxPreviewCount);
                    }
                }
            }
            this.addSelectedStyle(container);
            return container;
        };
        ForguncyTreeCellType.prototype.addSelectedStyle = function (container) {
            var element = this.CellElement;
            var cellTypeMetaData = element.CellType;
            var treeStyleInfo = cellTypeMetaData.TreeStyleInfo;
            if (treeStyleInfo) {
                if (treeStyleInfo.SelectedBackColor && treeStyleInfo.SelectedBackColor !== "") {
                    container.append($("<style> #".concat(this.treeID, " li a.curSelectedNode { background:").concat(Forguncy.ConvertToCssColor(treeStyleInfo.SelectedBackColor), ";}</style>")));
                }
                if (treeStyleInfo.SelectedForeColor && treeStyleInfo.SelectedForeColor !== "") {
                    container.append($("<style> #".concat(this.treeID, " li a.curSelectedNode { color:").concat(Forguncy.ConvertToCssColor(treeStyleInfo.SelectedForeColor), ";}</style>")));
                }
            }
        };
        ForguncyTreeCellType.prototype.initLeafIcon = function () {
            var element = this.CellElement;
            var cellTypeMetaData = element.CellType;
            if (cellTypeMetaData.LeafIcon) {
                var LeafIconSrc = this.getImageUrl(cellTypeMetaData.LeafIcon);
                var selectors = this.getAllSelectorList(ZtreeIconType.docu).map(function (v) {
                    return "span.button" + "." + v;
                });
                if (Forguncy.ImageDataHelper.IsSvg(LeafIconSrc)) {
                    this._addSvgLeafIcon(selectors);
                }
                else {
                    this._appendLeafIconStyle(selectors, LeafIconSrc);
                }
            }
        };
        ForguncyTreeCellType.prototype._addSvgLeafIcon = function (selectors) {
            var _this = this;
            var _a, _b;
            var element = this.CellElement;
            var cellTypeMetaData = element.CellType;
            var LeafIconSrc = this.getImageUrl(cellTypeMetaData.LeafIcon);
            var leafSvgColor = Forguncy.ConvertToCssColor(((_a = cellTypeMetaData.LeafIcon) === null || _a === void 0 ? void 0 : _a.UseCellTypeForeColor)
                ? this.getCellTypeForeColor(element)
                : (_b = cellTypeMetaData.LeafIcon) === null || _b === void 0 ? void 0 : _b.Color);
            Forguncy.ImageHelper.requestSvg(LeafIconSrc, function (svgElement) {
                var _a;
                var notNormalStyle = ((_a = cellTypeMetaData.LeafIcon) === null || _a === void 0 ? void 0 : _a.UseCellTypeForeColor)
                    ? _this.getSvgWithCellForeColorStyle(element, svgElement, ZtreeIconType.docu)
                    : [];
                Forguncy.ImageHelper.preHandleSvg($(svgElement), leafSvgColor);
                var base64 = Forguncy.MenuStyleUtils.GetBase64FromSvgElement(svgElement);
                _this._appendLeafIconStyle(selectors, base64, notNormalStyle);
            });
        };
        ForguncyTreeCellType.prototype.getSvgWithCellForeColorStyle = function (element, svgElement, ztreeIconType) {
            var _a, _b, _c, _d, _e, _f, _g, _h;
            var hoverSelectors = this.getAllSelectorList(ztreeIconType).map(function (v) {
                return "li span.button" + "." + v + ":hover";
            });
            var selectedSelectors = this.getAllSelectorList(ztreeIconType).map(function (v) {
                return "li span[data-selected=\"selected\"].button" + "." + v;
            });
            return [
                {
                    selectors: hoverSelectors,
                    value: (_d = (_c = (_b = (_a = element.StyleTemplate) === null || _a === void 0 ? void 0 : _a.Styles) === null || _b === void 0 ? void 0 : _b.Tree) === null || _c === void 0 ? void 0 : _c.HoverStyle) === null || _d === void 0 ? void 0 : _d.FontColor,
                },
                {
                    selectors: selectedSelectors,
                    value: (_h = (_g = (_f = (_e = element.StyleTemplate) === null || _e === void 0 ? void 0 : _e.Styles) === null || _f === void 0 ? void 0 : _f.Tree) === null || _g === void 0 ? void 0 : _g.SelectedStyle) === null || _h === void 0 ? void 0 : _h.FontColor,
                }
            ].filter(function (v) { return v.value; }).map(function (v) {
                var newElement = $(svgElement.cloneNode(true));
                Forguncy.ImageHelper.preHandleSvg(newElement, v.value);
                return {
                    style: Forguncy.MenuStyleUtils.GetBase64FromSvgElement(newElement[0]),
                    selectors: v.selectors
                };
            });
        };
        ForguncyTreeCellType.prototype._appendLeafIconStyle = function (selectors, src, notNormalStyle) {
            if (notNormalStyle === void 0) { notNormalStyle = []; }
            var iconSize = 16;
            var styles = __spreadArray([{
                    content: "\n            width:".concat(iconSize, "px;\n            height:").concat(iconSize, "px;\n            background-size: ").concat(iconSize, "px!important;\n           ").concat(src ? " background-image:url(".concat(src, ")!important;") : '', "\n           "),
                    selectors: selectors,
                }], notNormalStyle === null || notNormalStyle === void 0 ? void 0 : notNormalStyle.map(function (v) {
                return {
                    selectors: v.selectors,
                    content: "background-image:url(".concat(v.style, ")!important;")
                };
            }), true);
            this.appendStyleToBody(styles, 'leafIcon');
        };
        ForguncyTreeCellType.prototype._setFontStyle = function (styleInfo) {
            this.initOpenCloseIcon(this.CellElement, styleInfo.FontSize);
            this.initLeafIcon();
            var ul = this.treeContainer.find("#" + this.treeID);
            if (styleInfo.FontFamily) {
                ul.css("font-family", styleInfo.FontFamily);
            }
            else {
                ul.css("font-family", Forguncy.LocaleFonts.default);
            }
            if (styleInfo.FontSize && styleInfo.FontSize > 0) {
                ul.css("font-size", styleInfo.FontSize);
                var lineHeight = Math.ceil(styleInfo.FontSize + 8);
                var li = ul.find("li");
                li.css("line-height", lineHeight + "px");
                ul.find("li span").css("line-height", lineHeight + "px");
            }
            if (styleInfo.Foreground && styleInfo.Foreground !== "") {
                $("#" + this.ID).find("ul li a").css("color", Forguncy.ConvertToCssColor(styleInfo.Foreground));
            }
            else {
                $("#" + this.ID).find("ul li a").css("color", "");
            }
            $("#" + this.ID).find("ul li a.selected").css("color", "");
        };
        ForguncyTreeCellType.prototype.getStyleTemplateHelper = function () {
            var cellTypeMetaData = this.CellElement.CellType;
            if (cellTypeMetaData.TreeStyleInfo) {
                return null;
            }
            return ForguncyTreeCellType.StyleTemplateHelper;
        };
        ForguncyTreeCellType.prototype.getTreeSettings = function () {
            var self = this;
            var element = this.CellElement;
            var cellTypeMetaData = element.CellType;
            var showLine = !!cellTypeMetaData.ShowLine;
            if (showLine) {
                this._initLineCss();
            }
            var setting = {
                view: {
                    showLine: showLine,
                    dblClickExpand: false,
                    expandSpeed: this.isDesignerPreview ? "" : undefined
                },
                data: {
                    simpleData: {
                        enable: false
                    }
                },
                callback: {
                    onClick: function (e, treeId, treeNode) {
                        if (self.isDisabled()) {
                            return;
                        }
                        if (treeNode.tag !== self.currentTreeValue) {
                            self.currentTreeValue = treeNode.tag;
                            self.updateTreeActiveState();
                            self.commitValue();
                        }
                        self.updateSelectionStyle(self.getZTree(treeId), treeNode.tag);
                        var clickCommand = self.CellElement.CellType.TreeClickCommand;
                        self.onTreeClick(clickCommand, treeNode);
                    },
                    onNodeCreated: function (event, treeId, treeNode) {
                        if (treeNode.isLastNode) {
                            self.repaint();
                        }
                    },
                    onExpand: function () {
                        self.updateSimpleBar();
                    },
                    onCollapse: function () {
                        self.updateSimpleBar();
                    }
                }
            };
            return setting;
        };
        ForguncyTreeCellType.prototype._getBottom_docu = function (foreColor, scaleNum) {
            var svg = "<svg xmlns=\"http://www.w3.org/2000/svg\">\n            <line x1=\"50%\" y1=\"0\" x2=\"50%\" y2=\"50%\" stroke=\"".concat(foreColor, "\" /><line x1=\"50%\" y1=\"50%\" x2=\"100%\" y2=\"50%\" stroke-width=\"").concat(1 / scaleNum, "\" stroke=\"").concat(foreColor, "\" />\n            </svg>");
            var image = "background-image: url(\"data:image/svg+xml,".concat(encodeURIComponent(svg), "\");");
            return image;
        };
        ForguncyTreeCellType.prototype._getCenter_docuSvg = function (foreColor, scaleNum) {
            var svg = "<svg xmlns=\"http://www.w3.org/2000/svg\">\n            <line x1=\"50%\" y1=\"0\" x2=\"50%\" y2=\"100%\" stroke=\"".concat(foreColor, "\" /><line x1=\"50%\" y1=\"50%\" x2=\"100%\" y2=\"50%\" stroke-width=\"").concat(1 / scaleNum, "\" stroke=\"").concat(foreColor, "\" />\n            </svg>");
            var image = "background-image: url(\"data:image/svg+xml,".concat(encodeURIComponent(svg), "\");");
            return image;
        };
        ForguncyTreeCellType.prototype.onTreeClick = function (clickCommand, currentTreeNode) {
            if (!clickCommand) {
                return;
            }
            if (this.isCommandExecuting()) {
                return;
            }
            var context = this.getFormulaCalcContext();
            var initParams = {};
            Forguncy.CellHelper.setValueToCell(context, clickCommand.ValueTo, currentTreeNode.value, initParams);
            Forguncy.CellHelper.setValueToCell(context, clickCommand.NameTo, currentTreeNode.name, initParams);
            Forguncy.CellHelper.setValueToCell(context, clickCommand.LevelTo, currentTreeNode.level + 1, initParams);
            var commands = clickCommand.CommandList;
            if (commands && commands.length > 0) {
                this.executeCommand(commands, { initParams: initParams });
            }
        };
        ForguncyTreeCellType.prototype.getTreeNodes = function (callback) {
            var _this = this;
            var element = this.CellElement;
            var cellTypeMetaData = element.CellType;
            var bindingTreeLevelInfo = cellTypeMetaData.BindingSourceTreeLevelInfo;
            var isBinding = cellTypeMetaData.IsBinding;
            var treeBindingMode = cellTypeMetaData.TreeBindingMode;
            var items = cellTypeMetaData.Items;
            if (this.isDesignerPreview) {
                var defaultItem = this.designerPreviewCustomArgs[0];
                if (defaultItem) {
                    items = defaultItem;
                    var nodes = this.convertItemsToTreeNodes(items, null, "");
                    callback(nodes);
                    return;
                }
            }
            if (isBinding === true) {
                if (treeBindingMode === 1) {
                    this.getTreeNodesInDynamicMode(bindingTreeLevelInfo, callback);
                }
                else {
                    this.getBindingItemsInFixLevelMode(bindingTreeLevelInfo, function (items) {
                        _this.fixLevelModeItemTags(items);
                        callback(items);
                    });
                }
            }
            else {
                if (!items || items.length === 0) {
                    return [];
                }
                var nodes = this.convertItemsToTreeNodes(items, null, "");
                callback(nodes);
            }
        };
        ForguncyTreeCellType.prototype.fixLevelModeItemTags = function (items, parentNode) {
            if (parentNode === void 0) { parentNode = null; }
            if (items) {
                for (var _i = 0, items_1 = items; _i < items_1.length; _i++) {
                    var item = items_1[_i];
                    item.tag = parentNode ? parentNode.tag + "_" + item.value : item.value;
                    this.fixLevelModeItemTags(item.children, item);
                }
            }
        };
        ForguncyTreeCellType.prototype.isEmpty = function (obj) {
            if (obj === null || obj === "" || obj === undefined) {
                return true;
            }
            return false;
        };
        ForguncyTreeCellType.prototype.addTableColumnParamInfo = function (list, columnName) {
            if (!this.isEmpty(columnName) && list.indexOf(columnName) === -1) {
                list.push(columnName);
            }
        };
        ForguncyTreeCellType.prototype.getBindingItemsInFixLevelMode = function (bindingTreeLevelInfo, callback) {
            var _this = this;
            if (!(bindingTreeLevelInfo === null || bindingTreeLevelInfo === void 0 ? void 0 : bindingTreeLevelInfo.BindingDataSource)) {
                callback([]);
                return;
            }
            this.getBindingDataSourceValue(bindingTreeLevelInfo.BindingDataSource, null, function (tableData) {
                var items = [];
                var idCache = {};
                var idPidcache = {};
                if (tableData && tableData.length > 0) {
                    for (var i = 0; i < tableData.length; i++) {
                        var value = tableData[i]["id"];
                        var pid = tableData[i]["pid"];
                        if (value === null || value === undefined || idPidcache["_" + pid + "_" + value]) {
                            continue;
                        }
                        var itemInfo = {};
                        var text = tableData[i]["name"];
                        if (text === undefined || text === null) {
                            text = "";
                        }
                        itemInfo.name = text;
                        itemInfo.id = value;
                        itemInfo.pid = pid;
                        itemInfo.tag = value;
                        itemInfo.value = value;
                        items.push(itemInfo);
                        if (!idCache[value]) {
                            idCache[value] = [];
                        }
                        idCache[value].push(itemInfo);
                        idPidcache["_" + pid + "_" + value] = itemInfo;
                    }
                }
                if (bindingTreeLevelInfo.SubBindingTreeLevelInfo) {
                    _this.getBindingItemsInFixLevelMode(bindingTreeLevelInfo.SubBindingTreeLevelInfo, function (subItems) {
                        for (var i = 0; i < subItems.length; i++) {
                            var cacheItems = idCache[subItems[i].pid];
                            if (cacheItems) {
                                for (var _i = 0, cacheItems_1 = cacheItems; _i < cacheItems_1.length; _i++) {
                                    var cacheItem = cacheItems_1[_i];
                                    if (cacheItem) {
                                        if (!cacheItem.children) {
                                            cacheItem.children = [];
                                        }
                                        cacheItem.children.push(subItems[i]);
                                    }
                                }
                            }
                        }
                        callback(items);
                    });
                }
                else {
                    callback(items);
                }
            });
        };
        ForguncyTreeCellType.prototype.getTreeNodesInDynamicMode = function (bindingTreeLevelInfo, callback) {
            var _this = this;
            this.getBindingDataSourceValue(bindingTreeLevelInfo.BindingDataSource, null, function (items) {
                var allDataNodes = items.map(function (item) {
                    var _a, _b;
                    return ({
                        id: (_a = item["id"]) === null || _a === void 0 ? void 0 : _a.toString(),
                        pid: (_b = item["pid"]) === null || _b === void 0 ? void 0 : _b.toString(),
                        text: item["name"],
                        name: item["name"],
                        value: item["id"],
                        tag: item["id"]
                    });
                });
                var idNodesCache = {};
                var parentIdNodesCache = {};
                for (var _i = 0, allDataNodes_1 = allDataNodes; _i < allDataNodes_1.length; _i++) {
                    var node = allDataNodes_1[_i];
                    if (!idNodesCache[node.id]) {
                        idNodesCache[node.id] = [];
                    }
                    idNodesCache[node.id].push(node);
                    if (!parentIdNodesCache[node.pid]) {
                        parentIdNodesCache[node.pid] = [];
                    }
                    parentIdNodesCache[node.pid].push(node);
                }
                _this.buildChildren(allDataNodes, parentIdNodesCache);
                var roots = allDataNodes.filter(function (i) { return _this.isRootNode(i, idNodesCache); });
                var existId = {};
                var uniqueRoots = roots.filter(function (i) {
                    if (existId[i.id]) {
                        return false;
                    }
                    existId[i.id] = true;
                    return true;
                });
                callback(uniqueRoots);
            });
        };
        ForguncyTreeCellType.prototype.buildChildren = function (filteredNodes, parentIdDataNodesCache) {
            var _loop_1 = function (node) {
                var children = parentIdDataNodesCache[node.id];
                if (children) {
                    var existId_1 = {};
                    node.children = children.filter(function (i) {
                        if (existId_1[i.id]) {
                            return false;
                        }
                        existId_1[i.id] = true;
                        return true;
                    });
                }
                else {
                    node.children = [];
                }
            };
            for (var _i = 0, filteredNodes_1 = filteredNodes; _i < filteredNodes_1.length; _i++) {
                var node = filteredNodes_1[_i];
                _loop_1(node);
            }
        };
        ForguncyTreeCellType.prototype.isRootNode = function (node, idNodesCache) {
            if (!node.pid) {
                return true;
            }
            var nodes = idNodesCache[node.pid];
            if (!nodes) {
                return true;
            }
            return false;
        };
        ForguncyTreeCellType.prototype.convertItemsToTreeNodes = function (items, pId, pTag) {
            if (items && items.length > 0) {
                var nodes = [];
                for (var i = 0; i < items.length; i++) {
                    var item = items[i];
                    var node = {
                        id: (this.nodeId++).toString(),
                        text: item.Text,
                        name: this.getApplicationResource(item.Text),
                        value: item.Value,
                        pid: pId,
                        tag: pTag === "" ? item.Value : (pTag + "_" + item.Value),
                        uId: (this.nodeId++).toString(),
                        children: []
                    };
                    nodes.push(node);
                    var subNodes = this.convertItemsToTreeNodes(item.SubItems, node.id, node.tag);
                    if (subNodes && subNodes.length > 0) {
                        for (var k = 0; k < subNodes.length; k++) {
                            node.children.push(subNodes[k]);
                        }
                    }
                }
                return nodes;
            }
            return null;
        };
        ForguncyTreeCellType.prototype.onPageLoaded = function (info) {
            return __awaiter(this, void 0, void 0, function () {
                var self_1;
                var _this = this;
                return __generator(this, function (_a) {
                    if (this.isDesignerPreview) {
                        return [2, new Promise(function (resolve) {
                                _this.loadZTreeNodes(function () {
                                    var _a;
                                    var _b = _this.designerPreviewCustomArgs[0], defaultItem = _b === void 0 ? [] : _b;
                                    _this.selectTreeNode((_a = defaultItem[0]) === null || _a === void 0 ? void 0 : _a.Value);
                                    _this.repaint();
                                    resolve("");
                                });
                            })];
                    }
                    else {
                        self_1 = this;
                        this.onDependenceCellValueChanged(function (uiAction) {
                            self_1.loadZTreeNodes(function () {
                                self_1.selectTreeNode(self_1.currentTreeValue);
                            });
                        });
                        this.loadZTreeNodes(function () {
                            _this.selectTreeNode(_this.currentTreeValue);
                            _this.createSimpleBar();
                        });
                    }
                    return [2];
                });
            });
        };
        ForguncyTreeCellType.prototype.getImageUrl = function (image) {
            if (!image) {
                return "";
            }
            if (!image.Name) {
                return "";
            }
            var src = "";
            if (image.BuiltIn) {
                src = Forguncy.Helper.SpecialPath.getBuiltInImageFolderPath() + image.Name;
            }
            else {
                src = Forguncy.Helper.SpecialPath.getImageEditorUploadImageFolderPath() + encodeURIComponent(image.Name);
            }
            return src;
        };
        ForguncyTreeCellType.prototype.setIconStyle = function (src, defaultUrl, svgColor, type, element, UseCellTypeForeColor) {
            var _this = this;
            var selectors = this.getAllSelectorList(type).map(function (v) {
                return ".ztree li span.button" + "." + v;
            });
            if (src.length === 0) {
                var content = "background-image: url(".concat(defaultUrl, ");");
                return this.appendStyleToBody([{ content: content, selectors: selectors }], selectors.join(','));
            }
            if (Forguncy.ImageDataHelper.IsSvg(src)) {
                Forguncy.ImageHelper.requestSvg(src, function (svgElement) {
                    var _a, _b, _c, _d, _e, _f, _g, _h;
                    svgElement.setAttribute('height', '100%');
                    svgElement.setAttribute('width', '100%');
                    var hoverSelectors = _this.getAllSelectorList(type).map(function (v) {
                        return ".ztree li span.button" + "." + v + ":hover";
                    });
                    var selectedSelectors = _this.getAllSelectorList(type).map(function (v) {
                        return ".ztree li span[data-selected=\"selected\"].button" + "." + v;
                    });
                    var notNormalColors = [
                        {
                            selectors: hoverSelectors,
                            value: (_d = (_c = (_b = (_a = element.StyleTemplate) === null || _a === void 0 ? void 0 : _a.Styles) === null || _b === void 0 ? void 0 : _b.Tree) === null || _c === void 0 ? void 0 : _c.HoverStyle) === null || _d === void 0 ? void 0 : _d.FontColor,
                        },
                        {
                            selectors: selectedSelectors,
                            value: (_h = (_g = (_f = (_e = element.StyleTemplate) === null || _e === void 0 ? void 0 : _e.Styles) === null || _f === void 0 ? void 0 : _f.Tree) === null || _g === void 0 ? void 0 : _g.SelectedStyle) === null || _h === void 0 ? void 0 : _h.FontColor,
                        }
                    ];
                    if (UseCellTypeForeColor) {
                        notNormalColors.forEach(function (v) {
                            if (!v.value) {
                                return;
                            }
                            var newElement = $(svgElement.cloneNode(true));
                            Forguncy.ImageHelper.preHandleSvg(newElement, v.value);
                            var content = "background-image: url(".concat(Forguncy.MenuStyleUtils.GetBase64FromSvgElement(newElement[0]), ");");
                            _this.appendStyleToBody([{ content: content, selectors: v.selectors }], v.selectors.join(','));
                        });
                    }
                    Forguncy.ImageHelper.preHandleSvg($(svgElement), svgColor);
                    var content = "background-image: url(".concat(Forguncy.MenuStyleUtils.GetBase64FromSvgElement(svgElement), ");");
                    _this.appendStyleToBody([{ selectors: selectors, content: content }], selectors.join(','));
                });
            }
            else {
                var content = "background-image: url(".concat(src, ");");
                this.appendStyleToBody([{ selectors: selectors, content: content }], selectors.join(','));
            }
        };
        ForguncyTreeCellType.prototype.appendStyleToBody = function (items, uniqueKey) {
            var _this = this;
            var oldStyle = this.treeContainer.find('style');
            for (var i = 0; i < oldStyle.length; i++) {
                if (oldStyle[i].getAttribute('data-uniqueKey') === uniqueKey) {
                    $(oldStyle[i]).remove();
                }
            }
            var style = document.createElement('style');
            style.type = 'text/css';
            style.setAttribute('data-uniqueKey', uniqueKey);
            style.innerHTML = "\n            ".concat(items.map(function (v) {
                var selectors = v.selectors, content = v.content;
                return "\n                 ".concat(selectors.map(function (v) { return "[data-page-id=\"".concat(_this._pageID, "\"][id_temp=").concat(_this.ID, "] ").concat(v); }).join(","), "{\n                    ").concat(content, "\n                }\n                ").concat(selectors.map(function (v) { return "[data-page-id=\"".concat(_this._pageID, "\"]#").concat(_this.ID, " ").concat(v); }).join(","), "{\n                    ").concat(content, "\n                }");
            }).join(''), "\n");
            this.treeContainer.append(style);
        };
        ForguncyTreeCellType.prototype.getCellTypeForeColor = function (element) {
            var _a, _b, _c, _d, _e;
            return ((_a = element.StyleInfo) === null || _a === void 0 ? void 0 : _a.Foreground) || ((_e = (_d = (_c = (_b = element.StyleTemplate) === null || _b === void 0 ? void 0 : _b.Styles) === null || _c === void 0 ? void 0 : _c.Tree) === null || _d === void 0 ? void 0 : _d.NormalStyle) === null || _e === void 0 ? void 0 : _e.FontColor);
        };
        ForguncyTreeCellType.prototype.initOpenCloseIcon = function (element, fontSize) {
            var _a, _b, _c, _d, _e, _f;
            var cellTypeMetaData = element.CellType;
            var ExpandIconSrc = this.getImageUrl(cellTypeMetaData.ExpandIcon);
            var CloseIconSrc = this.getImageUrl(cellTypeMetaData.CloseIcon);
            var ExpandSvgColor = Forguncy.ConvertToCssColor(((_a = cellTypeMetaData.ExpandIcon) === null || _a === void 0 ? void 0 : _a.UseCellTypeForeColor)
                ? this.getCellTypeForeColor(element)
                : (_b = cellTypeMetaData.ExpandIcon) === null || _b === void 0 ? void 0 : _b.Color);
            var CloseSvgColor = Forguncy.ConvertToCssColor(((_c = cellTypeMetaData.CloseIcon) === null || _c === void 0 ? void 0 : _c.UseCellTypeForeColor)
                ? this.getCellTypeForeColor(element)
                : (_d = cellTypeMetaData.CloseIcon) === null || _d === void 0 ? void 0 : _d.Color);
            var defaultCloseUrl = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAJ9JREFUOBFjYBjUoKGhgQmE8TmSEZckSCM3N/cckPzXr19TgPx/2NSyYBOEif3//18XxsZF43UeLk3I4iheQPMvExcX13GQ4m/fvlkCKbgXkL0DNwDm53///hkwMjL+hdqiCaWvg2igl5iZmJguIIfJIPIC1KlwCuQlYBicBAkAw8Ac2d9wRUAGxV7Amw6AgXkZ2TaS2SBvgDDJGumqAQA1Hj0c2PUgTQAAAABJRU5ErkJggg==";
            var defaultExpandUrl = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAFxJREFUOBFjYBgFAx8CjMhOaGhoYELm42ID1f2DycENAGnm5uae8+/fPwNGRsa/MAXI9P///5mZmJgufP36NQVmCFE2IhuCzoa7ACQBcgW6Amx8mO3Y5EbFBiIEAEVwGAZkoAtuAAAAAElFTkSuQmCC";
            this.setIconStyle(CloseIconSrc, defaultCloseUrl, CloseSvgColor, ZtreeIconType.close, element, (_e = cellTypeMetaData.CloseIcon) === null || _e === void 0 ? void 0 : _e.UseCellTypeForeColor);
            this.setIconStyle(ExpandIconSrc, defaultExpandUrl, ExpandSvgColor, ZtreeIconType.open, element, (_f = cellTypeMetaData.ExpandIcon) === null || _f === void 0 ? void 0 : _f.UseCellTypeForeColor);
        };
        ForguncyTreeCellType.prototype.loadZTreeNodes = function (completeCallBack) {
            var _this = this;
            var nodeCallback = function (nodes) {
                var element = _this.CellElement;
                var cellTypeMetaData = element.CellType;
                $.fn.zTree.init($("#" + _this.treeID), _this.getTreeSettings(), nodes);
                if (cellTypeMetaData.DefaultExpandStyle === 0) {
                    var tree = $.fn.zTree.getZTreeObj(_this.treeID);
                    var nodes_1 = tree.transformToArray(tree.getNodes());
                    for (var i = 0; i < nodes_1.length; i++) {
                        var node = nodes_1[i];
                        tree.expandNode(node, undefined, undefined, false, true);
                    }
                }
                else if (cellTypeMetaData.DefaultExpandStyle === 1) {
                    var tree = $.fn.zTree.getZTreeObj(_this.treeID);
                    var level = cellTypeMetaData.SpecifyExpandedLevel - 1;
                    var nodes_2 = tree.transformToArray(tree.getNodes());
                    for (var i = 0; i < nodes_2.length; i++) {
                        var node = nodes_2[i];
                        if (node.level < level) {
                            tree.expandNode(node, undefined, undefined, false, true);
                        }
                    }
                }
                completeCallBack();
            };
            this.getTreeNodes(nodeCallback);
        };
        ForguncyTreeCellType.prototype.getDefaultValue = function () {
            var currentTreeActiveState = this.getTreeActiveState();
            if (currentTreeActiveState) {
                return {
                    Value: currentTreeActiveState
                };
            }
            return null;
        };
        ForguncyTreeCellType.prototype.getTreeActiveState = function () {
            var pageName = this.IsInMasterPage === true ? Forguncy.Page.getMasterPageName() : Forguncy.Page.getPageName();
            if (!ForguncyTreeCellType.treeStorage || !ForguncyTreeCellType.treeStorage[pageName] || !ForguncyTreeCellType.treeStorage[pageName][this.ID]) {
                return;
            }
            var currentTreeStorage = ForguncyTreeCellType.treeStorage[pageName][this.ID];
            return currentTreeStorage["active_treeNode"];
        };
        ForguncyTreeCellType.prototype.updateTreeActiveState = function () {
            if (!ForguncyTreeCellType.treeStorage) {
                ForguncyTreeCellType.treeStorage = {};
            }
            var pageName = this.IsInMasterPage === true ? Forguncy.ForguncyData.pageInfo.masterPageName : Forguncy.ForguncyData.pageInfo.pageName;
            if (!ForguncyTreeCellType.treeStorage[pageName]) {
                ForguncyTreeCellType.treeStorage[pageName] = {};
            }
            if (!ForguncyTreeCellType.treeStorage[pageName][this.ID]) {
                ForguncyTreeCellType.treeStorage[pageName][this.ID] = [];
            }
            ForguncyTreeCellType.treeStorage[pageName][this.ID]["active_treeNode"] = this.currentTreeValue;
        };
        ForguncyTreeCellType.prototype.onWindowResized = function () {
            _super.prototype.onWindowResized.call(this);
            this.updateSimpleBar();
            this.simpleBar && this.updateContainerForSimplebar();
        };
        ForguncyTreeCellType.prototype.createSimpleBar = function () {
            var queryStr = "#" + this.ID + "_div";
            var container = $(queryStr);
            if (Forguncy.Platform.isIpad() || Forguncy.Platform.isMobile()) {
                container.css("overflow", "auto");
                return;
            }
            if (container[0]) {
                this.simpleBar = new SimpleBar(container[0]);
                this.updateContainerForSimplebar();
            }
        };
        ForguncyTreeCellType.prototype.updateSimpleBar = function () {
            this.simpleBar && this.simpleBar.recalculate();
        };
        ForguncyTreeCellType.prototype.reload = function () {
            var _this = this;
            this.loadZTreeNodes(function () {
                _this.selectTreeNode(_this.currentTreeValue);
                _this.repaint();
            });
        };
        ForguncyTreeCellType.prototype.repaint = function () {
            var cell = Forguncy.ForguncyData.pageInfo.pageElementManager.cells.getCell(this.ID);
            if (cell) {
                cell.repaint();
            }
        };
        ForguncyTreeCellType.prototype.destroy = function () {
            _super.prototype.destroy.call(this);
            this.treeContainer = null;
        };
        ForguncyTreeCellType.treeStorage = {};
        ForguncyTreeCellType.StyleTemplateHelper = new ForguncyTreeCellTypeStyleTemplateHelper();
        return ForguncyTreeCellType;
    }(Forguncy.Plugin.CellTypeBaseWithSimplebar));
    Forguncy.ForguncyTreeCellType = ForguncyTreeCellType;
})(Forguncy || (Forguncy = {}));
Forguncy.Plugin.CellTypeHelper.registerCellType("Forguncy.CustomMenu.ForguncyTreeCellType, Forguncy.CustomMenu", Forguncy.ForguncyTreeCellType);
