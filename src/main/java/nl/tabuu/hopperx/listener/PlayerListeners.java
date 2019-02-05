package nl.tabuu.hopperx.listener;

import nl.tabuu.hopperx.HopperX;
import nl.tabuu.hopperx.hopper.HopperLinkWrapper;
import nl.tabuu.hopperx.hopper.XHopper;
import nl.tabuu.hopperx.hopper.XHopperDestination;
import nl.tabuu.tabuucore.configuration.IConfiguration;
import nl.tabuu.tabuucore.util.Dictionary;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerListeners implements Listener {

    IConfiguration _config;
    Dictionary _local;

    public PlayerListeners(){
        _config = HopperX.getInstance().getConfigurationManager().getConfiguration("config");
        _local = HopperX.getInstance().getConfigurationManager().getConfiguration("lang").getDictionary("");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void playerHopperTeleport(PlayerMoveEvent e){
        Player player = e.getPlayer();
        Block fromBlock = e.getFrom().getBlock();
        Block toBlock = e.getTo().getBlock();
        Block toFloorBlock = toBlock.getRelative(BlockFace.DOWN);

        if(fromBlock.equals(toBlock) || fromBlock.getType().equals(Material.HOPPER))
            return;

        if(toFloorBlock.getType().equals(Material.HOPPER)){
            if(HopperX.getInstance().getHopperManager().isRegistered((org.bukkit.block.Hopper) toFloorBlock.getState())){
                XHopper hopper = HopperX.getInstance().getHopperManager().getHopperAt(toFloorBlock.getLocation());
                if(hopper.isTeleporting() && hopper.getDestination(XHopperDestination.LINKED).isPresent()){
                    Location teleportLocation = hopper.getDestination(XHopperDestination.LINKED).get().getLocation();
                    teleportLocation.setYaw(e.getTo().getYaw());
                    teleportLocation.setPitch(e.getTo().getPitch());
                    teleportLocation.add(0.5, 1, 0.5);
                    player.teleport(teleportLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);

                    //Particle and sound effects
                    toBlock.getLocation().getWorld().playEffect(toBlock.getLocation(), Effect.ENDER_SIGNAL, 1);
                    toBlock.getWorld().playSound(toBlock.getLocation(), Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1, 1);
                    toBlock.getWorld().spawnParticle(Particle.DRAGON_BREATH, toBlock.getLocation().clone().add(0.5, 1, 0.5),20,0.5, 0.5, 0.5, 0);

                    teleportLocation.getWorld().playEffect(teleportLocation, Effect.ENDER_SIGNAL, 1);
                    teleportLocation.getWorld().playSound(teleportLocation, Sound.ENTITY_SHULKER_TELEPORT, 1, 1);
                    teleportLocation.getWorld().spawnParticle(Particle.DRAGON_BREATH, player.getLocation().clone().add(0, 0.5, 0),20,0.5, 0.5, 0.5, 0);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerInteract(PlayerInteractEntityEvent e){
        Entity entity = e.getRightClicked();
        if(HopperX.getInstance().getHopperManager().isLinking(e.getPlayer()) && entity instanceof Player && e.getHand().equals(EquipmentSlot.HAND)){
            Player player = (Player) entity;

            HopperLinkWrapper link = HopperX.getInstance().getHopperManager().getLinking(e.getPlayer());
            link.getHopper().setDestination(link.getHopperDestination(), player.getInventory());
            HopperX.getInstance().getHopperManager().finishLinking(e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void hopperInteract(PlayerInteractEvent e){
        Block block = e.getClickedBlock();

        if(block != null && block.getType().equals(Material.HOPPER)){
            Hopper hopperBlock = (Hopper) block.getState();

            if(!HopperX.getInstance().getHopperManager().isRegistered(hopperBlock))
                HopperX.getInstance().getHopperManager().register(hopperBlock);
            XHopper hopper = HopperX.getInstance().getHopperManager().getHopperAt(hopperBlock.getLocation());

            if(e.getAction().equals(Action.LEFT_CLICK_BLOCK) && e.getPlayer().isSneaking()){
                hopper.openGui(e.getPlayer());
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void hopperPlace(BlockPlaceEvent e){
        Block block = e.getBlockPlaced();
        Player player = e.getPlayer();
        if(block.getType().equals(Material.HOPPER)) {
            if(HopperX.getInstance().getHopperManager().canPlaceHopperAt(block.getLocation())) {
                HopperX.getInstance().getHopperManager().register((Hopper) block.getState());
            }
            else {
                player.sendMessage(_local.translate("CHUNKLIMIT_EXCEEDED", "{CHUNK_LIMIT}", _config.getInt("MaxHoppersPerChunk") + ""));
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void hopperBreak(BlockBreakEvent e){
        Block block = e.getBlock();
        if(block.getType().equals(Material.HOPPER)){
            Hopper hopper = (Hopper) block.getState();
            if(HopperX.getInstance().getHopperManager().isRegistered(hopper))
                HopperX.getInstance().getHopperManager().unregister(hopper);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void linkHopper(PlayerInteractEvent e){
        Player player = e.getPlayer();
        Block block = e.getClickedBlock();

        if(block == null)
            return;

        if(HopperX.getInstance().getHopperManager().isLinking(player) && block.getState() instanceof Container) {
            Container container = (Container) block.getState();
            HopperLinkWrapper link = HopperX.getInstance().getHopperManager().getLinking(player);
            int maxDistance = _config.getInt("DefaultValues.MAX_LINK_DISTANCE");
            int minDistance = _config.getInt("DefaultValues.MIN_LINK_DISTANCE");
            double distance = container.getLocation().distance(link.getHopper().getLocation());
            if(distance <= maxDistance && distance >= minDistance) {
                link.getHopper().setDestination(link.getHopperDestination(), container.getInventory());
                HopperX.getInstance().getHopperManager().finishLinking(player);
                player.sendMessage(_local.translate("LINK_SUCCESSFUL"));

                //Particle and sound effects.
                container.getWorld().spawnParticle(Particle.REDSTONE, container.getLocation().clone().add(0.5, 0.5, 0.5), 50, 0.5, 0.5, 0.5, 0, new Particle.DustOptions(Color.RED, 1));
                container.getWorld().playSound(container.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
            }
            else if(distance < minDistance){
                HopperX.getInstance().getHopperManager().finishLinking(player);
                player.sendMessage(_local.translate("LINK_DISTANCE_BENEATH", "{MIN_DISTANCE}", "" + minDistance));
            }
            else {
                HopperX.getInstance().getHopperManager().finishLinking(player);
                player.sendMessage(_local.translate("LINK_DISTANCE_EXCEEDED", "{MAX_DISTANCE}", "" + maxDistance));
            }

            e.setCancelled(true);
        }
    }
}
