package pl.admerpro.aaantylogout;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import pl.admerpro.aaantylogout.combat.CombatManager;
import pl.admerpro.aaantylogout.combat.CombatResult;
import pl.admerpro.aaantylogout.combat.CombatSession;
import pl.admerpro.aaantylogout.config.PluginSettings;
import pl.admerpro.aaantylogout.history.BattleRecord;
import pl.admerpro.aaantylogout.history.HistoryService;
import pl.admerpro.aaantylogout.listener.CombatListener;
import pl.admerpro.aaantylogout.util.MessageService;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RestartProtectionTest {
    @Rule public TemporaryFolder folder = new TemporaryFolder();

    private AAAntylogoutPlugin plugin() {
        AAAntylogoutPlugin plugin = mock(AAAntylogoutPlugin.class);
        when(plugin.getDataFolder()).thenReturn(folder.getRoot());
        when(plugin.getLogger()).thenReturn(Logger.getAnonymousLogger());
        return plugin;
    }

    private CombatSession session() {
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("Fighter");
        return new CombatSession(player, System.currentTimeMillis() - 1000);
    }

    @Test public void shutdownQuitNeverPunishesOrBroadcasts() {
        AAAntylogoutPlugin plugin = plugin();
        Server server = mock(Server.class);
        when(plugin.getServer()).thenReturn(server);
        when(server.isStopping()).thenReturn(true);
        CombatManager manager = mock(CombatManager.class);
        Player player = mock(Player.class);
        new CombatListener(plugin, manager, mock(MessageService.class))
            .onQuit(new PlayerQuitEvent(player, (String) null));
        verify(manager).shutdown();
        verifyNoMoreInteractions(manager);
        verifyNoInteractions(player);
        verify(server, never()).broadcastMessage(anyString());
    }

    @Test public void ordinaryLogoutStillFinishesCombatAsLogout() {
        AAAntylogoutPlugin plugin = plugin();
        Server server = mock(Server.class);
        when(plugin.getServer()).thenReturn(server);
        when(plugin.settings()).thenReturn(mock(PluginSettings.class));
        CombatManager manager = mock(CombatManager.class);
        Player player = mock(Player.class);
        UUID id = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(id);
        when(manager.isInCombat(id)).thenReturn(true);
        new CombatListener(plugin, manager, mock(MessageService.class))
            .onQuit(new PlayerQuitEvent(player, (String) null));
        verify(manager).finishGroup(player, CombatResult.LOGOUT);
        verify(manager, never()).shutdown();
    }

    @Test public void interruptedSessionsAreRecoveredOnlyOnceWithoutRestoringCombat() {
        AAAntylogoutPlugin plugin = plugin();
        PluginSettings settings = mock(PluginSettings.class);
        when(settings.recordServerStop()).thenReturn(true);
        CombatSession session = session();
        HistoryService original = new HistoryService(plugin, settings);
        original.checkpointSessions(List.of(session));
        HistoryService restarted = new HistoryService(plugin, settings);
        restarted.recoverInterruptedSessions();
        List<BattleRecord> records = restarted.getRecords(session.playerId());
        assertEquals(1, records.size());
        assertEquals(CombatResult.SERVER_STOP, records.get(0).result());
        new HistoryService(plugin, settings).recoverInterruptedSessions();
        assertEquals(1, new HistoryService(plugin, settings).getRecords(session.playerId()).size());
    }

    @Test public void finishedCombatIsNotRecoveredAgain() {
        AAAntylogoutPlugin plugin = plugin();
        PluginSettings settings = mock(PluginSettings.class);
        when(settings.recordServerStop()).thenReturn(true);
        CombatSession session = session();
        HistoryService history = new HistoryService(plugin, settings);
        history.checkpointSessions(List.of(session));
        history.record(BattleRecord.fromSession(session, CombatResult.TIMEOUT, null, System.currentTimeMillis()));
        HistoryService restarted = new HistoryService(plugin, settings);
        restarted.recoverInterruptedSessions();
        assertEquals(1, restarted.getRecords(session.playerId()).size());
        assertEquals(CombatResult.TIMEOUT, restarted.getRecords(session.playerId()).get(0).result());
    }

    @Test public void disabledStopHistoryDiscardsInterruptedSessions() {
        AAAntylogoutPlugin plugin = plugin();
        PluginSettings settings = mock(PluginSettings.class);
        CombatSession session = session();
        new HistoryService(plugin, settings).checkpointSessions(List.of(session));
        HistoryService restarted = new HistoryService(plugin, settings);
        restarted.recoverInterruptedSessions();
        assertTrue(restarted.getRecords(session.playerId()).isEmpty());
        when(settings.recordServerStop()).thenReturn(true);
        HistoryService nextRestart = new HistoryService(plugin, settings);
        nextRestart.recoverInterruptedSessions();
        assertTrue(nextRestart.getRecords(session.playerId()).isEmpty());
    }
}
