package com.wldst.ruder.util;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

public class QuickSort {
    public static void main(String[] args) {
	int[] arr = { 5, 2, 9, 1, 5, 6 };
	quickSort(arr, 0, arr.length - 1);
	for (int i : arr) {
	    System.out.print(i + " ");
	}
    }



    public static void quickSort(int[] arr, int low, int high) {
	if (low < high) {
	    int partitionIndex = partition(arr, low, high);
	    quickSort(arr, low, partitionIndex - 1);
	    quickSort(arr, partitionIndex + 1, high);
	}
    }

    public static int partition(int[] arr, int low, int high) {
	int pivot = arr[high];
	int i = low - 1;
	for (int j = low; j < high; j++) {
	    if (arr[j] < pivot) {
		i++;
		int temp = arr[i];
		arr[i] = arr[j];
		arr[j] = temp;
	    }
	}
	int temp = arr[i + 1];
	arr[i + 1] = arr[high];
	arr[high] = temp;
	return i + 1;
    }
    
    
    public static void quickSort(String[] arr) {
	Arrays.sort(arr);
    }

     
    
    
    public static void quickSort(Double[] arr, int low, int high) {
	if (low < high) {
	    int partitionIndex = partition(arr, low, high);
	    quickSort(arr, low, partitionIndex - 1);
	    quickSort(arr, partitionIndex + 1, high);
	}
    }

    public static int partition(Double[] arr, int low, int high) {
	Double pivot = arr[high];
	int i = low - 1;
	for (int j = low; j < high; j++) {
	    if (Double.valueOf(arr[j]) < Double.valueOf(pivot)) {
		i++;
		Double temp = arr[i];
		arr[i] = arr[j];
		arr[j] = temp;
	    }
	}
	Double temp = arr[i + 1];
	arr[i + 1] = arr[high];
	arr[high] = temp;
	return i + 1;
    }
    
    public static void quickSort(Float[] arr, int low, int high) {
	if (low < high) {
	    int partitionIndex = partition(arr, low, high);
	    quickSort(arr, low, partitionIndex - 1);
	    quickSort(arr, partitionIndex + 1, high);
	}
    }

    public static int partition(Float[] arr, int low, int high) {
	Float pivot = arr[high];
	int i = low - 1;
	for (int j = low; j < high; j++) {
	    if (Float.valueOf(arr[j]) < Float.valueOf(pivot)) {
		i++;
		Float temp = arr[i];
		arr[i] = arr[j];
		arr[j] = temp;
	    }
	}
	Float temp = arr[i + 1];
	arr[i + 1] = arr[high];
	arr[high] = temp;
	return i + 1;
    }
    
    public static void quickSort(BigDecimal[] arr, int low, int high) {
	if (low < high) {
	    int partitionIndex = partition(arr, low, high);
	    quickSort(arr, low, partitionIndex - 1);
	    quickSort(arr, partitionIndex + 1, high);
	}
    }

    public static int partition(BigDecimal[] arr, int low, int high) {
	BigDecimal pivot = arr[high];
	int i = low - 1;
	for (int j = low; j < high; j++) {
	    if ((arr[j].compareTo(pivot))<0) {
		i++;
		BigDecimal temp = arr[i];
		arr[i] = arr[j];
		arr[j] = temp;
	    }
	}
	BigDecimal temp = arr[i + 1];
	arr[i + 1] = arr[high];
	arr[high] = temp;
	return i + 1;
    }
    
    public static void quickSort(Long[] arr, int low, int high) {
	if (low < high) {
	    int partitionIndex = partition(arr, low, high);
	    quickSort(arr, low, partitionIndex - 1);
	    quickSort(arr, partitionIndex + 1, high);
	}
    }

    public static int partition(Long[] arr, int low, int high) {
	Long pivot = arr[high];
	int i = low - 1;
	for (int j = low; j < high; j++) {
	    if (Long.valueOf(arr[j]) < Long.valueOf(pivot)) {
		i++;
		Long temp = arr[i];
		arr[i] = arr[j];
		arr[j] = temp;
	    }
	}
	Long temp = arr[i + 1];
	arr[i + 1] = arr[high];
	arr[high] = temp;
	return i + 1;
    }
    
    public static void quickSort(Date[] arr, int low, int high) {
	if (low < high) {
	    int partitionIndex = partition(arr, low, high);
	    quickSort(arr, low, partitionIndex - 1);
	    quickSort(arr, partitionIndex + 1, high);
	}
    }

    public static int partition(Date[] arr, int low, int high) {
	Date pivot = arr[high];
	int i = low - 1;
	for (int j = low; j < high; j++) {
	    if (arr[j].before(pivot)) {
		i++;
		Date temp = arr[i];
		arr[i] = arr[j];
		arr[j] = temp;
	    }
	}
	Date temp = arr[i + 1];
	arr[i + 1] = arr[high];
	arr[high] = temp;
	return i + 1;
    }
}
