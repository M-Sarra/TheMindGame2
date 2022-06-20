package  server.logic;

public class Example {

    public static void main(String[] args) {
        TypeTesterr t = new TypeTesterr();
        t.printType(1.34f+2.54d);
        t.printType(1.34d+2.54f);
        try {
            GameController controller = new GameController(new TheMindGameUI());
            String gameName = "Default";
            controller.CreateNewGame(gameName);
            int count = 5;
            for (int i = 0 ; i < count;i++)
                controller.AddBot(null,"Bot"+i,gameName);
            controller.StartGame(null,gameName);
            while (controller.IsOpen()) {
                Thread.sleep(50);
            }
        }catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
    }

}

class TypeTesterr {
    void printType(byte x) {
        System.out.println("byte");
    }
    void printType(short x) {
        System.out.println("short");
    }
    void printType(int x) {
        System.out.println("int");
    }
    void printType(long x) {
        System.out.println("long");
    }
    void printType(float x) {
        System.out.println("float");
    }
    void printType(double x) {
        System.out.println("double");
    }
}
