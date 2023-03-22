package main;

import java.util.ArrayList;
import java.util.List;

public class Main {

  public static List<Integer> list = new ArrayList<>();

  public static void main(String[] args) {
    new Producer("_Unsync_Producer_1").start();
    new Producer("_Unsync_Producer_2").start();
    new Consumer("_Unsync_Consumer_1").start();
    new Consumer("_Unsync_Consumer_2").start();
  }
}
