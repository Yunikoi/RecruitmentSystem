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
var Forguncy;
(function (Forguncy) {
    var ForguncyMobileMenuCellType = (function (_super) {
        __extends(ForguncyMobileMenuCellType, _super);
        function ForguncyMobileMenuCellType() {
            var _this = _super !== null && _super.apply(this, arguments) || this;
            _this.menuContainerName = "mobileMenuContainer";
            return _this;
        }
        ForguncyMobileMenuCellType.prototype.createContent = function () {
            var container = Forguncy.MenuCellTypeBase.prototype.createContent.call(this);
            container.css("z-index", "9999");
            return container;
        };
        ForguncyMobileMenuCellType.prototype.initMenuItemStyle = function (cellTypeMetaData, container) {
            container = this.setFirstItemLevelStyle(cellTypeMetaData, container);
            var rootLevelStyle = cellTypeMetaData.MenuLevelsStyle[0];
            if (rootLevelStyle.BackColor) {
                $(container).css("background-color", Forguncy.ConvertToCssColor(rootLevelStyle.BackColor));
            }
            $(container).find("ul li").css("position", "relative");
            $(container).find("> ul > li").css("vertical-align", "top");
            var rootLevelHeight = rootLevelStyle.Height;
            var childNodeWidth = document.documentElement.clientWidth / cellTypeMetaData.Items.length;
            $(container).find("ul li ul").css("min-width", childNodeWidth + "px");
            $(container).find("ul li ul").css("width", "auto");
            $(container).find("ul li ul").css("position", "absolute");
            $(container).find("ul li ul").css("bottom", rootLevelHeight + "px");
            $(container).find("ul li ul").css("z-index", "10000000");
            $(container).find("li a").css("overflow", "hidden");
            $(container).find("li a").css("text-overflow", "ellipsis");
            if ($(container).find("> ul > li").last().find("> ul").length > 0) {
                $(container).find("> ul > li > ul").last().css("right", "0px");
            }
            $(container).find("> ul ul").css("display", "none");
        };
        ForguncyMobileMenuCellType.prototype.initMenuItems = function (itemsInfo, levelsStyle, levelIndex) {
            if (itemsInfo != null && itemsInfo.length > 0) {
                var itemsLength = itemsInfo.length;
                var ul = $("<ul></ul>");
                var hasChildren = false;
                for (var i = 0; i < itemsLength; i++) {
                    var itemInfo = itemsInfo[i];
                    hasChildren = true;
                    var li = $("<li></li>");
                    var subMenusDom = this.initMenuItems(itemInfo.SubItems, levelsStyle, levelIndex + 1);
                    var a = this.createHyperlinkHtml(itemInfo, levelIndex, levelsStyle[levelIndex], subMenusDom != null);
                    li.append(a);
                    if (subMenusDom != null) {
                        li.append(subMenusDom);
                    }
                    ul.append(li);
                }
                if (hasChildren === false) {
                    return null;
                }
                this.setMenuLevelStyle(ul, levelsStyle[levelIndex], levelIndex);
                if (levelIndex == 0) {
                    ul.css("line-height", "inherit");
                    ul.css("font-size", "inherit");
                }
                return ul;
            }
            return null;
        };
        ForguncyMobileMenuCellType.prototype.setMenuLevelStyle = function (ul, style, levelIndex) {
            _super.prototype.setMenuLevelStyle.call(this, ul, style, levelIndex);
            var target = ul.find(">li");
            if (style) {
                if (style.BackColor) {
                    target.css("background", Forguncy.ConvertToCssColor(style.BackColor));
                }
            }
        };
        ForguncyMobileMenuCellType.prototype.createHyperlinkHtml = function (itemInfo, levelIndex, style, hasArrow) {
            var _this = this;
            var cellTypeMetaData = this.CellElement.CellType;
            var a = $("<a></a>");
            var itemStyle = {};
            Forguncy.MenuCellTypeBase.prototype.setHyperLinkStyle.call(this, itemStyle, style, levelIndex);
            var text = this.getApplicationResource(itemInfo.Text);
            a.append($("<span>" + text + "</span>"));
            if (hasArrow === true) {
                a.append(this.createArrowHtml(itemStyle.itemHeight, levelIndex, cellTypeMetaData.Orientation));
            }
            if (itemInfo.Notification) {
                var span = $("<span></span>");
                span.css("background-color", "red");
                span.css("color", "white");
                span.css("font-size", 12);
                span.css("font-weight", 700);
                span.css("text-align", "center");
                span.css("font-family", Forguncy.LocaleFonts.default);
                span.css("font-style", "normal");
                span.css("display", "none");
                span.css("position", "absolute");
                span.css("right", 5);
                span.css("top", 5);
                span.css("max-width", "calc(100% - 10px)");
                span.css("height", "16px");
                span.css("border-radius", "8px");
                span.css("text-overflow", "ellipsis");
                span.css("line-height", "1");
                span.css("overflow", "hidden");
                span.css("padding", "2px 4px");
                this.notifyNodes.push({ "dom": span, "formula": itemInfo.Notification });
                a.append(span);
            }
            a.css("height", itemStyle.itemHeight + "px");
            a.css("line-height", itemStyle.itemHeight + "px");
            a.css("font-size", itemStyle.itemFontSize + "px");
            a.css("display", "block");
            a.css("cursor", "pointer");
            a.css("text-decoration", "none");
            var self = this;
            a.click(function (event) {
                event.stopPropagation();
                event.preventDefault();
                var aLink = a;
                if (aLink.next().length > 0) {
                    if (aLink.hasClass("open")) {
                        aLink.next().css("display", "none");
                        aLink.removeClass("open");
                    }
                    else {
                        $(".mobileMenuContainer ul li a").removeClass("open");
                        aLink.parent().parent().find("ul").css("display", "none");
                        aLink.next().css("display", "block");
                        aLink.addClass("open");
                    }
                }
                if (_this.executeOnClickCommand(itemInfo)) {
                    $(".mobileMenuContainer ul li a").removeClass("open");
                    $(".mobileMenuContainer ul ul").css("display", "none");
                }
                return;
            });
            return a;
        };
        ForguncyMobileMenuCellType.prototype.createArrowHtml = function (itemHeight, levelIndex, orientation) {
            var i = $("<i></i>");
            i.addClass("mobilearrow");
            return i;
        };
        ForguncyMobileMenuCellType.prototype.setStyleAfterLoad = function () {
            this.initNotifyNumber();
            $("#" + this.ID).parent("div").width(0);
            $("#" + this.ID).parent("div").parent().width(0);
        };
        return ForguncyMobileMenuCellType;
    }(Forguncy.MenuCellTypeBase));
    Forguncy.ForguncyMobileMenuCellType = ForguncyMobileMenuCellType;
})(Forguncy || (Forguncy = {}));
Forguncy.Plugin.CellTypeHelper.registerCellType("Forguncy.CustomMenu.ForguncyMobileMenuCellType, Forguncy.CustomMenu", Forguncy.ForguncyMobileMenuCellType);
document.onclick = function () {
    $(".mobileMenuContainer ul li a").removeClass("open");
    $(".mobileMenuContainer ul ul").css("display", "none");
};
