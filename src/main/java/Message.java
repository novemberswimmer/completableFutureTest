import com.sun.xml.internal.ws.util.CompletedFuture;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Message {
    private final String message;


    @FieldPosition(start = 1, length = 1)
    private String field1;
    @FieldPosition(start = 2, length = 1)
    private String field2;
    @FieldPosition(start = 3, length = 1)
    private String field3;
    @FieldPosition(start = 4, length = 1)
    private String field4;

    private String field5;

    public Message(String message) {
        System.out.println(Thread.currentThread().getName());
        this.message = message;

        /*Create and executor object to contain all parallel thread execution of CompletableFuture, instead of using the
        default ForkJoinPool.commonPool()
         */
        Executor executor = Executors.newFixedThreadPool(this.getClass().getDeclaredFields().length);

        try {

            /*
            Place holder for all CompleteFuture that will asynchronously process setting of attribute value if an
            attribute is annotated with FieldPosition.  This will be use to block execution so that the current method
            (which is the constructor) will not exit untlil all CompletableFuture is finish.
             */
            List<CompletableFuture<Void>> futureFiledValueList = new ArrayList();

            /**Loop through all object attribute process any attribute with FieldPosition Annotation*/
            for (Field f: this.getClass().getDeclaredFields()) {
                FieldPosition fp = f.getAnnotation(FieldPosition.class);
                if (fp != null) {
                    f.setAccessible(true);

                    /*Collect all completableFuture in a list so that completion of all an be check*/
                    futureFiledValueList.add(CompletableFuture.supplyAsync(supplyFieldValue(f),executor)
                            .thenAccept(getExecuting_theAcceptAsync(f)));
                }
            }

            /*block execution until all completableFuture is done*/
            CompletableFuture<Void> allFutureFieldValue = CompletableFuture.allOf(futureFiledValueList
                    .toArray(new CompletableFuture[futureFiledValueList.size()]));

            System.out.println("Block main thread until all future is completed. Current Thread : " + Thread.currentThread().getName());
            allFutureFieldValue.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            /*always shutdown the executor.  Not doing so will cause forever wait.  Though the method will end/return there will be a ghost
            * process which is the executor waiting.*/
            ((ExecutorService) executor).shutdown();;
        }

        System.out.println("End of constructor");
    }

    private Consumer<String> getExecuting_theAcceptAsync(Field f) {
        return str -> {
            try {
                System.out.println("Executing theAcceptAsync");
                f.set(this, str);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        };
    }

    public String getField1() {
        this.field1 = this.field1 == null ?  "field1 field Not Set" : this.field1;
        return this.field1;
    }

    public String getField2() {
        this.field2 = this.field2 == null ?  "field2 field Not Set" : this.field2;
        return this.field2;
    }

    public String getField3() {
        this.field3 = this.field3 == null ?  "field3 field Not Set" : this.field3;
        return this.field3;
    }

    public String getField4() {
        this.field4 = this.field4 == null ?  "field4 field Not Set" : this.field4;
        return this.field4;
    }


    public String getField5() {
        this.field5 = this.field5 == null ?  "field5 field Not Set" : this.field5;
        return this.field5;
    }


    private Supplier<String> supplyFieldValue(Field field) {
        Supplier<String> supplyFieldFunction = () -> {
            return (String) extractMessage(field);
        };
        return supplyFieldFunction;
    }

    private Object extractMessage(Field field){

        try {
            /*access the annotation value for FieldPosition annotation*/
            FieldPosition fp = field.getAnnotation(FieldPosition.class);
            
            String subStr =  message.substring(fp.start()-1, fp.start()-1 + fp.length());
            System.out.println("Extracting vaue for field name " + field.getName() + "@ start: " + (fp.start()-1)
                    + " length "+ fp.length() + " value : " + subStr + " from message " + this.message + " Time " + new Date());
            return subStr;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
