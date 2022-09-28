var edge_function =
/******/ (function(modules) { // webpackBootstrap
/******/ 	// The module cache
/******/ 	var installedModules = {};
/******/
/******/ 	// The require function
/******/ 	function __webpack_require__(moduleId) {
/******/
/******/ 		// Check if module is in cache
/******/ 		if(installedModules[moduleId]) {
/******/ 			return installedModules[moduleId].exports;
/******/ 		}
/******/ 		// Create a new module (and put it into the cache)
/******/ 		var module = installedModules[moduleId] = {
/******/ 			i: moduleId,
/******/ 			l: false,
/******/ 			exports: {}
/******/ 		};
/******/
/******/ 		// Execute the module function
/******/ 		modules[moduleId].call(module.exports, module, module.exports, __webpack_require__);
/******/
/******/ 		// Flag the module as loaded
/******/ 		module.l = true;
/******/
/******/ 		// Return the exports of the module
/******/ 		return module.exports;
/******/ 	}
/******/
/******/
/******/ 	// expose the modules object (__webpack_modules__)
/******/ 	__webpack_require__.m = modules;
/******/
/******/ 	// expose the module cache
/******/ 	__webpack_require__.c = installedModules;
/******/
/******/ 	// define getter function for harmony exports
/******/ 	__webpack_require__.d = function(exports, name, getter) {
/******/ 		if(!__webpack_require__.o(exports, name)) {
/******/ 			Object.defineProperty(exports, name, { enumerable: true, get: getter });
/******/ 		}
/******/ 	};
/******/
/******/ 	// define __esModule on exports
/******/ 	__webpack_require__.r = function(exports) {
/******/ 		if(typeof Symbol !== 'undefined' && Symbol.toStringTag) {
/******/ 			Object.defineProperty(exports, Symbol.toStringTag, { value: 'Module' });
/******/ 		}
/******/ 		Object.defineProperty(exports, '__esModule', { value: true });
/******/ 	};
/******/
/******/ 	// create a fake namespace object
/******/ 	// mode & 1: value is a module id, require it
/******/ 	// mode & 2: merge all properties of value into the ns
/******/ 	// mode & 4: return value when already ns object
/******/ 	// mode & 8|1: behave like require
/******/ 	__webpack_require__.t = function(value, mode) {
/******/ 		if(mode & 1) value = __webpack_require__(value);
/******/ 		if(mode & 8) return value;
/******/ 		if((mode & 4) && typeof value === 'object' && value && value.__esModule) return value;
/******/ 		var ns = Object.create(null);
/******/ 		__webpack_require__.r(ns);
/******/ 		Object.defineProperty(ns, 'default', { enumerable: true, value: value });
/******/ 		if(mode & 2 && typeof value != 'string') for(var key in value) __webpack_require__.d(ns, key, function(key) { return value[key]; }.bind(null, key));
/******/ 		return ns;
/******/ 	};
/******/
/******/ 	// getDefaultExport function for compatibility with non-harmony modules
/******/ 	__webpack_require__.n = function(module) {
/******/ 		var getter = module && module.__esModule ?
/******/ 			function getDefault() { return module['default']; } :
/******/ 			function getModuleExports() { return module; };
/******/ 		__webpack_require__.d(getter, 'a', getter);
/******/ 		return getter;
/******/ 	};
/******/
/******/ 	// Object.prototype.hasOwnProperty.call
/******/ 	__webpack_require__.o = function(object, property) { return Object.prototype.hasOwnProperty.call(object, property); };
/******/
/******/ 	// __webpack_public_path__
/******/ 	__webpack_require__.p = "";
/******/
/******/
/******/ 	// Load entry module and return exports
/******/ 	return __webpack_require__(__webpack_require__.s = "./src/index.ts");
/******/ })
/************************************************************************/
/******/ ({

/***/ "./node_modules/@segment/tsub/dist/index.js":
/*!**************************************************!*\
  !*** ./node_modules/@segment/tsub/dist/index.js ***!
  \**************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\nObject.defineProperty(exports, \"__esModule\", { value: true });\nexports.Store = exports.matches = exports.transform = void 0;\nvar transformers_1 = __webpack_require__(/*! ./transformers */ \"./node_modules/@segment/tsub/dist/transformers.js\");\nObject.defineProperty(exports, \"transform\", { enumerable: true, get: function () { return transformers_1.default; } });\nvar matchers_1 = __webpack_require__(/*! ./matchers */ \"./node_modules/@segment/tsub/dist/matchers.js\");\nObject.defineProperty(exports, \"matches\", { enumerable: true, get: function () { return matchers_1.default; } });\nvar store_1 = __webpack_require__(/*! ./store */ \"./node_modules/@segment/tsub/dist/store.js\");\nObject.defineProperty(exports, \"Store\", { enumerable: true, get: function () { return store_1.default; } });\n//# sourceMappingURL=index.js.map\n\n//# sourceURL=webpack://edge_function/./node_modules/@segment/tsub/dist/index.js?");

/***/ }),

/***/ "./node_modules/@segment/tsub/dist/matchers.js":
/*!*****************************************************!*\
  !*** ./node_modules/@segment/tsub/dist/matchers.js ***!
  \*****************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\nObject.defineProperty(exports, \"__esModule\", { value: true });\nvar get = __webpack_require__(/*! dlv */ \"./node_modules/dlv/dist/dlv.umd.js\");\nfunction matches(event, matcher) {\n    if (!matcher) {\n        throw new Error('No matcher supplied!');\n    }\n    switch (matcher.type) {\n        case 'all':\n            return all();\n        case 'fql':\n            return fql(matcher.ir, event);\n        default:\n            throw new Error(\"Matcher of type \" + matcher.type + \" unsupported.\");\n    }\n}\nexports.default = matches;\nfunction all() {\n    return true;\n}\nfunction fql(ir, event) {\n    if (!ir) {\n        return false;\n    }\n    try {\n        ir = JSON.parse(ir);\n    }\n    catch (e) {\n        throw new Error(\"Failed to JSON.parse FQL intermediate representation \\\"\" + ir + \"\\\": \" + e);\n    }\n    var result = fqlEvaluate(ir, event);\n    if (typeof result !== 'boolean') {\n        // An error was returned, or a lowercase, typeof, or similar function was run alone. Nothing to evaluate.\n        return false;\n    }\n    return result;\n}\n// FQL is 100% type strict in Go. Show no mercy to types which do not comply.\nfunction fqlEvaluate(ir, event) {\n    // If the given ir chunk is not an array, then we should check the single given path or value for literally `true`.\n    if (!Array.isArray(ir)) {\n        return getValue(ir, event) === true;\n    }\n    // Otherwise, it is a sequence of ordered steps to follow to reach our solution!\n    var item = ir[0];\n    switch (item) {\n        /*** Unary cases ***/\n        // '!' => Invert the result\n        case '!':\n            return !fqlEvaluate(ir[1], event);\n        /*** Binary cases ***/\n        // 'or' => Any condition being true returns true\n        case 'or':\n            for (var i = 1; i < ir.length; i++) {\n                if (fqlEvaluate(ir[i], event)) {\n                    return true;\n                }\n            }\n            return false;\n        // 'and' => Any condition being false returns false\n        case 'and':\n            for (var i = 1; i < ir.length; i++) {\n                if (!fqlEvaluate(ir[i], event)) {\n                    return false;\n                }\n            }\n            return true;\n        // Equivalence comparisons\n        case '=':\n        case '!=':\n            return compareItems(getValue(ir[1], event), getValue(ir[2], event), item, event);\n        // Numerical comparisons\n        case '<=':\n        case '<':\n        case '>':\n        case '>=':\n            // Compare the two values with the given operator.\n            return compareNumbers(getValue(ir[1], event), getValue(ir[2], event), item, event);\n        /*** Functions ***/\n        // 'contains(str1, str2)' => The first string has a substring of the second string\n        case 'contains':\n            return contains(getValue(ir[1], event), getValue(ir[2], event));\n        // 'match(str, match)' => The given string matches the provided glob matcher\n        case 'match':\n            return match(getValue(ir[1], event), getValue(ir[2], event));\n        // 'lowercase(str)' => Returns a lowercased string, null if the item is not a string\n        case 'lowercase':\n            var target = getValue(ir[1], event);\n            if (typeof target !== 'string') {\n                return null;\n            }\n            return target.toLowerCase();\n        // 'typeof(val)' => Returns the FQL type of the value\n        case 'typeof':\n            // TODO: Do we need mapping to allow for universal comparisons? e.g. Object -> JSON, Array -> List, Floats?\n            return typeof getValue(ir[1], event);\n        // 'length(val)' => Returns the length of an array or string, NaN if neither\n        case 'length':\n            return length(getValue(ir[1], event));\n        // If nothing hit, we or the IR messed up somewhere.\n        default:\n            throw new Error(\"FQL IR could not evaluate for token: \" + item);\n    }\n}\nfunction getValue(item, event) {\n    // If item is an array, leave it as-is.\n    if (Array.isArray(item)) {\n        return item;\n    }\n    // If item is an object, it has the form of `{\"value\": VAL}`\n    if (typeof item === 'object') {\n        return item.value;\n    }\n    // Otherwise, it's an event path, e.g. \"properties.email\"\n    return get(event, item);\n}\nfunction compareNumbers(first, second, operator, event) {\n    // Check if it's more IR (such as a length() function)\n    if (isIR(first)) {\n        first = fqlEvaluate(first, event);\n    }\n    if (isIR(second)) {\n        second = fqlEvaluate(second, event);\n    }\n    if (typeof first !== 'number' || typeof second !== 'number') {\n        return false;\n    }\n    // Reminder: NaN is not comparable to any other number (including NaN) and will always return false as desired.\n    switch (operator) {\n        // '<=' => The first number is less than or equal to the second.\n        case '<=':\n            return first <= second;\n        // '>=' => The first number is greater than or equal to the second\n        case '>=':\n            return first >= second;\n        // '<' The first number is less than the second.\n        case '<':\n            return first < second;\n        // '>' The first number is greater than the second.\n        case '>':\n            return first > second;\n        default:\n            throw new Error(\"Invalid operator in compareNumbers: \" + operator);\n    }\n}\nfunction compareItems(first, second, operator, event) {\n    // Check if it's more IR (such as a lowercase() function)\n    if (isIR(first)) {\n        first = fqlEvaluate(first, event);\n    }\n    if (isIR(second)) {\n        second = fqlEvaluate(second, event);\n    }\n    if (typeof first === 'object' && typeof second === 'object') {\n        first = JSON.stringify(first);\n        second = JSON.stringify(second);\n    }\n    // Objects with the exact same contents AND order ARE considered identical. (Don't compare by reference)\n    // Even in Go, this MUST be the same byte order.\n    // e.g. {a: 1, b:2} === {a: 1, b:2} BUT {a:1, b:2} !== {b:2, a:1}\n    // Maybe later we'll use a stable stringifier, but we're matching server-side behavior for now.\n    switch (operator) {\n        // '=' => The two following items are exactly identical\n        case '=':\n            return first === second;\n        // '!=' => The two following items are NOT exactly identical.\n        case '!=':\n            return first !== second;\n        default:\n            throw new Error(\"Invalid operator in compareItems: \" + operator);\n    }\n}\nfunction contains(first, second) {\n    if (typeof first !== 'string' || typeof second !== 'string') {\n        return false;\n    }\n    return first.indexOf(second) !== -1;\n}\nfunction match(str, glob) {\n    if (typeof str !== 'string' || typeof glob !== 'string') {\n        return false;\n    }\n    return globMatches(glob, str);\n}\nfunction length(item) {\n    // Match server-side behavior.\n    if (item === null) {\n        return 0;\n    }\n    // Type-check to avoid returning .length of an object\n    if (!Array.isArray(item) && typeof item !== 'string') {\n        return NaN;\n    }\n    return item.length;\n}\n// This is a heuristic technically speaking, but should be close enough. The odds of someone trying to test\n// a func with identical IR notation is pretty low.\nfunction isIR(value) {\n    // TODO: This can be better checked by checking if this is a {\"value\": THIS}\n    if (!Array.isArray(value)) {\n        return false;\n    }\n    // Function checks\n    if ((value[0] === 'lowercase' || value[0] === 'length' || value[0] === 'typeof') &&\n        value.length === 2) {\n        return true;\n    }\n    if ((value[0] === 'contains' || value[0] === 'match') && value.length === 3) {\n        return true;\n    }\n    return false;\n}\n// Any reputable glob matcher is designed to work on filesystems and doesn't allow the override of the separator\n// character \"/\". This is problematic since our server-side representation e.g. evaluates \"match('ab/c', 'a*)\"\n// as TRUE, whereas any glob matcher for JS available does false. So we're rewriting it here.\n// See: https://github.com/segmentio/glob/blob/master/glob.go\nfunction globMatches(pattern, str) {\n    var _a, _b;\n    Pattern: while (pattern.length > 0) {\n        var star = void 0;\n        var chunk = void 0;\n        (_a = scanChunk(pattern), star = _a.star, chunk = _a.chunk, pattern = _a.pattern);\n        if (star && chunk === '') {\n            // Trailing * matches rest of string\n            return true;\n        }\n        // Look for match at current position\n        var _c = matchChunk(chunk, str), t = _c.t, ok = _c.ok, err = _c.err;\n        if (err) {\n            return false;\n        }\n        // If we're the last chunk, make sure we've exhausted the str\n        // otherwise we'll give a false result even if we could still match\n        // using the star\n        if (ok && (t.length === 0 || pattern.length > 0)) {\n            str = t;\n            continue;\n        }\n        if (star) {\n            // Look for match, skipping i+1 bytes.\n            for (var i = 0; i < str.length; i++) {\n                ;\n                (_b = matchChunk(chunk, str.slice(i + 1)), t = _b.t, ok = _b.ok, err = _b.err);\n                if (ok) {\n                    // If we're the last chunk, make sure we exhausted the str.\n                    if (pattern.length === 0 && t.length > 0) {\n                        continue;\n                    }\n                    str = t;\n                    continue Pattern;\n                }\n                if (err) {\n                    return false;\n                }\n            }\n        }\n        return false;\n    }\n    return str.length === 0;\n}\nfunction scanChunk(pattern) {\n    var result = {\n        star: false,\n        chunk: '',\n        pattern: '',\n    };\n    while (pattern.length > 0 && pattern[0] === '*') {\n        pattern = pattern.slice(1);\n        result.star = true;\n    }\n    var inRange = false;\n    var i;\n    Scan: for (i = 0; i < pattern.length; i++) {\n        switch (pattern[i]) {\n            case '\\\\':\n                // Error check handled in matchChunk: bad pattern.\n                if (i + 1 < pattern.length) {\n                    i++;\n                }\n                break;\n            case '[':\n                inRange = true;\n                break;\n            case ']':\n                inRange = false;\n                break;\n            case '*':\n                if (!inRange) {\n                    break Scan;\n                }\n        }\n    }\n    result.chunk = pattern.slice(0, i);\n    result.pattern = pattern.slice(i);\n    return result;\n}\n// matchChunk checks whether chunk matches the beginning of s.\n// If so, it returns the remainder of s (after the match).\n// Chunk is all single-character operators: literals, char classes, and ?.\nfunction matchChunk(chunk, str) {\n    var _a, _b;\n    var result = {\n        t: '',\n        ok: false,\n        err: false,\n    };\n    while (chunk.length > 0) {\n        if (str.length === 0) {\n            return result;\n        }\n        switch (chunk[0]) {\n            case '[':\n                var char = str[0];\n                str = str.slice(1);\n                chunk = chunk.slice(1);\n                var notNegated = true;\n                if (chunk.length > 0 && chunk[0] === '^') {\n                    notNegated = false;\n                    chunk = chunk.slice(1);\n                }\n                // Parse all ranges\n                var foundMatch = false;\n                var nRange = 0;\n                while (true) {\n                    if (chunk.length > 0 && chunk[0] === ']' && nRange > 0) {\n                        chunk = chunk.slice(1);\n                        break;\n                    }\n                    var lo = '';\n                    var hi = '';\n                    var err = void 0;\n                    (_a = getEsc(chunk), lo = _a.char, chunk = _a.newChunk, err = _a.err);\n                    if (err) {\n                        return result;\n                    }\n                    hi = lo;\n                    if (chunk[0] === '-') {\n                        ;\n                        (_b = getEsc(chunk.slice(1)), hi = _b.char, chunk = _b.newChunk, err = _b.err);\n                        if (err) {\n                            return result;\n                        }\n                    }\n                    if (lo <= char && char <= hi) {\n                        foundMatch = true;\n                    }\n                    nRange++;\n                }\n                if (foundMatch !== notNegated) {\n                    return result;\n                }\n                break;\n            case '?':\n                str = str.slice(1);\n                chunk = chunk.slice(1);\n                break;\n            case '\\\\':\n                chunk = chunk.slice(1);\n                if (chunk.length === 0) {\n                    result.err = true;\n                    return result;\n                }\n            // Fallthrough, missing break intentional.\n            default:\n                if (chunk[0] !== str[0]) {\n                    return result;\n                }\n                str = str.slice(1);\n                chunk = chunk.slice(1);\n        }\n    }\n    result.t = str;\n    result.ok = true;\n    result.err = false;\n    return result;\n}\n// getEsc gets a possibly-escaped character from chunk, for a character class.\nfunction getEsc(chunk) {\n    var result = {\n        char: '',\n        newChunk: '',\n        err: false,\n    };\n    if (chunk.length === 0 || chunk[0] === '-' || chunk[0] === ']') {\n        result.err = true;\n        return result;\n    }\n    if (chunk[0] === '\\\\') {\n        chunk = chunk.slice(1);\n        if (chunk.length === 0) {\n            result.err = true;\n            return result;\n        }\n    }\n    // Unlike Go, JS strings operate on characters instead of bytes.\n    // This is why we aren't copying over the GetRuneFromString stuff.\n    result.char = chunk[0];\n    result.newChunk = chunk.slice(1);\n    if (result.newChunk.length === 0) {\n        result.err = true;\n    }\n    return result;\n}\n//# sourceMappingURL=matchers.js.map\n\n//# sourceURL=webpack://edge_function/./node_modules/@segment/tsub/dist/matchers.js?");

/***/ }),

/***/ "./node_modules/@segment/tsub/dist/store.js":
/*!**************************************************!*\
  !*** ./node_modules/@segment/tsub/dist/store.js ***!
  \**************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\nObject.defineProperty(exports, \"__esModule\", { value: true });\nvar Store = /** @class */ (function () {\n    function Store(rules) {\n        this.rules = [];\n        this.rules = rules || [];\n    }\n    Store.prototype.getRulesByDestinationName = function (destinationName) {\n        var rules = [];\n        for (var _i = 0, _a = this.rules; _i < _a.length; _i++) {\n            var rule = _a[_i];\n            // Rules with no destinationName are global (workspace || workspace::source)\n            if (rule.destinationName === destinationName || rule.destinationName === undefined) {\n                rules.push(rule);\n            }\n        }\n        return rules;\n    };\n    return Store;\n}());\nexports.default = Store;\n//# sourceMappingURL=store.js.map\n\n//# sourceURL=webpack://edge_function/./node_modules/@segment/tsub/dist/store.js?");

/***/ }),

/***/ "./node_modules/@segment/tsub/dist/transformers.js":
/*!*********************************************************!*\
  !*** ./node_modules/@segment/tsub/dist/transformers.js ***!
  \*********************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\nObject.defineProperty(exports, \"__esModule\", { value: true });\nvar MD5 = __webpack_require__(/*! tiny-hashes/md5 */ \"./node_modules/tiny-hashes/md5/index.mjs\");\nvar get = __webpack_require__(/*! dlv */ \"./node_modules/dlv/dist/dlv.umd.js\");\nvar ldexp = __webpack_require__(/*! math-float64-ldexp */ \"./node_modules/math-float64-ldexp/lib/index.js\");\nvar set = __webpack_require__(/*! dset/dist/dset.js */ \"./node_modules/dset/dist/dset.js\");\nvar unset_1 = __webpack_require__(/*! ./unset */ \"./node_modules/@segment/tsub/dist/unset.js\");\nfunction transform(payload, transformers) {\n    var transformedPayload = payload;\n    for (var _i = 0, transformers_1 = transformers; _i < transformers_1.length; _i++) {\n        var transformer = transformers_1[_i];\n        switch (transformer.type) {\n            case 'drop':\n                return null;\n            case 'drop_properties':\n                dropProperties(transformedPayload, transformer.config);\n                break;\n            case 'allow_properties':\n                allowProperties(transformedPayload, transformer.config);\n                break;\n            case 'sample_event':\n                if (sampleEvent(transformedPayload, transformer.config)) {\n                    break;\n                }\n                return null;\n            case 'map_properties':\n                mapProperties(transformedPayload, transformer.config);\n                break;\n            case 'hash_properties':\n                // Not yet supported, but don't throw an error. Just ignore.\n                break;\n            default:\n                throw new Error(\"Transformer of type \\\"\" + transformer.type + \"\\\" is unsupported.\");\n        }\n    }\n    return transformedPayload;\n}\nexports.default = transform;\n// dropProperties removes all specified props from the object.\nfunction dropProperties(payload, config) {\n    for (var key in config.drop) {\n        if (!config.drop.hasOwnProperty(key)) {\n            continue;\n        }\n        // If key is empty, it refers to the top-level object.\n        var field = key === '' ? payload : get(payload, key);\n        // Can only drop props off of arrays and objects.\n        if (typeof field !== 'object' || field === null) {\n            continue;\n        }\n        for (var _i = 0, _a = config.drop[key]; _i < _a.length; _i++) {\n            var target = _a[_i];\n            delete field[target];\n        }\n    }\n}\n// allowProperties ONLY allows the specific targets within the keys. (e.g. \"a.foo\": [\"bar\", \"baz\"]\n// on {a: {foo: {bar: 1, baz: 2}, other: 3}} will not have any drops, as it only looks inside a.foo\nfunction allowProperties(payload, config) {\n    for (var key in config.allow) {\n        if (!config.allow.hasOwnProperty(key)) {\n            continue;\n        }\n        // If key is empty, it refers to the top-level object.\n        var field = key === '' ? payload : get(payload, key);\n        // Can only drop props off of arrays and objects.\n        if (typeof field !== 'object' || field === null) {\n            continue;\n        }\n        // Execution order fortunately doesn't really matter (e.g. if someone filtered off of foo.bar, then foo.bar.baz)\n        // except for micro-optimization.\n        for (var k in field) {\n            if (!field.hasOwnProperty(k)) {\n                continue;\n            }\n            if (config.allow[key].indexOf(k) === -1) {\n                delete field[k];\n            }\n        }\n    }\n}\nfunction mapProperties(payload, config) {\n    // Some configs might try to modify or read from a field multiple times. We will only ever read\n    // values as they were before any modifications began. Thus, if you try to override e.g.\n    // {a: {b: 1}} with set(a, 'b', 2) (which results in {a: {b: 2}}) and then try to copy a.b into\n    // a.c, you will get {a: {b: 2, c:1}} and NOT {a: {b:2, c:2}}. This prevents map evaluation\n    // order from mattering, and === what server-side does.\n    // See: https://github.com/segmentio/tsub/blob/661695a63b60b90471796e667458f076af788c19/transformers/map_properties.go#L179-L200\n    var initialPayload = JSON.parse(JSON.stringify(payload));\n    for (var key in config.map) {\n        if (!config.map.hasOwnProperty(key)) {\n            continue;\n        }\n        var actionMap = config.map[key];\n        // Can't manipulate non-objects. Check that the parent is one. Strip the last .field\n        // from the string.\n        var splitKey = key.split('.');\n        var parent_1 = void 0;\n        if (splitKey.length > 1) {\n            splitKey.pop();\n            parent_1 = get(initialPayload, splitKey.join('.'));\n        }\n        else {\n            parent_1 = payload;\n        }\n        if (typeof parent_1 !== 'object') {\n            continue;\n        }\n        // These actions are exclusive to each other.\n        if (actionMap.copy) {\n            var valueToCopy = get(initialPayload, actionMap.copy);\n            if (valueToCopy !== undefined) {\n                set(payload, key, valueToCopy);\n            }\n        }\n        else if (actionMap.move) {\n            var valueToMove = get(initialPayload, actionMap.move);\n            if (valueToMove !== undefined) {\n                set(payload, key, valueToMove);\n            }\n            unset_1.unset(payload, actionMap.move);\n        }\n        // Have to check only if property exists, as null, undefined, and other vals could be explicitly set.\n        else if (actionMap.hasOwnProperty('set')) {\n            set(payload, key, actionMap.set);\n        }\n        // to_string is not exclusive and can be paired with other actions. Final action.\n        if (actionMap.to_string) {\n            var valueToString = get(payload, key);\n            // Do not string arrays and objects. Do not double-encode strings.\n            if (typeof valueToString === 'string' ||\n                (typeof valueToString === 'object' && valueToString !== null)) {\n                continue;\n            }\n            // TODO: Check stringifier in Golang for parity.\n            if (valueToString !== undefined) {\n                set(payload, key, JSON.stringify(valueToString));\n            }\n            else {\n                // TODO: Check this behavior.\n                set(payload, key, 'undefined');\n            }\n        }\n    }\n}\nfunction sampleEvent(payload, config) {\n    if (config.sample.percent <= 0) {\n        return false;\n    }\n    else if (config.sample.percent >= 1) {\n        return true;\n    }\n    // If we're not filtering deterministically, just use raw percentage.\n    if (!config.sample.path) {\n        return samplePercent(config.sample.percent);\n    }\n    // Otherwise, use a deterministic hash.\n    return sampleConsistentPercent(payload, config);\n}\nfunction samplePercent(percent) {\n    // Math.random returns [0, 1) => 0.0<>0.9999...\n    return Math.random() <= percent;\n}\n// sampleConsistentPercent converts an input string of bytes into a consistent uniform\n// continuous distribution of [0.0, 1.0]. This is based on\n// http://mumble.net/~campbell/tmp/random_real.c, but using the digest\n// result of the input value as the random information.\n// IMPORTANT - This function needs to === the Golang implementation to ensure that the two return the same vals!\n// See: https://github.com/segmentio/sampler/blob/65cb04132305a04fcd4bcaef67d57fbe40c30241/sampler.go#L13-L38\n// Since AJS supports IE9+ (typed arrays were introduced in IE10) we're doing some manual array math.\n// This could be done directly with strings, but arrays are easier to reason about/have better function support.\nfunction sampleConsistentPercent(payload, config) {\n    var field = get(payload, config.sample.path);\n    // Operate off of JSON bytes. TODO: Validate all type behavior, esp. strings.\n    var digest = MD5(JSON.stringify(field));\n    var exponent = -64;\n    // Manually maintain 64-bit int as an array.\n    var significand = [];\n    // Left-shift and OR for first 8 bytes of digest. (8 bytes * 8 = 64 bits)\n    consumeDigest(digest.slice(0, 8), significand);\n    var leadingZeros = 0;\n    for (var i = 0; i < 64; i++) {\n        if (significand[i] === 1) {\n            break;\n        }\n        leadingZeros++;\n    }\n    if (leadingZeros !== 0) {\n        // Use the last 8 bytes of the digest, same as before.\n        var val = [];\n        consumeDigest(digest.slice(9, 16), val);\n        exponent -= leadingZeros;\n        // Left-shift away leading zeros in significand.\n        significand.splice(0, leadingZeros);\n        // Right-shift val by 64 minus leading zeros and push into significand.\n        val.splice(64 - leadingZeros);\n        significand = significand.concat(val);\n    }\n    // Flip 64th bit\n    significand[63] = significand[63] === 0 ? 1 : 0;\n    // Convert our manual binary into a JS num (binary arr => binary string => psuedo-int) and run the ldexp!\n    return ldexp(parseInt(significand.join(''), 2), exponent) < config.sample.percent;\n}\n// Array byte filler helper\nfunction consumeDigest(digest, arr) {\n    for (var i = 0; i < 8; i++) {\n        var remainder = digest[i];\n        for (var binary = 128; binary >= 1; binary /= 2) {\n            if (remainder - binary >= 0) {\n                remainder -= binary;\n                arr.push(1);\n            }\n            else {\n                arr.push(0);\n            }\n        }\n    }\n}\n//# sourceMappingURL=transformers.js.map\n\n//# sourceURL=webpack://edge_function/./node_modules/@segment/tsub/dist/transformers.js?");

/***/ }),

/***/ "./node_modules/@segment/tsub/dist/unset.js":
/*!**************************************************!*\
  !*** ./node_modules/@segment/tsub/dist/unset.js ***!
  \**************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\nObject.defineProperty(exports, \"__esModule\", { value: true });\nexports.unset = void 0;\nvar get = __webpack_require__(/*! dlv */ \"./node_modules/dlv/dist/dlv.umd.js\");\nfunction unset(obj, prop) {\n    if (get(obj, prop)) {\n        var segs = prop.split('.');\n        var last = segs.pop();\n        while (segs.length && segs[segs.length - 1].slice(-1) === '\\\\') {\n            last = segs.pop().slice(0, -1) + '.' + last;\n        }\n        while (segs.length)\n            obj = obj[(prop = segs.shift())];\n        return delete obj[last];\n    }\n    return true;\n}\nexports.unset = unset;\n//# sourceMappingURL=unset.js.map\n\n//# sourceURL=webpack://edge_function/./node_modules/@segment/tsub/dist/unset.js?");

/***/ }),

/***/ "./node_modules/const-ninf-float64/lib/index.js":
/*!******************************************************!*\
  !*** ./node_modules/const-ninf-float64/lib/index.js ***!
  \******************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\n\n// EXPORTS //\n\nmodule.exports = Number.NEGATIVE_INFINITY;\n\n\n//# sourceURL=webpack://edge_function/./node_modules/const-ninf-float64/lib/index.js?");

/***/ }),

/***/ "./node_modules/const-pinf-float64/lib/index.js":
/*!******************************************************!*\
  !*** ./node_modules/const-pinf-float64/lib/index.js ***!
  \******************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\n\n// EXPORTS //\n\nmodule.exports = Number.POSITIVE_INFINITY;\n\n\n//# sourceURL=webpack://edge_function/./node_modules/const-pinf-float64/lib/index.js?");

/***/ }),

/***/ "./node_modules/const-smallest-float64/lib/index.js":
/*!**********************************************************!*\
  !*** ./node_modules/const-smallest-float64/lib/index.js ***!
  \**********************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\n\n// MODULE //\n\nvar setReadOnly = __webpack_require__( /*! utils-define-read-only-property */ \"./node_modules/utils-define-read-only-property/lib/index.js\" );\n\n\n// CONSTANTS //\n\nvar constants = {};\n\n// 1 / Math.pow( 2, 1023-1 )\nsetReadOnly( constants, 'VALUE', 2.2250738585072014e-308 );\n\n// 1 / Math.pow( 2, 1023-1+52 )\nsetReadOnly( constants, 'DENORMALIZED', 4.940656458412465e-324 );\n\n\n// EXPORTS //\n\nmodule.exports = constants;\n\n\n//# sourceURL=webpack://edge_function/./node_modules/const-smallest-float64/lib/index.js?");

/***/ }),

/***/ "./node_modules/dlv/dist/dlv.umd.js":
/*!******************************************!*\
  !*** ./node_modules/dlv/dist/dlv.umd.js ***!
  \******************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

eval("!function(t,n){ true?module.exports=function(t,n,e,i,o){for(n=n.split?n.split(\".\"):n,i=0;i<n.length;i++)t=t?t[n[i]]:o;return t===o?e:t}:undefined}(this);\n//# sourceMappingURL=dlv.umd.js.map\n\n\n//# sourceURL=webpack://edge_function/./node_modules/dlv/dist/dlv.umd.js?");

/***/ }),

/***/ "./node_modules/dset/dist/dset.js":
/*!****************************************!*\
  !*** ./node_modules/dset/dist/dset.js ***!
  \****************************************/
/*! no static exports found */
/***/ (function(module, exports) {

eval("module.exports = function (obj, keys, val) {\n\tkeys.split && (keys=keys.split('.'));\n\tvar i=0, l=keys.length, t=obj, x, k;\n\tfor (; i < l;) {\n\t\tk = keys[i++];\n\t\tif (k === '__proto__' || k === 'constructor' || k === 'prototype') continue;\n\t\tt = t[k] = (i === l ? val : ((x=t[k]) != null ? x : (keys[i]*0 !== 0 || !!~keys[i].indexOf('.')) ? {} : []));\n\t}\n}\n\n\n//# sourceURL=webpack://edge_function/./node_modules/dset/dist/dset.js?");

/***/ }),

/***/ "./node_modules/math-abs/lib/index.js":
/*!********************************************!*\
  !*** ./node_modules/math-abs/lib/index.js ***!
  \********************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\n\n/**\n* FUNCTION: abs( x )\n*\tComputes the absolute value of `x`.\n*\n* @param {Number} x - input value\n* @returns {Number} absolute value\n*/\nfunction abs( x ) {\n\tif ( x < 0 ) {\n\t\treturn -x;\n\t}\n\tif ( x === 0 ) {\n\t\treturn 0; // handle negative zero\n\t}\n\treturn x;\n} // end FUNCTION abs()\n\n\n// EXPORTS //\n\nmodule.exports = abs;\n\n\n//# sourceURL=webpack://edge_function/./node_modules/math-abs/lib/index.js?");

/***/ }),

/***/ "./node_modules/math-float64-copysign/lib/index.js":
/*!*********************************************************!*\
  !*** ./node_modules/math-float64-copysign/lib/index.js ***!
  \*********************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\n\n// MODULES //\n\nvar toWords = __webpack_require__( /*! math-float64-to-words */ \"./node_modules/math-float64-to-words/lib/index.js\" );\nvar getHighWord = __webpack_require__( /*! math-float64-get-high-word */ \"./node_modules/math-float64-get-high-word/lib/index.js\" );\nvar fromWords = __webpack_require__( /*! math-float64-from-words */ \"./node_modules/math-float64-from-words/lib/index.js\" ); \n\n\n// VARIABLES //\n\n// 10000000000000000000000000000000 => 2147483648 => 0x80000000\nvar SIGN_MASK = 0x80000000;\n\n// 01111111111111111111111111111111 => 2147483647 => 0x7fffffff\nvar MAGNITUDE_MASK = 0x7fffffff;\n\n\n// COPYSIGN //\n\n/**\n* FUNCTION: copysign( x, y )\n*\tReturns a double-precision floating-point number with the magnitude of `x` and the sign of `y`.\n*\n* @param {Number} x - number from which to derive a magnitude\n* @param {Number} y - number from which to derive a sign\n* @returns {Number} a double-precision floating-point number\n*/\nfunction copysign( x, y ) {\n\tvar hx;\n\tvar hy;\n\n\t// Split `x` into higher and lower order words:\n\tx = toWords( x );\n\thx = x[ 0 ];\n\n\t// Turn off the sign bit of `x`:\n\thx &= MAGNITUDE_MASK;\n\n\t// Extract the higher order word from `y`:\n\thy = getHighWord( y );\n\n\t// Leave only the sign bit of `y` turned on:\n\thy &= SIGN_MASK;\n\n\t// Copy the sign bit of `y` to `x`:\n\thx |= hy;\n\n\t// Return a new value having the same magnitude as `x`, but with the sign of `y`:\n\treturn fromWords( hx, x[ 1 ] );\n} // end FUNCTION copysign()\n\n\n// EXPORTS //\n\nmodule.exports = copysign;\n\n\n//# sourceURL=webpack://edge_function/./node_modules/math-float64-copysign/lib/index.js?");

/***/ }),

/***/ "./node_modules/math-float64-exponent/lib/index.js":
/*!*********************************************************!*\
  !*** ./node_modules/math-float64-exponent/lib/index.js ***!
  \*********************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\n\n// MODULES //\n\nvar getHighWord = __webpack_require__( /*! math-float64-get-high-word */ \"./node_modules/math-float64-get-high-word/lib/index.js\" );\n\n\n// VARIABLES //\n\n// Exponent mask: 01111111111100000000000000000000\nvar EXP_MASK = 0x7ff00000;\nvar BIAS = 1023;\n\n\n// EXPONENT //\n\n/**\n* FUNCTION: exponent( x )\n*\tReturns an integer corresponding to the unbiased exponent of a double-precision floating-point number.\n*\n* @param {Number} x - input value\n* @returns {Number} unbiased exponent\n*/\nfunction exponent( x ) {\n\t// Extract from the input value a higher order word (unsigned 32-bit integer) which contains the exponent:\n\tvar high = getHighWord( x );\n\n\t// Apply a mask to isolate only the exponent bits and then shift off all bits which are part of the fraction:\n\thigh = ( high & EXP_MASK ) >>> 20;\n\n\t// Remove the bias and return:\n\treturn high - BIAS;\n} // end FUNCTION exponent()\n\n\n// EXPORTS //\n\nmodule.exports = exponent;\n\n\n//# sourceURL=webpack://edge_function/./node_modules/math-float64-exponent/lib/index.js?");

/***/ }),

/***/ "./node_modules/math-float64-from-words/lib/index.js":
/*!***********************************************************!*\
  !*** ./node_modules/math-float64-from-words/lib/index.js ***!
  \***********************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\n\n// MODULES //\n\nvar indices = __webpack_require__( /*! ./indices.js */ \"./node_modules/math-float64-from-words/lib/indices.js\" );\n\n\n// NOTES //\n\n/**\n* float64 (64 bits)\n* f := fraction (significand/mantissa) (52 bits)\n* e := exponent (11 bits)\n* s := sign bit (1 bit)\n*\n* |-------- -------- -------- -------- -------- -------- -------- --------|\n* |                                Float64                                |\n* |-------- -------- -------- -------- -------- -------- -------- --------|\n* |              Uint32               |               Uint32              |\n* |-------- -------- -------- -------- -------- -------- -------- --------|\n*\n* If little endian (more significant bits last):\n*                         <-- lower      higher -->\n* |   f7       f6       f5       f4       f3       f2    e2 | f1 |s|  e1  |\n*\n* If big endian (more significant bits first):\n*                         <-- higher      lower -->\n* |s| e1    e2 | f1     f2       f3       f4       f5        f6      f7   |\n*\n*\n* Note: in which Uint32 should we place the higher order bits? If LE, the second; if BE, the first.\n* Refs: http://pubs.opengroup.org/onlinepubs/9629399/chap14.htm\n*/\n\n\n// VARIABLES //\n\nvar FLOAT64_VIEW = new Float64Array( 1 );\nvar UINT32_VIEW = new Uint32Array( FLOAT64_VIEW.buffer );\n\nvar HIGH = indices.HIGH;\nvar LOW = indices.LOW;\n\n\n// TO FLOAT64 //\n\n/**\n* FUNCTION: toFloat64( high, low )\n*\tCreates a double-precision floating-point number from a higher order word (32-bit integer) and a lower order word (32-bit integer).\n*\n* @param {Number} high - higher order word (unsigned 32-bit integer)\n* @param {Number} low - lower order word (unsigned 32-bit integer)\n* @returns {Number} floating-point number\n*/\nfunction toFloat64( high, low ) {\n\tUINT32_VIEW[ HIGH ] = high;\n\tUINT32_VIEW[ LOW ] = low;\n\treturn FLOAT64_VIEW[ 0 ];\n} // end FUNCTION toFloat64()\n\n\n// EXPORTS //\n\nmodule.exports = toFloat64;\n\n\n//# sourceURL=webpack://edge_function/./node_modules/math-float64-from-words/lib/index.js?");

/***/ }),

/***/ "./node_modules/math-float64-from-words/lib/indices.js":
/*!*************************************************************!*\
  !*** ./node_modules/math-float64-from-words/lib/indices.js ***!
  \*************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\n\n// MODULES //\n\nvar isLittleEndian = __webpack_require__( /*! utils-is-little-endian */ \"./node_modules/utils-is-little-endian/lib/index.js\" );\n\n\n// INDICES //\n\nvar HIGH;\nvar LOW;\n\nif ( isLittleEndian === true ) {\n\tHIGH = 1; // second index\n\tLOW = 0; // first index\n} else {\n\tHIGH = 0; // first index\n\tLOW = 1; // second index\n}\n\n\n// EXPORTS //\n\nmodule.exports = {\n\t'HIGH': HIGH,\n\t'LOW': LOW\n};\n\n\n//# sourceURL=webpack://edge_function/./node_modules/math-float64-from-words/lib/indices.js?");

/***/ }),

/***/ "./node_modules/math-float64-get-high-word/lib/high.js":
/*!*************************************************************!*\
  !*** ./node_modules/math-float64-get-high-word/lib/high.js ***!
  \*************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\n\n// MODULES //\n\nvar isLittleEndian = __webpack_require__( /*! utils-is-little-endian */ \"./node_modules/utils-is-little-endian/lib/index.js\" );\n\n\n// INDEX //\n\nvar HIGH;\nif ( isLittleEndian === true ) {\n\tHIGH = 1; // second index\n} else {\n\tHIGH = 0; // first index\n}\n\n\n// EXPORTS //\n\nmodule.exports = HIGH;\n\n\n//# sourceURL=webpack://edge_function/./node_modules/math-float64-get-high-word/lib/high.js?");

/***/ }),

/***/ "./node_modules/math-float64-get-high-word/lib/index.js":
/*!**************************************************************!*\
  !*** ./node_modules/math-float64-get-high-word/lib/index.js ***!
  \**************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\n\n// MODULES //\n\nvar HIGH = __webpack_require__( /*! ./high.js */ \"./node_modules/math-float64-get-high-word/lib/high.js\" );\n\n\n// NOTES //\n\n/**\n* float64 (64 bits)\n* f := fraction (significand/mantissa) (52 bits)\n* e := exponent (11 bits)\n* s := sign bit (1 bit)\n*\n* |-------- -------- -------- -------- -------- -------- -------- --------|\n* |                                Float64                                |\n* |-------- -------- -------- -------- -------- -------- -------- --------|\n* |              Uint32               |               Uint32              |\n* |-------- -------- -------- -------- -------- -------- -------- --------|\n*\n* If little endian (more significant bits last):\n*                         <-- lower      higher -->\n* |   f7       f6       f5       f4       f3       f2    e2 | f1 |s|  e1  |\n*\n* If big endian (more significant bits first):\n*                         <-- higher      lower -->\n* |s| e1    e2 | f1     f2       f3       f4       f5        f6      f7   |\n*\n*\n* Note: in which Uint32 can we find the higher order bits? If LE, the second; if BE, the first.\n* Refs: http://pubs.opengroup.org/onlinepubs/9629399/chap14.htm\n*/\n\n\n// VARIABLES //\n\nvar FLOAT64_VIEW = new Float64Array( 1 );\nvar UINT32_VIEW = new Uint32Array( FLOAT64_VIEW.buffer );\n\n\n// HIGH WORD //\n\n/**\n* FUNCTION: highWord( x )\n*\tReturns an unsigned 32-bit integer corresponding to the more significant 32 bits of a double-precision floating-point number.\n*\n* @param {Number} x - input value\n* @returns {Number} higher order word\n*/\nfunction highWord( x ) {\n\tFLOAT64_VIEW[ 0 ] = x;\n\treturn UINT32_VIEW[ HIGH ];\n} // end FUNCTION highWord()\n\n\n// EXPORTS //\n\nmodule.exports = highWord;\n\n\n//# sourceURL=webpack://edge_function/./node_modules/math-float64-get-high-word/lib/index.js?");

/***/ }),

/***/ "./node_modules/math-float64-ldexp/lib/index.js":
/*!******************************************************!*\
  !*** ./node_modules/math-float64-ldexp/lib/index.js ***!
  \******************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\n\n// NOTES //\n\n/**\n* Notes:\n*\t=> ldexp: load exponent (see [The Open Group]{@link http://pubs.opengroup.org/onlinepubs/9699919799/functions/ldexp.html}).\n*/\n\n\n// MODULES //\n\nvar PINF = __webpack_require__( /*! const-pinf-float64 */ \"./node_modules/const-pinf-float64/lib/index.js\" );\nvar NINF = __webpack_require__( /*! const-ninf-float64 */ \"./node_modules/const-ninf-float64/lib/index.js\" );\nvar normalize = __webpack_require__( /*! math-float64-normalize */ \"./node_modules/math-float64-normalize/lib/index.js\" );\nvar floatExp = __webpack_require__( /*! math-float64-exponent */ \"./node_modules/math-float64-exponent/lib/index.js\" );\nvar copysign = __webpack_require__( /*! math-float64-copysign */ \"./node_modules/math-float64-copysign/lib/index.js\" );\nvar toWords = __webpack_require__( /*! math-float64-to-words */ \"./node_modules/math-float64-to-words/lib/index.js\" );\nvar fromWords = __webpack_require__( /*! math-float64-from-words */ \"./node_modules/math-float64-from-words/lib/index.js\" );\n\n\n// VARIABLES //\n\nvar BIAS = 1023;\n\n// -(BIAS+(52-1)) = -(1023+51) = -1074\nvar MIN_SUBNORMAL_EXPONENT = -1074;\n\n// -BIAS = -1023\nvar MAX_SUBNORMAL_EXPONENT = -BIAS;\n\n// 11111111110 => 2046 - BIAS = 1023\nvar MAX_EXPONENT = BIAS;\n\n// 1/(1<<52) = 1/(2**52) = 1/4503599627370496\nvar TWO52_INV = 2.220446049250313e-16;\n\n// Exponent all 0s: 10000000000011111111111111111111\nvar CLEAR_EXP_MASK = 0x800fffff; // 2148532223\n\n\n// LDEXP //\n\n/**\n* FUNCTION: ldexp( frac, exp )\n*\tMultiplies a double-precision floating-point number by an integer power of two.\n*\n* @param {Number} frac - fraction\n* @param {Number} exp - exponent\n* @returns {Number} double-precision floating-point number\n*/\nfunction ldexp( frac, exp ) {\n\tvar high;\n\tvar tmp;\n\tvar w;\n\tvar m;\n\tif (\n\t\tfrac === 0 || // handles +-0\n\t\tfrac !== frac || // handles NaN\n\t\tfrac === PINF ||\n\t\tfrac === NINF\n\t) {\n\t\treturn frac;\n\t}\n\t// Normalize the input fraction:\n\ttmp = normalize( frac );\n\tfrac = tmp[ 0 ];\n\texp += tmp[ 1 ];\n\n\t// Extract the exponent from `frac` and add it to `exp`:\n\texp += floatExp( frac );\n\n\t// Check for underflow/overflow...\n\tif ( exp < MIN_SUBNORMAL_EXPONENT ) {\n\t\treturn copysign( 0, frac );\n\t}\n\tif ( exp > MAX_EXPONENT ) {\n\t\tif ( frac < 0 ) {\n\t\t\treturn NINF;\n\t\t}\n\t\treturn PINF;\n\t}\n\t// Check for a subnormal and scale accordingly to retain precision...\n\tif ( exp <= MAX_SUBNORMAL_EXPONENT ) {\n\t\texp += 52;\n\t\tm = TWO52_INV;\n\t} else {\n\t\tm = 1.0;\n\t}\n\t// Split the fraction into higher and lower order words:\n\tw = toWords( frac );\n\thigh = w[ 0 ];\n\n\t// Clear the exponent bits within the higher order word:\n\thigh &= CLEAR_EXP_MASK;\n\n\t// Set the exponent bits to the new exponent:\n\thigh |= ((exp+BIAS) << 20);\n\n\t// Create a new floating-point number:\n\treturn m * fromWords( high, w[ 1 ] );\n} // end FUNCTION ldexp()\n\n\n// EXPORTS //\n\nmodule.exports = ldexp;\n\n\n//# sourceURL=webpack://edge_function/./node_modules/math-float64-ldexp/lib/index.js?");

/***/ }),

/***/ "./node_modules/math-float64-normalize/lib/index.js":
/*!**********************************************************!*\
  !*** ./node_modules/math-float64-normalize/lib/index.js ***!
  \**********************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\n\n// MODULES //\n\nvar SMALLEST_FLOAT64 = __webpack_require__( /*! const-smallest-float64 */ \"./node_modules/const-smallest-float64/lib/index.js\" ).VALUE;\nvar isinfinite = __webpack_require__( /*! validate.io-infinite */ \"./node_modules/validate.io-infinite/lib/index.js\" );\nvar abs = __webpack_require__( /*! math-abs */ \"./node_modules/math-abs/lib/index.js\" );\n\n\n// CONSTANTS //\n\n// (1<<52)\nvar SCALAR = 4503599627370496;\n\n\n// NORMALIZE //\n\n/**\n* FUNCTION: normalize( x )\n*\tReturns a normal number `y` and exponent `exp` satisfying `x = y * 2^exp`.\n*\n* @param {Number} x - input value\n* @returns {Number[]|Null} a two-element array containing `y` and `exp`\n*/\nfunction normalize( x ) {\n\tif ( x !== x || isinfinite( x ) ) {\n\t\treturn [ x, 0 ];\n\t}\n\tif ( x !== 0 && abs( x ) < SMALLEST_FLOAT64 ) {\n\t\treturn [ x*SCALAR, -52 ];\n\t}\n\treturn [ x, 0 ];\n} // end FUNCTION normalize()\n\n\n// EXPORTS //\n\nmodule.exports = normalize;\n\n\n//# sourceURL=webpack://edge_function/./node_modules/math-float64-normalize/lib/index.js?");

/***/ }),

/***/ "./node_modules/math-float64-to-words/lib/index.js":
/*!*********************************************************!*\
  !*** ./node_modules/math-float64-to-words/lib/index.js ***!
  \*********************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\n\n// MODULES //\n\nvar indices = __webpack_require__( /*! ./indices.js */ \"./node_modules/math-float64-to-words/lib/indices.js\" );\n\n\n// NOTES //\n\n/**\n* float64 (64 bits)\n* f := fraction (significand/mantissa) (52 bits)\n* e := exponent (11 bits)\n* s := sign bit (1 bit)\n*\n* |-------- -------- -------- -------- -------- -------- -------- --------|\n* |                                Float64                                |\n* |-------- -------- -------- -------- -------- -------- -------- --------|\n* |              Uint32               |               Uint32              |\n* |-------- -------- -------- -------- -------- -------- -------- --------|\n*\n* If little endian (more significant bits last):\n*                         <-- lower      higher -->\n* |   f7       f6       f5       f4       f3       f2    e2 | f1 |s|  e1  |\n*\n* If big endian (more significant bits first):\n*                         <-- higher      lower -->\n* |s| e1    e2 | f1     f2       f3       f4       f5        f6      f7   |\n*\n*\n* Note: in which Uint32 can we find the higher order bits? If LE, the second; if BE, the first.\n* Refs: http://pubs.opengroup.org/onlinepubs/9629399/chap14.htm\n*/\n\n\n// VARIABLES //\n\nvar FLOAT64_VIEW = new Float64Array( 1 );\nvar UINT32_VIEW = new Uint32Array( FLOAT64_VIEW.buffer );\n\nvar HIGH = indices.HIGH;\nvar LOW = indices.LOW;\n\n\n// WORDS //\n\n/**\n* FUNCTION: words( x )\n*\tSplits a floating-point number into a higher order word (32-bit integer) and a lower order word (32-bit integer).\n*\n* @param {Number} x - input value\n* @returns {Number[]} two-element array containing a higher order word and a lower order word\n*/\nfunction words( x ) {\n\tFLOAT64_VIEW[ 0 ] = x;\n\treturn [ UINT32_VIEW[ HIGH ], UINT32_VIEW[ LOW ] ];\n} // end FUNCTION words()\n\n\n// EXPORTS //\n\nmodule.exports = words;\n\n\n//# sourceURL=webpack://edge_function/./node_modules/math-float64-to-words/lib/index.js?");

/***/ }),

/***/ "./node_modules/math-float64-to-words/lib/indices.js":
/*!***********************************************************!*\
  !*** ./node_modules/math-float64-to-words/lib/indices.js ***!
  \***********************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\n\n// MODULES //\n\nvar isLittleEndian = __webpack_require__( /*! utils-is-little-endian */ \"./node_modules/utils-is-little-endian/lib/index.js\" );\n\n\n// INDICES //\n\nvar HIGH;\nvar LOW;\n\nif ( isLittleEndian ) {\n\tHIGH = 1; // second index\n\tLOW = 0; // first index\n} else {\n\tHIGH = 0; // first index\n\tLOW = 1; // second index\n}\n\n\n// EXPORTS //\n\nmodule.exports = {\n\t'HIGH': HIGH,\n\t'LOW': LOW\n};\n\n\n//# sourceURL=webpack://edge_function/./node_modules/math-float64-to-words/lib/indices.js?");

/***/ }),

/***/ "./node_modules/tiny-hashes/md5/index.mjs":
/*!************************************************!*\
  !*** ./node_modules/tiny-hashes/md5/index.mjs ***!
  \************************************************/
/*! exports provided: default */
/***/ (function(__webpack_module__, __webpack_exports__, __webpack_require__) {

"use strict";
eval("__webpack_require__.r(__webpack_exports__);\nfor(var r=[],o=0;o<64;)r[o]=0|4294967296*Math.sin(++o%Math.PI);/* harmony default export */ __webpack_exports__[\"default\"] = (function(t){var e,f,n,a=[e=1732584193,f=4023233417,~e,~f],c=[],h=unescape(encodeURI(t))+\"\",u=h.length;for(t=--u/4+2|15,c[--t]=8*u;~u;)c[u>>2]|=h.charCodeAt(u)<<8*u--;for(o=h=0;o<t;o+=16){for(u=a;h<64;u=[n=u[3],e+((n=u[0]+[e&f|~e&n,n&e|~n&f,e^f^n,f^(e|~n)][u=h>>4]+r[h]+~~c[o|15&[h,5*h+1,3*h+5,7*h][u]])<<(u=[7,12,17,22,5,9,14,20,4,11,16,23,6,10,15,21][4*u+h++%4])|n>>>-u),e,f])e=0|u[1],f=u[2];for(h=4;h;)a[--h]+=u[h]}for(t=\"\";h<32;)t+=(a[h>>3]>>4*(1^h++)&15).toString(16);return t});\n//# sourceMappingURL=index.mjs.map\n\n\n//# sourceURL=webpack://edge_function/./node_modules/tiny-hashes/md5/index.mjs?");

/***/ }),

/***/ "./node_modules/utils-define-read-only-property/lib/index.js":
/*!*******************************************************************!*\
  !*** ./node_modules/utils-define-read-only-property/lib/index.js ***!
  \*******************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\n\n/**\n* FUNCTION: setReadOnly( obj, prop, value )\n*\tDefines a read-only property.\n*\n* @param {Object} obj - object on which to define the property\n* @param {String} prop - property name\n* @param {*} value - value to set\n* @returns {Void}\n*/\nfunction setReadOnly( obj, prop, value ) {\n\tObject.defineProperty( obj, prop, {\n\t\t'value': value,\n\t\t'configurable': false,\n\t\t'writable': false,\n\t\t'enumerable': true\n\t});\n} // end FUNCTION setReadOnly()\n\n\n// EXPORTS //\n\nmodule.exports = setReadOnly;\n\n\n//# sourceURL=webpack://edge_function/./node_modules/utils-define-read-only-property/lib/index.js?");

/***/ }),

/***/ "./node_modules/utils-is-little-endian/lib/ctors.js":
/*!**********************************************************!*\
  !*** ./node_modules/utils-is-little-endian/lib/ctors.js ***!
  \**********************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\n\nvar ctors = {\n\t'uint16': Uint16Array,\n\t'uint8': Uint8Array\n};\n\n\n// EXPORTS //\n\nmodule.exports = ctors;\n\n\n//# sourceURL=webpack://edge_function/./node_modules/utils-is-little-endian/lib/ctors.js?");

/***/ }),

/***/ "./node_modules/utils-is-little-endian/lib/index.js":
/*!**********************************************************!*\
  !*** ./node_modules/utils-is-little-endian/lib/index.js ***!
  \**********************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\n\n// MODULES //\n\nvar ctors = __webpack_require__( /*! ./ctors.js */ \"./node_modules/utils-is-little-endian/lib/ctors.js\" );\n\n\n// IS LITTLE ENDIAN //\n\n/**\n* FUNCTION: isLittleEndian()\n*\tReturns a boolean indicating if an environment is little endian.\n*\n* @returns {Boolean} boolean indicating if an environment is little endian\n*/\nfunction isLittleEndian() {\n\tvar uint16_view;\n\tvar uint8_view;\n\n\tuint16_view = new ctors[ 'uint16' ]( 1 );\n\n\t// Set the uint16 view to a value having distinguishable lower and higher order words.\n\t// 4660 => 0x1234 => 0x12 0x34 => '00010010 00110100' => (0x12,0x34) == (18,52)\n\tuint16_view[ 0 ] = 0x1234;\n\n\t// Create a uint8 view on top of the uint16 buffer:\n\tuint8_view = new ctors[ 'uint8' ]( uint16_view.buffer );\n\n\t// If little endian, the least significant byte will be first...\n\treturn ( uint8_view[ 0 ] === 0x34 );\n} // end FUNCTION isLittleEndian()\n\n\n// EXPORTS //\n\nmodule.exports = isLittleEndian();\n\n\n//# sourceURL=webpack://edge_function/./node_modules/utils-is-little-endian/lib/index.js?");

/***/ }),

/***/ "./node_modules/validate.io-infinite/lib/index.js":
/*!********************************************************!*\
  !*** ./node_modules/validate.io-infinite/lib/index.js ***!
  \********************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\n\n// VARIABLES //\n\nvar pinf = Number.POSITIVE_INFINITY;\nvar ninf = Number.NEGATIVE_INFINITY;\n\n\n// IS INFINITE //\n\n/**\n* FUNCTION: isInfinite( x )\n*\tValidates if a value is infinite.\n*\n* @param {*} x - value to validate\n* @returns {Boolean} boolean indicating if a value is infinite\n*/\nfunction isInfinite( x ) {\n\treturn ( x === pinf || x === ninf );\n} // end FUNCTION isInfinite()\n\n\n// EXPORTS //\n\nmodule.exports = isInfinite;\n\n\n//# sourceURL=webpack://edge_function/./node_modules/validate.io-infinite/lib/index.js?");

/***/ }),

/***/ "./src/index.ts":
/*!**********************!*\
  !*** ./src/index.ts ***!
  \**********************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
eval("\nObject.defineProperty(exports, \"__esModule\", { value: true });\nconst tsub_1 = __webpack_require__(/*! @segment/tsub */ \"./node_modules/@segment/tsub/dist/index.js\");\nvar routingRules = [\n    {\n        \"matchers\": [\n            {\n                \"ir\": \"[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Item Impression\\\"}]\",\n                \"type\": \"fql\",\n                \"config\": {\n                    \"expr\": \"event = \\\"Item Impression\\\"\"\n                }\n            }\n        ],\n        \"scope\": \"destinations\",\n        \"target_type\": \"workspace::project::destination::config\",\n        \"transformers\": [\n            [\n                {\n                    \"type\": \"drop\"\n                }\n            ]\n        ],\n        \"destinationName\": \"Google Tag Manager\"\n    },\n    {\n        \"matchers\": [\n            {\n                \"ir\": \"[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Search - Query\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Search - Select Location\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Search - View\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Search - Click Location Box\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Search - Select Keyword\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Search - View Map\\\"}],[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Search - Change List View\\\"}]]]]]]]\",\n                \"type\": \"fql\",\n                \"config\": {\n                    \"expr\": \"event = \\\"Search - Query\\\" or event = \\\"Search - Select Location\\\" or event = \\\"Search - View\\\" or event = \\\"Search - Click Location Box\\\" or event = \\\"Search - Select Keyword\\\" or event = \\\"Search - View Map\\\" or event = \\\"Search - Change List View\\\"\"\n                }\n            },\n            {\n                \"ir\": \"[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Item Impression\\\"}]\",\n                \"type\": \"fql\",\n                \"config\": {\n                    \"expr\": \"event = \\\"Item Impression\\\"\"\n                }\n            },\n            {\n                \"ir\": \"[\\\"=\\\",\\\"properties.gtm\\\",{\\\"value\\\":true}]\",\n                \"type\": \"fql\",\n                \"config\": {\n                    \"expr\": \"properties.gtm = true\"\n                }\n            },\n            {\n                \"ir\": \"[\\\"=\\\",\\\"properties.ec\\\",{\\\"value\\\":true}]\",\n                \"type\": \"fql\",\n                \"config\": {\n                    \"expr\": \"properties.ec = true\"\n                }\n            },\n            {\n                \"ir\": \"[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"IaF - Landing Page Shown\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Favorite\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Schedule a Call - CTA Click\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Email Me - CTA Click\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Log In\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Partner With Us - CTA Click\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Verify Phone - Submit Phone\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Verify Phone - Submit Code\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Purchase - Start\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Reserve Schedule\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Search - Change List View\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Search - View\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Search - View Map\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Search - Query\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Item Impression\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Search - Click Location Box\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Search - Click Keyword Box\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Search - Select Keyword\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Search - Select Location\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Manage Membership - Start\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Manage Membership - Mobile - Cancel Request Screen Impression\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Manage Membership - Engage Cancel\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Manage Membership - Mobile - Self-Cancel Survey\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Manage Membership - Mobile - Cancel Chat\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Manage Membership - Cancel Chat - Error Timeout\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Manage Membership - Initiate Cancel\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Manage Membership - Desktop - Cancel Chat Window Impression\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Manage Membership - Cancel\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Manage Membership - Complete\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Manage Membership - Offer Shown\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Manage Membership - Select Plan\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Manage Membership - Lite Plan\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Manage Add Ons - Review Selection\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Manage Add Ons - Complete\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Manage Add Ons - Start\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Manage Add Ons - Select Add On\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Hold Survey\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"CP Live - Start video\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"CP Live - Enter class\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Video Search\\\"}],[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Manage Membership - Click Hold / Cancel\\\"}]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]\",\n                \"type\": \"fql\",\n                \"config\": {\n                    \"expr\": \"event = \\\"IaF - Landing Page Shown\\\" or event = \\\"Favorite\\\" or event = \\\"Schedule a Call - CTA Click\\\" or event = \\\"Email Me - CTA Click\\\" or event = \\\"Log In\\\" or event = \\\"Partner With Us - CTA Click\\\" or event = \\\"Verify Phone - Submit Phone\\\" or event = \\\"Verify Phone - Submit Code\\\" or event = \\\"Purchase - Start\\\" or event = \\\"Reserve Schedule\\\" or event = \\\"Search - Change List View\\\" or event = \\\"Search - View\\\" or event = \\\"Search - View Map\\\" or event = \\\"Search - Query\\\" or event = \\\"Item Impression\\\" or event = \\\"Search - Click Location Box\\\" or event = \\\"Search - Click Keyword Box\\\" or event = \\\"Search - Select Keyword\\\" or event = \\\"Search - Select Location\\\" or event = \\\"Manage Membership - Start\\\" or event = \\\"Manage Membership - Mobile - Cancel Request Screen Impression\\\" or event = \\\"Manage Membership - Engage Cancel\\\" or event = \\\"Manage Membership - Mobile - Self-Cancel Survey\\\" or event = \\\"Manage Membership - Mobile - Cancel Chat\\\" or event = \\\"Manage Membership - Cancel Chat - Error Timeout\\\" or event = \\\"Manage Membership - Initiate Cancel\\\" or event = \\\"Manage Membership - Desktop - Cancel Chat Window Impression\\\" or event = \\\"Manage Membership - Cancel\\\" or event = \\\"Manage Membership - Complete\\\" or event = \\\"Manage Membership - Offer Shown\\\" or event = \\\"Manage Membership - Select Plan\\\" or event = \\\"Manage Membership - Lite Plan\\\" or event = \\\"Manage Add Ons - Review Selection\\\" or event = \\\"Manage Add Ons - Complete\\\" or event = \\\"Manage Add Ons - Start\\\" or event = \\\"Manage Add Ons - Select Add On\\\" or event = \\\"Hold Survey\\\" or event = \\\"CP Live - Start video\\\" or event = \\\"CP Live - Enter class\\\" or event = \\\"Video Search\\\" or event = \\\"Manage Membership - Click Hold / Cancel\\\"\"\n                }\n            },\n            {\n                \"ir\": \"[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"successful_purchase\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"view_cart\\\"}],[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"account_created\\\"}],[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"Gift Purchase Success\\\"}]]]]\",\n                \"type\": \"fql\",\n                \"config\": {\n                    \"expr\": \"event = \\\"successful_purchase\\\" or event = \\\"view_cart\\\" or event = \\\"account_created\\\" or event = \\\"Gift Purchase Success\\\"\"\n                }\n            }\n        ],\n        \"scope\": \"destinations\",\n        \"target_type\": \"workspace::project::destination::config\",\n        \"transformers\": [\n            [\n                {\n                    \"type\": \"drop\"\n                }\n            ],\n            [\n                {\n                    \"type\": \"drop\"\n                }\n            ],\n            [\n                {\n                    \"type\": \"drop\"\n                }\n            ],\n            [\n                {\n                    \"type\": \"drop\"\n                }\n            ],\n            [\n                {\n                    \"type\": \"drop\"\n                }\n            ],\n            [\n                {\n                    \"type\": \"drop\"\n                }\n            ]\n        ],\n        \"destinationName\": \"Google Analytics\"\n    },\n    {\n        \"matchers\": [\n            {\n                \"ir\": \"[\\\"or\\\",[\\\"=\\\",\\\"type\\\",{\\\"value\\\":\\\"page\\\"}],[\\\"=\\\",\\\"type\\\",{\\\"value\\\":\\\"alias\\\"}]]\",\n                \"type\": \"fql\",\n                \"config\": {\n                    \"expr\": \"type = \\\"page\\\" or type = \\\"alias\\\"\"\n                }\n            }\n        ],\n        \"scope\": \"destinations\",\n        \"target_type\": \"workspace::project::destination::config\",\n        \"transformers\": [\n            [\n                {\n                    \"type\": \"drop\"\n                }\n            ]\n        ],\n        \"destinationName\": \"Webhooks\"\n    },\n    {\n        \"matchers\": [\n            {\n                \"ir\": \"[\\\"!\\\",[\\\"or\\\",[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"CP VOD - Start video\\\"}],[\\\"=\\\",\\\"event\\\",{\\\"value\\\":\\\"CP VOD - Track video\\\"}]]]\",\n                \"type\": \"fql\",\n                \"config\": {\n                    \"expr\": \"!(event = \\\"CP VOD - Start video\\\" or event = \\\"CP VOD - Track video\\\")\"\n                }\n            }\n        ],\n        \"scope\": \"destinations\",\n        \"target_type\": \"workspace::project::destination::config\",\n        \"transformers\": [\n            [\n                {\n                    \"type\": \"drop\"\n                }\n            ]\n        ],\n        \"destinationName\": \"Amazon Kinesis\"\n    }\n];\nvar event = {\n    \"anonymousId\": \"23adfd82-aa0f-45a7-a756-24f2a7a4c895\",\n    \"context\": {\n        \"library\": {\n            \"name\": \"analytics.js\",\n            \"version\": \"2.11.1\"\n        },\n        \"page\": {\n            \"path\": \"/academy/\",\n            \"referrer\": \"\",\n            \"search\": \"\",\n            \"title\": \"Analytics Academy\",\n            \"url\": \"https://segment.com/academy/\"\n        },\n        \"userAgent\": \"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36\",\n        \"ip\": \"108.0.78.21\"\n    },\n    \"event\": \"Course Clicked\",\n    \"integrations\": {},\n    \"messageId\": \"ajs-f8ca1e4de5024d9430b3928bd8ac6b96\",\n    \"properties\": {\n        \"title\": \"Intro to Analytics\",\n        \"summary\": \"An introduction to analytics.js\",\n        \"firstName\": \"Mark\",\n        \"lastName\": \"McGark\",\n        \"phoneNumber\": \"123-456-7890\"\n    },\n    \"receivedAt\": \"2015-12-12T19:11:01.266Z\",\n    \"sentAt\": \"2015-12-12T19:11:01.169Z\",\n    \"timestamp\": \"2015-12-12T19:11:01.249Z\",\n    \"type\": \"track\",\n    \"userId\": \"AiUGstSDIg\",\n    \"originalTimestamp\": \"2015-12-12T19:11:01.152Z\"\n};\nvar store = new tsub_1.Store(routingRules);\nfunction fnMatch(payload) {\n    // @ts-ignore\n    var rulesToApply = store.getRulesByDestinationName('Amazon Kinesis');\n    var result = '';\n    for (var i = 0; i < rulesToApply.length; i++) {\n        var rule = rulesToApply[i];\n        var matchers = rule.matchers;\n        for (var j = 0; j < matchers.length; j++) {\n            if (tsub_1.matches(payload, matchers[j])) {\n                console.log(`Match found ${payload.event}`);\n                result = payload.event;\n            }\n        }\n    }\n    return result;\n}\nconsole.log(`PRAY-fnMatch ${fnMatch(event)}`);\nexports.default = {\n    fnMatch\n};\n\n\n//# sourceURL=webpack://edge_function/./src/index.ts?");

/***/ })

/******/ })["default"];