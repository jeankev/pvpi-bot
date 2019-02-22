package com.jeankev.pvpi;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.*;

@SpringBootApplication
public class PvpiApplication {

	/******** START CONFIGURATION ********/
    public static final String URL_SQLITE_FILE = "jdbc:sqlite:C:/Users/jeankev/Desktop/pvpi/src/main/resources/BDD.db";
    /******** END CONFIGURATION ********/


    /******** START SQL ********/
    public static final String SQL_INIT_PLAYER_TABLE = "CREATE TABLE IF NOT EXISTS player (\n pseudo VARCHAR(255) PRIMARY KEY,\n ligue VARCHAR(255)\n);";
    public static final String SQL_INSERT_PLAYER = "INSERT INTO player(pseudo,ligue) VALUES(?,?)";

    public static final String SQL_GET_PLAYER_LIST = "SELECT * FROM player;";
    /******** END SQL ********/


	public static void main(String[] args) {
        initBDD();
		SpringApplication.run(PvpiApplication.class, args);

		final DiscordClient client = new DiscordClientBuilder("NTQ3NjY1OTkxNzkwNzU1ODQw.D06GwA.zJJ7Wx1QalW1CKk0YI6AmgEesnI").build();

		client.getEventDispatcher().on(ReadyEvent.class)
				.subscribe(ready -> System.out.println("Logged in as " + ready.getSelf().getUsername()));

		client.getEventDispatcher().on(MessageCreateEvent.class)
				.map(MessageCreateEvent::getMessage)
				.filter(msg -> msg.getContent().map("!playerlist"::equals).orElse(false))
				.flatMap(Message::getChannel)
				.flatMap(channel -> channel.createMessage(getListOfPlayer()))
				.subscribe();

		client.login().block();
	}

	private static String getListOfPlayer(){
	    String listOfPlayer = "";
        try (Connection conn = DriverManager.getConnection(URL_SQLITE_FILE);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_GET_PLAYER_LIST)) {
            while (rs.next()) {
                listOfPlayer += "\n" + rs.getString("pseudo") + " rush " + rs.getString("ligue");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return listOfPlayer;
    }

    private static void addPlayer(String pseudo, String ligue){
        try (Connection conn = DriverManager.getConnection(URL_SQLITE_FILE);
             PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT_PLAYER)) {
            pstmt.setString(1, pseudo);
            pstmt.setString(2, ligue);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void removePlayer(){
        //TODO removePlayer
    }

    private static void initBDD(){
        try (Connection conn = DriverManager.getConnection(URL_SQLITE_FILE); Statement stmt = conn.createStatement()) {
            if (conn != null) {
                stmt.execute(SQL_INIT_PLAYER_TABLE);
                addPlayer("jeankev", "Rubis");
                addPlayer("toto", "Saphir");
                addPlayer("tata", "Rubis");
                System.out.println("initBDD OK !");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
