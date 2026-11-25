package ru.edu.pr06;
import java.util.*;

//ИНДИВИДУАЛЬНОЕ ЗАДАНИЕ

import java.time.DayOfWeek;
import java.time.LocalDate;


public class Warehouse {
 private final Map<String,StockItem> items=new HashMap<>(); private final List<StockMovement> movements=new ArrayList<>();
 public void addItem(StockItem item) {
  if (item == null) {
   throw new IllegalArgumentException("Карточки товара не существует");
  }
  String code = item.getCode();
  if (items.containsKey(code)) {
   throw new IllegalArgumentException("Такой код уже занят");
  }
  items.put(code, item);
 }
 public void receive(String code,int q) {
  StockItem item = items.get(code);
  if (item == null) {
   throw new IllegalArgumentException("Карточки товара не существует");
  }
  item.increase(q);
  StockMovement movement = new StockMovement(code, MovementType.RECEIVE, q); //товар приняли
  movements.add(movement);
 }

 void issue(String code, int q, LocalDate date) {

  // ИНДИВИДУАЛЬНОЕ ЗАДАНИЕ ----------------------------------------
  DayOfWeek day = date.getDayOfWeek();

  if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
   throw new IllegalArgumentException("В выходные склад не выдаёт товар");
  }

  // ____________________________________________________________________ :)
  StockItem item = items.get(code);
  if (item == null) {
   throw new IllegalArgumentException("Карточки товара не существует");
  }
  item.decrease(q);
  StockMovement movement = new StockMovement(code, MovementType.ISSUE, q);
  movements.add(movement);
 }

 public void issue(String code, int q) {
  issue(code, q, LocalDate.now());
 }

 public List<StockItem> findByCategory(Category c) {
  List<StockItem> result = new ArrayList<>();
  for (StockItem item : items.values()) {
   if (item.getCategory() == c) {
    result.add(item);
   }
  }
  return result;
 }

 public Map<Category,Integer> totalsByCategory() {
  Map<Category, Integer> totals = new HashMap<>();
  for (StockItem item : items.values()) {
   Category category = item.getCategory();
   int previousTotal = totals.getOrDefault(category, 0);
   totals.put(category, previousTotal + item.getQuantity());
  }

  return totals;
 }

 public int movementsCount() {
  return movements.size();
 }

 public int itemsCount() {
  return items.size();
 }

 public List<StockMovement> addMovementsByType(Warehouse wh, MovementType type) {
  List<StockMovement> result = new ArrayList<>();
  for (StockMovement movement : wh.movements) {
   if (movement.type() == type) {
    result.add(movement);
   }
  }
  return result;
 }

}
