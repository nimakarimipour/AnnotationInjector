package com.uber;
import com.ibm.wala.types.TypeName.IntegerMask;
import com.ibm.wala.util.collections.HashMapFactory;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Map;

import static com.ibm.wala.types.TypeName.*;

public class Super {

   @Nullable
   Object test() {
      return new Object();
   }
}
