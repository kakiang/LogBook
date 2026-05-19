package com.hervekakiang.logbook;

public interface OnItemClickListener<T> {
    void onItemClick(T obj);
    void onItemLongClick(T obj);
}