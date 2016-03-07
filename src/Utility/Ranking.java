package Utility;

import java.util.Arrays;
import java.util.Comparator;

public class Ranking {
	public int[] rank(double[] value){
		Item[] arr = new Item[value.length];
		for(int i=0;i<value.length;i++){
			Item item = new Item(value[i], i);
			arr[i] = item;
		}
		Arrays.sort(arr, new ItemComparator());
		int[] list = new int[arr.length];
		for(int i=0;i<arr.length;i++){
			list[i] = arr[i].term;
		}
		return list;
	}
}

class ItemComparator implements Comparator{
	public int compare(Object arg0, Object arg1){
		Item x1 = (Item)arg0;
		Item x2 = (Item)arg1;
		int result = 0;
		if(x2.key < x1.key)
			result = -1;
		else if(x2.key > x1.key)
			result = 1;
		return result;
	}
}

class Item {
	public double key;
	public int term;
	
	public Item(double key, int term){
		this.key = key;
		this.term = term;
	}
}
