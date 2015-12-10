package com.ginkgocap.ywxt.framework.dal.dao.helper;

import java.util.ArrayList;
import java.util.List;

import com.ginkgocap.ywxt.framework.dal.dao.util.MinHeap;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:19:11
 * @Copyright Copyright©2015 www.gintong.com
 */
public class ListOrderHelper {

	public static  List getOrderedList(List<List> allList,boolean forward){
		List all = new ArrayList();
//		List resList = new ArrayList();
		for(List list : allList){
			all.addAll(list);
		}
		return getOrderedList(forward,all);
	}

	private static List getOrderedList(boolean forward, List obsLs) {
		if(obsLs.size() == 0){
			return obsLs ;
		}
		List<Object[]> orderedList = new ArrayList<Object[]>();
		Object[] baseObs = null;
		for(int i=0;i<obsLs.size();i++){
			baseObs = (Object[]) obsLs.get(i);
			if(null != baseObs){
				break;
			}
		}
		if(null == baseObs){
			return orderedList;
		}
		MinHeap minHeap = new MinHeap(obsLs.size(),baseObs.length);
		int totalNum = 0;
		for(Object obj :obsLs){
			if(null != obj ){
			  minHeap.insert((Object[])obj);
			  totalNum ++;
			}
		}
		Object obj;
		for(int i=0;i<totalNum;i++){
			Object[] obs= minHeap.removemin();
			orderedList.add(obs);
		}
		if(forward){
			List<Object[]> newOrderedLs = new ArrayList<Object[]>();
			for(int j=orderedList.size()-1;j>=0;j--){
				newOrderedLs.add(orderedList.get(j));
			}
			return newOrderedLs;
		}
		else{
		  return orderedList;
		}
	}
	
	public static void main(String args[]){
		List<Object[]> obsList = new ArrayList<Object[]>();
	    obsList.add(new Object[]{"dasf","dasf",1238664989234l});
	
	    obsList.add(new Object[]{"dasf","dasf",1239258234054l});
	    obsList.add(new Object[]{"dasf","dasf",     1239248234054l});
	    obsList.add(new Object[]{"dasf","dasf",    1238815492549l});
	    obsList.add(new Object[]{"dasf","dasf",256l});
	    obsList.add(new Object[]{"dasf","dasf",1238750973757l});
	    
	    List<List> allList = new ArrayList<List>();
	    allList.add(obsList);
	    List resList =  ListOrderHelper.getOrderedList(allList, true);
	    for(int i =0 ;i<resList.size();i++){
	    	Object[] obArray = (Object[])resList.get(i);
	    	System.out.println(obArray[obArray.length-1]);
	    }
	    
	}
}
