package org.xson.web;

import java.util.List;

import org.xson.web.util.PatternMatchUtils;

/**
 * 前置、后置处理器VO
 */
public class BeforeAfterVo implements Comparable<BeforeAfterVo> {

	private MethodObject	mo;
	private int				order;
	private List<String>	includeList;
	private List<String>	excludeList;

	public BeforeAfterVo(MethodObject mo, int order, List<String> includeList, List<String> excludeList) {
		this.mo = mo;
		this.order = order;
		this.includeList = includeList;
		this.excludeList = excludeList;
	}

	public BeforeAfterVo(MethodObject mo, int order) {
		this.mo = mo;
		this.order = order;
	}

	public boolean match(String url) {
		if (null != excludeList) {
			for (String pattern : excludeList) {
				if (PatternMatchUtils.simpleMatch(pattern, url)) {
					return false;
				}
			}
		}
		if (null != includeList) {
			for (String pattern : includeList) {
				if (!PatternMatchUtils.simpleMatch(pattern, url)) {
					return false;
				}
			}
		}
		return true;
	}

	public MethodObject getMo() {
		return mo;
	}

	@Override
	public int compareTo(BeforeAfterVo o) {
		if (this.order > o.order) {
			return 1;
		} else if (this.order < o.order) {
			return -1;
		}
		return 0;
	}
}
