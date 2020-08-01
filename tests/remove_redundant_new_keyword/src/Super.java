package com.uber;
public class Super {
   Object test() {
       return foo(this.new Bar(), this.new Foo());
   }
   Object foo(Bar b, Foo f) {
     return Object();
   }
   class Foo{ }
   class Bar{ }
}
