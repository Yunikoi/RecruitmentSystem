var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var AIMessageType = Forguncy.Plugin.AIMessageType;
var ChatMessageRole = Forguncy.Plugin.ChatMessageRole;
const innerVariablePrefix = "e07747ed-4769-46ad-ac38-f0b518bba523_";
const FGC_currentKey = innerVariablePrefix + 'currentKey';
const FGC_functionCalls = innerVariablePrefix + 'functionCalls';
const FGC_returnValueMap = innerVariablePrefix + 'returnValueMap';
const FGC_request = innerVariablePrefix + 'request';
const FGC_response = innerVariablePrefix + 'response';
const FGC_requestCount = innerVariablePrefix + 'requestCount';
const FGC_usageInfo = innerVariablePrefix + 'usageInfo';
const MAX_TOOL_CALL_COUNT = 200;
var SenderEnum;
(function (SenderEnum) {
    SenderEnum["User"] = "user";
    SenderEnum["AI"] = "assistant";
    SenderEnum["Tool"] = "tool";
})(SenderEnum || (SenderEnum = {}));
function normalizeHistoryMessages(rawHistory, rawQuestion) {
    var _a;
    if (!Array.isArray(rawHistory)) {
        return [];
    }
    if (rawHistory.length === 0) {
        return rawHistory;
    }
    const lastMessage = rawHistory[rawHistory.length - 1];
    if ((lastMessage === null || lastMessage === void 0 ? void 0 : lastMessage.role) === SenderEnum.User && ((_a = lastMessage.message) !== null && _a !== void 0 ? _a : '') === (rawQuestion !== null && rawQuestion !== void 0 ? rawQuestion : '')) {
        return rawHistory.slice(0, -1);
    }
    return rawHistory;
}
function filterThinkTags(input) {
    var _a;
    return (_a = input === null || input === void 0 ? void 0 : input.replace(/<think>.*?<\/think>/gs, '')) !== null && _a !== void 0 ? _a : "";
}
class FrontEndCommand extends Forguncy.Plugin.CommandBase {
    constructor() {
        super(...arguments);
        this._evaluatePrompt = (prompt) => {
            let _prompt = this.evaluateFormula(prompt !== null && prompt !== void 0 ? prompt : '');
            const regex = /\[=([^\]]+)\]/g;
            const matches = _prompt.match(regex);
            const context = {};
            if (matches) {
                matches.forEach(v => {
                    const formula = v.substring(1, v.length - 1);
                    context[v] = this.evaluateFormula(formula);
                });
            }
            _prompt = _prompt.replace(/\[=([^\]]+)\]/g, (match, prefix, key) => {
                if (context[match] !== undefined) {
                    return `[${context[match]}]`;
                }
                return match;
            });
            return _prompt;
        };
        this._processExecuteSubCommandResult = () => __awaiter(this, void 0, void 0, function* () {
            var _a, _b;
            const { nextFunctionMap, response, nextReturnValueMap, request, requestCount, usageInfo } = this.parsePreFunctionCallParam();
            if (Object.keys(nextFunctionMap).length === 0) { //finish execute
                const toolCallResults = [];
                Object.keys(nextReturnValueMap).forEach(key => {
                    const value = nextReturnValueMap[key];
                    if (value) {
                        toolCallResults.push({ key: key, value: value });
                    }
                });
                let message = undefined;
                let finalUsageInfo = usageInfo;
                const oldRequest = JSON.parse(request);
                let chatsToProcess = oldRequest.chats;
                if (toolCallResults.length > 0) {
                    const toolCalls = response.assistantChatMessage.toolCalls;
                    const toolCallsList = oldRequest.chats.filter(v => v.role === ChatMessageRole.Assistant && v.toolCalls);
                    const lastToolCall = toolCallsList[toolCallsList.length - 1];
                    if (lastToolCall === undefined || JSON.stringify(lastToolCall.toolCalls) !== JSON.stringify(toolCalls)) {
                        //避免重复添加toolCalls,因为mcp执行之前可能已经加过了
                        oldRequest.chats.push({
                            role: ChatMessageRole.Assistant,
                            content: (_b = (_a = response.assistantChatMessage) === null || _a === void 0 ? void 0 : _a.content) !== null && _b !== void 0 ? _b : '',
                            toolCallId: undefined,
                            toolCalls: toolCalls
                        });
                    }
                    toolCallResults.forEach(v => {
                        var _a, _b, _c;
                        const fnName = (_a = toolCalls.find((vv) => vv.id === v.key)) === null || _a === void 0 ? void 0 : _a.functionName;
                        if (fnName) {
                            const param = this._getCommandParam();
                            const paramDefs = (_c = (_b = param.FunctionDefinitionList.find(v => v.Name === fnName)) === null || _b === void 0 ? void 0 : _b.ReturnValues) !== null && _c !== void 0 ? _c : [];
                            const defMap = {};
                            paramDefs.forEach(v => {
                                defMap[v.Name] = v.Description;
                            });
                            const content = [];
                            Object.keys(v.value).forEach(returnParam => {
                                const desc = defMap[returnParam];
                                const descPart = desc && desc.trim() !== '' ? `(${desc})` : '';
                                content.push(`${returnParam}${descPart}:${stringify(v.value[returnParam])}`);
                            });
                            oldRequest.chats.push({
                                role: ChatMessageRole.Tool,
                                content: content.join(';'),
                                toolCallId: v.key,
                            });
                        }
                    });
                    const needReAsk = toolCallResults.filter(v => Object.keys(v.value).length !== 0).length > 0;
                    if (needReAsk) {
                        const reAskResult = yield this.CommandExecutor.openAICallAsync(oldRequest);
                        this.addLogIfHave(reAskResult);
                        const result = yield this._executeCommandTillNoToolCall(reAskResult, oldRequest, requestCount, usageInfo);
                        message = result.message;
                        finalUsageInfo = result.usageInfo;
                        chatsToProcess = result.chats;
                    }
                }
                this._processMessage(message, finalUsageInfo, chatsToProcess);
            }
            else { //keep execute other toolCall
                const keys = Object.keys(nextFunctionMap);
                const firstOne = keys[0];
                if (firstOne) {
                    this._commandExecutor(nextFunctionMap[firstOne], nextFunctionMap, firstOne, nextReturnValueMap, request, JSON.stringify(response), requestCount, usageInfo);
                    return;
                }
            }
        });
        this._executeCommandTillNoToolCall = (response, request, count, usageInfo) => __awaiter(this, void 0, void 0, function* () {
            var _c, _d, _e, _f, _g, _h, _j, _k, _l, _m, _o, _p, _q, _r;
            let message = "";
            if (MessageIsMessageAssistantDto(response)) {
                message = response.message;
            }
            usageInfo.InputTokenCount += (_d = (_c = response === null || response === void 0 ? void 0 : response.usageInfo) === null || _c === void 0 ? void 0 : _c.inputTokenCount) !== null && _d !== void 0 ? _d : 0;
            usageInfo.OutputTokenCount += (_f = (_e = response === null || response === void 0 ? void 0 : response.usageInfo) === null || _e === void 0 ? void 0 : _e.outputTokenCount) !== null && _f !== void 0 ? _f : 0;
            usageInfo.Duration += (_h = (_g = response === null || response === void 0 ? void 0 : response.usageInfo) === null || _g === void 0 ? void 0 : _g.duration) !== null && _h !== void 0 ? _h : 0;
            usageInfo.AiConfigName = (_k = (_j = response.usageInfo) === null || _j === void 0 ? void 0 : _j.aiConfigName) !== null && _k !== void 0 ? _k : '';
            usageInfo.AiModelName = (_m = (_l = response.usageInfo) === null || _l === void 0 ? void 0 : _l.aiModelName) !== null && _m !== void 0 ? _m : '';
            if ((_o = response === null || response === void 0 ? void 0 : response.usageInfo) === null || _o === void 0 ? void 0 : _o.isTokenCountEstimated) {
                usageInfo.IsTokenCountEstimated = true;
            }
            if (count >= MAX_TOOL_CALL_COUNT) {
                return { message, usageInfo, chats: request.chats };
            }
            if (MessageIsMessageToolDto(response)) {
                request.chats.push({
                    role: ChatMessageRole.Assistant,
                    content: (_q = (_p = response.assistantChatMessage) === null || _p === void 0 ? void 0 : _p.content) !== null && _q !== void 0 ? _q : '',
                    toolCallId: undefined,
                    toolCalls: (_r = response.assistantChatMessage) === null || _r === void 0 ? void 0 : _r.toolCalls
                });
                const keys = Object.keys(response.functionCalls);
                const { mcpKeys, localKeys } = this._divideKeys(keys, response.functionCalls);
                const { result: returnValueMap, oldRequest } = yield this._callAllMcpFunctions(mcpKeys, response.functionCalls, JSON.stringify(request));
                const firstOne = localKeys[0];
                if (firstOne) {
                    this._commandExecutor(response.functionCalls[firstOne], response.functionCalls, firstOne, returnValueMap, JSON.stringify(oldRequest), JSON.stringify(response), count, usageInfo);
                    return { message: undefined, usageInfo, chats: oldRequest.chats };
                }
                else {
                    const reAskResult = yield this.CommandExecutor.openAICallAsync(oldRequest);
                    this.addLogIfHave(reAskResult);
                    const result = yield this._executeCommandTillNoToolCall(reAskResult, oldRequest, count + 1, usageInfo);
                    return result;
                }
            }
            return { message: MessageIsMessageAssistantDto(response) ? response.message : message, usageInfo, chats: request.chats };
        });
        this._commandExecutor = (item, functionCalls, currentKey, returnValueMap, request, response, requestCount, usageInfo) => __awaiter(this, void 0, void 0, function* () {
            const param = this._getCommandParam();
            const fn = param.FunctionDefinitionList.find(v => v.Name === item.functionName);
            if (fn && !fn.Disabled) {
                const defaultParams = {};
                fn.Parameters.forEach(v => {
                    defaultParams[v.Name] = undefined;
                });
                const inputArg = Object.assign(Object.assign(Object.assign({}, defaultParams), item.arguments), { [FGC_currentKey]: currentKey, [FGC_functionCalls]: JSON.stringify(functionCalls), [FGC_returnValueMap]: JSON.stringify(returnValueMap), [FGC_request]: request, [FGC_response]: response, [FGC_requestCount]: requestCount + '', [FGC_usageInfo]: JSON.stringify(usageInfo) });
                const validateResult = this._checkParamValid(fn, item.arguments);
                if (!validateResult.valid) {
                    console.error(validateResult.errorMessage);
                    return;
                }
                const returnValues = {};
                fn.ReturnValues.forEach(i => returnValues[i.Name] = { Name: i.Name });
                this.log(`${this.getPluginResource("functionCall")}:${fn.Name}`);
                this.log(`${this.getPluginResource("functionParameters")}:${JSON.stringify(item.arguments)}`);
                this.executeSubCommands(fn.Command, inputArg, {
                    returnValues: returnValues
                }, true);
            }
        });
        this._getCommandParam = () => {
            return this.CommandParam;
        };
    }
    _divideKeys(keys, functionCalls) {
        const param = this._getCommandParam();
        const mcpKeys = [];
        const localKeys = [];
        keys.forEach(v => {
            const item = functionCalls[v];
            const fn = param.FunctionDefinitionList.find(v => v.Name === item.functionName);
            if (fn) {
                localKeys.push(v);
            }
            else {
                mcpKeys.push(v);
            }
        });
        return { mcpKeys, localKeys };
    }
    execute() {
        var _a, _b;
        return __awaiter(this, void 0, void 0, function* () {
            if (this.subCommandInfo) {
                yield this._processExecuteSubCommandResult();
                return;
            }
            const param = this._getCommandParam();
            const rawQuestion = this.evaluateFormula(param.Question);
            const question = this._getQuestionWithAttachments(rawQuestion, this.evaluateFormula(param.Attachment));
            const promptLink = this._evaluatePrompt(param.ServerPropertiesId.PromptLink);
            const userPromptLink = this._evaluatePrompt(param.ServerPropertiesId.UserPromptLink);
            const rawHistory = param.HistoryMessages ? this.evaluateFormula(param.HistoryMessages) : [];
            const normalizedRawHistory = normalizeHistoryMessages(rawHistory, rawQuestion);
            const historyList = ChatMessageConverter.toChatMessageParamList(normalizedRawHistory, this.getPluginResource('attachmentQuestionTemplate'));
            const chats = [
                ...historyList,
                { role: ChatMessageRole.User, content: question }
            ];
            const data = {
                systemPrompt: promptLink,
                userPrompt: userPromptLink,
                modelName: param.AiModel,
                chats: chats,
                functionList: param.ServerPropertiesId.ServerFunctionDefinitions,
                paramsValues: {},
                timeout: param.Timeout,
                mcpServices: (_b = (_a = param.MCPTools) === null || _a === void 0 ? void 0 : _a.map(v => v.Name)) !== null && _b !== void 0 ? _b : [],
                excludeReasoningContent: param.ResultType !== "Text",
            };
            param.PromptRefersFormulas.forEach(v => {
                var _a;
                data.paramsValues = (_a = data.paramsValues) !== null && _a !== void 0 ? _a : {};
                data.paramsValues[v] = this.evaluateFormula(v);
            });
            const response = yield this.CommandExecutor.openAICallAsync(data);
            this.addLogIfHave(response);
            let usageInfo = { InputTokenCount: 0, OutputTokenCount: 0, Duration: 0, AiConfigName: '', AiModelName: '', IsTokenCountEstimated: false };
            const result = yield this._executeCommandTillNoToolCall(response, data, 0, usageInfo);
            usageInfo = result.usageInfo;
            this._processMessage(result.message, usageInfo, result.chats);
        });
    }
    _getQuestionWithAttachments(question, attachments) {
        if (attachments) {
            let attachedFiles = [];
            try {
                attachedFiles = typeof attachments === 'string' ? JSON.parse(attachments) : attachments;
            }
            catch (e) {
                this.log(e);
            }
            if (Array.isArray(attachedFiles) && attachedFiles.length > 0) {
                const template = this.getPluginResource('attachmentQuestionTemplate');
                const fileSections = attachedFiles
                    .filter((f) => f.textContent && f.fileName)
                    .map((f) => template.replace('{0}', f.fileName).replace('{1}', f.textContent));
                if (fileSections.length > 0) {
                    return fileSections.join('\n\n') + '\n\n' + question;
                }
            }
        }
        return question;
    }
    _callAllMcpFunctions(mcpKeys, functionCalls, request) {
        return __awaiter(this, void 0, void 0, function* () {
            const resultsArr = yield Promise.all(mcpKeys.map(key => {
                const item = functionCalls[key];
                if (item) {
                    return this._callMcpFunction(item.functionName, item.arguments);
                }
                else {
                    return Promise.resolve(this.getPluginResource("callMCPError"));
                }
            }));
            const result = {};
            resultsArr.forEach((returnValue, index) => {
                result[mcpKeys[index]] = returnValue;
            });
            const oldRequest = JSON.parse(request);
            resultsArr.forEach((item, index) => {
                oldRequest.chats.push({
                    role: ChatMessageRole.Tool,
                    content: item,
                    toolCallId: mcpKeys[index],
                });
            });
            return { result, oldRequest };
        });
    }
    _callMcpFunction(name, args) {
        return __awaiter(this, void 0, void 0, function* () {
            this.log(`${this.getPluginResource("mcpCallStart")}:${name}`);
            this.log(`${this.getPluginResource("mcpCallArgs")}:${JSON.stringify(args)}`);
            const reusltList = yield new Promise(resolve => {
                const dataList = [];
                //@ts-ignore
                Forguncy.Common.fetchEventSourceAsync('OpenAI/CallMCPTool', {
                    functionName: name,
                    arguments: args,
                }, (eventData) => {
                    if (eventData === '[DONE]') {
                        resolve(dataList);
                        return;
                    }
                    try {
                        const data = JSON.parse(eventData);
                        dataList.push(data);
                    }
                    catch (e) {
                        console.error(this._format(this.getPluginResource("parseStreamDataError"), e, eventData));
                    }
                }, () => {
                    //onClose
                });
            });
            const result = reusltList.map(res => { var _a; return ((_a = res.resultType) === null || _a === void 0 ? void 0 : _a.toLowerCase()) === "text" ? res === null || res === void 0 ? void 0 : res.data : this.getPluginResource("unknownMCPResponseType"); }).join('\n');
            this.log(`${this.getPluginResource("mcpCallReturnValue")}:${result}`);
            return result;
        });
    }
    addLogIfHave(response) {
        if (response.logInfo) {
            this.log(`${this.getPluginResource("modelName")}:${response.logInfo.modelName}`);
            this.log(`${this.getPluginResource("userPromptLogLabel")}:${response.logInfo.userPrompt}`);
            this.log(`${this.getPluginResource("promptLogLabel")}:${response.logInfo.prompt}`);
        }
    }
    _format(str, ...args) {
        return args.reduce((prev, curr, index) => prev.replaceAll(`{${index}}`, curr), str);
    }
    ;
    _checkParamValid(value, param) {
        const defParams = {};
        value.Parameters.map(v => {
            defParams[v.Name] = !v.Required;
        });
        Object.keys(param).forEach(v => {
            defParams[v] = true;
        });
        const notPassParam = Object.keys(defParams).filter(v => !defParams[v]);
        const valid = notPassParam.length === 0;
        let errorMessage = "";
        if (!valid) {
            errorMessage = this.getPluginResource("paramNotMatch") + notPassParam.join(',');
        }
        return { valid, errorMessage };
    }
    parsePreFunctionCallParam() {
        var _a, _b, _c, _d;
        const executeSuccessKey = this.subCommandInfo.initParams[FGC_currentKey];
        const allFunctionCalls = JSON.parse(this.subCommandInfo.initParams[FGC_functionCalls]);
        const returnValueMap = JSON.parse(this.subCommandInfo.initParams[FGC_returnValueMap]);
        const request = this.subCommandInfo.initParams[FGC_request];
        const responseStr = this.subCommandInfo.initParams[FGC_response];
        const requestCount = parseInt(this.subCommandInfo.initParams[FGC_requestCount]) + 1;
        let usageInfo = { InputTokenCount: 0, OutputTokenCount: 0, Duration: 0, AiConfigName: '', AiModelName: '', IsTokenCountEstimated: false };
        try {
            const usageInfoStr = this.subCommandInfo.initParams[FGC_usageInfo];
            if (usageInfoStr) {
                usageInfo = JSON.parse(usageInfoStr);
            }
        }
        catch (e) {
            usageInfo = { InputTokenCount: 0, OutputTokenCount: 0, Duration: 0, AiConfigName: '', AiModelName: '', IsTokenCountEstimated: false };
        }
        const nextFunctionMap = {};
        const nextReturnValueMap = returnValueMap;
        nextReturnValueMap[executeSuccessKey] = this.subCommandInfo.returnResult.returnValues;
        Object.keys(allFunctionCalls).forEach(v => {
            if (v !== executeSuccessKey) {
                nextFunctionMap[v] = allFunctionCalls[v];
            }
        });
        const response = JSON.parse(responseStr);
        if ((_a = this.subCommandInfo.returnResult) === null || _a === void 0 ? void 0 : _a.returnValues) {
            const functionName = (_c = (_b = response === null || response === void 0 ? void 0 : response.functionCalls) === null || _b === void 0 ? void 0 : _b[executeSuccessKey]) === null || _c === void 0 ? void 0 : _c.functionName;
            this.log((_d = this.getPluginResource("functionCallSuccess")) === null || _d === void 0 ? void 0 : _d.replace("{{0}}", functionName));
            this.log(`${this.getPluginResource("functionCallResult")}:${JSON.stringify(this.subCommandInfo.returnResult.returnValues)}`);
        }
        return { nextFunctionMap, response, nextReturnValueMap, request, requestCount, usageInfo };
    }
    _processMessage(rawMessage, usageInfo, chats) {
        var _a;
        const param = this._getCommandParam();
        let message = rawMessage;
        if (message !== undefined) {
            if (param.ResultType != "Text") {
                message = this._extractJsonContent(message);
            }
        }
        message = (_a = message === null || message === void 0 ? void 0 : message.trim()) !== null && _a !== void 0 ? _a : "";
        const nullValues = [undefined, null];
        if (!nullValues.includes(param.ResultTo) && !nullValues.includes(message)) {
            const location = Forguncy.Helper.getCellLocation(param.ResultTo, this.getFormulaCalcContext());
            this.log(`${this.getPluginResource("assistantResponse")}:${message}`);
            if (location) {
                Forguncy.Page.getCellByLocation(location).setValue(message);
            }
            else {
                Forguncy.CommandHelper.setVariableValue(param.ResultTo, message);
            }
        }
        if (param.UsageResultTo && !nullValues.includes(param.UsageResultTo)) {
            const usageResultLocation = Forguncy.Helper.getCellLocation(param.UsageResultTo, this.getFormulaCalcContext());
            const usageResultValue = {
                InputTokenCount: usageInfo.InputTokenCount,
                OutputTokenCount: usageInfo.OutputTokenCount,
                AiConfigName: usageInfo.AiConfigName,
                AiModelName: usageInfo.AiModelName,
                Duration: usageInfo.Duration,
                IsTokenCountEstimated: usageInfo.IsTokenCountEstimated
            };
            if (usageResultLocation) {
                Forguncy.Page.getCellByLocation(usageResultLocation).setValue(usageResultValue);
            }
            else {
                Forguncy.CommandHelper.setVariableValue(param.UsageResultTo, usageResultValue);
            }
        }
        if (param.ToolCallMessagesTo && !nullValues.includes(param.ToolCallMessagesTo)) {
            const lastUserIndex = chats.map(v => v.role).lastIndexOf(ChatMessageRole.User);
            const toolCallMessages = chats.slice(lastUserIndex + 1);
            const toolCallMessagesDto = ChatMessageConverter.toJsonResultDtoList(toolCallMessages, param.FunctionDefinitionList);
            const location = Forguncy.Helper.getCellLocation(param.ToolCallMessagesTo, this.getFormulaCalcContext());
            if (location) {
                Forguncy.Page.getCellByLocation(location).setValue(toolCallMessagesDto);
            }
            else {
                Forguncy.CommandHelper.setVariableValue(param.ToolCallMessagesTo, toolCallMessagesDto);
            }
        }
    }
    _extractJsonContent(str) {
        const JsonPattern = new RegExp("```json(.*?)```", "s");
        if (!str) {
            return str;
        }
        str = filterThinkTags(str);
        const match = str.match(JsonPattern);
        if (match) {
            return match[1];
        }
        else {
            return str;
        }
    }
}
function MessageIsMessageAssistantDto(param) {
    return param.type === AIMessageType.Assistant;
}
function MessageIsMessageToolDto(param) {
    return param.type === AIMessageType.Tool;
}
class ChatMessageConverter {
    static toChatMessageParamList(history, attachmentTemplate) {
        if (!Array.isArray(history)) {
            return [];
        }
        // Card messages are only for UI replay and should not be sent back to the model.
        return history.filter(item => item.cardMetaData == null).map(item => {
            var _a;
            let content = filterThinkTags(item.message);
            if (item.attachedFiles && item.attachedFiles.length > 0) {
                try {
                    const fileSections = item.attachedFiles
                        .filter(f => f.textContent && f.fileName)
                        .map(f => {
                        if (attachmentTemplate) {
                            return attachmentTemplate.replace('{0}', f.fileName || "").replace('{1}', f.textContent || "");
                        }
                        return "";
                    })
                        .filter(v => !!v);
                    if (fileSections.length > 0) {
                        content = fileSections.join('\n\n') + '\n\n' + content;
                    }
                }
                catch (e) {
                    console.error("ChatMessageConverter.toChatMessageParamList error", e);
                }
            }
            return {
                content: content,
                role: (_a = this.senderToRoleMap[item.role]) !== null && _a !== void 0 ? _a : ChatMessageRole.User,
                toolCalls: item.toolCalls,
                toolCallId: item.toolCallId
            };
        });
    }
    static toJsonResultDtoList(messages, functionDefinitionList) {
        var _a;
        if (!Array.isArray(messages)) {
            return [];
        }
        const localFunctionNames = new Set((_a = functionDefinitionList === null || functionDefinitionList === void 0 ? void 0 : functionDefinitionList.map(f => f.Name)) !== null && _a !== void 0 ? _a : []);
        return messages.map(item => {
            var _a, _b, _c;
            let isMcp = undefined;
            if (item.role === ChatMessageRole.Assistant && item.toolCalls && item.toolCalls.length > 0) {
                // Check if all tool calls are MCP (not in local function list)
                isMcp = item.toolCalls.every(tc => !localFunctionNames.has(tc.functionName));
            }
            return {
                message: (_a = item.content) !== null && _a !== void 0 ? _a : "",
                role: (_b = this.roleToSenderMap[item.role]) !== null && _b !== void 0 ? _b : SenderEnum.User,
                toolCalls: item.toolCalls,
                toolCallId: (_c = item.toolCallId) !== null && _c !== void 0 ? _c : undefined,
                sendTime: Forguncy.ConvertDateToOADate(new Date()),
                isMcp: isMcp
            };
        });
    }
}
ChatMessageConverter.roleToSenderMap = {
    [ChatMessageRole.Assistant]: SenderEnum.AI,
    [ChatMessageRole.Tool]: SenderEnum.Tool,
    [ChatMessageRole.User]: SenderEnum.User,
    [ChatMessageRole.System]: SenderEnum.User,
    [ChatMessageRole.Function]: SenderEnum.Tool,
};
ChatMessageConverter.senderToRoleMap = {
    [SenderEnum.AI]: ChatMessageRole.Assistant,
    [SenderEnum.Tool]: ChatMessageRole.Tool,
    [SenderEnum.User]: ChatMessageRole.User,
};
Forguncy.Plugin.CommandFactory.registerCommand("AiAssistantChat.AiAssistantChatCommand, AiAssistantChat", FrontEndCommand);
function stringify(input) {
    if (input === undefined || input === null) {
        return '';
    }
    if (typeof input === 'boolean') {
        return input;
    }
    if (typeof input === 'number') {
        return input;
    }
    if (typeof input === 'object') {
        return JSON.stringify(input);
    }
    return String(input);
}
