package com.alibaba.datax.common.element;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;

/**
 * Created by jingxing on 14-8-24.
 */

public interface Record {

	public void addColumn(Column column);

	public void setColumn(int i, final Column column);

	public Column getColumn(int i);

	public String toString();

	public int getColumnNumber();

	public int getByteSize();

	public int getMemorySize();

	JSONObject toJsonObject();
}
