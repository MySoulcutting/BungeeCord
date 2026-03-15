package net.md_5.bungee.command;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;

/**
 * Command to toggle maintenance mode on the proxy.
 */
public class CommandMaintenance extends Command
{

    public CommandMaintenance()
    {
        super( "maintenance", "bungeecord.command.maintenance" );
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        BungeeCord bungee = BungeeCord.getInstance();
        boolean newState = !bungee.config.isMaintenanceMode();
        bungee.config.setMaintenanceMode( newState );

        String status = newState ? ChatColor.RED + "ON" : ChatColor.GREEN + "OFF";
        sender.sendMessage( new ComponentBuilder( "Maintenance mode is now " + status ).create() );
    }
}
