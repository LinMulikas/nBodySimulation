import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.LongStream;

public class parallel{
    public static void main(String[] args){
        apple[] apples = new apple[1000000];

        for(int i = 0; i < 1000000; i++){
            apples[i] = new apple(i);
        }


        long start = System.currentTimeMillis();

        for(apple i : apples){
            i.setWeight(100);
        }

//        Arrays.stream(apples).forEach(apple -> apple.setWeight(100));

        long end = System.currentTimeMillis();

//        for(int i = 0; i < 100; i++){
//            System.out.println(apples[i].weight);
//        }


        System.out.println((end - start) / 1000.0 + " Second");
    }

}
