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
    var ForguncyMenuCellTypeStyleTemplateHelper = (function (_super) {
        __extends(ForguncyMenuCellTypeStyleTemplateHelper, _super);
        function ForguncyMenuCellTypeStyleTemplateHelper() {
            var _this = _super.call(this) || this;
            _this.CellTypeString = "Forguncy.CustomMenu.ForguncyMenuCellType";
            _this.TemplateNameParts = [
                "LEVEL0",
                "LEVEL1",
                "LEVEL2",
                "LEVEL3",
            ];
            return _this;
        }
        ForguncyMenuCellTypeStyleTemplateHelper.prototype.MapPartsNameToDom = function (container) {
            this.Container = container;
            return {
                LEVEL0: container.find("ul[level=0]>li>a"),
                LEVEL1: container.find("ul[level=1]>li>a"),
                LEVEL2: container.find("ul[level=2]>li>a"),
                LEVEL3: container.find("ul[level=3]>li>a"),
            };
        };
        return ForguncyMenuCellTypeStyleTemplateHelper;
    }(Forguncy.CellTypeStyleTemplateBase));
    Forguncy.ForguncyMenuCellTypeStyleTemplateHelper = ForguncyMenuCellTypeStyleTemplateHelper;
    var ForguncyMenuCellType = (function (_super) {
        __extends(ForguncyMenuCellType, _super);
        function ForguncyMenuCellType() {
            var _this = _super !== null && _super.apply(this, arguments) || this;
            _this.menuItemMaxHeight = 40;
            _this.menuItemHeightGap = 6;
            _this.menuItemMaxFontSize = 20;
            _this.menuItemFontSizeGap = 3;
            _this.nofiticationPaddingLeftRight = 7;
            _this.nofiticationMarginRight = 3;
            _this.spanTextMarginLeftRight = 10;
            _this.horizontalHyperlinkPaddingLeft = 8;
            _this.verticalHyperlinkPaddingLeft = 15;
            _this.arrowWidthHeight = 16;
            _this.arrowMarginRight = 16;
            _this.menuContainerName = "menuContainer";
            _this._hiddenMenuItems = [];
            return _this;
        }
        Object.defineProperty(ForguncyMenuCellType.prototype, "orientation", {
            get: function () {
                var cellType = this.CellElement.CellType;
                return cellType.Orientation;
            },
            enumerable: false,
            configurable: true
        });
        Object.defineProperty(ForguncyMenuCellType.prototype, "canVerticallyStretch", {
            get: function () {
                var pageStretchInfo;
                var formularCalcContext = this.getFormulaCalcContext();
                if (formularCalcContext && Forguncy['PageManager'] && Forguncy['PageManager'].getPageStretchInfo) {
                    pageStretchInfo = Forguncy['PageManager'].getPageStretchInfo(formularCalcContext.PageID);
                }
                if (!pageStretchInfo) {
                    return false;
                }
                var currentStretchMode = pageStretchInfo.StretchMode;
                return currentStretchMode === 2
                    || currentStretchMode === 3
                    || currentStretchMode === 4;
            },
            enumerable: false,
            configurable: true
        });
        ForguncyMenuCellType.prototype.initMenuItemStyle = function (cellTypeMetaData, container) {
            if (cellTypeMetaData.Orientation === 0) {
                container = this.setFirstItemLevelStyle(cellTypeMetaData, container);
                if (cellTypeMetaData.MenuLevelsStyle && cellTypeMetaData.MenuLevelsStyle[0] && cellTypeMetaData.MenuLevelsStyle[0].Width) {
                    container.find("> ul > li").css("width", cellTypeMetaData.MenuLevelsStyle[0].Width + "px");
                }
                container.find("ul li").css("position", "relative");
                container.find("ul li ul").css("position", "absolute");
                container.find("li li ul").css("position", "absolute");
                container.find("li li ul").css("top", "0");
                container.find("ul li ul").css("z-index", "10000000");
            }
            container.find("ul").css("user-select", "none");
            container.find("li a").css("overflow", "hidden");
            if (cellTypeMetaData.DefaultExpandStyle === 0 && cellTypeMetaData.Orientation === 1
                && !cellTypeMetaData.IsSingleExpand) {
                container.find("> ul i").addClass("down");
            }
            else {
                container.find("> ul ul").css("display", "none");
            }
        };
        ForguncyMenuCellType.prototype.initMenuItems = function (itemsInfo, levelsStyle, levelIndex, aTag) {
            if (itemsInfo && itemsInfo.length > 0) {
                var itemsLength = itemsInfo.length;
                var ul = $("<ul level=" + levelIndex + "></ul>");
                var hasChildren = false;
                for (var i = 0; i < itemsLength; i++) {
                    var itemInfo = itemsInfo[i];
                    hasChildren = true;
                    var li = $("<li></li>");
                    var subMenusDom = this.initMenuItems(itemInfo.SubItems, levelsStyle, levelIndex + 1, aTag + ";index=" + i + ";level=" + (levelIndex + 1));
                    var a = this.createHyperlinkHtml(itemInfo, levelIndex, levelsStyle[levelIndex], aTag + ";index=" + i + "", !!subMenusDom);
                    li.append(a);
                    if (subMenusDom) {
                        li.append(subMenusDom);
                        var menuCellType = this.CellElement.CellType;
                        if (menuCellType.Orientation === 0) {
                            var width = void 0;
                            if (levelIndex + 1 < levelsStyle.length) {
                                width = levelsStyle[levelIndex + 1].Width;
                            }
                            this.showDropDownWhenHoverOnHorizontalLayout(li, width);
                        }
                    }
                    ul.append(li);
                }
                if (hasChildren === false) {
                    return null;
                }
                this.setMenuLevelStyle(ul, levelsStyle[levelIndex], levelIndex);
                return ul;
            }
            return null;
        };
        ForguncyMenuCellType.prototype.showDropDownWhenHoverOnHorizontalLayout = function (li, width) {
            var _this = this;
            li.hover(function () {
                var hyperlink = li.find("> a");
                var isExpand = $(hyperlink).attr("isExpand");
                if (isExpand === "false") {
                    $(hyperlink).find("i").addClass("down");
                    $(hyperlink).next().css("display", "block");
                    var current_liLeft = $(hyperlink).parent().offset().left;
                    var current_liWidth = $(hyperlink).parent().outerWidth(true);
                    var dropdown_ul = $(hyperlink).next();
                    var dropdown_ulLevel = parseInt(dropdown_ul.attr("level"));
                    var dropdown_ulAlign = dropdown_ul.attr("pos-align");
                    var dropdownWidth = _this.getDropDown_ulMaxWidth(dropdown_ul);
                    if (width) {
                        dropdownWidth = width;
                    }
                    else if (dropdown_ulLevel === 1 && dropdownWidth < current_liWidth) {
                        dropdownWidth = current_liWidth;
                    }
                    dropdown_ul.css("width", dropdownWidth + "px");
                    if (dropdown_ulLevel > 1) {
                        if (dropdown_ulAlign === "right") {
                            dropdown_ul.css("right", current_liWidth + "px");
                        }
                        else {
                            if (current_liLeft + current_liWidth + dropdownWidth < document.body.offsetWidth) {
                                dropdown_ul.css("left", current_liWidth + "px");
                            }
                            else {
                                dropdown_ul.css("right", current_liWidth + "px");
                                dropdown_ul.find("ul").attr("pos-align", "right");
                            }
                        }
                    }
                    $(hyperlink).attr("isExpand", true);
                }
            }, function () {
                var hyperlink = li.find("> a");
                var isExpand = $(hyperlink).attr("isExpand");
                if (isExpand === "true") {
                    $(hyperlink).next().css("display", "none");
                    $(hyperlink).find("i").removeClass("down");
                    $(hyperlink).attr("isExpand", false);
                }
            });
        };
        ForguncyMenuCellType.prototype.getDropDown_ulMaxWidth = function (dropdown_ul) {
            var allSubHyperlinks = dropdown_ul.find("> li > a");
            if (allSubHyperlinks.length > 0) {
                var iconMaxWidth = 0;
                var arroMaxwWidth = 0;
                var notifyMaxWidth = 0;
                var textMaxWidth = 0;
                for (var i = 0; i < allSubHyperlinks.length; i++) {
                    var subHyperlink = $(allSubHyperlinks[i]);
                    if (subHyperlink.find("*[elemType='icon']").length > 0) {
                        var iconWidth = subHyperlink.find("*[elemType='icon']").outerWidth(true);
                        if (iconWidth > iconMaxWidth) {
                            iconMaxWidth = iconWidth;
                        }
                    }
                    if (subHyperlink.find("i[elemType='arrow']").length > 0) {
                        var arrowWidth = subHyperlink.find("i[elemType='arrow']").outerWidth(true);
                        if (arrowWidth > arroMaxwWidth) {
                            arroMaxwWidth = arrowWidth;
                        }
                    }
                    if (subHyperlink.find("em[elemType='notification']").length > 0) {
                        var notifyWidth = subHyperlink.find("em[elemType='notification']").outerWidth(true);
                        if (notifyWidth > notifyMaxWidth) {
                            notifyMaxWidth = notifyWidth;
                        }
                    }
                    if (subHyperlink.find("span[elemType='text']").length > 0) {
                        var textWidth = subHyperlink.find("span[elemType='text']").outerWidth(true) + 1;
                        if (textWidth > textMaxWidth) {
                            textMaxWidth = textWidth;
                        }
                    }
                }
                return this.horizontalHyperlinkPaddingLeft + iconMaxWidth + arroMaxwWidth + notifyMaxWidth + textMaxWidth;
            }
        };
        ForguncyMenuCellType.prototype.setMenuLevelStyle = function (ul, style, levelIndex) {
            _super.prototype.setMenuLevelStyle.call(this, ul, style, levelIndex);
            if (style) {
                if (style.Bold) {
                    ul.find("> li > a").css("font-weight", "bold");
                }
                if (style.Height && style.Height > 0) {
                    var lineHeight = style.Height;
                    if (levelIndex === 0 && lineHeight > this.CellElement.Height) {
                        lineHeight = this.CellElement.Height;
                    }
                    ul.css("line-height", lineHeight + "px");
                }
                var menuSelector = "ul[level='".concat(levelIndex, "'] > li > a:hover");
                var cssSelector = "#".concat(this.ID, " ").concat(menuSelector, ", [id_temp=").concat(this.ID, "] ").concat(menuSelector);
                if (style.HoverForeColor) {
                    Forguncy.MenuStyleUtils.InsertForeColorRule(this.rootContainer, cssSelector, style.HoverForeColor);
                }
                if (style.HoverBackColor) {
                    Forguncy.MenuStyleUtils.InsertBackColorRule(this.rootContainer, cssSelector, style.HoverBackColor);
                }
            }
        };
        ForguncyMenuCellType.prototype.createNotification = function (badgeValue, target, isRunMethodCall) {
            if (isRunMethodCall === void 0) { isRunMethodCall = false; }
            if (badgeValue === null || badgeValue === undefined || badgeValue === "") {
                return;
            }
            var span = $("<em elemType='notification'></em>");
            span.css("background-color", "red");
            span.css("color", "white");
            span.css("padding", "3px " + this.nofiticationPaddingLeftRight + "px");
            span.css("border-radius", 10);
            span.css("font-size", 12);
            span.css("font-weight", 700);
            span.css("line-height", 1);
            span.css("text-align", "center");
            span.css("font-style", "normal");
            span.css("margin-right", this.nofiticationMarginRight);
            span.css("display", "block");
            span.css("float", "right");
            span.css("float", "right");
            if (!isRunMethodCall) {
                this.notifyNodes.push({ "dom": span, "formula": badgeValue });
            }
            else {
                span.text(badgeValue);
            }
            target.after(span);
            span.wrap($("<div>").css("flex-grow", 1));
        };
        ForguncyMenuCellType.prototype.createHyperlinkHtml = function (itemInfo, levelIndex, style, aTag, hasArrow) {
            var _this = this;
            var isExpand = itemInfo.Expand;
            var cellTypeMetaData = this.CellElement.CellType;
            if (cellTypeMetaData.Orientation === 0 || cellTypeMetaData.IsSingleExpand) {
                isExpand = false;
            }
            else {
                if (isExpand === undefined) {
                    if (cellTypeMetaData.DefaultExpandStyle === 0) {
                        isExpand = true;
                    }
                    else {
                        isExpand = false;
                    }
                }
            }
            this.setExpandToStorage(levelIndex, aTag, isExpand, true);
            itemInfo.aTag = aTag;
            var a = $("<a isExpand=" + isExpand + " aTag='" + aTag + "'></a>");
            if (itemInfo.IsDefaultItem) {
                this.setSelectToStorage(aTag, true);
            }
            var itemStyle = {};
            itemStyle.itemHeight = this.menuItemMaxHeight - levelIndex * this.menuItemHeightGap;
            itemStyle.itemFontSize = this.menuItemMaxFontSize - levelIndex * this.menuItemFontSizeGap;
            _super.prototype.setHyperLinkStyle.call(this, itemStyle, style, levelIndex);
            if (itemInfo.IconPath && itemInfo.IconPath !== "") {
                if (itemInfo.IsOldMenuPath) {
                    itemInfo.IsBuiltInIconPath = false;
                }
                var iconColor = itemInfo.IconColorUseItemColor ? "currentColor" : itemInfo.IconColor;
                a.append(this.createIconHtml(itemInfo.IconPath, iconColor, itemStyle.itemHeight, itemStyle.iconWidth, itemStyle.iconHeight, itemInfo.IsBuiltInIconPath, itemInfo.IsOldMenuPath));
            }
            var text = this.getApplicationResource(itemInfo.Text);
            if (text === undefined || text === null) {
                text = "";
            }
            a.append($("<span elemType='text' style='margin-left:" + this.spanTextMarginLeftRight + "px; margin-right:" + this.spanTextMarginLeftRight + "px'>" + this.getApplicationResource(text) + "</span>"));
            a.children().wrapAll($("<div>").addClass("fgc-plugin-menu-icontextwrapper"));
            if (itemInfo.Notification) {
                this.createNotification(itemInfo.Notification, a.find(".fgc-plugin-menu-icontextwrapper"));
            }
            if (hasArrow) {
                a.append(this.createArrowHtml());
            }
            a.css("height", itemStyle.itemHeight + "px");
            a.css("line-height", itemStyle.itemHeight + "px");
            a.css("font-size", itemStyle.itemFontSize + "px");
            a.css("cursor", "pointer");
            a.css("text-decoration", "none");
            if (cellTypeMetaData.Orientation === 0) {
                a.css("padding-left", this.horizontalHyperlinkPaddingLeft + "px");
            }
            else {
                a.css("padding-left", (levelIndex + 1) * this.verticalHyperlinkPaddingLeft + "px");
            }
            a.click(function () {
                var target = a;
                var aTag = target.attr("aTag");
                _this.setSelectItem(aTag);
                if (!_this.executeOnClickCommand(itemInfo)) {
                    _this.openCloseDropdownWhenClick(a);
                }
                return false;
            });
            return a;
        };
        ForguncyMenuCellType.prototype.createArrowHtml = function () {
            var _this = this;
            var i = $("<i elemType='arrow'></i>");
            var svg = $("\n<svg class=\"arrow\" width=\"64px\" height=\"64px\" viewBox=\"0 0 64 64\" version=\"1.1\" \nstyle=\"display:block; width: 100%; height: 100%;opacity:0.5\" \nxmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" >\n    <g class=\"arrowIcon\">\n        <polygon points=\"23.7562579 9 45.7140959 32.0000268 23.7562579 55.0000535 18.9046897 49.8969226 35.9750221 32.0000268 18.9046897 14.1031309\"></polygon>\n    </g>\n</svg>");
            i.addClass("fgc-plugin-menu-arrow");
            i.append(svg);
            i.css("width", this.arrowWidthHeight + "px");
            i.css("height", this.arrowWidthHeight + "px");
            i.css("margin-right", this.arrowMarginRight + "px");
            i.click(function () {
                var a = i.parent("a");
                _this.openCloseDropdownWhenClick(a);
                return false;
            });
            return i;
        };
        ForguncyMenuCellType.prototype.openCloseDropdownWhenClick = function (a) {
            var menuCellType = this.CellElement.CellType;
            if (menuCellType.Orientation === 0) {
                return;
            }
            var oldIsExpand = a.attr("isExpand") === "true";
            this.setExpandItems(a, !oldIsExpand);
            this.updateSimpleBar();
        };
        ForguncyMenuCellType.prototype.setExpandItems = function (a, isExpand) {
            for (var i = 0; i < a.length; i++) {
                var item = $(a[i]);
                var levelIndex = item.parents("ul").attr("level");
                var aTag = item.attr("aTag");
                this.setExpandToStorage(levelIndex, aTag, isExpand);
            }
            this.setExpandStyle(a, isExpand);
            var menuCellType = this.CellElement.CellType;
            if (menuCellType.Orientation === 1 && menuCellType.IsSingleExpand && isExpand) {
                this.collapseSiblingItems(a);
            }
        };
        ForguncyMenuCellType.prototype.collapseSiblingItems = function (a) {
            this.setExpandItems(a.parent().siblings().children("a[isExpand='true']"), false);
        };
        ForguncyMenuCellType.prototype.setExpandStyle = function (a, isExpand) {
            $(a).attr("isExpand", isExpand);
            if (isExpand) {
                $("i", a).addClass("down");
                a.next().show();
            }
            else {
                $("i", a).removeClass("down");
                a.next().hide();
            }
        };
        ForguncyMenuCellType.prototype.setSelectStyle = function (aTag) {
            $("#" + this.ID).find("a").removeClass("selected");
            var newSelected = $("#" + this.ID).find("a[aTag='" + aTag + "']");
            newSelected.addClass("selected");
            var cellTypeMetaData = this.CellElement.CellType;
            if (cellTypeMetaData.Orientation === 1) {
                var notExpandedParents = newSelected.parents("#" + this.ID + " ul").prev("a[isexpand=false]");
                this.setExpandItems(notExpandedParents, true);
            }
        };
        ForguncyMenuCellType.prototype.setExpandToStorage = function (levelIndex, aTag, isExpand, isDefaultValue) {
            Forguncy.MenuCellTypeBase.menuStorage.updateExpand(this.getPageName(), this.ID, levelIndex, aTag, isExpand, isDefaultValue);
        };
        ForguncyMenuCellType.prototype.setStyleAfterLoad = function () {
            var innerContainer = $("#".concat(this.ID));
            var outerContainer = this.getOuterContainer();
            if (this.orientation === 0) {
                outerContainer.css("overflow", "");
                if (this.canVerticallyStretch) {
                    innerContainer.css('height', '100%')
                        .find('>ul').css('height', '100%')
                        .find('>li').css('height', '100%')
                        .find('>a').css({
                        'height': '100%',
                        'display': 'flex',
                        'align-items': 'center'
                    });
                    innerContainer.find('>ul>li>a div[elemtype="icon"]').css('margin', '');
                    innerContainer.find('>ul>li>a span[elemtype="text"]').css('flex-grow', '1');
                }
            }
            else {
                this.createSimpleBar(outerContainer);
            }
            this.initNotifyNumber();
            this.applyStorage();
            this.getStyleTemplateHelper().ApplyTemplateStyle(this.CellElement.StyleTemplate, this.rootContainer);
        };
        ForguncyMenuCellType.prototype.applyStorage = function () {
            var _this = this;
            var pageName = this.getPageName();
            var expandStorage = Forguncy.MenuCellTypeBase.menuStorage.getExpandStorage(pageName, this.ID);
            if (expandStorage) {
                for (var levelIndex in expandStorage) {
                    for (var tag in expandStorage[levelIndex]) {
                        var a = $("#" + this.ID).find("a[aTag='" + tag + "']");
                        var isExpand = expandStorage[levelIndex][tag];
                        this.setExpandStyle(a, isExpand);
                    }
                }
            }
            var selectStorage = Forguncy.MenuCellTypeBase.menuStorage.getSelectStorage(pageName, this.ID);
            if (selectStorage) {
                this.setSelectStyle(selectStorage);
            }
            var scrollStorage = Forguncy.MenuCellTypeBase.menuStorage.getScrollStorage(pageName, this.ID);
            if (scrollStorage) {
                var onMenuScrolled_1 = function () {
                    $("#" + _this.ID).parent(".simplebar-content").scrollTop(scrollStorage);
                    Forguncy.Page.unbind(Forguncy.PageEvents.PageDefaultDataLoaded, onMenuScrolled_1);
                };
                Forguncy.Page.bind(Forguncy.PageEvents.PageDefaultDataLoaded, onMenuScrolled_1);
            }
        };
        ForguncyMenuCellType.prototype.setBackColor = function (value) {
            if (value) {
                _super.prototype.setBackColor.call(this, value);
                return;
            }
            var firstLevelBackColor;
            var secondLevelBackColor;
            var cellTypeMetaData = this.CellElement.CellType;
            if (cellTypeMetaData.MenuLevelsStyle) {
                if (cellTypeMetaData.MenuLevelsStyle.length > 0) {
                    firstLevelBackColor = cellTypeMetaData.MenuLevelsStyle[0].BackColor;
                    if (cellTypeMetaData.MenuLevelsStyle.length > 1) {
                        secondLevelBackColor = cellTypeMetaData.MenuLevelsStyle[1].BackColor;
                    }
                }
            }
            var innerContainer = $("#".concat(this.ID));
            var outerContainer = this.getOuterContainer();
            if (this.orientation === 0) {
                if (firstLevelBackColor) {
                    innerContainer.css("background", firstLevelBackColor);
                }
            }
            else {
                if (secondLevelBackColor) {
                    outerContainer.css("background", secondLevelBackColor);
                }
                else if (firstLevelBackColor) {
                    outerContainer.css("background", firstLevelBackColor);
                }
            }
        };
        ForguncyMenuCellType.prototype.getStyleTemplateHelper = function () {
            return ForguncyMenuCellType.StyleTemplateHelper;
        };
        ForguncyMenuCellType.prototype.createIconHtml = function (iconPath, iconColor, itemHeight, iconWidth, iconHeight, isBuiltIn, isOldPath) {
            var imgHtml = _super.prototype.createIconHtml.call(this, iconPath, iconColor, itemHeight, iconWidth, iconHeight, isBuiltIn, isOldPath);
            imgHtml.css("float", "left");
            imgHtml.css("line-height", 0);
            return imgHtml;
        };
        ForguncyMenuCellType.prototype.setForeColorToLevel = function (menuCell, levelIndex, foreColor) {
            menuCell.find("ul[level=".concat(levelIndex, "]>li>a")).css("color", foreColor);
            menuCell.find("ul[level=".concat(levelIndex, "]>li>a svg")).attr("fill", foreColor);
        };
        ForguncyMenuCellType.prototype.setFontStyle = function (styleInfo) {
            if (!styleInfo) {
                return;
            }
            if (styleInfo.FontSize) {
                delete styleInfo.FontSize;
            }
            if (styleInfo.Underline) {
                styleInfo.Underline = false;
            }
            _super.prototype.setFontStyle.call(this, styleInfo);
            var cellType = this.CellElement.CellType;
            var menuCell = $("#".concat(this.ID));
            if (styleInfo.Foreground) {
                for (var i = 0; i < cellType.MenuLevelsStyle.length; i++) {
                    var currentMenuLevelStyle = cellType.MenuLevelsStyle[i];
                    var usedForeColor = currentMenuLevelStyle.ForeColor
                        ? Forguncy.ConvertToCssColor(currentMenuLevelStyle.ForeColor)
                        : Forguncy.ConvertToCssColor(styleInfo.Foreground);
                    currentMenuLevelStyle.ForeColor = null;
                    this.setForeColorToLevel(menuCell, i, usedForeColor);
                }
            }
            else {
                for (var i = 0; i < cellType.MenuLevelsStyle.length; i++) {
                    var currentMenuLevelStyle = cellType.MenuLevelsStyle[i];
                    currentMenuLevelStyle.ForeColor = null;
                }
            }
        };
        ForguncyMenuCellType.prototype.onWindowResized = function () {
            _super.prototype.onWindowResized.call(this);
            this.updateSimpleBar();
            this.simpleBar && this.updateContainerForSimplebar();
        };
        ForguncyMenuCellType.prototype.getOuterContainer = function () {
            return $("#" + this.ID + "_div");
        };
        ForguncyMenuCellType.prototype.createSimpleBar = function (container) {
            var _this = this;
            if (Forguncy.Platform.isIpad() || Forguncy.Platform.isMobile()) {
                container.css("overflow", "auto");
                return;
            }
            this.simpleBar = new SimpleBar(container[0]);
            this.updateContainerForSimplebar();
            $("#" + this.ID).parent(".simplebar-content").unbind("scroll");
            $("#" + this.ID).parent(".simplebar-content").bind("scroll", function () {
                _this.cacheScrollTop($("#" + _this.ID).parent(".simplebar-content").scrollTop());
            });
        };
        ForguncyMenuCellType.prototype.updateSimpleBar = function () {
            this.simpleBar && this.simpleBar.recalculate();
        };
        ForguncyMenuCellType.prototype.cacheScrollTop = function (top) {
            Forguncy.MenuCellTypeBase.menuStorage.updateScrollTop(this.getPageName(), this.ID, top);
        };
        ForguncyMenuCellType.prototype.SetDataSourceByObjTree = function (dataSource, valueProperty, labelProperty, childrenProperty, iconProperty) {
            var parsedDataSource = typeof dataSource === "string" ? JSON.parse(dataSource) : __spreadArray([], dataSource, true);
            var treeData = this.buildTreeByDataSource(parsedDataSource, valueProperty, labelProperty !== null && labelProperty !== void 0 ? labelProperty : valueProperty, childrenProperty, iconProperty, null);
            var metaData = this.CellElement.CellType;
            var items = this.convertToMenuItems(treeData, metaData.DefaultExpandStyle == 0);
            this.setMenuItems(items);
        };
        ForguncyMenuCellType.prototype.SetDataSourceByIdPidTable = function (dataSource, valueProperty, labelProperty, parentValue, iconProperty) {
            var parsedDataSource = typeof dataSource === "string" ? JSON.parse(dataSource) : __spreadArray([], dataSource, true);
            var treeData = this.buildTree(parsedDataSource.map(function (i) { return (__assign(__assign({}, i), { icon: i[iconProperty], value: i[valueProperty], label: i[labelProperty !== null && labelProperty !== void 0 ? labelProperty : valueProperty], parentValue: i[parentValue] })); }));
            var metaData = this.CellElement.CellType;
            var items = this.convertToMenuItems(treeData, metaData.DefaultExpandStyle == 0);
            this.setMenuItems(items);
        };
        ForguncyMenuCellType.prototype.buildTreeByDataSource = function (dataSource, valueProperty, labelProperty, childrenProperty, iconProperty, parentValue) {
            var _this = this;
            if (!dataSource) {
                return [];
            }
            return dataSource.map(function (i) {
                var _a;
                return ({
                    value: i[valueProperty],
                    label: i[labelProperty],
                    icon: (_a = i[iconProperty]) === null || _a === void 0 ? void 0 : _a.toString(),
                    parentValue: parentValue,
                    children: _this.buildTreeByDataSource(i[childrenProperty], valueProperty, labelProperty, childrenProperty, iconProperty, i.value)
                });
            });
        };
        ForguncyMenuCellType.prototype.GetSelectPath = function (type) {
            var _a;
            var aTag = Forguncy.MenuCellTypeBase.menuStorage.getSelectedATag(this.getPageName(), this.ID);
            if (!aTag) {
                return {
                    PathArray: [],
                };
            }
            var flattenItems = this.flattenItems();
            var aTagItemsMap = new Map(flattenItems.map(function (item) { return [item.aTag, item]; }));
            var valueItemsMap = new Map(flattenItems.map(function (item) { return [item.Value, item]; }));
            var values = [];
            var currentATag = aTag;
            var propertyName = type === "valuePath" ? "Value" : "Text";
            while (true) {
                if (!aTagItemsMap.has(currentATag)) {
                    break;
                }
                var item = aTagItemsMap.get(currentATag);
                values.push(item[propertyName]);
                currentATag = (_a = valueItemsMap.get(item.ParentValue)) === null || _a === void 0 ? void 0 : _a.aTag;
            }
            return {
                PathArray: values.reverse()
            };
        };
        ForguncyMenuCellType.prototype.GetUnboundDataSourcesSelectPath = function () {
            return this.GetSelectPath("labelPath");
        };
        ForguncyMenuCellType.prototype.ReloadBindingItems = function () {
            if (this.CellElement.CellType.UseBinding) {
                this.refreshBindingMenu();
            }
        };
        ForguncyMenuCellType.prototype.isEmpty = function (obj) {
            return obj === null || obj === "" || obj === undefined;
        };
        ForguncyMenuCellType.prototype.SetBadge = function (itemValue, badgeValue) {
            var _this = this;
            var metaData = this.CellElement.CellType;
            var flattenItems = this.flattenItems();
            var matchedItemATags = new Set();
            var propertyName = metaData.UseBinding ? "Value" : "Text";
            for (var _i = 0, flattenItems_1 = flattenItems; _i < flattenItems_1.length; _i++) {
                var item = flattenItems_1[_i];
                if (item[propertyName] == itemValue) {
                    matchedItemATags.add(item.aTag);
                }
            }
            var isEmpty = this.isEmpty(badgeValue);
            matchedItemATags.forEach(function (aTag) {
                var el = $("a[atag='" + aTag + "']", _this.getContainer());
                var em = el.find("em[elemtype=\"notification\"]");
                if (em && em.length > 0) {
                    if (isEmpty) {
                        em.css("display", "none");
                    }
                    else {
                        em.css("display", "block");
                        em.text(badgeValue);
                    }
                }
                else {
                    _this.createNotification(badgeValue, el.find(".fgc-plugin-menu-icontextwrapper"), true);
                }
            });
        };
        ForguncyMenuCellType.prototype.HideItems = function (items) {
            var _this = this;
            if (!items || typeof items !== 'string') {
                return;
            }
            var needHideItemsSet = new Set(items.split(','));
            var needHideItemATags = this.flattenItems().filter(function (item) { return needHideItemsSet.has(_this.getApplicationResource(item.Text)); });
            needHideItemATags.forEach(function (item) {
                var el = $("a[atag='" + item.aTag + "']", _this.getContainer());
                var parent = el.parent();
                if (!$("li a", parent.parent()).length) {
                    parent.parent().parent().children("a").children("i").css("display", "none");
                }
                _this._hiddenMenuItems.push(el);
                parent.css("display", "none");
            });
            return;
        };
        ForguncyMenuCellType.prototype.ShowItems = function (items) {
            var _this = this;
            if (!items || typeof items !== 'string') {
                return;
            }
            var itemSet = new Set(items.split(','));
            var needDeleteIndexSet = new Set();
            this._hiddenMenuItems.forEach(function (el, index) {
                if (itemSet.has(el.find("span").text())) {
                    el.parent().css("display", _this.orientation === 0 ? "inline-block" : "block");
                    el.parent().parent().parent().children("a").children("i").css("display", "block");
                    needDeleteIndexSet.add(index);
                }
            });
            this._hiddenMenuItems = this._hiddenMenuItems.filter(function (v, i) { return !needDeleteIndexSet.has(i); });
        };
        ForguncyMenuCellType.prototype.HighlightMenu = function (item) {
            if (!item) {
                return;
            }
            var activeMenuTextVal = item;
            var container = $("#" + this.ID);
            var self = this;
            container.find("a").each(function () {
                var aText = $(this).find("span").text();
                if (aText === activeMenuTextVal) {
                    $(this).addClass("selected");
                    self.expandParent($(this));
                    var aTag = $(this).attr("aTag");
                    Forguncy.MenuCellTypeBase.menuStorage.updateSelect(self.getPageName(), self.ID, aTag, false);
                }
                else {
                    $(this).removeClass("selected");
                }
            });
        };
        ForguncyMenuCellType.prototype.expandParent = function (a) {
            if (this.orientation === 0) {
                return;
            }
            var parent = a.parent("li").parent("ul").parent("li").find(">a");
            if (parent.length > 0) {
                if (parent.attr("isExpand") === "false") {
                    this.setExpandItems(parent, true);
                }
                this.expandParent(parent);
            }
        };
        ForguncyMenuCellType.StyleTemplateHelper = new ForguncyMenuCellTypeStyleTemplateHelper();
        return ForguncyMenuCellType;
    }(Forguncy.MenuCellTypeBase));
    Forguncy.ForguncyMenuCellType = ForguncyMenuCellType;
})(Forguncy || (Forguncy = {}));
Forguncy.Plugin.CellTypeHelper.registerCellType("Forguncy.CustomMenu.ForguncyMenuCellType, Forguncy.CustomMenu", Forguncy.ForguncyMenuCellType);
