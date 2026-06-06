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
var __assign = (this && this.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
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
var __rest = (this && this.__rest) || function (s, e) {
    var t = {};
    for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p) && e.indexOf(p) < 0)
        t[p] = s[p];
    if (s != null && typeof Object.getOwnPropertySymbols === "function")
        for (var i = 0, p = Object.getOwnPropertySymbols(s); i < p.length; i++) {
            if (e.indexOf(p[i]) < 0 && Object.prototype.propertyIsEnumerable.call(s, p[i]))
                t[p[i]] = s[p[i]];
        }
    return t;
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
    var MenuStorageOption = (function () {
        function MenuStorageOption() {
            this._data = {};
        }
        MenuStorageOption.prototype.ensureMenuStorage = function () {
            var propertyNames = [];
            for (var _i = 0; _i < arguments.length; _i++) {
                propertyNames[_i] = arguments[_i];
            }
            var obj = this._data;
            for (var i = 0; i < propertyNames.length; i++) {
                var name_1 = propertyNames[i];
                if (!obj[name_1]) {
                    obj[name_1] = {};
                }
                obj = obj[name_1];
            }
        };
        MenuStorageOption.prototype.updateSelect = function (pageName, cellID, aTag, isDefaultValue) {
            this.ensureMenuStorage(pageName, cellID);
            if (isDefaultValue) {
                if (!this._data[pageName][cellID]["select_a_tag"]) {
                    this._data[pageName][cellID]["select_a_tag"] = aTag;
                }
            }
            else {
                this._data[pageName][cellID]["select_a_tag"] = aTag;
            }
        };
        MenuStorageOption.prototype.getSelectedATag = function (pageName, cellID) {
            this.ensureMenuStorage(pageName, cellID);
            return this._data[pageName][cellID]["select_a_tag"];
        };
        MenuStorageOption.prototype.updateExpand = function (pageName, cellID, levelIndex, aTag, isExpand, isDefaultValue) {
            this.ensureMenuStorage(pageName, cellID, "expand_tag", levelIndex);
            if (isDefaultValue) {
                if (this._data[pageName][cellID]["expand_tag"][levelIndex][aTag] === null ||
                    this._data[pageName][cellID]["expand_tag"][levelIndex][aTag] === undefined) {
                    this._data[pageName][cellID]["expand_tag"][levelIndex][aTag] = isExpand;
                }
            }
            else {
                this._data[pageName][cellID]["expand_tag"][levelIndex][aTag] = isExpand;
            }
        };
        MenuStorageOption.prototype.updateScrollTop = function (pageName, cellID, top) {
            this.ensureMenuStorage(pageName, cellID);
            this._data[pageName][cellID]["scrollTop"] = top;
        };
        MenuStorageOption.prototype.getMenuStorage = function (pageName, cellID) {
            var _a;
            return (_a = this._data[pageName]) === null || _a === void 0 ? void 0 : _a[cellID];
        };
        MenuStorageOption.prototype.getSelectStorage = function (pageName, cellID) {
            var _a;
            return (_a = this.getMenuStorage(pageName, cellID)) === null || _a === void 0 ? void 0 : _a["select_a_tag"];
        };
        MenuStorageOption.prototype.getExpandStorage = function (pageName, cellID) {
            var _a;
            return (_a = this.getMenuStorage(pageName, cellID)) === null || _a === void 0 ? void 0 : _a["expand_tag"];
        };
        MenuStorageOption.prototype.getScrollStorage = function (pageName, cellID) {
            var _a;
            return (_a = this.getMenuStorage(pageName, cellID)) === null || _a === void 0 ? void 0 : _a["scrollTop"];
        };
        return MenuStorageOption;
    }());
    var MenuCellTypeBase = (function (_super) {
        __extends(MenuCellTypeBase, _super);
        function MenuCellTypeBase() {
            var _this = _super !== null && _super.apply(this, arguments) || this;
            _this.pageNavigatedBind = _this.pageNavigated.bind(_this);
            _this.notifyNodes = [];
            return _this;
        }
        MenuCellTypeBase.prototype.pageNavigated = function (arg1, arg2) {
            this.highlight(arg2.pageName);
        };
        MenuCellTypeBase.prototype.highlight = function (pageName) {
            var cellTypeMetaData = this.CellElement.CellType;
            var items = cellTypeMetaData.Items;
            var needHighlightItems = [];
            this.getNeedHighlightItems(items, pageName, needHighlightItems);
            if (!needHighlightItems || needHighlightItems.length === 0) {
                return;
            }
            if (this.isAnyItemSelected(needHighlightItems)) {
                return;
            }
            var needSelectItem = needHighlightItems[0];
            this.setSelectItem(needSelectItem.aTag);
        };
        MenuCellTypeBase.prototype.setSelectItem = function (aTag) {
            var selectedItem = MenuCellTypeBase.menuStorage.getSelectStorage(this.getPageName(), this.ID);
            if (selectedItem === aTag) {
                return;
            }
            this.setSelectToStorage(aTag);
            this.setSelectStyle(aTag);
        };
        MenuCellTypeBase.prototype.setSelectStyle = function (aTag) {
        };
        MenuCellTypeBase.prototype.getNeedHighlightItems = function (items, pageName, result) {
            if (!items) {
                return;
            }
            for (var i = 0; i < items.length; i++) {
                var item = items[i];
                if (this.hasCommandNavigateToPage(item.CommandList, pageName)) {
                    result.push(item);
                }
                this.getNeedHighlightItems(item.SubItems, pageName, result);
            }
        };
        MenuCellTypeBase.prototype.hasCommandNavigateToPage = function (commandList, pageName) {
            var _a;
            if (commandList && commandList.length > 0) {
                for (var i = 0; i < commandList.length; i++) {
                    var command = commandList[i];
                    if (command && !command["Disabled"] && command["$type"] && command["$type"].indexOf("NavigateCommand") !== -1 && command.PageName === pageName) {
                        return true;
                    }
                    if (command && !command["Disabled"] && command["$type"] && command["$type"].indexOf("ConditionCommand") !== -1) {
                        var conditionPairs = command["ConditionAndCommandPairList"];
                        if (conditionPairs && conditionPairs.length) {
                            for (var i_1 = 0; i_1 < conditionPairs.length; i_1++) {
                                var commands = (_a = conditionPairs[i_1]) === null || _a === void 0 ? void 0 : _a.CommandList;
                                if (commands) {
                                    if (this.hasCommandNavigateToPage(commands, pageName)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return false;
        };
        MenuCellTypeBase.prototype.isAnyItemSelected = function (items) {
            var menuStoragePageName = this.getPageName();
            var currentHighlightItem = MenuCellTypeBase.menuStorage.getSelectStorage(menuStoragePageName, this.ID);
            if (!currentHighlightItem) {
                return false;
            }
            for (var i = 0; i < items.length; i++) {
                var aTag = items[i].aTag;
                if (aTag === currentHighlightItem) {
                    return true;
                }
            }
            return false;
        };
        MenuCellTypeBase.prototype.setSelectToStorage = function (aTag, isDefaultValue) {
            MenuCellTypeBase.menuStorage.updateSelect(this.getPageName(), this.ID, aTag, isDefaultValue);
        };
        MenuCellTypeBase.prototype.createContent = function () {
            Forguncy.Page.bind(Forguncy.PageEvents.PageDefaultDataLoaded, this.pageNavigatedBind, "*");
            var element = this.CellElement;
            var cellTypeMetaData = element.CellType;
            this.adjustMenuStyleBasedOnTemplate(element);
            var horizontalClassName = cellTypeMetaData.Orientation === 0 ? "horizontal" : "";
            var container = $("<div id='" + this.ID + "' class=\"" + this.menuContainerName + " " + horizontalClassName + "\"/>");
            this.rootContainer = container;
            if (!cellTypeMetaData.UseBinding) {
                this.removeNoPermissionItems(cellTypeMetaData);
                cellTypeMetaData.Items = this.fillValueAndParentValueOfItems(cellTypeMetaData.Items);
                this.createMenu(cellTypeMetaData, container);
            }
            return container;
        };
        MenuCellTypeBase.prototype.createMenu = function (cellTypeMetaData, container) {
            this.createMenuDom(cellTypeMetaData, container);
            this.initMenuItemStyle(cellTypeMetaData, container);
        };
        MenuCellTypeBase.prototype.initNotifyNumber = function () {
            var notices = this.notifyNodes;
            var _loop_1 = function (i) {
                var item = notices[i];
                var dom = item.dom;
                var formula = item.formula;
                this_1.onFormulaResultChanged(formula, function (result) {
                    if (result) {
                        dom.text(result);
                        dom.css("display", "block");
                    }
                    else {
                        dom.css("display", "none");
                    }
                });
            };
            var this_1 = this;
            for (var i = 0; i < notices.length; i++) {
                _loop_1(i);
            }
        };
        MenuCellTypeBase.prototype.destroy = function () {
            Forguncy.Page.unbind(Forguncy.PageEvents.PageDefaultDataLoaded, this.pageNavigatedBind, "*");
        };
        MenuCellTypeBase.prototype.removeNoPermissionItems = function (cellTypeMetaData) {
            var menuPermission = this.getUIPermission(1);
            if (!menuPermission) {
                return;
            }
            var itemsPermissions = menuPermission.Children;
            this.checkMenuPermissions(cellTypeMetaData.Items, itemsPermissions);
        };
        MenuCellTypeBase.prototype.fillValueAndParentValueOfItems = function (menuItems, parentValue) {
            var _this = this;
            if (parentValue === void 0) { parentValue = null; }
            return menuItems.map(function (item) {
                var _a;
                return __assign(__assign({}, item), { Value: item.Text, ParentValue: parentValue, SubItems: ((_a = item.SubItems) === null || _a === void 0 ? void 0 : _a.length) ? _this.fillValueAndParentValueOfItems(item.SubItems, item.Value) : [] });
            });
        };
        MenuCellTypeBase.prototype.checkMenuPermissions = function (items, permissions) {
            var _a;
            if (!permissions) {
                return;
            }
            var count = items.length - 1;
            for (var i = 0; i <= count; i++) {
                var menuItem = items[i];
                var permission = this.findAndRemovePermission(menuItem, permissions);
                if (permission) {
                    menuItem.CanVisitRoleList = permission.AllowRoles;
                    menuItem.CanVisitPermissionGroups = permission.AllowPermissionGroups;
                }
                if (this.checkMenuAuthority(menuItem) === false) {
                    items.splice(i, 1);
                    i--;
                    count--;
                }
                else if (menuItem.SubItems) {
                    this.checkMenuPermissions(menuItem.SubItems, (_a = permission === null || permission === void 0 ? void 0 : permission.Children) !== null && _a !== void 0 ? _a : []);
                }
            }
        };
        MenuCellTypeBase.prototype.findAndRemovePermission = function (menuItem, permissions) {
            var index = permissions.findIndex(function (p) { return p.Name == menuItem.Text; });
            if (index !== -1) {
                var removedItem = permissions.splice(index, 1)[0];
                return removedItem;
            }
            return null;
        };
        MenuCellTypeBase.prototype.createMenuDom = function (cellTypeMetaData, container) {
            var items = cellTypeMetaData.Items;
            var levelsStyle = cellTypeMetaData.MenuLevelsStyle;
            this.notifyNodes = [];
            var ul = this.initMenuItems(items, levelsStyle, 0, "level=0");
            if (ul !== null) {
                container.append(ul);
            }
        };
        MenuCellTypeBase.prototype.initMenuItemStyle = function (cellTypeMetaData, container) {
        };
        MenuCellTypeBase.prototype.initMenuItems = function (itemsInfo, levelsStyle, levelIndex, aTag) {
            return null;
        };
        MenuCellTypeBase.prototype.buildTree = function (items) {
            if (Forguncy.TreeHelper.hasCircularReference(items, "value", "parentValue")) {
                return [];
            }
            return Forguncy.TreeHelper.buildTree(items, "value", "parentValue");
        };
        MenuCellTypeBase.prototype.flattenItems = function (tree) {
            if (tree === void 0) { tree = this.CellElement.CellType.Items; }
            return Forguncy.TreeHelper.flattenTree(tree, "SubItems");
        };
        MenuCellTypeBase.prototype.convertToMenuItems = function (items, expand, levelIndex) {
            var _this = this;
            if (expand === void 0) { expand = true; }
            if (levelIndex === void 0) { levelIndex = 0; }
            return items.map(function (item) {
                var _a;
                var label = item.label, value = item.value, parentValue = item.parentValue, children = item.children, customColumns = __rest(item, ["label", "value", "parentValue", "children"]);
                return {
                    CanVisitRoleList: [],
                    CanVisitPermissionGroups: [],
                    IsBuiltInIconPath: false,
                    IsBuiltInSelectedIconPath: false,
                    IsDefaultItem: false,
                    SubItems: _this.convertToMenuItems(children, expand, levelIndex + 1),
                    Text: label,
                    Value: value,
                    ParentValue: parentValue,
                    aTag: "level=" + levelIndex,
                    IconPath: (_a = item.icon) === null || _a === void 0 ? void 0 : _a.toString(),
                    SelectedIconPath: null,
                    Expand: expand,
                    Notification: false,
                    CustomColumns: customColumns
                };
            });
        };
        MenuCellTypeBase.prototype.getMenuItemsByDataSource = function (metaData) {
            return __awaiter(this, void 0, void 0, function () {
                var dataSource, treeData;
                return __generator(this, function (_a) {
                    switch (_a.label) {
                        case 0: return [4, DataSourceUtils.getDataSourceByModel(this, metaData.BindingOptions)];
                        case 1:
                            dataSource = _a.sent();
                            treeData = this.buildTree(dataSource);
                            return [2, this.convertToMenuItems(treeData, metaData.DefaultExpandStyle == 0)];
                    }
                });
            });
        };
        MenuCellTypeBase.prototype.setHyperLinkStyle = function (itemStyle, style, levelIndex) {
            if (style) {
                if (style.FontSize > 0) {
                    itemStyle.itemFontSize = style.FontSize;
                    itemStyle.itemHeight = style.FontSize * 2;
                }
                if (style.Height > 0) {
                    itemStyle.itemHeight = style.Height;
                }
            }
            itemStyle.itemHeight = Math.max(itemStyle.itemHeight, 20);
            var height = this.CellElement.Height || this.CellElement.FullHeight;
            if (levelIndex === 0 && itemStyle.itemHeight > height) {
                itemStyle.itemHeight = height;
            }
            itemStyle.iconWidth = itemStyle.itemHeight - 12;
            itemStyle.iconHeight = itemStyle.itemHeight;
            if (style) {
                if (style.IconWidth > 0) {
                    itemStyle.iconWidth = style.IconWidth;
                }
                if (style.IconHeight > 0) {
                    itemStyle.iconHeight = style.IconHeight;
                }
            }
            itemStyle.iconHeight = itemStyle.iconHeight > itemStyle.itemHeight ? itemStyle.itemHeight : itemStyle.iconHeight;
        };
        MenuCellTypeBase.prototype.checkMenuAuthority = function (itemInfo) {
            var permissionMode = this.getPermissionMode();
            if (permissionMode == Forguncy.PermissionMode.Role) {
                return this.checkRoleAuthority(itemInfo.CanVisitRoleList);
            }
            if (permissionMode == Forguncy.PermissionMode.PermissionGroup) {
                return this.checkPermissionGroupsAuthority(itemInfo.CanVisitPermissionGroups);
            }
            return false;
        };
        MenuCellTypeBase.prototype.setMenuLevelStyle = function (ul, style, levelIndex) {
            var target = ul.find(">li>a");
            if (style) {
                if (style.BackColor) {
                    target.css("background", Forguncy.ConvertToCssColor(style.BackColor));
                    target.css("background-origin", "border-box");
                }
                if (style.ForeColor) {
                    target.css("color", Forguncy.ConvertToCssColor(style.ForeColor));
                    $(".arrow", target).attr("fill", Forguncy.ConvertToCssColor(style.ForeColor));
                }
                if (style.FontFamily) {
                    target.css("font-family", "'" + style.FontFamily + "'");
                }
                if (style.FontSize && style.FontSize > 0 && style.FontSize > style.Height) {
                    target.css("line-height", "2");
                    target.css("font-size", style.FontSize + "px");
                }
                var menuSelector = "ul[level='".concat(levelIndex, "'] > li > a.selected");
                var cssSelector = "#".concat(this.ID, " ").concat(menuSelector, ", [id_temp=").concat(this.ID, "] ").concat(menuSelector);
                if (style.SelectedBackColor) {
                    Forguncy.MenuStyleUtils.InsertBackColorRule(this.rootContainer, cssSelector, Forguncy.ConvertToCssColor(style.SelectedBackColor));
                }
                if (style.SelectedForeColor) {
                    var arrawSelector = "ul[level = '".concat(levelIndex, "'] > li > a.selected g.arrowIcon");
                    var arrawCssSelector = "#".concat(this.ID, " ").concat(arrawSelector, ", [id_temp=").concat(this.ID, "] ").concat(arrawSelector);
                    Forguncy.MenuStyleUtils.InsertForeColorRule(this.rootContainer, cssSelector, Forguncy.ConvertToCssColor(style.SelectedForeColor));
                    Forguncy.MenuStyleUtils.InsertArrowFillColorRule(this.rootContainer, arrawCssSelector, Forguncy.ConvertToCssColor(style.SelectedForeColor));
                }
            }
        };
        MenuCellTypeBase.prototype.setFirstItemLevelStyle = function (cellTypeMetaData, container) {
            container.find("> ul > li").css("display", "inline-block");
            container.find("> ul > li").css("width", 100 / cellTypeMetaData.Items.length + "%");
            return container;
        };
        MenuCellTypeBase.prototype.adjustMenuStyleBasedOnTemplate = function (element) {
            var cellType = element.CellType;
            if (this.CellElement.StyleInfo && this.CellElement.StyleInfo.Foreground) {
                for (var i = 0; i < cellType.MenuLevelsStyle.length; i++) {
                    var currentMenuLevelStyle = cellType.MenuLevelsStyle[i];
                    if (!currentMenuLevelStyle.ForeColor) {
                        currentMenuLevelStyle.ForeColor = this.CellElement.StyleInfo.Foreground;
                    }
                }
            }
            if (!element.StyleTemplate) {
                return;
            }
            if (!cellType.TemplateKey) {
                return null;
            }
            var styles = element.StyleTemplate.Styles;
            var cellParts = Object.keys(styles);
            for (var i = 0; i < cellParts.length; i++) {
                var partName = cellParts[i];
                var partStyle = styles[partName];
                this.mergeMenuItemStyle(partStyle, cellType.MenuLevelsStyle[i]);
            }
        };
        MenuCellTypeBase.prototype.mergeMenuItemStyle = function (templatePartStyle, originalStyle) {
            if (!originalStyle) {
                return;
            }
            for (var key in templatePartStyle) {
                var value = templatePartStyle[key];
                var style = value;
                if (key === "NormalStyle") {
                    if (style.Background && !originalStyle.BackColor) {
                        originalStyle.BackColor = Forguncy.ConvertToCssColor(style.Background);
                    }
                    if (style.FontColor && !originalStyle.ForeColor) {
                        originalStyle.ForeColor = Forguncy.ConvertToCssColor(style.FontColor);
                    }
                }
                else if (key === "HoverStyle") {
                    if (style.Background && !originalStyle.HoverBackColor) {
                        originalStyle.HoverBackColor = Forguncy.ConvertToCssColor(style.Background);
                    }
                    if (style.FontColor && !originalStyle.HoverForeColor) {
                        originalStyle.HoverForeColor = Forguncy.ConvertToCssColor(style.FontColor);
                    }
                }
                else if (key === "SelectedStyle") {
                    if (style.Background && !originalStyle.SelectedBackColor) {
                        originalStyle.SelectedBackColor = Forguncy.ConvertToCssColor(style.Background);
                    }
                    if (style.FontColor && !originalStyle.SelectedForeColor) {
                        originalStyle.SelectedForeColor = Forguncy.ConvertToCssColor(style.FontColor);
                    }
                }
            }
        };
        MenuCellTypeBase.prototype.getPageName = function () {
            return this.IsInMasterPage === true ? Forguncy.Page.getMasterPageName() : Forguncy.Page.getPageName();
        };
        MenuCellTypeBase.prototype.createIconHtml = function (iconPath, iconColor, itemHeight, iconWidth, iconHeight, isBuiltIn, isOldPath) {
            var imgHtml = $("<div elemType='icon'></div>");
            imgHtml.css("width", iconWidth + "px");
            imgHtml.css("height", iconHeight + "px");
            imgHtml.css("background-size", "contain");
            imgHtml.css("background-position", "center");
            imgHtml.css("background-repeat", "no-repeat");
            imgHtml.css("margin", "auto");
            imgHtml.css("line-height", "normal");
            if ((iconPath === null || iconPath === void 0 ? void 0 : iconPath.toLowerCase().startsWith("http://")) || (iconPath === null || iconPath === void 0 ? void 0 : iconPath.toLowerCase().startsWith("https://"))) {
                imgHtml.css("background-image", "url(\"" + iconPath + "\")");
            }
            else if (isOldPath && !isBuiltIn) {
                imgHtml.css("background-image", "url(\"" + Forguncy.Helper.SpecialPath.getUploadFileFolderPathInDesigner() + "ForguncyCustomMenu/IconImages/" + encodeURIComponent(iconPath) + "\")");
            }
            else if (iconPath) {
                var src = void 0;
                if (isBuiltIn) {
                    src = Forguncy.Helper.SpecialPath.getBuiltInImageFolderPath() + iconPath;
                }
                else if (Forguncy.Common.isForguncyFile(iconPath)) {
                    src = Forguncy.Helper.SpecialPath.getBaseUrl() + "Upload/" + iconPath;
                }
                else {
                    src = Forguncy.Helper.SpecialPath.getImageEditorUploadImageFolderPath() + encodeURIComponent(iconPath);
                }
                if (this.IsSvg(iconPath)) {
                    Forguncy.ImageHelper.requestSvg(src, function (svgElement) {
                        var svg = $(svgElement);
                        Forguncy.ImageHelper.preHandleSvg(svg, iconColor);
                        if (!iconColor) {
                            svg.attr("fill", "currentColor");
                        }
                        imgHtml.append(svg);
                    });
                }
                else {
                    imgHtml.css("background-image", "url(\"" + src + "\")");
                }
            }
            return imgHtml;
        };
        MenuCellTypeBase.prototype.IsSvg = function (name) {
            return name && name.length > 4 &&
                (name.substr(name.length - 4, 4).toLowerCase() === ".svg" ||
                    name.toLowerCase().indexOf(".svg?v=") > 0);
        };
        MenuCellTypeBase.prototype.executeOnClickCommand = function (itemInfo) {
            var _a;
            var _b, _c, _d, _e;
            var cellTypeMetaData = this.CellElement.CellType;
            var commands = cellTypeMetaData.ClickCommand;
            if (!commands) {
                if (!((_b = itemInfo.CommandList) === null || _b === void 0 ? void 0 : _b.length)) {
                    return false;
                }
                this.executeCommand(itemInfo.CommandList);
                return true;
            }
            var _f = commands.ParamProperties, value = _f.value, label = _f.label, parentId = _f.parentId, customColumns = __rest(_f, ["value", "label", "parentId"]);
            var initParams = (_a = {},
                _a[value] = itemInfo.Value,
                _a[label] = (_c = this.getApplicationResource(itemInfo.Text)) !== null && _c !== void 0 ? _c : itemInfo.Text,
                _a[parentId] = itemInfo.ParentValue,
                _a);
            for (var initParamsKey in customColumns) {
                initParams[initParamsKey] = itemInfo.CustomColumns[initParamsKey];
            }
            var newCommand = __assign({}, commands);
            newCommand.Commands = __spreadArray(__spreadArray([], ((_d = itemInfo.CommandList) !== null && _d !== void 0 ? _d : []), true), ((_e = commands.Commands) !== null && _e !== void 0 ? _e : []), true);
            this.executeCustomCommandObject(newCommand, initParams, "click");
            return true;
        };
        MenuCellTypeBase.prototype.onPageLoaded = function () {
            this.initializeProperties();
        };
        MenuCellTypeBase.prototype.initializeProperties = function () {
            var _this = this;
            var metadata = this.CellElement.CellType;
            if (!metadata.UseBinding) {
                this.setStyleAfterLoad();
                metadata.Items = this.fillValueAndParentValueOfItems(metadata.Items);
                return;
            }
            if (metadata.BindingOptions) {
                this.refreshBindingMenu();
                this.onBindingDataSourceDependenceCellValueChanged(metadata.BindingOptions, function () {
                    _this.refreshBindingMenu();
                });
            }
        };
        MenuCellTypeBase.prototype.refreshBindingMenu = function () {
            return __awaiter(this, void 0, void 0, function () {
                var metadata, items;
                return __generator(this, function (_a) {
                    switch (_a.label) {
                        case 0:
                            metadata = this.CellElement.CellType;
                            return [4, this.getMenuItemsByDataSource(metadata)];
                        case 1:
                            items = _a.sent();
                            this.setMenuItems(items);
                            return [2];
                    }
                });
            });
        };
        MenuCellTypeBase.prototype.setMenuItems = function (items) {
            this.adjustMenuStyleBasedOnTemplate(this.CellElement);
            this.rootContainer.empty();
            this.CellElement.CellType.Items = items;
            this.createMenu(this.CellElement.CellType, this.rootContainer);
            this.setStyleAfterLoad();
            this.setFontStyle(this.CellElement.StyleInfo);
        };
        MenuCellTypeBase.prototype.setStyleAfterLoad = function () {
        };
        MenuCellTypeBase.menuStorage = new MenuStorageOption();
        return MenuCellTypeBase;
    }(Forguncy.Plugin.CellTypeBaseWithSimplebar));
    Forguncy.MenuCellTypeBase = MenuCellTypeBase;
    var DataSourceUtils = (function () {
        function DataSourceUtils() {
        }
        DataSourceUtils.getDataSourceByModel = function (cellType, bindingDataSourceModel, options) {
            if (options === void 0) { options = null; }
            return new Promise(function (resolve) { return cellType.getBindingDataSourceValue(bindingDataSourceModel, options, resolve); });
        };
        return DataSourceUtils;
    }());
    Forguncy.DataSourceUtils = DataSourceUtils;
    var CellHelper = (function () {
        function CellHelper() {
        }
        CellHelper.setValueToCell = function (context, cellLocationFormula, value, initParams) {
            if (!cellLocationFormula) {
                return;
            }
            var cellLocation = Forguncy.Helper.getCellLocation(cellLocationFormula, context);
            var cell = Forguncy.Page.getCellByLocation(cellLocation);
            if (cell) {
                cell.setValue(value);
            }
            else {
                initParams[cellLocationFormula] = value;
            }
        };
        return CellHelper;
    }());
    Forguncy.CellHelper = CellHelper;
})(Forguncy || (Forguncy = {}));
