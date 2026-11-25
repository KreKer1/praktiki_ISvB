package ru.edu.pr06;
public class StockItem {
 private final String code; private final String name; private final Category category; private int quantity;
 public StockItem(String code,String name,Category category,int quantity) { if(quantity<0) throw new IllegalArgumentException();

     if (code == null || code.isBlank()) {
         throw new IllegalArgumentException("Укажите код товара");
     }
     if (name == null ||  name.isBlank()) {
         throw new IllegalArgumentException("Укажите имя товара");
     }
     if (category == null) {
         throw new IllegalArgumentException("Укажите категорию товара");
     }

     this.code=code;this.name=name;this.category=category;this.quantity=quantity; }
 public void increase(int q) {
  if (q <= 0) {
   throw new IllegalArgumentException("Количество должно быть больше нуля");
  }
  if (q > Integer.MAX_VALUE - quantity) {
   throw new IllegalArgumentException("Слишком большое количество товара");
  }
  quantity = quantity + q;
 }

 public void decrease(int q) {
   if (q <= 0) {
    throw new IllegalArgumentException("Количество должно быть больше нуля");
   }
   if (q > quantity) {
    throw new IllegalArgumentException("На складе нет столько товара");
   }
   quantity = quantity - q;
 }

 public String getCode(){return code;} public Category getCategory(){return category;} public int getQuantity(){return quantity;}

    @Override
    public boolean equals(Object other) {
     if(!(other instanceof StockItem)) {
         return false;
     }
     StockItem otherItem = (StockItem) other;
     return code.equals(otherItem.getCode());
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }
}
