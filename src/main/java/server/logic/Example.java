package  server.logic;

import client.DirectMessenger;
import client.RemoteGamePanel;
import server.log.ConsoleLogger;
import server.logic.model.BotPlayer;
import server.logic.model.IGameController;
import server.logic.model.Player;

public class Example {

    public static void main(String[] args) {
        TypeTesterr t = new TypeTesterr();
        t.printType(1.34f+2.54d);
        t.printType(1.34d+2.54f);
        try {
            GameController controller = new GameController(new ConsoleLogger());
            Player host = new Player() {
                @Override
                public String GetName() {
                    return "Host";
                }

                @Override
                public void GiveCard(Integer card) {

                }

                @Override
                public void StatusChanged(GameStatus status) {

                }

                @Override
                public void NotifyPlaysCard(String player, Integer card) {

                }

                @Override
                public void NotifyNinjaPropose(String player) {

                }

                @Override
                public void NotifyNinjaAgreement() {

                }

                @Override
                public void NotifyHeartMissed() {

                }
            };
            String hostToken = controller.Register(host);
            String gameName = "Default";
            int count = 5;
            controller.CreateNewGame(hostToken, gameName,count);
            for (int i = 0 ; i < count;i++) {
                String name = "Bot" + i;
                AddBot(controller, name, gameName);
            }

            controller.StartGame(null,gameName);
            while (controller.IsOpen()) {
                Thread.sleep(50);
            }
        }catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }


    }

    static String AddBot(GameController controller, String name,String gameName) {
        TheMindGame game = controller.GetGameByName(gameName);
        if (game == null)
            return "Invalid game.";
        BotPlayer bot = new BotPlayer(name);
        MessageGamePanel gamePanel = new MessageGamePanel(game,name);
        DirectMessenger gameMessenger = new DirectMessenger(gamePanel);
        gamePanel.SetMessenger1(gameMessenger);
        RemoteGamePanel botPanel = new RemoteGamePanel(bot);
        DirectMessenger botMessenger = new DirectMessenger(botPanel);
        gameMessenger.SetAudience(botMessenger);
        botMessenger.SetAudience(gameMessenger);
        botPanel.SetMessenger(botMessenger);
        String botToken = controller.Join1(gamePanel, gameName);
        gamePanel.SetToken(botToken);
        bot.Join(botToken,  game);
        return "Success";
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
