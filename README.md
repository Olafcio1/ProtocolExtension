<div align="center">

<h1>ProtocolExtension</h1>
<p>Minecraft protocol additions</p>
<hr>

![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/Olafcio1/ProtocolExtension/total?style=plastic&logo=github&color=%23FFFFFF)
![Modrinth Downloads](https://img.shields.io/modrinth/dt/protocolextension?style=plastic&logo=modrinth&color=%2300AF5C)<br/>
![GitHub last commit](https://img.shields.io/github/last-commit/Olafcio1/ProtocolExtension?style=plastic&logo=refinedgithub&color=%239E95B7&link=https%3A%2F%2Fgithub.com%2FOlafcio1%2FProtocolExtension%2Fcommits%2F)

</div>

## 🤔 What is ProtocolExtension
PX* adds more packets to the game so servers can further customize playing experience without making their own client-side mod for the clients to install.

This makes it easier for both the servers, and the players, as it is a universal solution for potentially lots of servers, without degrading user experience - you can still play servers without PX with PX installed client-side, and on servers with PX without it installed client-side.

###### [1]: ProtocolExtension

## 🪶 Packets

---
| Name                       | Brief                                                            | Version |
|----------------------------|------------------------------------------------------------------|---------|
| **[Serverbound]**          |                                                                  |         |
| KeyPressedC2SPayload       | Sent when the client presses a key in-game.                      | v0.1    |
| MouseMoveC2SPayload        | Sent when the client moves the mouse in a screen.                | v0.1    |
| **[Clientbound]**          |                                                                  |         |
| ActivateS2CPayload         | Received when the server supports PX packets.                    | v0.1    |
| ClearHUDS2CPayload         | Clears all previously created HUD elements by the server.        | v0.2    |
| DeleteHUDElementS2CPayload | Deletes a HUD element previously created by the server.          | v0.1    |
| PutHUDElementS2CPayload    | Adds or modifies a HUD element previously created by the server. | v0.1    |
| ServerCommandS2CPayload    | Sets the player's sneaking and sprinting states.                 | v0.2    |
| SetWindowTitleS2CPayload   | Changes the player's window title.                               | v0.2    |
| ToggleHUDS2CPayload        | Toggles HUD visibility.                                          | v0.0    |

## 🎐 Quickstart
To start developing with ProtocolExtension, first you need to have a Java IDE installed. I provide support only for IntelliJ IDEA.

1. **Create a Paper plugin project:**

   the easiest method is using the [Minecraft Development](https://plugins.jetbrains.com/plugin/8327-minecraft-development) IDE plugin.
   <br/><br/>

2. **Embed ProtocolExtension into your project:**

   the recommended method is embedding its JAR file in your project structure and
   adding it as a compile-only package in your package manager.
   <br/><br/>

   - download the latest PX .jar file and add it to the `libs/` directory
   - add the following code to your `maven.pom`
      ```xml
      <dependency>
            <groupId>pl.olafcio</groupId>
            <artifactId>protocolextension</artifactId>
            <version>0.2</version>
            <scope>provided</scope>
            <systemPath>${project.basedir}/libs/protocolextension-0.2.jar</systemPath>
      </dependency>
     ```

3. **Sync the project**

   you should see a button appear with your package manager and a *refresh* icon in the top-right corner of IntelliJ.
   click it, and your project dependencies should update.
   <br/><br/>

4. **Try ProtocolExtension's API**

   when PX is ready to use, go to your plugin's main class (typically named after your project's name) and add this code:
   ```java
   import net.kyori.adventure.text.Component;
   import org.bukkit.event.EventHandler;
   import pl.olafcio.ProtocolExtension;

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
       ProtocolExtension.getAPI().playerManager().putHUD(event.getPlayer(), 1, 0d, 0d, Component.text("Hello, guy!"));
   }
   ```

An online documentation covering all topics related to ProtocolExtension will be available soon. For those wanting to help, please comment on [this issue](https://github.com/Olafcio1/ProtocolExtension/issues/1).
