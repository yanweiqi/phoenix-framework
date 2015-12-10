package com.ginkgocap.ywxt.framework.dal.dao.util;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:21:23
 * @Copyright Copyright©2015 www.gintong.com
 */
public class MinHeap {
	private Object[][] heap;
	private int maxsize;
	private int size;
	
	public MinHeap(int max,int objSize){
		maxsize = max + 1;
		heap = new Object[maxsize][objSize];
		size = 0;
		Object[] baseObjArray = new Object[objSize];
		for(int i=0;i<objSize;i++){
			baseObjArray[i] = Long.valueOf(-1);
		}
		heap[0] = baseObjArray;
	}
	
	private int leftchild(int pos){
		return 2*pos;
	}
	
	private int rightchild(int pos){
		return 2*pos + 1;
	}
	
	private int parent(int pos){
		return pos / 2;
	}
	
	private boolean isleaf(int pos){
		return ((pos > size/2) && (pos <= size));
	}
	
	private void swap(int pos1, int pos2){
		Object[] tmp = heap[pos1];
		heap[pos1] = heap[pos2];
		heap[pos2] = tmp;
	}
	
	public void insert(Object[] objs){
		heap[++size] = objs;
		int current = size;
		Long newValue = new Long(""+heap[current][objs.length -1]);
		Long parentValue =new Long(""+heap[parent(current)][objs.length -1]);
		while(newValue.longValue()< parentValue.longValue()){
			swap(current, parent(current));
			current = parent(current);
			newValue = new Long(""+heap[current][objs.length -1]);
			parentValue =new Long(""+heap[parent(current)][objs.length -1]);
		}
	}
	
	public Object[] removemin(){
		swap(1, size);
		if(--size != 0)
			pushdown(1);
		return heap[size+1];
	}
	
	public Object[] minvalue(){
		return size>0 ? heap[1] : heap[0];
	}
	
	public int size(){
		return size;
	}
	
	private void pushdown(int position){
		int smallestchild;
		while(!isleaf(position)){
			smallestchild = leftchild(position);
			
			if((smallestchild < size) && getOrderedValue(smallestchild).longValue()> getOrderedValue(smallestchild+1).longValue())
				smallestchild = smallestchild + 1;
			if(getOrderedValue(position).longValue() <= getOrderedValue(smallestchild).longValue())
				return ;
			swap(position, smallestchild);
			position = smallestchild;
		}
	}
	
	public Long getOrderedValue(int position){
		Object[] obs = heap[position];
		Long value =new Long(""+obs[obs.length -1]);
        return value;
	}
	public void print(){
		for(int i=1; i<=size; i++){
			for(int j=0; j<heap[i].length; j++){
				System.out.print(heap[i][j] + " ");
			}
			System.out.print("\n");
		}
		System.out.print("\n");
	}
}
