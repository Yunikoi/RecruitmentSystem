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
var PostDataToWebApi;
(function (PostDataToWebApi) {
    var PostDataCommand = (function (_super) {
        __extends(PostDataCommand, _super);
        function PostDataCommand() {
            return _super !== null && _super.apply(this, arguments) || this;
        }
        PostDataCommand.prototype.execute = function () {
            var _this = this;
            var commandSettings = this.CommandParam;
            var data = this.generateData(commandSettings.PostData, 0);
            var webUrl = this.evaluateFormula(commandSettings.WebUrl);
            if (webUrl) {
                webUrl = webUrl.trim().replace(/^\//, "");
                if (webUrl.toLowerCase().indexOf("http://") !== 0 && webUrl.toLowerCase().indexOf("https://") !== 0) {
                    webUrl = Forguncy.Helper.SpecialPath.getBaseUrl() + webUrl;
                }
            }
            var contentType = "application/x-www-form-urlencoded; charset=utf-8";
            var postData = commandSettings.PostData;
            if (commandSettings.BodyType === BodyType.Json) {
                contentType = "application/json; charset=utf-8";
            }
            else if (postData.DataItemType === DataItemType.Array) {
                if (postData.Data.IsStringify) {
                    contentType = "application/json; charset=utf-8";
                }
            }
            else if (postData.DataItemType === DataItemType.Object) {
                if (postData.Data.IsStringify) {
                    contentType = "application/json; charset=utf-8";
                }
            }
            else {
                data = this.getStringFromSimpleData(data);
                contentType = "application/json; charset=utf-8";
            }
            this.CommandExecutingInfo.suspend = true;
            var that = this;
            $.ajax({
                url: webUrl,
                headers: { "DebugThreadId": this.CommandExecutingInfo.debugThreadId },
                data: data,
                type: commandSettings.Method,
                beforeSend: function (request) {
                    request.setRequestHeader("Content-Type", contentType);
                    if (commandSettings.Header) {
                        for (var i = 0; i < commandSettings.Header.length; i++) {
                            var type = that.evaluateFormula(commandSettings.Header[i].Type);
                            if (type) {
                                request.setRequestHeader(type, that.evaluateFormula(commandSettings.Header[i].Value));
                            }
                        }
                    }
                },
                success: function (result) {
                    try {
                        if (commandSettings.SuccessCallback) {
                            var callback = new Function("result", commandSettings.SuccessCallback);
                            callback(result);
                        }
                        if (commandSettings.NewParameter) {
                            var value = _this.getValueFromResponse(result);
                            Forguncy.CommandHelper.setVariableValue(commandSettings.NewParameter, value);
                        }
                    }
                    finally {
                        _this.CommandExecutingInfo.suspend = false;
                    }
                },
                error: function (error) {
                    try {
                        if (commandSettings.ErrorCallback) {
                            var callback = new Function("error", commandSettings.ErrorCallback);
                            callback(error.responseText);
                        }
                    }
                    finally {
                        _this.CommandExecutingInfo.suspend = false;
                    }
                }
            });
        };
        PostDataCommand.prototype.getValueFromResponse = function (data) {
            if (typeof data === "string") {
                var str = data;
                if (/^\{.*\}$/.test(str) || /^\[.*\]$/.test(str)) {
                    return JSON.parse(str);
                }
                return str;
            }
            return data;
        };
        PostDataCommand.prototype.getStringFromSimpleData = function (data) {
            if (data === null) {
                return null;
            }
            if (typeof data === "string") {
                return data;
            }
            else {
                return data.toString();
            }
        };
        PostDataCommand.prototype.generateData = function (data, rowIndex) {
            var _this = this;
            if (this.CommandParam.BodyType == BodyType.Json) {
                var jsonContent = this.CommandParam.JsonString;
                jsonContent = this.evaluateFormulaInContent(jsonContent, null);
                return jsonContent;
            }
            else if (data.DataItemType === DataItemType.ValueOrFormula) {
                var valueOrFormula = data.Data;
                if (typeof valueOrFormula === "string") {
                    return this.evaluateFormula(valueOrFormula);
                }
                return data.Data === undefined ? null : data.Data;
            }
            else if (data.DataItemType === DataItemType.Object) {
                var obj_1 = {};
                var objectData = data.Data;
                if (objectData.Data) {
                    objectData.Data.forEach(function (property) {
                        obj_1[property.Name] = _this.generateData(property, rowIndex);
                    });
                }
                if (objectData.IsStringify) {
                    return JSON.stringify(obj_1);
                }
                else {
                    return obj_1;
                }
            }
            else if (data.DataItemType === DataItemType.Array) {
                var arr_1 = [];
                var arrayData = data.Data;
                if (arrayData.Data) {
                    if (arrayData.CountMode === ArrayCountMode.Auto) {
                        arrayData.Data.forEach(function (property, index) {
                            arr_1.push(_this.generateData(property, index));
                        });
                    }
                    else if (arrayData.CountMode === ArrayCountMode.Count) {
                        var count = parseInt(arrayData.Count);
                        if (isNaN(count)) {
                            if (arrayData.Count.charAt(0) === '=') {
                                var cellLocation = this.getCellLocation(arrayData.Count);
                                count = Forguncy.Page.getCellByLocation(cellLocation).getValue();
                            }
                            else {
                                count = 0;
                            }
                        }
                        if (arrayData.Data.length > 0) {
                            for (var index = 0; index < count; index++) {
                                if (index < arrayData.Data.length) {
                                    arr_1.push(this.generateData(arrayData.Data[index], index));
                                }
                                else {
                                    arr_1.push(this.generateData(arrayData.Data[0], index));
                                }
                            }
                        }
                    }
                }
                if (arrayData.IsStringify) {
                    return JSON.stringify(arr_1);
                }
                else {
                    return arr_1;
                }
            }
            else if (data.DataItemType === DataItemType.ListViewData) {
                var listViewData = data.Data;
                var listViewName = this.evaluateFormula(listViewData.ListViewName);
                var columnName = this.evaluateFormula(listViewData.ColumnName);
                if (listViewName) {
                    var listView = Forguncy.Page.getListView(listViewName);
                    if (listView) {
                        var indexOrName = columnName;
                        var index = parseInt(indexOrName);
                        if (isNaN(index)) {
                            return listView.getValue(rowIndex, indexOrName);
                        }
                        else {
                            return listView.getValue(rowIndex, index - 1);
                        }
                    }
                }
            }
        };
        return PostDataCommand;
    }(Forguncy.Plugin.CommandBase));
    PostDataToWebApi.PostDataCommand = PostDataCommand;
    var DataItemType;
    (function (DataItemType) {
        DataItemType[DataItemType["ValueOrFormula"] = 0] = "ValueOrFormula";
        DataItemType[DataItemType["Object"] = 1] = "Object";
        DataItemType[DataItemType["Array"] = 4] = "Array";
        DataItemType[DataItemType["ListViewData"] = 5] = "ListViewData";
    })(DataItemType || (DataItemType = {}));
    var BodyType;
    (function (BodyType) {
        BodyType[BodyType["Default"] = 0] = "Default";
        BodyType[BodyType["Json"] = 1] = "Json";
        BodyType[BodyType["MultipartFormData"] = 2] = "MultipartFormData";
    })(BodyType || (BodyType = {}));
    var ArrayCountMode;
    (function (ArrayCountMode) {
        ArrayCountMode[ArrayCountMode["Auto"] = 0] = "Auto";
        ArrayCountMode[ArrayCountMode["Count"] = 1] = "Count";
    })(ArrayCountMode || (ArrayCountMode = {}));
})(PostDataToWebApi || (PostDataToWebApi = {}));
Forguncy.Plugin.CommandFactory.registerCommand("PostDataToWebApi.PostDataCommand, PostDataToWebApi", PostDataToWebApi.PostDataCommand);
