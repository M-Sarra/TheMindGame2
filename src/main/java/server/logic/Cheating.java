package  server.logic;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Cheating {
    public  static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        int t = sc.nextInt();
        int min=Integer.MAX_VALUE;

        for(int i=0;i<t;i++){
            List<Integer> a=new ArrayList<>();
            List<Integer> b=new ArrayList<>();
           int n=sc.nextInt();
           for(int j=0;j<n;j++){
               a.add(sc.nextInt());
               b.add(sc.nextInt());
           }for(int j=0;j<n;j++) {
               System.out.println(FindSum(a,b,3));
                if (min > FindSum(a, b, j)) {
                    min = FindSum(a, b, j);
                }
            }
           System.out.println(min);
        }

    }
    public static int FindSum(List<Integer> a,List<Integer> b,int start){
        int output=a.get(start);
        int count =(int)a.stream().count();
        for (int i=start;i<count+start-1;i++){
            int j=(i%count);
            if(j+1==count){
                output+=Moghayese(a.get(0),b.get(j));
            }else{
            output+=Moghayese(a.get(j+1),b.get(j));
        }}
        return output;
    }
    public static int Moghayese(int a,int b){
        if(a>=b){
            return a-b;
        }
        else {
            return 0;
        }
    }
}
