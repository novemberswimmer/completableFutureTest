import java.lang.reflect.Field;

public class Application {

    public static void main(String args[]) throws InterruptedException {
        Message message = new Message("Testefsfff");


            Class classInstance = message.getClass();
            System.out.println(message.getField1());
            System.out.println(message.getField2());
            System.out.println(message.getField3());
            System.out.println(message.getField4());
            System.out.println(message.getField5());
    }
}
