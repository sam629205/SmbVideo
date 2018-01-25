package com.zh.zhvideoplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Adolf on 2016/11/24.
 */
public abstract class MyBaseAdapter<T> extends BaseAdapter{
    protected Context context;
    protected List<T> infoList;
    protected LayoutInflater inflater;
    public MyBaseAdapter() {
    }

    public MyBaseAdapter(Context context, List<T> infoList) {
        this.context = context;
        this.infoList = infoList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return infoList.size();
    }

    @Override
    public Object getItem(int position) {
        return infoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    abstract public View getView(int position, View convertView, ViewGroup parent);

    public void setListWithSort(ArrayList<T> list, Comparator<? super T> comparator) {
        Collections.sort(list, comparator);
        setList(list);
    }

    public void setList(List<T> list) {
        this.infoList = list;
        notifyDataSetChanged();
    }
    public void addAll(List<T> list){
        this.infoList.addAll(list);
        notifyDataSetChanged();
    }
    public void delete(int index){
        this.infoList.remove(index);
        notifyDataSetChanged();
    }
}
