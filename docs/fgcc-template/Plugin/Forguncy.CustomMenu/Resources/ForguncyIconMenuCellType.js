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
    var ForguncyIconMenuCellType = (function (_super) {
        __extends(ForguncyIconMenuCellType, _super);
        function ForguncyIconMenuCellType() {
            var _this = _super !== null && _super.apply(this, arguments) || this;
            _this.menuContainerName = "iconMenuContainer";
            return _this;
        }
        ForguncyIconMenuCellType.prototype.createContent = function () {
            var container = Forguncy.MenuCellTypeBase.prototype.createContent.call(this);
            container.css("z-index", "9999");
            return container;
        };
        ForguncyIconMenuCellType.prototype.initMenuItemStyle = function (cellTypeMetaData, container) {
            this.setFirstItemLevelStyle(cellTypeMetaData, container);
        };
        ForguncyIconMenuCellType.prototype.initMenuItems = function (itemsInfo, levelsStyle, levelIndex, aTag) {
            if (itemsInfo && itemsInfo.length > 0) {
                var itemsLength = itemsInfo.length;
                var ul = $("<ul level=" + levelIndex + "></ul>");
                for (var i = 0; i < itemsLength; i++) {
                    var itemInfo = itemsInfo[i];
                    var li = $("<li></li>");
                    var a = this.createHyperlinkHtml(itemInfo, aTag + ";index=" + i + "");
                    li.append(a);
                    ul.append(li);
                }
                this.setMenuLevelStyle(ul, levelsStyle[levelIndex], levelIndex);
                return ul;
            }
            return null;
        };
        ForguncyIconMenuCellType.prototype.createHyperlinkHtml = function (itemInfo, aTag) {
            var a = $("<a aTag='" + aTag + "'></a>");
            itemInfo.aTag = aTag;
            if (itemInfo.IsDefaultItem) {
                this.setSelectToStorage(aTag, true);
            }
            var iconWidth = 30;
            var iconHeight = 30;
            var iconRowHeight = 36;
            var icon, selectedIcon;
            if (itemInfo.IconPath) {
                var iconColor = itemInfo.IconColorUseItemColor ? "currentColor" : itemInfo.IconColor;
                icon = this.createIconHtml(itemInfo.IconPath, iconColor, iconRowHeight, iconWidth, iconHeight, itemInfo.IsBuiltInIconPath, itemInfo.IsOldMenuPath);
                selectedIcon = this.createIconHtml(itemInfo.IconPath, "currentColor", iconRowHeight, iconWidth, iconHeight, itemInfo.IsBuiltInIconPath, itemInfo.IsOldMenuPath);
            }
            else {
                icon = this.createIconHtml("", "", iconRowHeight, iconWidth, iconHeight, false, false);
            }
            if (itemInfo.SelectedIconPath) {
                var iconColor = itemInfo.SelectedIconColorUseItemColor ? "currentColor" : itemInfo.SelectedIconColor;
                selectedIcon = this.createIconHtml(itemInfo.SelectedIconPath, iconColor, iconRowHeight, iconWidth, iconHeight, itemInfo.IsBuiltInSelectedIconPath, itemInfo.IsOldMenuPath);
            }
            if (icon) {
                icon.attr("IconType", "Icon");
                a.append(icon);
            }
            if (selectedIcon) {
                selectedIcon.attr("IconType", "SelectedIcon");
                selectedIcon.hide();
                a.append(selectedIcon);
            }
            var text = this.getApplicationResource(itemInfo.Text);
            if (text === undefined || text === null) {
                text = "";
            }
            a.append($("<span class='menuText'>" + text + "</span>"));
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
                span.css("top", "2px");
                span.css("left", "55%");
                span.css("height", "20px");
                span.css("width", "20px");
                span.css("border-radius", "10px");
                span.css("text-overflow", "ellipsis");
                span.css("line-height", 1);
                span.css("overflow", "hidden");
                span.css("padding", "4px 2px");
                this.notifyNodes.push({ "dom": span, "formula": itemInfo.Notification });
                a.append(span);
            }
            a.css("display", "block");
            a.css("cursor", "pointer");
            a.css("text-decoration", "none");
            a.css("padding", "2px 0 5px 0");
            a.css("position", "relative");
            var self = this;
            a.click(function () {
                self.setSelectItem($(this).attr("aTag"));
                self.executeOnClickCommand(itemInfo);
                return false;
            });
            return a;
        };
        ForguncyIconMenuCellType.prototype.createIconHtml = function (iconPath, iconColor, itemHeight, iconWidth, iconHeight, isBuiltIn, isOldPath) {
            var imgHtml = _super.prototype.createIconHtml.call(this, iconPath, iconColor, itemHeight, iconWidth, iconHeight, isBuiltIn, isOldPath);
            if (isOldPath && !isBuiltIn) {
                var bgImage = imgHtml.css("background-image");
                if (bgImage) {
                    bgImage = bgImage.replace(Forguncy.Helper.SpecialPath.getUploadFileFolderPathInDesigner() + "ForguncyCustomMenu/IconImages/", Forguncy.Helper.SpecialPath.getImageEditorUploadImageFolderPath());
                    imgHtml.css("background-image", bgImage);
                }
            }
            return imgHtml;
        };
        ForguncyIconMenuCellType.prototype.setStyleAfterLoad = function () {
            var parentDIV = $("#" + this.ID).parent("div");
            parentDIV.width(0);
            parentDIV.parent().width(0);
            parentDIV.css("overflow", "");
            this.initNotifyNumber();
            var pageName = this.getPageName();
            var selectItemTag = Forguncy.MenuCellTypeBase.menuStorage.getSelectStorage(pageName, this.ID);
            if (selectItemTag) {
                this.setSelectStyle(selectItemTag);
            }
        };
        ForguncyIconMenuCellType.prototype.setSelectStyle = function (aTag) {
            var oldSelectedItem = $(".iconMenuContainer ul li a." + "selected");
            this.toggleActive(oldSelectedItem);
            var newSelectedItem = $("#" + this.ID + " a[aTag='" + aTag + "']");
            this.toggleActive(newSelectedItem);
        };
        ForguncyIconMenuCellType.prototype.toggleActive = function (item) {
            item.toggleClass("selected");
            item.find("[IconType='Icon']").toggle();
            item.find("[IconType='SelectedIcon']").toggle();
        };
        ForguncyIconMenuCellType.prototype.setFontStyle = function (styleInfo) {
            var a = $("#" + this.ID + " ul li a");
            if (styleInfo.FontFamily && styleInfo.FontFamily !== "") {
                a.css("font-family", styleInfo.FontFamily);
            }
            if (styleInfo.FontSize && styleInfo.FontSize > 0) {
                a.css("font-size", styleInfo.FontSize);
            }
            if (styleInfo.FontStyle) {
                a.css("font-style", styleInfo.FontStyle.toLowerCase());
            }
            if (styleInfo.FontWeight) {
                a.css("font-weight", styleInfo.FontWeight.toLowerCase());
            }
        };
        return ForguncyIconMenuCellType;
    }(Forguncy.MenuCellTypeBase));
    Forguncy.ForguncyIconMenuCellType = ForguncyIconMenuCellType;
})(Forguncy || (Forguncy = {}));
Forguncy.Plugin.CellTypeHelper.registerCellType("Forguncy.CustomMenu.ForguncyIconMenuCellType, Forguncy.CustomMenu", Forguncy.ForguncyIconMenuCellType);
