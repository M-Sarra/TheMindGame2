package test;

import client.DirectMessenger;
import client.RemoteGamePanel;
import server.GameServer;
import server.log.ConsoleLogger;
import common.model.BotPlayer;
import common.model.Player;
import server.logic.GameController;
import common.model.GameStatus;
import common.MessageGamePanel;

public class Example {

    public static void main(String[] args) {
        try {
            GameController controller = new GameController(new ConsoleLogger());
            String hostToken = null;
            Player host = new Player() {
                @Override
                public String getName() {
                    return "Host";
                }

                @Override
                public void giveCard(Integer card) {

                }

                @Override
                public void giveToken(String token) {
                }

                @Override
                public void statusChanged(GameStatus status) {

                }

                @Override
                public void notifyPlaysCard(String player, Integer card) {

                }

                @Override
                public void notifyNinjaPropose(String player) {

                }

                @Override
                public void notifyNinjaAgreement() {

                }

                @Override
                public void notifyHeartMissed() {

                }

                @Override
                public void notifyJoin(String player) {

                }
            };
            hostToken = controller.register(host);
            String gameName = "Default";
            int count = 6;
             controller.createNewGame(hostToken, gameName,count);
            GameServer server = new GameServer(controller,new ConsoleLogger());
            server.start();
            for (int i = 0 ; i < 3;i++) {
                String name = "ClientBot" + i;
                BotPlayer bot = new BotPlayer(name,-1);
                addBot(server,controller,gameName, bot);
            }

            controller.startGame(null,gameName);
            while (controller.isOpen()) {
                Thread.sleep(50);
            }
        }catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
    }

    static String addBot(GameServer server, GameController controller, String gameName, BotPlayer bot) {
        MessageGamePanel gamePanel = new MessageGamePanel(controller);
        DirectMessenger gameMessenger = new DirectMessenger(gamePanel);
        gamePanel.setMessenger(gameMessenger);
        RemoteGamePanel botPanel = new RemoteGamePanel(bot);
        DirectMessenger botMessenger = new DirectMessenger(botPanel);
        gameMessenger.SetAudience(botMessenger);
        botMessenger.SetAudience(gameMessenger);
        botPanel.setMessenger(botMessenger);
        botPanel.register();
        botPanel.join(gameName);
        bot.join(null//playerToken
                 ,  botPanel);
        return "Success";
    }
}