package org.xson.common.validate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.xson.common.object.XCO;
import org.xson.common.validate.Container;
import org.xson.common.validate.RuleGroup;
import org.xson.web.util.StringUtils;

public class URLParameterHandler {

	public static XCO parseXCOParameter(HttpServletRequest request, String ruleGroupId) {
		RuleGroup group = Container.ruleGroupsMap.get(ruleGroupId);
		if (group == null) {
			throw new RuntimeException("校验模板不存在: " + ruleGroupId);
		}
		XCO xco = new XCO();
		List<RuleGroupItem> items = group.getItems();
		for (RuleGroupItem item : items) {
			String fieldName = item.getFieldName();
			if (null != fieldName) {
				String tmp = StringUtils.trim(request.getParameter(fieldName));
				if (null != tmp) {
					setXCOValue(xco, fieldName, item.getType(), tmp);
				}
			}
		}
		return xco;
	}

	private static void setXCOValue(XCO xco, String fieldName, TypeEnum type, String value) {
		if (type == TypeEnum.INTEGER) {
			xco.setIntegerValue(fieldName, Integer.parseInt(value));
		} else if (type == TypeEnum.LONG) {
			xco.setLongValue(fieldName, Long.parseLong(value));
		} else if (type == TypeEnum.FLOAT) {
			xco.setFloatValue(fieldName, Float.parseFloat(value));
		} else if (type == TypeEnum.DOUBLE) {
			xco.setDoubleValue(fieldName, Double.parseDouble(value));
		} else if (type == TypeEnum.STRING) {
			xco.setStringValue(fieldName, value);
		} else if (type == TypeEnum.BIGINTEGER) {
			xco.setBigIntegerValue(fieldName, new BigInteger(value));
		} else if (type == TypeEnum.BIGDECIMAL) {
			xco.setBigDecimalValue(fieldName, new BigDecimal(value));
		}
		// TODO 这里暂时不考虑集合、时间
	}
}
