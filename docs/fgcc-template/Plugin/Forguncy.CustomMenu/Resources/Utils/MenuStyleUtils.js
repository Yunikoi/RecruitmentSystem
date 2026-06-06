var Forguncy;
(function (Forguncy) {
    var MenuStyleUtils = (function () {
        function MenuStyleUtils() {
        }
        MenuStyleUtils.InsertArrowFillColorRule = function (container, cssSelector, fillColor) {
            container.prepend("<style>".concat(cssSelector, " {fill: ").concat(fillColor, " !important;}</style>"));
        };
        MenuStyleUtils.InsertForeColorRule = function (container, cssSelector, foreColor) {
            container.prepend("<style>".concat(cssSelector, " {color: ").concat(foreColor, " !important;}</style>"));
        };
        MenuStyleUtils.InsertBackColorRule = function (container, cssSelector, backgroundColor) {
            container.prepend("<style>".concat(cssSelector, " {background: ").concat(backgroundColor, " border-box !important}</style>"));
        };
        MenuStyleUtils.GetBase64FromSvgElement = function (element) {
            var div = document.createElement('div');
            div.appendChild(element.cloneNode(true));
            return 'data:image/svg+xml;base64,' + btoa(unescape(encodeURIComponent(div.innerHTML)));
        };
        return MenuStyleUtils;
    }());
    Forguncy.MenuStyleUtils = MenuStyleUtils;
})(Forguncy || (Forguncy = {}));
