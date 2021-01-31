ISTest = {}
DerivedTest = SuperDerivedTest:derive()

---@class LuaInclusionTest : SuperTest
LuaInclusionTest = SuperTest:new("parameter")

function ISTest.doSomething(param)
	local columnName, filterTxt
	InnerTest = {}
	if param then
		if param.selected == 2 then
			filterTxt = "true";
		elseif combo.selected == 3 then
			filterTxt = "false";
		end
	else
		filterTxt = "text";
	end
	columnName = "name"
end
