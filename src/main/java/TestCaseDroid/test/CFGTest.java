package TestCaseDroid.test;

public class CFGTest {
    private double num = 5.0;

    public double cal(int num, String type){

        double temp=0;

        if(type == "sum"){

            for(int i = 0; i <= num; i++){

                temp =temp + i;

            }
        }
        else if(type == "average"){

            for(int i = 0; i <= num; i++){

                temp = temp + i;

            }

            temp = temp / (num -1);

        }else{

            System.out.println("Please enter the right type(sum or average)");

        }

        return temp;

    }

    public static void main(String[] args) {
        CFGTest cfgTest = new CFGTest();
        System.out.println(cfgTest.cal(5, "sum"));
        System.out.println(cfgTest.cal(5, "average"));
        System.out.println(cfgTest.cal(5, "error"));
    }
}
