
public class DeadBlocksSample {

    public static void main(String[] args) {
        methodWithDeadBlock(true); 
    }

    public static void methodWithDeadBlock(boolean condition) {
        if (condition) {
            System.out.println("This might execute");
        } else {
            System.out.println("Dead block - condition is false at runtime");
            unreachableCode();
        }


        System.out.println("Live code");


        String result = getResult();
        if (result == null) {
            System.out.println("This might also be dead");
        }
    }

    public static void unreachableCode() {
        return;
       
    }

    public static String getResult() {
        return "not null";  
    }

    public static void methodWithLiveBlock() {
        boolean condition = true;

        if (condition) {
            System.out.println("Live block");
        }
    }
}