package com.uber;

import javax.annotation.Nullable;

public class Super {
   @Nullable
   Object test() {
       return foo(this.new Bar(), this.new Foo());
   }
   Object foo(Bar b, Foo f) {
     return Object();
   }
   class Foo{ }
   class Bar{ }
}
