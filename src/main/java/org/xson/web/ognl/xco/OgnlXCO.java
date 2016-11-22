package org.xson.web.ognl.xco;

import java.util.Collection;
import java.util.List;

import org.xson.common.object.XCO;
import org.xson.web.ognl.FieldVo;
import org.xson.web.ognl.FieldVoWrapper;
import org.xson.web.ognl.Ognl;
import org.xson.web.ognl.OgnlException;
import org.xson.web.ognl.var.VariableUnitVo;
import org.xson.web.ognl.var.VariableUnitVo.VariableUnitEnum;
import org.xson.web.ognl.var.VariableVo;
import org.xson.web.util.TypeUtils;


public class OgnlXCO {

	/**
	 * bean到XCO转换(目前只支持,简单结构)
	 */
	public static XCO beanToXCO(Object bean) {
		if (null == bean) {
			return null;
		}
		XCO xco = new XCO();
		FieldVoWrapper fieldVoWrapper = TypeUtils.getBeanField(bean.getClass());
		List<FieldVo> fieldList = fieldVoWrapper.getFieldList();
		for (FieldVo model : fieldList) {
			try {
				Object result = model.getGetter().invoke(bean);
				if (null != result) {
					Ognl.setValue(xco, model.getName(), result);
				}
			} catch (Exception e) {
				throw new OgnlException("bean to xco error: " + bean.getClass(), e);
			}
		}
		return xco;
	}

	/**
	 * 从XCO对象中递归取值
	 * 
	 * @param data
	 *            data为原始数据
	 * @param varVo
	 * @return
	 */
	public static Object getValue(XCO data, VariableVo varVo) {
		// 这里取值为空是否要报错, 应该严格报错, 只有最后一个为空，可以忽略
		if (null != varVo.getVarUnit()) {
			Object result = data.getObjectValue(varVo.getVarUnit().getName());
			if (null != result) {
				return result;
			}
			if (varVo.isHasDefault()) {
				result = varVo.getDefaultValue();
			}
			return result;
		}
		List<VariableUnitVo> varUnitList = varVo.getVarUnitList();
		int size = varUnitList.size();
		Object returnObj = data;
		for (int i = 0; i < size; i++) {
			boolean hasNext = (i + 1) < size;
			VariableUnitVo vUnitVo = varUnitList.get(i);
			if (returnObj instanceof XCO) {
				returnObj = getValueFromXCO(returnObj, vUnitVo, data);
			} else if (returnObj instanceof Collection) {
				returnObj = getValueFromCollection(returnObj, vUnitVo, data);
			} else if (returnObj.getClass().isArray()) {
				Class<?> clazz = returnObj.getClass();
				if (int[].class == clazz) {
					returnObj = getValueFromIntArray(returnObj, vUnitVo, data);
				} else if (long[].class == clazz) {
					returnObj = getValueFromLongArray(returnObj, vUnitVo, data);
				} else if (float[].class == clazz) {
					returnObj = getValueFromFloatArray(returnObj, vUnitVo, data);
				} else if (double[].class == clazz) {
					returnObj = getValueFromDoubleArray(returnObj, vUnitVo, data);
				} else if (byte[].class == clazz) {
					returnObj = getValueFromByteArray(returnObj, vUnitVo, data);
				} else if (short[].class == clazz) {
					returnObj = getValueFromShortArray(returnObj, vUnitVo, data);
				} else if (boolean[].class == clazz) {
					returnObj = getValueFromBooleanArray(returnObj, vUnitVo, data);
				} else if (char[].class == clazz) {
					returnObj = getValueFromCharArray(returnObj, vUnitVo, data);
				} else if (char[].class == clazz) {
					returnObj = getValueFromCharArray(returnObj, vUnitVo, data);
				} else {
					returnObj = getValueFromObjectArray(returnObj, vUnitVo, data);
				}
			} else {
				throw new OgnlException("get xco value error: " + returnObj);// 类型错误
			}
			if (null == returnObj && hasNext) {
				throw new OgnlException("get xco value error: " + varVo.getOriginal());
			}
		}

		if (null == returnObj && varVo.isHasDefault()) {
			returnObj = varVo.getDefaultValue();
		}

		return returnObj;
	}

	private static Object getValueFromXCO(Object target, VariableUnitVo peVo, XCO original) {

		String key = peVo.getName();
		if (VariableUnitEnum.VAR == peVo.getType()) {
			key = (String) original.getObjectValue(peVo.getName());
		}

		if (VariableUnitEnum.PROPERTY == peVo.getType() || VariableUnitEnum.VAR == peVo.getType()) {
			XCO xco = (XCO) target;
			Object value = xco.getObjectValue(key);
			if (null != value) {
				return value;
			}
			if ("size".equalsIgnoreCase(key) || "length".equalsIgnoreCase(key)) {
				return xco.size();
			}
			return null;
		}
		throw new OgnlException("getValueFromXCO error: " + target);
	}

	@SuppressWarnings("rawtypes")
	private static Object getValueFromCollection(Object target, VariableUnitVo peVo, XCO original) {

		int index = peVo.getIndex();
		if (VariableUnitEnum.VAR == peVo.getType()) {
			index = (Integer) original.getObjectValue(peVo.getName());
		}

		if (VariableUnitEnum.INDEX == peVo.getType() || VariableUnitEnum.VAR == peVo.getType()) {
			if (target instanceof List) {
				List list = (List) target;
				if (index < list.size()) {
					Object value = list.get(index);
					return value;
				}
			} else {
				int i = 0;
				Collection collection = (Collection<?>) target;
				for (Object obj : collection) {
					if (i++ == index) {
						return obj;
					}
				}
			}
		} else {
			String key = peVo.getName();
			if ("size".equalsIgnoreCase(key) || "length".equalsIgnoreCase(key)) {
				Collection<?> collection = (Collection<?>) target;
				return collection.size();
			}
		}
		return null;
	}

	private static Object getValueFromObjectArray(Object target, VariableUnitVo peVo, XCO original) {
		Object[] array = (Object[]) target;
		if (VariableUnitEnum.INDEX == peVo.getType()) {
			if (peVo.getIndex() < array.length) {
				Object value = array[peVo.getIndex()];
				return value;
			}
		} else if (VariableUnitEnum.VAR == peVo.getType()) {
			int index = (Integer) original.getObjectValue(peVo.getName());
			if (index < array.length) {
				Object value = array[index];
				return value;
			}
		} else {
			String key = peVo.getName();
			if ("length".equalsIgnoreCase(key)) {
				return array.length;
			}
		}
		return null;
	}

	private static Object getValueFromIntArray(Object target, VariableUnitVo peVo, XCO original) {
		int[] array = (int[]) target;
		if (VariableUnitEnum.INDEX == peVo.getType()) {
			if (peVo.getIndex() < array.length) {
				return array[peVo.getIndex()];
			}
		} else if (VariableUnitEnum.VAR == peVo.getType()) {
			int index = (Integer) original.getObjectValue(peVo.getName());
			if (index < array.length) {
				return array[index];
			}
		} else {
			String key = peVo.getName();
			if ("length".equalsIgnoreCase(key)) {
				return array.length;
			}
		}
		return null;
	}

	private static Object getValueFromLongArray(Object target, VariableUnitVo peVo, XCO original) {
		long[] array = (long[]) target;
		if (VariableUnitEnum.INDEX == peVo.getType()) {
			if (peVo.getIndex() < array.length) {
				return array[peVo.getIndex()];
			}
		} else if (VariableUnitEnum.VAR == peVo.getType()) {
			int index = (Integer) original.getObjectValue(peVo.getName());
			if (index < array.length) {
				return array[index];
			}
		} else {
			String key = peVo.getName();
			if ("length".equalsIgnoreCase(key)) {
				return array.length;
			}
		}
		return null;
	}

	private static Object getValueFromBooleanArray(Object target, VariableUnitVo peVo, XCO original) {
		boolean[] array = (boolean[]) target;
		if (VariableUnitEnum.INDEX == peVo.getType()) {
			if (peVo.getIndex() < array.length) {
				return array[peVo.getIndex()];
			}
		} else if (VariableUnitEnum.VAR == peVo.getType()) {
			int index = (Integer) original.getObjectValue(peVo.getName());
			if (index < array.length) {
				return array[index];
			}
		} else {
			String key = peVo.getName();
			if ("length".equalsIgnoreCase(key)) {
				return array.length;
			}
		}
		return null;
	}

	private static Object getValueFromByteArray(Object target, VariableUnitVo peVo, XCO original) {
		byte[] array = (byte[]) target;
		if (VariableUnitEnum.INDEX == peVo.getType()) {
			if (peVo.getIndex() < array.length) {
				return array[peVo.getIndex()];
			}
		} else if (VariableUnitEnum.VAR == peVo.getType()) {
			int index = (Integer) original.getObjectValue(peVo.getName());
			if (index < array.length) {
				return array[index];
			}
		} else {
			String key = peVo.getName();
			if ("length".equalsIgnoreCase(key)) {
				return array.length;
			}
		}
		return null;
	}

	private static Object getValueFromCharArray(Object target, VariableUnitVo peVo, XCO original) {
		char[] array = (char[]) target;
		if (VariableUnitEnum.INDEX == peVo.getType()) {
			if (peVo.getIndex() < array.length) {
				return array[peVo.getIndex()];
			}
		} else if (VariableUnitEnum.VAR == peVo.getType()) {
			int index = (Integer) original.getObjectValue(peVo.getName());
			if (index < array.length) {
				return array[index];
			}
		} else {
			String key = peVo.getName();
			if ("length".equalsIgnoreCase(key)) {
				return array.length;
			}
		}
		return null;
	}

	private static Object getValueFromDoubleArray(Object target, VariableUnitVo peVo, XCO original) {
		double[] array = (double[]) target;
		if (VariableUnitEnum.INDEX == peVo.getType()) {
			if (peVo.getIndex() < array.length) {
				return array[peVo.getIndex()];
			}
		} else if (VariableUnitEnum.VAR == peVo.getType()) {
			int index = (Integer) original.getObjectValue(peVo.getName());
			if (index < array.length) {
				return array[index];
			}
		} else {
			String key = peVo.getName();
			if ("length".equalsIgnoreCase(key)) {
				return array.length;
			}
		}
		return null;
	}

	private static Object getValueFromFloatArray(Object target, VariableUnitVo peVo, XCO original) {
		float[] array = (float[]) target;
		if (VariableUnitEnum.INDEX == peVo.getType()) {
			if (peVo.getIndex() < array.length) {
				return array[peVo.getIndex()];
			}
		} else if (VariableUnitEnum.VAR == peVo.getType()) {
			int index = (Integer) original.getObjectValue(peVo.getName());
			if (index < array.length) {
				return array[index];
			}
		} else {
			String key = peVo.getName();
			if ("length".equalsIgnoreCase(key)) {
				return array.length;
			}
		}
		return null;
	}

	private static Object getValueFromShortArray(Object target, VariableUnitVo peVo, XCO original) {
		short[] array = (short[]) target;
		if (VariableUnitEnum.INDEX == peVo.getType()) {
			if (peVo.getIndex() < array.length) {
				return array[peVo.getIndex()];
			}
		} else if (VariableUnitEnum.VAR == peVo.getType()) {
			int index = (Integer) original.getObjectValue(peVo.getName());
			if (index < array.length) {
				return array[index];
			}
		} else {
			String key = peVo.getName();
			if ("length".equalsIgnoreCase(key)) {
				return array.length;
			}
		}
		return null;
	}

}
