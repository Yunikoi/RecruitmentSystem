var Forguncy;
(function (Forguncy) {
    var CellTypeStyleTemplateBase = (function () {
        function CellTypeStyleTemplateBase() {
        }
        CellTypeStyleTemplateBase.prototype.ApplyTemplateStyle = function (styleTemplate, container) {
            if (!styleTemplate || !container) {
                return;
            }
            this.TemplateKey = styleTemplate.Key;
            var templateDomParts = this.MapPartsNameToDom(container);
            for (var i = 0; i < this.TemplateNameParts.length; i++) {
                var partName = this.TemplateNameParts[i];
                var className = Forguncy.CellTypeStyleTemplateUtils.FormatTemplateStyleClassName(this.CellTypeString, this.TemplateKey, partName);
                var partDom = templateDomParts[partName];
                partDom.addClass(className);
            }
            this.OnTemplateStyleApplied(styleTemplate, container);
            this.Clear();
        };
        CellTypeStyleTemplateBase.prototype.MapPartsNameToDom = function (container) {
            return null;
        };
        CellTypeStyleTemplateBase.prototype.OnTemplateStyleApplied = function (styleTemplate, container) { };
        CellTypeStyleTemplateBase.prototype.Clear = function () {
            this.Container = null;
            this.TemplateKey = null;
        };
        return CellTypeStyleTemplateBase;
    }());
    Forguncy.CellTypeStyleTemplateBase = CellTypeStyleTemplateBase;
})(Forguncy || (Forguncy = {}));
