package org.jjm.basic.model;

import java.util.List;

/**
 * 分页对象
 * 
 * @ClassName: Pager
 * @Description: TODO
 * @author: 蒋金敏
 * @date: 2016年2月6日 下午6:23:01
 * @version: V1.0
 * @param <T>
 */
public class Pager<T> {
	/**
	 * 分页大小
	 */
	private int size;
	/**
	 * 分页的起始页
	 */
	private int offset;
	/**
	 * 总记录数
	 */
	private Long total;
	/**
	 * 分页数据
	 */
	private List<T> datas;

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

	public List<T> getDatas() {
		return datas;
	}

	public void setDatas(List<T> datas) {
		this.datas = datas;
	}

}
