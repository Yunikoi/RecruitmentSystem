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
var LoadingCommand;
(function (LoadingCommand) {
    var LoadingCommon = (function () {
        function LoadingCommon() {
        }
        LoadingCommon.hasCachedKeyDownHandler = function () {
            var _a, _b;
            return ((_a = document.onkeydown) !== null && _a !== void 0 ? _a : '').toString() === ((_b = LoadingCommon.loadingOnKeyDown) !== null && _b !== void 0 ? _b : '').toString();
        };
        LoadingCommon.loadingOnKeyDown = function (e) {
            var f1KeyCode = 112;
            var shiftKeyCode = 16;
            var ctrlKeyCode = 17;
            var altKeyCode = 18;
            var escKeyCode = 27;
            if (((e.keyCode < f1KeyCode &&
                e.keyCode !== ctrlKeyCode &&
                e.keyCode !== altKeyCode &&
                e.keyCode !== shiftKeyCode)) || e.keyCode === escKeyCode) {
                return false;
            }
        };
        return LoadingCommon;
    }());
    LoadingCommand.LoadingCommon = LoadingCommon;
    var StartLoading = (function (_super) {
        __extends(StartLoading, _super);
        function StartLoading() {
            var _this = _super.call(this) || this;
            if (!StartLoading.hasBindPageEvent) {
                StartLoading.hasBindPageEvent = true;
                Forguncy.Page.bind(Forguncy.PageEvents.PageDefaultDataLoaded, StartLoading.onCurrentPageChanged, "*");
                Forguncy.Page.bind(Forguncy.PageEvents.PopupClosed, StartLoading.onCurrentPageChanged, "*");
            }
            return _this;
        }
        StartLoading.onCurrentPageChanged = function (arg1, arg2) {
            var endLoading = new EndLoading();
            endLoading.execute();
        };
        StartLoading.prototype.buildLoadingDom = function () {
            var _a;
            var text = Forguncy.PageBuilder.pageLoadingText;
            text.text(this.evaluateFormula((_a = this.loadingCommandParam.LoadingText) !== null && _a !== void 0 ? _a : ''));
            text.css("color", this.loadingCommandParam.LoadingTextColor);
            Forguncy.PageBuilder.pageLoadingCover.attr({
                "LoadingTextColor": this.loadingCommandParam.LoadingTextColor,
                "LoadingText": this.loadingCommandParam.LoadingText,
            });
        };
        StartLoading.prototype.loadingColor2CssColor = function () {
            this.loadingCommandParam.LoadingTextColor =
                Forguncy.ConvertToCssColor(this.loadingCommandParam.LoadingTextColor);
        };
        StartLoading.prototype.processKeyDownEvent = function () {
            if (!LoadingCommon.hasCachedKeyDownHandler()) {
                LoadingCommon.domOnKeyDownHandlerCache = document.onkeydown;
                document.onkeydown = LoadingCommon.loadingOnKeyDown;
            }
        };
        StartLoading.prototype.execute = function () {
            var _this = this;
            this.CommandExecutingInfo.suspend = true;
            this.loadingCommandParam = this.CommandParam;
            this.loadingColor2CssColor();
            this.buildLoadingDom();
            this.processKeyDownEvent();
            Forguncy.PageBuilder.showPageLoadingCover("rgba(0,0,0,0.4)");
            window.requestAnimationFrame(function () {
                window.requestAnimationFrame(function () {
                    _this.CommandExecutingInfo.suspend = false;
                });
            });
        };
        StartLoading.hasBindPageEvent = false;
        return StartLoading;
    }(Forguncy.Plugin.CommandBase));
    LoadingCommand.StartLoading = StartLoading;
    var EndLoading = (function (_super) {
        __extends(EndLoading, _super);
        function EndLoading() {
            var _this = _super.call(this) || this;
            _this.EnableProtect = true;
            return _this;
        }
        EndLoading.prototype.endLoading = function () {
            Forguncy.PageBuilder.hidePageLoadingCover();
            if (LoadingCommon.hasCachedKeyDownHandler()) {
                document.onkeydown = LoadingCommon.domOnKeyDownHandlerCache;
            }
        };
        EndLoading.prototype.execute = function () {
            this.endLoading();
        };
        return EndLoading;
    }(Forguncy.Plugin.CommandBase));
    LoadingCommand.EndLoading = EndLoading;
})(LoadingCommand || (LoadingCommand = {}));
Forguncy.Plugin.CommandFactory.registerCommand("LoadingCommand.StartLoadingCommand, LoadingCommand", LoadingCommand.StartLoading);
Forguncy.Plugin.CommandFactory.registerCommand("LoadingCommand.EndLoadingCommand, LoadingCommand", LoadingCommand.EndLoading);
