package com.wldst.ruder.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.openjdk.jol.info.ClassLayout;

import com.wldst.ruder.constant.CruderConstant;
import com.wldst.ruder.domain.CrudSystem;
import com.wldst.ruder.module.workflow.constant.BpmDo;

import scala.collection.mutable.HashTable;

public class TestJava {

    public static void main(String[] args) {
	// TODO Auto-generated method stub
//	printRuder();
	System.out.print(ClassLayout.parseClass(Map.class).toPrintable());
	System.out.print(ClassLayout.parseClass(HashMap.class).toPrintable());
	System.out.print(ClassLayout.parseClass(HashTable.class).toPrintable());
	System.out.print(ClassLayout.parseClass(HashSet.class).toPrintable());
	System.out.print(ClassLayout.parseClass(ArrayList.class).toPrintable());
    }

    private static void printRuder() {
//	System.out.print(ClassLayout.parseClass(RuderApplication.class).toPrintable());
	System.out.print(ClassLayout.parseClass(CruderConstant.class).toPrintable());
	System.out.print(ClassLayout.parseClass(CrudSystem.class).toPrintable());
	
	System.out.print(ClassLayout.parseClass(BpmDo.class).toPrintable());
    }

}
